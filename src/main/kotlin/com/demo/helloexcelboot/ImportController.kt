package com.demo.helloexcelboot

import com.demo.helloexcelboot.ImportResult.Error
import com.demo.helloexcelboot.ImportResult.Ok
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import java.lang.Exception
import java.util.*

data class ImportJobStatus(
    val id: String,
    val toImport: List<String>,
    val errored: List<String>
)

sealed class ImportResult {
    data class Ok(val isbn: String) : ImportResult()
    data class Error(val isbn: String, val exception: Exception?) : ImportResult()
}

@HttpExchange(url="/isbn")
interface BookByISBNClient {
    @GetExchange("/{isbn}")
    fun getBookDetail(@PathVariable isbn: String): BookDetail
}

data class BookDetail(
    val title: String,
    val author: String
)

@Configuration
class BooksByISBNClientConfiguration {
    @Bean
    fun bookByISBNClient(): BookByISBNClient {
        val webClient = WebClient.builder()
            .baseUrl("http://localhost:6557") //TODO: extract to application.properties
            .build()

        return HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build()
            .createClient(BookByISBNClient::class.java)
    }

}

class ImportBookByISBNJob(
    private val isbn: String,
    private val jdbcTemplate: JdbcTemplate,
    private val bookByISBNClient: BookByISBNClient
) {
    fun import(): ImportResult {
        val bookDetail = bookByISBNClient.getBookDetail(isbn)
        jdbcTemplate.update(
            """
                INSERT INTO "inventory"."books"("title","isbn","author") VALUES (?, ?, ?)
                ON CONFLICT("isbn")
                DO UPDATE SET "title" = excluded."title", "author" = excluded."author"
            """.trimIndent(),
            bookDetail.title, isbn, bookDetail.author
        );
        return Ok(isbn)
    }
}


@Component
class ImportJobs(
    private val jdbcTemplate: JdbcTemplate,
    private val bookByISBNClient: BookByISBNClient
) {
    fun enqueue(booksToImport: List<BookToImportByISBN>): ImportJobStatus {
        val jobs = booksToImport.map {
            ImportBookByISBNJob(
               isbn =  it.isbn,
               jdbcTemplate = jdbcTemplate,
               bookByISBNClient = bookByISBNClient
            )
        }
        val importJobResults = jobs.map { it.import() }
        return ImportJobStatus(
            id = UUID.randomUUID().toString(),
            toImport = importJobResults.filterIsInstance<Ok>().map { it.isbn},
            errored = importJobResults.filterIsInstance<Error>().map { it.isbn }
        )
    }

}

@Controller
class ImportController(
    private val importJobs: ImportJobs
){

    @PostMapping("/books/import")
    fun uploadFile(
        file: MultipartFile
    ): ResponseEntity<ImportJobStatus> {

        val result = importJobs.enqueue(
            ImportBooksExcelFile(
                file.inputStream
            ).allNewBooksToImport()
        )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(result)
    }

}