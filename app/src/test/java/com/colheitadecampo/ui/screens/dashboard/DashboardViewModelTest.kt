package com.colheitadecampo.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.GroupStats
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fieldRepository: FieldRepository
    private lateinit var plotRepository: PlotRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: DashboardViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        fieldRepository = mockk()
        plotRepository = mockk()
        savedStateHandle = SavedStateHandle(mapOf(
            "fieldId" to "1"
        ))
        
        viewModel = DashboardViewModel(
            fieldRepository,
            plotRepository,
            savedStateHandle
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `init should load field and group stats`() = runTest {
        // Given
        val fieldId = 1L
        val field = Field(id = fieldId, name = "Test Field")
        val groupStats = listOf(
            GroupStats(grupoId = "G1", total = 10, colhidos = 5),
            GroupStats(grupoId = "G2", total = 8, colhidos = 2)
        )
        
        coEvery { fieldRepository.getFieldById(fieldId) } returns field
        coEvery { plotRepository.getFieldColheita(fieldId) } returns flowOf(groupStats)
        
        // When
        viewModel.loadData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(field, state.field)
        assertEquals(groupStats, state.groupStats)
        assertEquals(18, state.totalPlots)
        assertEquals(7, state.totalColhidos)
        assertEquals(38.89, state.porcentagemColhida, 0.01)
    }
    
    @Test
    fun `loadData should handle empty group stats`() = runTest {
        // Given
        val fieldId = 1L
        val field = Field(id = fieldId, name = "Test Field")
        val groupStats = emptyList<GroupStats>()
        
        coEvery { fieldRepository.getFieldById(fieldId) } returns field
        coEvery { plotRepository.getFieldColheita(fieldId) } returns flowOf(groupStats)
        
        // When
        viewModel.loadData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(field, state.field)
        assertEquals(groupStats, state.groupStats)
        assertEquals(0, state.totalPlots)
        assertEquals(0, state.totalColhidos)
        assertEquals(0.0, state.porcentagemColhida)
    }
}
