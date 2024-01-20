package com.demo.helloexcelboot

import com.demo.helloexcelboot.ImportJobResult.Error
import com.demo.helloexcelboot.ImportJobResult.Ok
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
import java.util.concurrent.CompletableFuture

data class ImportJobsResult(
    val id: String,
    val imported: List<String>,
    val failed: List<String>
) {
   companion object {

       fun from(
           id: String,
           jobResults: List<ImportJobResult>
       ): ImportJobsResult {
           return ImportJobsResult(
               id = id,
               imported =  jobResults.filterIsInstance<Ok>().map { it.isbn },
               failed = jobResults.filterIsInstance<Error>().map { it.isbn }
           )
       }
   }
}


sealed class ImportJobResult {
    data class Ok(val isbn: String) : ImportJobResult()
    data class Error(val isbn: String, val exception: Exception?) : ImportJobResult()
}

class ImportBookByISBNJob(
    private val isbn: String,
    private val jdbcTemplate: JdbcTemplate,
    private val openLibraryAPIClient: OpenLibraryAPIClient
) {
    fun import(): ImportJobResult {
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
class ImportJobExecutor {

    @Async
    fun executeAsync(jobs: List<ImportBookByISBNJob>) : CompletableFuture<ImportJobsResult> {
        val results = ImportJobsResult.from(
            id = UUID.randomUUID().toString(),
            jobResults = runBlocking {
                val asyncImportJobs = jobs.map { job ->
                    async(Dispatchers.IO) {
                        job.import()
                    }
                }
                asyncImportJobs.awaitAll()
            }
        )
        return CompletableFuture.completedFuture(results)
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
    ): ResponseEntity<ImportJobsResult> {
        val excelFile = ImportBooksExcelFile(file.inputStream)
        val booksToImport = excelFile.allNewBooksToImport()
        val importJobs = importJobFactory.jobFrom(booksToImport)

        importJobExecutor.executeAsync(importJobs)

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .build()
    }

}