package com.colheitadecampo.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import com.colheitadecampo.util.ExcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for exporting Excel files with harvest data
 */
@Singleton
class ExportarArquivoUseCase @Inject constructor(
    private val excelService: ExcelService,
    private val fieldRepository: FieldRepository,
    private val plotRepository: PlotRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    suspend fun exportarArquivo(fieldId: Long): Uri = withContext(Dispatchers.IO) {
        try {
            val field = fieldRepository.getFieldById(fieldId)
                ?: throw IllegalStateException("Campo não encontrado")
                
            val plots = plotRepository.getAllPlotsByFieldId(fieldId).first()
            
            // Generate updated Excel file
            excelService.writeXlsxAtualizado(field, plots)
        } catch (e: Exception) {
            Timber.e(e, "Error exporting file")
            throw e
        }
    }
    
    fun shareViaEmail(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_SUBJECT, "Exportação Colheita de Campo")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o arquivo de colheita atualizado.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Enviar por e-mail")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
