package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clerks")
data class ClerkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryMobile: String = "",
    val secondaryMobile: String = ""
)
