package com.demo.helloexcelboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.multipart.MultipartFile
import java.io.IOException


@SpringBootApplication
class HelloExcelBootApplication

fun main(args: Array<String>) {
    runApplication<HelloExcelBootApplication>(*args)
}


@Controller
class UploadController {

    @PostMapping("/contacts/upload")
    fun uploadFile(file: MultipartFile): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }
}