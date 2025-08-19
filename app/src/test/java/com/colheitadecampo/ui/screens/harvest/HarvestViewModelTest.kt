package com.colheitadecampo.ui.screens.harvest

import androidx.lifecycle.SavedStateHandle
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.domain.MarcarColhidoPorGrupoUseCase
import com.colheitadecampo.domain.MarcarColhidoPorRecidUseCase
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class HarvestViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var marcarColhidoPorRecidUseCase: MarcarColhidoPorRecidUseCase
    private lateinit var marcarColhidoPorGrupoUseCase: MarcarColhidoPorGrupoUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: HarvestViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        marcarColhidoPorRecidUseCase = mockk()
        marcarColhidoPorGrupoUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf(
            "fieldId" to "1"
        ))
        
        viewModel = HarvestViewModel(
            marcarColhidoPorRecidUseCase,
            marcarColhidoPorGrupoUseCase,
            savedStateHandle
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `onRecidTextChanged should update recidText`() = runTest {
        // When
        viewModel.onRecidTextChanged("TEST123")
        
        // Then
        assertEquals("TEST123", viewModel.uiState.value.recidText)
    }
    
    @Test
    fun `marcarColhidoPorRecid should mark plot as harvested and update state`() = runTest {
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
            grupoId = "G1",
            colhido = true
        )
        
        coEvery { marcarColhidoPorRecidUseCase.marcarColhido(recid, true) } returns plot
        
        // When
        viewModel.onRecidTextChanged(recid)
        viewModel.marcarColhidoPorRecid()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.recidText)
        assertEquals(plot, state.ultimoPlotColhido)
        assertTrue(state.mostrarMensagemSucesso)
        assertNull(state.errorMessage)
        
        coVerify { marcarColhidoPorRecidUseCase.marcarColhido(recid, true) }
    }
    
    @Test
    fun `marcarColhidoPorRecid should show error when plot not found`() = runTest {
        // Given
        val recid = "NONEXISTENT"
        coEvery { marcarColhidoPorRecidUseCase.marcarColhido(recid, true) } returns null
        
        // When
        viewModel.onRecidTextChanged(recid)
        viewModel.marcarColhidoPorRecid()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.recidText)
        assertNull(state.ultimoPlotColhido)
        assertFalse(state.mostrarMensagemSucesso)
        assertEquals("Plot não encontrado com RECID: NONEXISTENT", state.errorMessage)
        
        coVerify { marcarColhidoPorRecidUseCase.marcarColhido(recid, true) }
    }
    
    @Test
    fun `resetErrorMessage should clear error message`() {
        // Given
        viewModel = HarvestViewModel(
            marcarColhidoPorRecidUseCase,
            marcarColhidoPorGrupoUseCase,
            savedStateHandle
        )
        viewModel.setErrorMessage("Test error")
        
        // When
        viewModel.resetErrorMessage()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `resetSuccessMessage should clear success message`() {
        // Given
        viewModel = HarvestViewModel(
            marcarColhidoPorRecidUseCase,
            marcarColhidoPorGrupoUseCase,
            savedStateHandle
        )
        viewModel.setSuccessMessage()
        
        // When
        viewModel.resetSuccessMessage()
        
        // Then
        assertFalse(viewModel.uiState.value.mostrarMensagemSucesso)
    }
    
    @Test
    fun `abrirDialogColheitaGrupo should show dialog`() {
        // When
        viewModel.abrirDialogColheitaGrupo()
        
        // Then
        assertTrue(viewModel.uiState.value.mostrarDialogGrupo)
    }
    
    @Test
    fun `fecharDialogColheitaGrupo should hide dialog`() {
        // Given
        viewModel.abrirDialogColheitaGrupo()
        
        // When
        viewModel.fecharDialogColheitaGrupo()
        
        // Then
        assertFalse(viewModel.uiState.value.mostrarDialogGrupo)
    }
    
    @Test
    fun `onGrupoTextChanged should update grupoId`() {
        // When
        viewModel.onGrupoTextChanged("G1")
        
        // Then
        assertEquals("G1", viewModel.uiState.value.grupoId)
    }
    
    @Test
    fun `marcarColhidoPorGrupo should mark plots in group and update state`() = runTest {
        // Given
        val fieldId = 1L
        val grupoId = "G1"
        val field = Field(id = fieldId, name = "Test Field")
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId, grupoId = grupoId, colhido = true),
            Plot(recid = "2", fieldId = fieldId, grupoId = grupoId, colhido = true)
        )
        
        val result = MarcarColhidoPorGrupoUseCase.Result(
            field = field,
            plots = plots,
            plotsAtualizados = 2
        )
        
        coEvery { marcarColhidoPorGrupoUseCase.marcarColhidosPorGrupo(fieldId, grupoId, true) } returns result
        
        // When
        viewModel.onGrupoTextChanged(grupoId)
        viewModel.marcarColhidoPorGrupo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.grupoId)
        assertEquals(2, state.plotsDoGrupoColhidos)
        assertTrue(state.mostrarMensagemSucessoGrupo)
        assertNull(state.errorMessage)
        assertFalse(state.mostrarDialogGrupo)
        
        coVerify { marcarColhidoPorGrupoUseCase.marcarColhidosPorGrupo(fieldId, grupoId, true) }
    }
    
    @Test
    fun `marcarColhidoPorGrupo should show error when no plots updated`() = runTest {
        // Given
        val fieldId = 1L
        val grupoId = "G1"
        val field = Field(id = fieldId, name = "Test Field")
        
        val result = MarcarColhidoPorGrupoUseCase.Result(
            field = field,
            plots = emptyList(),
            plotsAtualizados = 0
        )
        
        coEvery { marcarColhidoPorGrupoUseCase.marcarColhidosPorGrupo(fieldId, grupoId, true) } returns result
        
        // When
        viewModel.onGrupoTextChanged(grupoId)
        viewModel.marcarColhidoPorGrupo()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.grupoId)
        assertEquals(0, state.plotsDoGrupoColhidos)
        assertFalse(state.mostrarMensagemSucessoGrupo)
        assertEquals("Nenhum plot encontrado no grupo: G1", state.errorMessage)
        assertFalse(state.mostrarDialogGrupo)
        
        coVerify { marcarColhidoPorGrupoUseCase.marcarColhidosPorGrupo(fieldId, grupoId, true) }
    }
    
    @Test
    fun `resetSuccessMessageGrupo should clear group success message`() {
        // Given
        viewModel = HarvestViewModel(
            marcarColhidoPorRecidUseCase,
            marcarColhidoPorGrupoUseCase,
            savedStateHandle
        )
        viewModel.setSuccessMessageGrupo(5)
        
        // When
        viewModel.resetSuccessMessageGrupo()
        
        // Then
        assertFalse(viewModel.uiState.value.mostrarMensagemSucessoGrupo)
    }
}
