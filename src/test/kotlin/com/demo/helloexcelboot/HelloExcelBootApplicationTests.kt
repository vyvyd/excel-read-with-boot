package com.demo.helloexcelboot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


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

    /**
     * Ensure you have a local database setup before you run
     * this test
     */
    @Test
    fun writesContentsToTheDatabase() {
        val excelFile = { name: String -> checkNotNull(this.javaClass.classLoader.getResourceAsStream(name)) }

        mockMvc.perform(
            multipart("/contacts/upload").file(
                MockMultipartFile(
                    "file",
                    excelFile("SampleExcelWorkbook.xlsx")
                )
            )
        ).andExpectAll(
            status().isAccepted,
            content().json(
                """{
              "recordsUpdated": 2,
              "errorMessage": null
            }
            """.trimIndent())
        )

        val rowCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(1) FROM "helloexcel"."contacts"
        """.trimIndent(), Integer::class.java);

        assertEquals(2, rowCount)
    }
}


