package com.colheitadecampo.data.repository

import com.colheitadecampo.data.local.ImportSessionDao
import com.colheitadecampo.data.model.ImportSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportSessionRepository @Inject constructor(private val importSessionDao: ImportSessionDao) {
    suspend fun insertImportSession(importSession: ImportSession): Long = 
        importSessionDao.insert(importSession)

    fun getRecentImportSessions(limit: Int): Flow<List<ImportSession>> = 
        importSessionDao.getRecentImportSessions(limit)
}
