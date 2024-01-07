package com.demo.helloexcelboot.openlibrary

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenLibraryDocument(
    @JsonProperty("title")       val title:String,
    @JsonProperty("author_name") val authors: List<String>
)