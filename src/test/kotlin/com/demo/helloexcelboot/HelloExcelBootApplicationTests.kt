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
            jsonPath("$.toImport", contains(
                "9780590353427",
                "9781338216660",
                "9780006479888",
                "9780141199702",
                "9780201835953"
            )),
            jsonPath("$.errored", empty<String>())

        )

        val allBooks = getAllImportedBooksFromDB(jdbcTemplate)
        assertEquals(5, allBooks.size)

        assertEquals("Roald Dahl", allBooks[0].author)
        assertEquals("Fantastic Mr Fox", allBooks[0].title)

        assertEquals("Jack Thorne, John Tiffany, J. K. Rowling, Jack Thorne, Jean-François Ménard", allBooks[1].author)
        assertEquals("Harry Potter and the Cursed Child", allBooks[1].title)

        assertEquals("George R. R. Martin", allBooks[2].author)
        assertEquals("A Game of Thrones", allBooks[2].title)

        assertEquals("Charles Dickens", allBooks[3].author)
        assertEquals("A Tale of Two Cities", allBooks[3].title)

        assertEquals("Frederick P. Brooks", allBooks[4].author)
        assertEquals("The Mythical Man-Month", allBooks[4].title)
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


