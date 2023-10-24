package com.demo.helloexcelboot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File


@SpringBootTest
@AutoConfigureMockMvc
class HelloExcelBootApplicationTests {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc;

    @Test
    fun contextLoads() {
    }

    @Test
    fun writesContentsToTheDatabase() {
        val excelFile = { name: String -> this.javaClass.getResourceAsStream(name) }

        val file = MockMultipartFile(
            "SampleExcelWorkbook",
            excelFile("SampleExcelWorkbook")
        )

        mockMvc.perform(
            multipart("/contacts/upload").file(file)
        ).andExpect(status().isAccepted)

        val rowCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(1) FROM "helloexcel"."contacts"
        """.trimIndent(), Integer::class.java);

        assertEquals(0, rowCount)
    }
}


