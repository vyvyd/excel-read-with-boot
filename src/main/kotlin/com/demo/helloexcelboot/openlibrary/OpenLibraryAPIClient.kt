package com.demo.helloexcelboot.openlibrary

import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface OpenLibraryAPIClient {

    @GetExchange("/search.json")
    fun searchBy(@RequestParam isbn: String): OpenLibrarySearchResults
}