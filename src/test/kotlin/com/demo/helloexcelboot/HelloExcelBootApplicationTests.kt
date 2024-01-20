package com.demo.helloexcelboot

import org.awaitility.Awaitility.await
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
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
import java.time.Duration


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

    @BeforeEach
    fun beforeEach() {
        cleanBooksTable()
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
        ).andExpect(status().isAccepted)

        await()
            .atLeast(Duration.ofMillis(250))
            .atMost(Duration.ofSeconds(3)) //Mockoon latency is 2000ms
            .with()
            .pollInterval(Duration.ofMillis(250))
            .until { getAllImportedBooksFromDB(jdbcTemplate).size == 5 }

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
        ).andExpect(status().isAccepted)

        await()
            .atLeast(Duration.ofMillis(250))
            .atMost(Duration.ofSeconds(3)) //Mockoon latency is 2000ms
            .with()
            .pollInterval(Duration.ofMillis(250))
            .until { getAllImportedBooksFromDB(jdbcTemplate).size == 5 }

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

    private fun cleanBooksTable() {
        return jdbcTemplate.execute(
            "TRUNCATE \"inventory\".\"books\";"
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


