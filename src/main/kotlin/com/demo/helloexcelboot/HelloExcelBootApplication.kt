package com.demo.helloexcelboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication
@EnableAsync
class HelloExcelBootApplication

fun main(args: Array<String>) {
    runApplication<HelloExcelBootApplication>(*args)
}



