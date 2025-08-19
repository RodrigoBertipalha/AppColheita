package com.colheitadecampo.domain

import android.net.Uri
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.ImportSession
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.ImportSessionRepository
import com.colheitadecampo.data.repository.PlotRepository
import com.colheitadecampo.util.ExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for importing Excel files
 */
@Singleton
class ImportarArquivoUseCase @Inject constructor(
    private val excelService: ExcelService,
    private val fieldRepository: FieldRepository,
    private val plotRepository: PlotRepository,
    private val importSessionRepository: ImportSessionRepository
) {
    sealed class ImportStrategy {
        object Replace : ImportStrategy()
        object Merge : ImportStrategy()
    }

    suspend fun importFile(uri: Uri, strategy: ImportStrategy): Triple<Field, Int, Int> = withContext(Dispatchers.IO) {
        try {
            // Parse the file
            val (field, plots, fileName) = excelService.parseXlsx(uri)
            val fileSize = excelService.getFileSize(uri)
            
            when (strategy) {
                is ImportStrategy.Replace -> {
                    // Delete all existing fields and plots
                    fieldRepository.deleteAllFields()
                    
                    // Insert new field and get its ID
                    val fieldId = fieldRepository.insertField(field)
                    
                    // Update plots with field ID and insert
                    val updatedPlots = plots.map { it.copy(fieldId = fieldId) }
                    plotRepository.insertPlots(updatedPlots)
                    
                    // Record import session
                    val importSession = ImportSession(
                        path = uri.toString(),
                        tamanhoBytes = fileSize,
                        linhasProcessadas = plots.size,
                        dataHora = LocalDateTime.now()
                    )
                    importSessionRepository.insertImportSession(importSession)
                    
                    Triple(field.copy(id = fieldId), plots.size, plots.size)
                }
                
                is ImportStrategy.Merge -> {
                    // Get last field, if exists
                    val lastField = fieldRepository.getAllFields().first().lastOrNull()
                    val fieldId = if (lastField != null) {
                        // Keep the last field ID
                        val updatedField = field.copy(id = lastField.id)
                        fieldRepository.insertField(updatedField)
                        lastField.id
                    } else {
                        // Insert as new field
                        fieldRepository.insertField(field)
                    }
                    
                    // Prepare plots with field ID
                    val updatedPlots = plots.map { it.copy(fieldId = fieldId) }
                    
                    // Count new plots (not in the database)
                    var newPlotsCount = 0
                    val existingPlots = mutableListOf<Plot>()
                    
                    for (plot in updatedPlots) {
                        val existingPlot = plotRepository.getPlotByRecid(plot.recid)
                        if (existingPlot == null) {
                            newPlotsCount++
                            existingPlots.add(plot) // New plot to insert
                        } else {
                            // Keep colhido status and other original fields from existing plot
                            existingPlots.add(existingPlot)
                        }
                    }
                    
                    // Insert/update plots
                    plotRepository.insertPlots(existingPlots)
                    
                    // Record import session
                    val importSession = ImportSession(
                        path = uri.toString(),
                        tamanhoBytes = fileSize,
                        linhasProcessadas = plots.size,
                        dataHora = LocalDateTime.now()
                    )
                    importSessionRepository.insertImportSession(importSession)
                    
                    Triple(field.copy(id = fieldId), plots.size, newPlotsCount)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error importing file")
            throw e
        }
    }
    
    suspend fun getFilePreview(uri: Uri, rowLimit: Int = 20) = withContext(Dispatchers.IO) {
        excelService.getPreview(uri, rowLimit)
    }
}
