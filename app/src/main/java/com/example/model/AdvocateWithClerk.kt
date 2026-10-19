package com.example.model

import androidx.room.Embedded
import androidx.room.Relation

data class AdvocateWithClerk(
    @Embedded
    val advocate: AdvocateEntity,

    @Relation(
        parentColumn = "clerkId",
        entityColumn = "id"
    )
    val clerk: ClerkEntity?
) {
    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return advocate.name.lowercase().contains(q) ||
                advocate.code.lowercase().contains(q) ||
                advocate.panelAdvocate.lowercase().contains(q) ||
                (q in listOf("panel", "panel advocate", "panelist") && advocate.panelAdvocate.isNotBlank())
    }
}
