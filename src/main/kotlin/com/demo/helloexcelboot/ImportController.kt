package com.demo.helloexcelboot

import com.demo.helloexcelboot.ImportResult.Error
import com.demo.helloexcelboot.ImportResult.Ok
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.util.*
import kotlin.Exception

data class ImportJobStatus(
    val id: String,
    val toImport: List<String>,
    val errored: List<String>
)

sealed class ImportResult {
    data class Ok(val isbn: String) : ImportResult()
    data class Error(val isbn: String, val exception: Exception?) : ImportResult()
}

@HttpExchange
interface OpenLibraryAPIClient {

    @GetExchange("/search.json")
    fun searchBy(@RequestParam isbn: String): OpenLibrarySearchResults
}

data class OpenLibrarySearchResults(
    val docs: List<OpenLibraryDocument>,
)

data class OpenLibraryDocument(
    @JsonProperty("title")       val title:String,
    @JsonProperty("author_name") val authors: List<String>
)

@Configuration
class OpenLibraryAPIClientConfiguration {
    @Bean
    fun openLibraryAPIClient(): OpenLibraryAPIClient {
        val webClient = WebClient.builder()
            .baseUrl("http://localhost:6557") //TODO: extract to application.properties
            .codecs { it.defaultCodecs().maxInMemorySize(1024 * 1024) }
            .build()

        return HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build()
            .createClient(OpenLibraryAPIClient::class.java)
    }

}

class ImportBookByISBNJob(
    private val isbn: String,
    private val jdbcTemplate: JdbcTemplate,
    private val openLibraryAPIClient: OpenLibraryAPIClient
) {
    fun import(): ImportResult {
        try {

            val bookDetail = openLibraryAPIClient.searchBy(isbn)
            check(bookDetail.docs.size == 1)

            val title = bookDetail.docs.first().title
            val author = bookDetail.docs.first().authors.joinToString(", ")

            jdbcTemplate.update(
                """
                INSERT INTO "inventory"."books"("title","isbn","author") VALUES (?, ?, ?)
                ON CONFLICT("isbn")
                DO UPDATE SET "title" = excluded."title", "author" = excluded."author"
            """.trimIndent(),
                title, isbn, author
            );
            return Ok(isbn)
        } catch (ex: Exception) {
            return Error(isbn, ex)
        }

    }
}


@Component
class ImportJobs(
    private val jdbcTemplate: JdbcTemplate,
    private val openLibraryAPIClient: OpenLibraryAPIClient,
) {
    fun enqueue(booksToImport: List<BookToImportByISBN>): ImportJobStatus {
        val jobs = booksToImport.map {
            ImportBookByISBNJob(
               isbn =  it.isbn,
               jdbcTemplate = jdbcTemplate,
               openLibraryAPIClient = openLibraryAPIClient
            )
        }
        val importJobResults = runBlocking {
            val asyncImportJobs = jobs.map { job ->
                async(Dispatchers.IO) {
                    job.import()
                }
            }
            asyncImportJobs.awaitAll()
        }

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