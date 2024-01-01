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
            jsonPath("$.toImport", containsInAnyOrder(
                "9780590353427",
                "9781338216660",
                "9780006479888",
                "9780141199702",
                "9780201835953"
            )
            ),
            jsonPath("$.errored", empty<String>())

        )

        val allBooks = getAllImportedBooksFromDB(jdbcTemplate)
        assertEquals(5, allBooks.size)

        assertTrue(allBooks.any {
            it.author == "Roald Dahl" && it.title == "Fantastic Mr Fox"
        })

        assertTrue(allBooks.any {
            it.author == "Jack Thorne, John Tiffany, J. K. Rowling, Jack Thorne, Jean-François Ménard" && it.title == "Harry Potter and the Cursed Child"
        })

        assertTrue(allBooks.any {
            it.author == "George R. R. Martin" && it.title == "A Game of Thrones"
        })

        assertTrue(allBooks.any {
            it.author == "Charles Dickens" && it.title == "A Tale of Two Cities"
        })

        assertTrue(allBooks.any {
            it.author == "Frederick P. Brooks" && it.title == "The Mythical Man-Month"
        })

    }


    @Test
    fun writesContentsToTheDatabaseButIgnoresISBNWithErrors() {
        val excelFile = { name: String -> checkNotNull(this.javaClass.classLoader.getResourceAsStream(name)) }

        mockMvc.perform(
            multipart("/books/import").file(
                MockMultipartFile(
                    "file",
                    excelFile("SampleExcelWorkbookWithInvalidISBN.xlsx")
                )
            )
        ).andExpectAll(
            status().isAccepted,
            jsonPath("$.id",not(emptyOrNullString())),
            jsonPath("$.toImport", containsInAnyOrder(
                "9780590353427",
                "9781338216660",
                "9780006479888",
                "9780141199702",
                "9780201835953"
            )
            ),
            jsonPath("$.errored", contains(
                "1245678"
            ))

        )

        val allBooks = getAllImportedBooksFromDB(jdbcTemplate)
        assertEquals(5, allBooks.size)

        assertTrue(allBooks.any {
            it.author == "Roald Dahl" && it.title == "Fantastic Mr Fox"
        })

        assertTrue(allBooks.any {
            it.author == "Jack Thorne, John Tiffany, J. K. Rowling, Jack Thorne, Jean-François Ménard" && it.title == "Harry Potter and the Cursed Child"
        })

        assertTrue(allBooks.any {
            it.author == "George R. R. Martin" && it.title == "A Game of Thrones"
        })

        assertTrue(allBooks.any {
            it.author == "Charles Dickens" && it.title == "A Tale of Two Cities"
        })

        assertTrue(allBooks.any {
            it.author == "Frederick P. Brooks" && it.title == "The Mythical Man-Month"
        })

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


