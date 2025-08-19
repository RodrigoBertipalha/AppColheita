package com.colheitadecampo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colheitadecampo.data.model.Field
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(field: Field): Long

    @Query("SELECT * FROM fields ORDER BY dataArquivo DESC LIMIT 1")
    fun getLastField(): Flow<Field?>

    @Query("SELECT * FROM fields ORDER BY dataArquivo DESC")
    fun getAllFields(): Flow<List<Field>>

    @Query("SELECT * FROM fields WHERE id = :fieldId")
    suspend fun getFieldById(fieldId: Long): Field?

    @Query("DELETE FROM fields WHERE id = :fieldId")
    suspend fun deleteField(fieldId: Long)

    @Transaction
    @Query("DELETE FROM fields")
    suspend fun deleteAllFields()
}
