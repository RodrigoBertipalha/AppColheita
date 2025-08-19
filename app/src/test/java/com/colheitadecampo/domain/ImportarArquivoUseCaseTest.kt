package com.colheitadecampo.domain

import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.ImportSession
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.ImportSessionRepository
import com.colheitadecampo.data.repository.PlotRepository
import com.colheitadecampo.util.ExcelService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ImportarArquivoUseCaseTest {
    
    private lateinit var fieldRepository: FieldRepository
    private lateinit var plotRepository: PlotRepository
    private lateinit var importSessionRepository: ImportSessionRepository
    private lateinit var excelService: ExcelService
    private lateinit var useCase: ImportarArquivoUseCase
    
    @Before
    fun setup() {
        fieldRepository = mockk()
        plotRepository = mockk()
        importSessionRepository = mockk()
        excelService = mockk()
        useCase = ImportarArquivoUseCase(
            fieldRepository, 
            plotRepository, 
            importSessionRepository,
            excelService
        )
    }
    
    @Test
    fun `importar should correctly import data in REPLACE mode`() = runTest {
        // Given
        val mockFile = mockk<File>()
        val mockInputStream = mockk<InputStream>()
        every { mockFile.name } returns "test_file.xlsx"
        every { mockFile.inputStream() } returns mockInputStream
        
        val fieldName = "Test Field"
        val fieldId = 1L
        val field = Field(id = fieldId, name = fieldName)
        
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId, grupoId = "G1", colhido = false),
            Plot(recid = "2", fieldId = fieldId, grupoId = "G1", colhido = false),
            Plot(recid = "3", fieldId = fieldId, grupoId = "G2", colhido = false)
        )
        
        every { excelService.extractFieldName(any()) } returns fieldName
        every { excelService.extractPlots(any(), fieldId) } returns plots
        
        coEvery { fieldRepository.getFieldByName(fieldName) } returns null
        coEvery { fieldRepository.insertField(any()) } returns fieldId
        coEvery { plotRepository.deleteAllPlotsByFieldId(fieldId) } returns Unit
        coEvery { plotRepository.insertPlots(plots) } returns Unit
        coEvery { importSessionRepository.insertImportSession(any()) } returns 1L
        
        // When
        val result = useCase.importar(mockFile, ImportStrategy.REPLACE)
        
        // Then
        assertEquals(fieldId, result.field.id)
        assertEquals(fieldName, result.field.name)
        assertEquals(plots.size, result.plotsImportados)
        coVerify { 
            fieldRepository.getFieldByName(fieldName)
            fieldRepository.insertField(any())
            plotRepository.insertPlots(plots)
            importSessionRepository.insertImportSession(any())
        }
    }
    
    @Test
    fun `importar should correctly import data in MERGE mode with existing field`() = runTest {
        // Given
        val mockFile = mockk<File>()
        val mockInputStream = mockk<InputStream>()
        every { mockFile.name } returns "test_file.xlsx"
        every { mockFile.inputStream() } returns mockInputStream
        
        val fieldName = "Test Field"
        val fieldId = 1L
        val field = Field(id = fieldId, name = fieldName)
        
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId, grupoId = "G1", colhido = false),
            Plot(recid = "2", fieldId = fieldId, grupoId = "G1", colhido = false)
        )
        
        val existingPlots = listOf(
            Plot(recid = "3", fieldId = fieldId, grupoId = "G2", colhido = true),
            Plot(recid = "4", fieldId = fieldId, grupoId = "G2", colhido = false)
        )
        
        every { excelService.extractFieldName(any()) } returns fieldName
        every { excelService.extractPlots(any(), fieldId) } returns plots
        
        coEvery { fieldRepository.getFieldByName(fieldName) } returns field
        coEvery { plotRepository.getAllPlotsByFieldId(fieldId) } returns existingPlots
        coEvery { plotRepository.deletePlotsWithRecids(any()) } returns Unit
        coEvery { plotRepository.insertPlots(plots) } returns Unit
        coEvery { importSessionRepository.insertImportSession(any()) } returns 1L
        
        // When
        val result = useCase.importar(mockFile, ImportStrategy.MERGE)
        
        // Then
        assertEquals(fieldId, result.field.id)
        assertEquals(fieldName, result.field.name)
        assertEquals(plots.size, result.plotsImportados)
        coVerify { 
            fieldRepository.getFieldByName(fieldName)
            plotRepository.getAllPlotsByFieldId(fieldId)
            plotRepository.deletePlotsWithRecids(plots.map { it.recid })
            plotRepository.insertPlots(plots)
            importSessionRepository.insertImportSession(any())
        }
        coVerify(exactly = 0) { plotRepository.deleteAllPlotsByFieldId(any()) }
    }
}
