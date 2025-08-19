package com.colheitadecampo.domain

import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import com.colheitadecampo.util.ExcelService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ExportarArquivoUseCaseTest {
    
    private lateinit var fieldRepository: FieldRepository
    private lateinit var plotRepository: PlotRepository
    private lateinit var excelService: ExcelService
    private lateinit var useCase: ExportarArquivoUseCase
    
    @Before
    fun setup() {
        fieldRepository = mockk()
        plotRepository = mockk()
        excelService = mockk()
        useCase = ExportarArquivoUseCase(
            fieldRepository,
            plotRepository,
            excelService
        )
    }
    
    @Test
    fun `exportar should correctly create export file`() = runTest {
        // Given
        val fieldId = 1L
        val field = Field(id = fieldId, name = "Test Field")
        
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId, grupoId = "G1", colhido = true),
            Plot(recid = "2", fieldId = fieldId, grupoId = "G1", colhido = false),
            Plot(recid = "3", fieldId = fieldId, grupoId = "G2", colhido = true)
        )
        
        val mockExportFile = mockk<File>()
        every { mockExportFile.exists() } returns true
        every { mockExportFile.absolutePath } returns "/test/path/Test_Field_export.xlsx"
        
        coEvery { fieldRepository.getFieldById(fieldId) } returns field
        coEvery { plotRepository.getAllPlotsByFieldId(fieldId) } returns plots
        every { excelService.createExportFile(field, plots) } returns mockExportFile
        
        // When
        val result = useCase.exportar(fieldId)
        
        // Then
        assertNotNull(result)
        assertEquals(mockExportFile.absolutePath, result.filePath)
        assertEquals(field.name, result.fieldName)
        assertEquals(plots.count { it.colhido }, result.plotsColhidos)
        assertEquals(plots.size, result.totalPlots)
        coVerify { 
            fieldRepository.getFieldById(fieldId)
            plotRepository.getAllPlotsByFieldId(fieldId)
            excelService.createExportFile(field, plots)
        }
    }
    
    @Test
    fun `exportar should return null when field not found`() = runTest {
        // Given
        val fieldId = 1L
        coEvery { fieldRepository.getFieldById(fieldId) } returns null
        
        // When
        val result = useCase.exportar(fieldId)
        
        // Then
        assertEquals(null, result)
    }
}
