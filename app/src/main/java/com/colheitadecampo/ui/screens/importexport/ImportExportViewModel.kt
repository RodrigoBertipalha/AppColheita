package com.colheitadecampo.ui.screens.importexport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.ImportSession
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.ImportSessionRepository
import com.colheitadecampo.domain.ExportarArquivoUseCase
import com.colheitadecampo.domain.ImportarArquivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val importarArquivoUseCase: ImportarArquivoUseCase,
    private val exportarArquivoUseCase: ExportarArquivoUseCase,
    private val importSessionRepository: ImportSessionRepository,
    private val fieldRepository: FieldRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImportExportState())
    val state: StateFlow<ImportExportState> = combine(
        _state,
        importSessionRepository.getRecentImportSessions(5),
        fieldRepository.getLastField()
    ) { state, importSessions, lastField ->
        state.copy(
            recentImportSessions = importSessions,
            currentField = lastField,
            isLoading = false
        )
    }
    .catch { e ->
        Timber.e(e, "Error loading import/export data")
        _state.update { it.copy(error = e.localizedMessage, isLoading = false) }
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ImportExportState(isLoading = true)
    )

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, selectedFileUri = uri) }
            try {
                val preview = importarArquivoUseCase.getFilePreview(uri)
                _state.update { it.copy(
                    filePreview = preview,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error previewing file")
                _state.update { it.copy(
                    error = e.localizedMessage,
                    isLoading = false
                ) }
            }
        }
    }

    fun onImportStrategySelected(strategy: ImportarArquivoUseCase.ImportStrategy) {
        _state.update { it.copy(selectedImportStrategy = strategy) }
    }

    fun importFile() {
        val uri = state.value.selectedFileUri ?: return
        val strategy = state.value.selectedImportStrategy ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = importarArquivoUseCase.importFile(uri, strategy)
                _state.update { it.copy(
                    importResult = result,
                    isImportSuccess = true,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error importing file")
                _state.update { it.copy(
                    error = e.localizedMessage,
                    isLoading = false
                ) }
            }
        }
    }

    fun exportFile() {
        val fieldId = state.value.currentField?.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val exportedFileUri = exportarArquivoUseCase.exportarArquivo(fieldId)
                _state.update { it.copy(
                    exportedFileUri = exportedFileUri,
                    isExportSuccess = true,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error exporting file")
                _state.update { it.copy(
                    error = e.localizedMessage,
                    isLoading = false
                ) }
            }
        }
    }

    fun shareExportedFile() {
        val uri = state.value.exportedFileUri ?: return
        exportarArquivoUseCase.shareViaEmail(uri)
    }

    fun resetError() {
        _state.update { it.copy(error = null) }
    }

    fun resetImportSuccess() {
        _state.update { it.copy(isImportSuccess = false) }
    }

    fun resetExportSuccess() {
        _state.update { it.copy(isExportSuccess = false) }
    }
}

data class ImportExportState(
    val selectedFileUri: Uri? = null,
    val filePreview: List<Map<String, String>> = emptyList(),
    val selectedImportStrategy: ImportarArquivoUseCase.ImportStrategy? = ImportarArquivoUseCase.ImportStrategy.Replace,
    val recentImportSessions: List<ImportSession> = emptyList(),
    val currentField: Field? = null,
    val importResult: Triple<Field, Int, Int>? = null,
    val exportedFileUri: Uri? = null,
    val isImportSuccess: Boolean = false,
    val isExportSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
