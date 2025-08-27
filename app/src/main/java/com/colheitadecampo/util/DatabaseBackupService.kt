package com.colheitadecampo.util

import android.content.Context
import android.net.Uri
import androidx.room.RoomDatabase
import com.colheitadecampo.data.local.ColheitaDatabase
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupService @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val database: ColheitaDatabase
) {
    /**
     * Cria um backup do banco de dados antes da importação
     * Isso pode ajudar a recuperar dados caso algo dê errado durante o processo de importação
     */
    suspend fun backupDatabaseBeforeImport(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Fechar conexões com o banco de dados - não precisamos invalidar o banco
                // pois o Room gerencia as conexões automaticamente
                
                // Localizar arquivo do banco de dados
                val databaseFile = context.getDatabasePath("colheita_database")
                if (!databaseFile.exists()) {
                    Timber.w("Arquivo de banco de dados não encontrado")
                    return@withContext false
                }
                
                // Criar diretório de backup se não existir
                val backupDir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
                val timestamp = System.currentTimeMillis()
                val backupFile = File(backupDir, "colheita_database_backup_$timestamp.db")
                
                // Copiar banco para o arquivo de backup
                databaseFile.copyTo(backupFile, overwrite = true)
                
                Timber.d("Backup do banco criado com sucesso em: ${backupFile.absolutePath}")
                return@withContext true
            } catch (e: Exception) {
                Timber.e(e, "Erro ao criar backup do banco de dados")
                return@withContext false
            }
        }
    }
    
    /**
     * Restaura o último backup do banco de dados em caso de falha na importação
     */
    suspend fun restoreLatestBackup(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Fechar conexões com o banco de dados - não precisamos invalidar o banco
                // pois o Room gerencia as conexões automaticamente
                
                // Diretório de backup
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists() || !backupDir.isDirectory) {
                    Timber.w("Diretório de backup não encontrado")
                    return@withContext false
                }
                
                // Encontrar backup mais recente
                val backupFiles = backupDir.listFiles { file -> file.name.endsWith(".db") }
                    ?.sortedByDescending { it.lastModified() }
                
                if (backupFiles.isNullOrEmpty()) {
                    Timber.w("Nenhum arquivo de backup encontrado")
                    return@withContext false
                }
                
                val latestBackup = backupFiles[0]
                val databaseFile = context.getDatabasePath("colheita_database")
                
                // Restaurar banco de dados
                latestBackup.copyTo(databaseFile, overwrite = true)
                
                Timber.d("Backup restaurado com sucesso de: ${latestBackup.absolutePath}")
                return@withContext true
            } catch (e: Exception) {
                Timber.e(e, "Erro ao restaurar backup do banco de dados")
                return@withContext false
            }
        }
    }
}
