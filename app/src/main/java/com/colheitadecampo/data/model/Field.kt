package com.colheitadecampo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "fields")
data class Field(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nomeCampo: String,
    val dataArquivo: LocalDateTime,
    val origemArquivoPath: String
)
