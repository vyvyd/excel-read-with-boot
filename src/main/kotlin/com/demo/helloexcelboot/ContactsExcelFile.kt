package com.demo.helloexcelboot

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import java.io.InputStream

class ContactsExcelFile(
    private val inputStream: InputStream
) {

    fun allContacts(): List<Contact> {
        val workbook = ReadableWorkbook(inputStream)

        val maybeSheetWithContactNames = workbook.sheets
            .filter { it.name == "Names" }
            .findFirst()

        val maybeAllRowsInThatSheet = maybeSheetWithContactNames
            .map { it.read() }
            .map { it.drop(1) } // since we don't want headers

        val contacts = maybeAllRowsInThatSheet
            .map { it.toContacts() }
            .orElse(emptyList())

        workbook.close()
        return contacts
    }

    private fun List<Row>.toContacts() : List<Contact> {
        return this.map { row ->
            Contact(
                row.getCellText(0), // Name column
                row.getCellText(1)  // Email column
            )
        }
    }

}