package com.colheitadecampo.domain

import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class MarcarColhidoPorGrupoUseCaseTest {
    
    private lateinit var plotRepository: PlotRepository
    private lateinit var fieldRepository: FieldRepository
    private lateinit var useCase: MarcarColhidoPorGrupoUseCase
    
    @Before
    fun setup() {
        plotRepository = mockk()
        fieldRepository = mockk()
        useCase = MarcarColhidoPorGrupoUseCase(plotRepository, fieldRepository)
    }
    
    @Test
    fun `marcarColhidosPorGrupo should update all plots in group`() = runTest {
        // Given
        val fieldId = 1L
        val grupoId = "G1"
        val field = Field(id = fieldId, name = "Test Field")
        
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId, grupoId = grupoId, colhido = false),
            Plot(recid = "2", fieldId = fieldId, grupoId = grupoId, colhido = false),
            Plot(recid = "3", fieldId = fieldId, grupoId = grupoId, colhido = false)
        )
        
        coEvery { fieldRepository.getFieldById(fieldId) } returns field
        coEvery { plotRepository.getPlotsByFieldIdAndGrupoId(fieldId, grupoId) } returns plots
        coEvery { plotRepository.updateColhidoStatusForGrupo(fieldId, grupoId, true) } returns 3
        
        // When
        val result = useCase.marcarColhidosPorGrupo(fieldId, grupoId, true)
        
        // Then
        assertEquals(plots.size, result.plotsAtualizados)
        assertEquals(field, result.field)
        assertEquals(plots.map { it.copy(colhido = true) }, result.plots)
        coVerify { plotRepository.updateColhidoStatusForGrupo(fieldId, grupoId, true) }
    }
    
    @Test
    fun `marcarColhidosPorGrupo should return zero updates when no plots found`() = runTest {
        // Given
        val fieldId = 1L
        val grupoId = "NONEXISTENT"
        val field = Field(id = fieldId, name = "Test Field")
        
        coEvery { fieldRepository.getFieldById(fieldId) } returns field
        coEvery { plotRepository.getPlotsByFieldIdAndGrupoId(fieldId, grupoId) } returns emptyList()
        coEvery { plotRepository.updateColhidoStatusForGrupo(fieldId, grupoId, true) } returns 0
        
        // When
        val result = useCase.marcarColhidosPorGrupo(fieldId, grupoId, true)
        
        // Then
        assertEquals(0, result.plotsAtualizados)
        assertEquals(field, result.field)
        assertEquals(emptyList(), result.plots)
    }
}
