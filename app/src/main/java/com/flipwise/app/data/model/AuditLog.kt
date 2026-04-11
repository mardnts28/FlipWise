package com.flipwise.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)