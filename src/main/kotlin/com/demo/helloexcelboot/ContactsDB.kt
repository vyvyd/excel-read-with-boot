package com.demo.helloexcelboot

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ContactsDB {
    fun refreshAllContacts(allContacts: List<Contact>) : UploadFileResult
}

@Component
class JDBCBasedContactsDB(
    private val jdbcTemplate: JdbcTemplate
) : ContactsDB {

    @Transactional
    override fun refreshAllContacts(allContacts: List<Contact>) : UploadFileResult {
        return withErrorHandling {
            removeAllEntries();
            allContacts.forEach {
                addContact(it.name, it.email)
            }
            UploadFileResult(
                recordsUpdated = allContacts.size,
                errorMessage = null
            )
        }
    }

    fun withErrorHandling(block : () -> UploadFileResult) : UploadFileResult {
        return try {
            block()
        } catch (ex: Exception) {
            UploadFileResult(
                recordsUpdated = 0,
                errorMessage = ex.message
            )
        }
    }

    private fun removeAllEntries() {
        jdbcTemplate.execute("""
            DELETE from "helloexcel"."contacts";
        """.trimIndent())
    }

    private fun addContact(name: String, email: String) {
        jdbcTemplate.update("""
            INSERT INTO "helloexcel"."contacts"("name", "email") VALUES(?, ?);
        """.trimIndent(), name, email)
    }



}