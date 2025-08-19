package com.colheitadecampo.ui.screens.import_export

import androidx.lifecycle.SavedStateHandle
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.domain.ExportarArquivoUseCase
import com.colheitadecampo.domain.ImportStrategy
import com.colheitadecampo.domain.ImportarArquivoUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ImportExportViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var importarArquivoUseCase: ImportarArquivoUseCase
    private lateinit var exportarArquivoUseCase: ExportarArquivoUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ImportExportViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        importarArquivoUseCase = mockk()
        exportarArquivoUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf(
            "fieldId" to "1"
        ))
        
        viewModel = ImportExportViewModel(
            importarArquivoUseCase,
            exportarArquivoUseCase,
            savedStateHandle
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `onStrategyChange should update selected strategy`() {
        // When
        viewModel.onStrategyChange(ImportStrategy.REPLACE)
        
        // Then
        assertEquals(ImportStrategy.REPLACE, viewModel.uiState.value.selectedStrategy)
        
        // When
        viewModel.onStrategyChange(ImportStrategy.MERGE)
        
        // Then
        assertEquals(ImportStrategy.MERGE, viewModel.uiState.value.selectedStrategy)
    }
    
    @Test
    fun `importarArquivo should import file and update state`() = runTest {
        // Given
        val mockFile = mockk<File>()
        val field = Field(id = 1L, name = "Test Field")
        val result = ImportarArquivoUseCase.Result(
            field = field,
            plotsImportados = 10
        )
        
        coEvery { importarArquivoUseCase.importar(mockFile, ImportStrategy.REPLACE) } returns result
        
        // When
        viewModel.onStrategyChange(ImportStrategy.REPLACE)
        viewModel.importarArquivo(mockFile)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.importSuccess)
        assertEquals(10, state.plotsImportados)
        assertEquals("Test Field", state.fieldName)
        assertFalse(state.isLoading)
        
        coVerify { importarArquivoUseCase.importar(mockFile, ImportStrategy.REPLACE) }
    }
    
    @Test
    fun `exportarArquivo should export file and update state`() = runTest {
        // Given
        val fieldId = 1L
        val result = ExportarArquivoUseCase.Result(
            filePath = "/test/path/Test_Field_export.xlsx",
            fieldName = "Test Field",
            plotsColhidos = 5,
            totalPlots = 10
        )
        
        coEvery { exportarArquivoUseCase.exportar(fieldId) } returns result
        
        // When
        viewModel.exportarArquivo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.exportSuccess)
        assertEquals(result.filePath, state.exportFilePath)
        assertEquals(5, state.plotsColhidos)
        assertEquals(10, state.plotsTotal)
        assertFalse(state.isLoading)
        
        coVerify { exportarArquivoUseCase.exportar(fieldId) }
    }
    
    @Test
    fun `resetImportState should reset import state`() {
        // Given
        val mockFile = mockk<File>()
        val field = Field(id = 1L, name = "Test Field")
        val result = ImportarArquivoUseCase.Result(
            field = field,
            plotsImportados = 10
        )
        
        coEvery { importarArquivoUseCase.importar(mockFile, ImportStrategy.REPLACE) } returns result
        
        viewModel.onStrategyChange(ImportStrategy.REPLACE)
        viewModel.importarArquivo(mockFile)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.resetImportState()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.importSuccess)
        assertEquals(0, state.plotsImportados)
        assertEquals("", state.fieldName)
    }
    
    @Test
    fun `resetExportState should reset export state`() {
        // Given
        val fieldId = 1L
        val result = ExportarArquivoUseCase.Result(
            filePath = "/test/path/Test_Field_export.xlsx",
            fieldName = "Test Field",
            plotsColhidos = 5,
            totalPlots = 10
        )
        
        coEvery { exportarArquivoUseCase.exportar(fieldId) } returns result
        
        viewModel.exportarArquivo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.resetExportState()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.exportSuccess)
        assertEquals("", state.exportFilePath)
        assertEquals(0, state.plotsColhidos)
        assertEquals(0, state.plotsTotal)
    }
}
