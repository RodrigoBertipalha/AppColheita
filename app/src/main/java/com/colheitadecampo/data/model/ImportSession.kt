package com.colheitadecampo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "import_sessions")
data class ImportSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val tamanhoBytes: Long,
    val linhasProcessadas: Int,
    val dataHora: LocalDateTime
)
