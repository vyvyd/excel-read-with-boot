package com.demo.helloexcelboot

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import java.io.InputStream

class ImportBooksExcelFile(
    private val inputStream: InputStream,
) {

    fun allNewBooksToImport(): List<BookToImport> {
        val workbook = ReadableWorkbook(inputStream)

        val maybeSheetWithContactNames = workbook.sheets
            .filter { it.name == "Import" }
            .findFirst()

        val maybeAllRowsInThatSheet = maybeSheetWithContactNames
            .map { it.read() }
            .map { it.drop(1) } // since we don't want headers

        val contacts = maybeAllRowsInThatSheet
            .map { it.toListOfBooksToImport() }
            .orElse(emptyList())

        workbook.close()
        return contacts
    }

    private fun List<Row>.toListOfBooksToImport() : List<BookToImport> {
        return this.map { row ->
            BookToImport(
                row.getCellText(0).trim(), // ISBN column
            )
        }
    }

}