/*
package com.demo.helloexcelboot

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ContactsDB {
    fun refreshAllContacts(allContacts: List<BookToImport>) : NewImportJobResult
}

@Component
class JDBCBasedContactsDB(
    private val jdbcTemplate: JdbcTemplate
) : ContactsDB {

    @Transactional
    override fun refreshAllContacts(allContacts: List<BookToImport>) : NewImportJobResult {
        return withErrorHandling {
            removeAllEntries();
            allContacts.forEach {
                addContact(it.name, it.email)
            }
            NewImportJobResult(
                recordsUpdated = allContacts.size,
                errorMessage = null
            )
        }
    }

    fun withErrorHandling(block : () -> NewImportJobResult) : NewImportJobResult {
        return try {
            block()
        } catch (ex: Exception) {
            NewImportJobResult(
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



}*/
