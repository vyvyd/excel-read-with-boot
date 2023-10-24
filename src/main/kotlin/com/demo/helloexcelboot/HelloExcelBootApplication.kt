package com.demo.helloexcelboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile


@SpringBootApplication
class HelloExcelBootApplication

fun main(args: Array<String>) {
    runApplication<HelloExcelBootApplication>(*args)
}



@Controller
class UploadController(
    private val contactsDB: ContactsDB
){

    @PostMapping("/contacts/upload")
    fun uploadFile(
        file: MultipartFile
    ): ResponseEntity<String> {
        val excelFile = ContactsExcelFile(
            file.inputStream.readAllBytes()
        )

        contactsDB.refreshContacts(excelFile.allContacts())
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }
}