package com.colheitadecampo.data.repository

import com.colheitadecampo.data.local.FieldDao
import com.colheitadecampo.data.model.Field
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldRepository @Inject constructor(private val fieldDao: FieldDao) {
    fun getLastField(): Flow<Field?> = fieldDao.getLastField()

    fun getAllFields(): Flow<List<Field>> = fieldDao.getAllFields()

    suspend fun getFieldById(fieldId: Long): Field? = fieldDao.getFieldById(fieldId)

    suspend fun insertField(field: Field): Long = fieldDao.insert(field)

    suspend fun deleteField(fieldId: Long) = fieldDao.deleteField(fieldId)

    suspend fun deleteAllFields() = fieldDao.deleteAllFields()
}
