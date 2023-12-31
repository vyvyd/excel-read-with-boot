package com.demo.helloexcelboot

import com.demo.helloexcelboot.ImportResult.Error
import com.demo.helloexcelboot.ImportResult.Ok
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile
import java.lang.Exception
import java.util.*

data class ImportJobsResult(
    val id: String,
    val toImport: List<String>,
    val errored: List<String>
)

sealed class ImportResult {
    data class Ok(val isbn: String) : ImportResult()
    data class Error(val isbn: String, val exception: Exception?) : ImportResult()
}

class ImportBookByISBNJob(
    private val isbn: String,
    private val jdbcTemplate: JdbcTemplate
) {
    fun import(): ImportResult {
        jdbcTemplate.update(
            """
                INSERT INTO "inventory"."books"("title","isbn","author") VALUES (?, ?, ?)
                ON CONFLICT("isbn")
                DO UPDATE SET "title" = excluded."title", "author" = excluded."author"
            """.trimIndent(),
            "", isbn, ""
        );
        return Ok(isbn)
    }
}


@Component
class ImportJobs(
    private val jdbcTemplate: JdbcTemplate
) {
    fun enqueue(booksToImport: List<BookToImport>): ImportJobsResult {
        val jobs = booksToImport.map {
            ImportBookByISBNJob(
               isbn =  it.isbn,
               jdbcTemplate = jdbcTemplate
            )
        }
        val importJobResults = jobs.map { it.import() }
        return ImportJobsResult(
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
    ): ResponseEntity<ImportJobsResult> {

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