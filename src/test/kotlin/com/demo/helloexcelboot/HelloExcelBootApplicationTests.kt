package com.demo.helloexcelboot

import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@SpringBootTest
@AutoConfigureMockMvc
class HelloExcelBootApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc;

    @Test
    fun contextLoads() {
    }

    @Test
    fun writesContentsToTheDatabase() {
        val excelFile = { name: String -> checkNotNull(this.javaClass.classLoader.getResourceAsStream(name)) }

        mockMvc.perform(
            multipart("/books/import").file(
                MockMultipartFile(
                    "file",
                    excelFile("SampleExcelWorkbook.xlsx")
                )
            )
        ).andExpectAll(
            status().isAccepted,
            jsonPath("$.id",not(emptyOrNullString())),
            jsonPath("$.toImport", contains("978-0590353427", "978-1338216660")),
            jsonPath("$.errored", empty<String>())
        )
    }
}


