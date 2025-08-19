package com.colheitadecampo.domain

import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.PlotRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class MarcarColhidoPorRecidUseCaseTest {
    
    private lateinit var plotRepository: PlotRepository
    private lateinit var useCase: MarcarColhidoPorRecidUseCase
    
    @Before
    fun setup() {
        plotRepository = mockk()
        useCase = MarcarColhidoPorRecidUseCase(plotRepository)
    }
    
    @Test
    fun `marcarColhido should update plot colhido status to true`() = runTest {
        // Given
        val recid = "TEST123"
        val plot = Plot(
            recid = recid,
            fieldId = 1,
            locSeq = "A1",
            entryBookName = "Test Book",
            range = "1",
            row = "2",
            tier = "3",
            plot = "123",
            grupoId = "f1",
            colhido = false
        )
        
        coEvery { plotRepository.getPlotByRecid(recid) } returns plot
        coEvery { plotRepository.updateColhidoStatus(recid, true) } returns Unit
        
        // When
        val result = useCase.marcarColhido(recid, true)
        
        // Then
        assertEquals(plot.copy(colhido = true), result)
        coVerify { plotRepository.updateColhidoStatus(recid, true) }
    }
    
    @Test
    fun `marcarColhido should return null when plot not found`() = runTest {
        // Given
        val recid = "NONEXISTENT"
        coEvery { plotRepository.getPlotByRecid(recid) } returns null
        
        // When
        val result = useCase.marcarColhido(recid, true)
        
        // Then
        assertNull(result)
    }
}
