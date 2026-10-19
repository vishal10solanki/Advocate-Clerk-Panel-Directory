package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "advocates",
    foreignKeys = [
        ForeignKey(
            entity = ClerkEntity::class,
            parentColumns = ["id"],
            childColumns = ["clerkId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clerkId"),
        Index("code"),
        Index("name")
    ]
)
data class AdvocateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val primaryMobile: String = "",
    val secondaryMobile: String = "",
    val panelAdvocate: String = "",
    val clerkId: Long? = null,
    val additionalClerkIds: String = "",
    val initialSrNo: Int? = null
)

fun AdvocateEntity.getAllClerkIds(): List<Long> {
    val list = mutableListOf<Long>()
    if (clerkId != null && clerkId > 0) {
        list.add(clerkId)
    }
    if (additionalClerkIds.isNotBlank()) {
        additionalClerkIds.split(",").forEach { s ->
            s.trim().toLongOrNull()?.let { id ->
                if (!list.contains(id)) list.add(id)
            }
        }
    }
    return list
}

fun parsePanelList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return if (raw.contains("\n")) {
        raw.lines().map { it.trim() }.filter { it.isNotBlank() }
    } else if (raw.contains(";")) {
        raw.split(";").map { it.trim() }.filter { it.isNotBlank() }
    } else if (raw.contains(",")) {
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
    } else {
        listOf(raw.trim())
    }
}

fun AdvocateEntity.getPanelList(): List<String> = parsePanelList(panelAdvocate)
