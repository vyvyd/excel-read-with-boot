package com.demo.helloexcelboot

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ContactsDB {
    fun refreshContacts(allContacts: List<Contact>)
}

@Component
class JDBCContactsDB(
    private val jdbcTemplate: JdbcTemplate
) : ContactsDB {

    @Transactional
    override fun refreshContacts(allContacts: List<Contact>) {
        removeAllEntries();
        allContacts.forEach {
            addContact(it.name, it.email)
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