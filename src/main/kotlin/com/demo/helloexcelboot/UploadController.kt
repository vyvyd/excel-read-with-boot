package com.demo.helloexcelboot

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile

@Controller
class UploadController(
    private val contactsDB: ContactsDB
){

    @PostMapping("/contacts/upload")
    fun uploadFile(
        file: MultipartFile
    ): ResponseEntity<String> {

        contactsDB.refreshAllContacts(
            ContactsExcelFile(
                file.inputStream
            ).allContacts()
        )
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

}