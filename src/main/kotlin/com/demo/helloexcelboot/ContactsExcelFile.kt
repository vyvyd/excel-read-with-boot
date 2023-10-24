package com.demo.helloexcelboot

import org.dhatim.fastexcel.reader.ReadableWorkbook

class ContactsExcelFile(
    private val file: ByteArray
) {

    fun allContacts(): List<Contact> {
        val workbook = ReadableWorkbook(file.inputStream())
        val contactSheet = workbook.sheets.filter { it.name == "Names" }.findFirst()
        if (contactSheet.isEmpty) {
            return emptyList()
        }
        val contacts = contactSheet.get().read().drop(1).map { row ->
            Contact(row.getCellText(0), row.getCellText(1))
        }
        workbook.close()
        return contacts
    }

}