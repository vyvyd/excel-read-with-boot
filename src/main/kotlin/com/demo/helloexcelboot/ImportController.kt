package com.demo.helloexcelboot

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

data class NewImportJobResult(
    val id: String,
    val toImport: List<String>,
    val errored: List<String>
)

@Component
class ImportJobs {
    fun enqueue(booksToImport: List<BookToImport>): NewImportJobResult {
        return NewImportJobResult(
            id = UUID.randomUUID().toString(),
            toImport = booksToImport.map { it.isbn },
            errored = emptyList()
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
    ): ResponseEntity<NewImportJobResult> {

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