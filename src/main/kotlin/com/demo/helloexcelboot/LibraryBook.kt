package com.demo.helloexcelboot

import org.springframework.jdbc.core.JdbcTemplate

data class LibraryBook(
    val isbn: String,
    val title: String,
    val author: String
)

