package com.demo.helloexcelboot

import com.demo.helloexcelboot.ImportResult.Error
import com.demo.helloexcelboot.ImportResult.Ok
import com.demo.helloexcelboot.openlibrary.OpenLibraryAPIClient
import kotlinx.coroutines.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile
import java.util.*

data class ImportJobStatus(
    val id: String,
    val imported: List<String>,
    val failed: List<String>
)

sealed class ImportResult {
    data class Ok(val isbn: String) : ImportResult()
    data class Error(val isbn: String, val exception: Exception?) : ImportResult()
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
class ImportBookJobFactory(
    private val jdbcTemplate: JdbcTemplate,
    private val openLibraryAPIClient: OpenLibraryAPIClient,
) {
    fun jobFrom(booksToImport: List<BookToImportByISBN>): List<ImportBookByISBNJob> {
        return booksToImport.map {
            ImportBookByISBNJob(
               isbn =  it.isbn,
               jdbcTemplate = jdbcTemplate,
               openLibraryAPIClient = openLibraryAPIClient
            )
        }
    }
}

@Component
class ImportJobExecutor() {

    @Async
    fun executeAsync(jobs: List<ImportBookByISBNJob>) : ImportJobStatus {
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
            imported = importJobResults.filterIsInstance<Ok>().map { it.isbn},
            failed = importJobResults.filterIsInstance<Error>().map { it.isbn }
        )

    }
}

@Controller
class ImportController(
    private val importJobFactory: ImportBookJobFactory,
    private val importJobExecutor: ImportJobExecutor
){

    @PostMapping("/books/import")
    fun uploadFile(
        file: MultipartFile
    ): ResponseEntity<ImportJobStatus> {
        val excelFile = ImportBooksExcelFile(file.inputStream)
        val booksToImport = excelFile.allNewBooksToImport()
        val importJobs = importJobFactory.jobFrom(booksToImport)

        val result = importJobExecutor.executeAsync(importJobs)

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(result)
    }

}