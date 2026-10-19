package com.example.model

import androidx.room.Embedded
import androidx.room.Relation

data class ClerkWithAdvocates(
    @Embedded
    val clerk: ClerkEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "clerkId"
    )
    val advocates: List<AdvocateEntity>
) {
    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return clerk.name.lowercase().contains(q)
    }
}
