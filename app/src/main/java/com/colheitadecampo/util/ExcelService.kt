package com.colheitadecampo.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelService @Inject constructor(@dagger.hilt.android.qualifiers.ApplicationContext private val context: Context) {

    /**
     * Parse Excel file from content URI
     * @param uri The content URI to read from
     * @return Triple of Field, List<Plot>, and fileName
     */
    suspend fun parseXlsx(uri: Uri): Triple<Field, List<Plot>, String> {
        val fileName = getFileName(uri)
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Não foi possível abrir o arquivo")

        try {
            return inputStream.use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)
                var headerMap = getHeaderIndexMap(sheet)
                
                // Verify that the file contains all required columns (case-insensitive search)
                val requiredColumns = mapOf(
                    "Loc Seq" to listOf("loc seq", "locseq", "loc_seq"),
                    "entry book name" to listOf("entry book name", "entrybookname", "entry_book_name"),
                    "range" to listOf("range"),
                    "row" to listOf("row"),
                    "recid" to listOf("recid", "rec_id", "rec id"),
                    "tier" to listOf("tier"),
                    "plot" to listOf("plot"),
                    "GrupoId" to listOf("grupoid", "grupo_id", "grupo id", "group", "grupo")
                )
                
                // Map to normalize header names
                val normalizedHeaderMap = mutableMapOf<String, Int>()
                for ((key, columnIndex) in headerMap) {
                    val lowerKey = key.lowercase()
                    for ((reqKey, alternates) in requiredColumns) {
                        if (alternates.contains(lowerKey)) {
                            normalizedHeaderMap[reqKey] = columnIndex
                            break
                        }
                    }
                    // Keep original key as well
                    normalizedHeaderMap[key] = columnIndex
                }
                
                val missingColumns = requiredColumns.keys.filter { !normalizedHeaderMap.containsKey(it) }
                if (missingColumns.isNotEmpty()) {
                    throw IllegalArgumentException("Colunas obrigatórias ausentes: ${missingColumns.joinToString(", ")}")
                }
                
                // Use the normalized header map for all further processing
                headerMap = normalizedHeaderMap

                // Extract field name from file name (without extension)
                val fieldName = fileName.substringBeforeLast(".")

                // Create Field entity
                val field = Field(
                    nomeCampo = fieldName,
                    dataArquivo = LocalDateTime.now(),
                    origemArquivoPath = uri.toString()
                )

                // Parse plots
                val plots = mutableListOf<Plot>()
                for (i in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(i) ?: continue
                    try {
                        val recid = getCellValue(row, headerMap["recid"] ?: continue)
                        if (recid.isBlank()) continue

                        // Verificar a coluna decision para determinar se o plot deve ser descartado
                        // Primeiro, procurar em várias possíveis colunas relacionadas a descarte
                        val decisionIndex = headerMap["decision"] ?: headerMap["descartado"] ?: headerMap["discard"] ?: -1
                        val decision = if (decisionIndex != -1) getCellValue(row, decisionIndex).lowercase().trim() else ""
                        val isDiscarded = decision.contains("d") || 
                            decision.contains("discard") || 
                            decision.contains("descart") ||
                            decision == "1" || 
                            decision == "true" || 
                            decision == "yes" || 
                            decision == "sim"

                        plots.add(
                            Plot(
                                recid = recid,
                                fieldId = 0, // Will be updated after Field is inserted
                                locSeq = getCellValue(row, headerMap["Loc Seq"] ?: continue),
                                entryBookName = getCellValue(row, headerMap["entry book name"] ?: continue),
                                range = getCellValue(row, headerMap["range"] ?: continue),
                                row = getCellValue(row, headerMap["row"] ?: continue),
                                tier = getCellValue(row, headerMap["tier"] ?: continue),
                                plot = getCellValue(row, headerMap["plot"] ?: continue),
                                grupoId = getCellValue(row, headerMap["GrupoId"] ?: continue),
                                colhido = false,
                                descartado = isDiscarded,
                                decision = decision
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing row $i")
                    }
                }

                workbook.close()
                
                Triple(field, plots, fileName)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing Excel file")
            throw e
        }
    }

    /**
     * Create updated Excel file with harvest data
     * @param field The Field entity
     * @param plots The Plot entities
     * @return URI of the exported file
     */
    suspend fun writeXlsxAtualizado(field: Field, plots: List<Plot>): Uri {
        val originalUri = Uri.parse(field.origemArquivoPath)
        val inputStream = context.contentResolver.openInputStream(originalUri)
            ?: throw IllegalStateException("Não foi possível abrir o arquivo original")

        // Create output file in the app's Documents directory
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "${field.nomeCampo}_${timestamp}.xlsx"
        val outputDir = File(context.getExternalFilesDir(null), "Documents").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        try {
            inputStream.use { inStream ->
                val workbook = WorkbookFactory.create(inStream)
                val sheet = workbook.getSheetAt(0)
                val headerMap = getHeaderIndexMap(sheet)
                
                // Add "colhido" column header if it doesn't exist
                var colhidoIndex = headerMap["colhido"]
                if (colhidoIndex == null) {
                    val headerRow = sheet.getRow(0)
                    colhidoIndex = headerRow.physicalNumberOfCells
                    headerRow.createCell(colhidoIndex).setCellValue("colhido")
                }
                
                // Add "descartado" column header if it doesn't exist
                var descartadoIndex = headerMap["descartado"]
                if (descartadoIndex == null) {
                    val headerRow = sheet.getRow(0)
                    descartadoIndex = headerRow.physicalNumberOfCells
                    headerRow.createCell(descartadoIndex).setCellValue("descartado")
                }
                
                // Create a map of recid to Plot for faster lookup
                val plotsMap = plots.associateBy { it.recid }
                
                // Update or add harvest status and descartado status
                for (i in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(i) ?: continue
                    try {
                        val recid = getCellValue(row, headerMap["recid"] ?: continue)
                        if (recid.isBlank()) continue
                        
                        val plot = plotsMap[recid]
                        if (plot != null) {
                            // Atualiza status de colheita
                            val colhidoCell = row.getCell(colhidoIndex) ?: row.createCell(colhidoIndex)
                            colhidoCell.setCellValue(if (plot.colhido) 1.0 else 0.0)
                            
                            // Atualiza status de descarte
                            val descartadoCell = row.getCell(descartadoIndex) ?: row.createCell(descartadoIndex)
                            descartadoCell.setCellValue(if (plot.descartado) 1.0 else 0.0)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error updating row $i")
                    }
                }
                
                // Write to file
                FileOutputStream(outputFile).use { outStream ->
                    workbook.write(outStream)
                }
                workbook.close()
            }
            
            // Return content URI via FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
        } catch (e: Exception) {
            Timber.e(e, "Error exporting Excel file")
            throw e
        }
    }

    /**
     * Get file name from URI
     */
    private fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(
            uri, null, null, null, null
        ) ?: return ""

        cursor.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return uri.lastPathSegment ?: "unknown"
    }

    /**
     * Get file size from URI
     */
    fun getFileSize(uri: Uri): Long {
        val cursor = context.contentResolver.query(
            uri, null, null, null, null
        ) ?: return 0

        cursor.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    return it.getLong(sizeIndex)
                }
            }
        }
        return 0
    }

    /**
     * Get map of column headers to their indices
     */
    private fun getHeaderIndexMap(sheet: Sheet): Map<String, Int> {
        val headerRow = sheet.getRow(0) ?: return emptyMap()
        val headerMap = mutableMapOf<String, Int>()
        
        for (i in 0 until headerRow.physicalNumberOfCells) {
            val cellValue = getCellValue(headerRow, i)
            if (cellValue.isNotBlank()) {
                headerMap[cellValue] = i
            }
        }
        
        return headerMap
    }

    /**
     * Get cell value as string
     */
    private fun getCellValue(row: Row, columnIndex: Int): String {
        val cell = row.getCell(columnIndex) ?: return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                // Remove decimal point for whole numbers to evitar o ".0" no final
                val numValue = cell.numericCellValue
                if (numValue == numValue.toLong().toDouble()) {
                    numValue.toLong().toString()
                } else {
                    numValue.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue
                } catch (e: Exception) {
                    try {
                        val numValue = cell.numericCellValue
                        if (numValue == numValue.toLong().toDouble()) {
                            numValue.toLong().toString()
                        } else {
                            numValue.toString()
                        }
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
            else -> ""
        }
    }

    /**
     * Get the first N rows of an Excel file for preview
     */
    suspend fun getPreview(uri: Uri, rowLimit: Int = 20): List<Map<String, String>> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Não foi possível abrir o arquivo")

        return inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            val sheet = workbook.getSheetAt(0)
            val headerMap = getHeaderIndexMap(sheet)
            val headerIndices = headerMap.entries.associate { it.value to it.key }
            
            val result = mutableListOf<Map<String, String>>()
            val maxRows = minOf(rowLimit + 1, sheet.physicalNumberOfRows)
            
            for (i in 1 until maxRows) {
                val row = sheet.getRow(i) ?: continue
                val rowData = mutableMapOf<String, String>()
                
                for (j in headerIndices.keys) {
                    val columnName = headerIndices[j] ?: continue
                    rowData[columnName] = getCellValue(row, j)
                }
                
                result.add(rowData)
            }
            
            result
        }
    }

    /**
     * Copy file from URI to temporary file
     */
    fun copyToTemp(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Não foi possível abrir o arquivo")
            
        val fileName = getFileName(uri)
        val outputDir = context.cacheDir
        val outputFile = File(outputDir, "temp_$fileName")
        
        inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        
        return outputFile
    }
}
