package com.colheitadecampo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colheitadecampo.data.model.ImportSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(importSession: ImportSession): Long

    @Query("SELECT * FROM import_sessions ORDER BY dataHora DESC LIMIT :limit")
    fun getRecentImportSessions(limit: Int): Flow<List<ImportSession>>
}
