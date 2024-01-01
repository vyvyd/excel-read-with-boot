package com.demo.helloexcelboot

import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.sql.ResultSet


@SpringBootTest
@AutoConfigureMockMvc
class HelloExcelBootApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc;

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

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

        val allBooks = getAllImportedBooksFromDB(jdbcTemplate)
        assertEquals(2, allBooks.size)

        assertTrue(allBooks[0].author.isEmpty().not())
        assertTrue(allBooks[0].title.isEmpty().not())

        assertTrue(allBooks[1].author.isEmpty().not())
        assertTrue(allBooks[1].title.isEmpty().not())
    }

    private fun getAllImportedBooksFromDB(
        jdbcTemplate: JdbcTemplate
    ): List<LibraryBook> {
        return jdbcTemplate.query(
            "SELECT * from \"inventory\".\"books\";",
            BookRowMapper()
        )
    }
}

class BookRowMapper(): RowMapper<LibraryBook> {
    override fun mapRow(rs: ResultSet, rowNum: Int): LibraryBook {
        return LibraryBook(
            isbn = rs.getString("isbn"),
            author = rs.getString("author"),
            title = rs.getString("title")
        )
    }

}


