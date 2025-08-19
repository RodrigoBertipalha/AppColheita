package com.colheitadecampo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plots",
    indices = [
        Index(value = ["fieldId"]),
        Index(value = ["grupoId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Field::class,
            parentColumns = ["id"],
            childColumns = ["fieldId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Plot(
    @PrimaryKey
    val recid: String,
    val fieldId: Long,
    val locSeq: String,
    val entryBookName: String,
    val range: String,
    val row: String,
    val tier: String,
    val plot: String,
    val grupoId: String,
    val colhido: Boolean = false
)
