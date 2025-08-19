package com.colheitadecampo.ui.screens.harvest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import com.colheitadecampo.domain.MarcarColhidoPorGrupoUseCase
import com.colheitadecampo.domain.MarcarColhidoPorRecidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HarvestViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    private val plotRepository: PlotRepository,
    private val marcarColhidoPorRecidUseCase: MarcarColhidoPorRecidUseCase,
    private val marcarColhidoPorGrupoUseCase: MarcarColhidoPorGrupoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fieldId: Long = savedStateHandle.get<Long>("fieldId") ?: 0L

    private val _state = MutableStateFlow(HarvestState())
    
    init {
        loadField()
    }
    
    private fun loadField() {
        viewModelScope.launch {
            try {
                val field = fieldRepository.getFieldById(fieldId)
                _state.update { it.copy(field = field) }
            } catch (e: Exception) {
                Timber.e(e, "Error loading field")
            }
        }
    }

    private val _selectedGrupoId = MutableStateFlow<String?>(null)
    val selectedGrupoId = _selectedGrupoId.asStateFlow()

    private val _selectedPlots = MutableStateFlow<List<String>>(emptyList())
    val selectedPlots = _selectedPlots.asStateFlow()

    private val _showGroupDialog = MutableStateFlow(false)
    val showGroupDialog = _showGroupDialog.asStateFlow()

    val state: StateFlow<HarvestState> = combine(
        plotRepository.getTotalPlotsCount(fieldId),
        plotRepository.getHarvestedPlotsCount(fieldId),
        plotRepository.getDistinctGrupos(fieldId),
        _state
    ) { totalPlots, harvestedPlots, grupos, state ->
        state.copy(
            totalPlots = totalPlots,
            harvestedPlots = harvestedPlots,
            remainingPlots = totalPlots - harvestedPlots,
            availableGrupos = grupos,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HarvestState(isLoading = true)
    )

    fun updateRecidInput(recid: String) {
        _state.update { it.copy(recidInput = recid) }
    }

    fun marcarColhidoPorRecid() {
        val currentState = _state.value
        if (currentState.recidInput.isBlank()) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                marcarColhidoPorRecidUseCase.marcarColhido(
                    recid = currentState.recidInput,
                    colhido = true
                )
                
                _state.update { 
                    it.copy(
                        recidInput = "",
                        isLoading = false,
                        successMessage = "Plot ${currentState.recidInput} marcado como colhido!"
                    )
                }
                
                // Clear success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _state.update { it.copy(successMessage = null) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error marking plot as harvested")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Erro ao marcar plot como colhido"
                    )
                }
            }
        }
    }

    fun showGroupHarvestDialog(grupoId: String) {
        _state.update { 
            it.copy(
                selectedGrupoId = grupoId,
                showGroupDialog = true,
                selectedPlots = emptySet()
            )
        }
        
        // Load plots for this group
        viewModelScope.launch {
            try {
                plotRepository.getPlotsByGrupo(fieldId, grupoId).collect { plots ->
                    _state.update { it.copy(groupPlots = plots) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading group plots")
            }
        }
    }

    fun hideGroupDialog() {
        _state.update {
            it.copy(
                showGroupDialog = false,
                selectedGrupoId = null,
                selectedPlots = emptySet(),
                groupPlots = emptyList()
            )
        }
    }

    fun togglePlotSelection(recid: String) {
        val currentSelection = _state.value.selectedPlots
        _state.update {
            it.copy(
                selectedPlots = if (recid in currentSelection) {
                    currentSelection - recid
                } else {
                    currentSelection + recid
                }
            )
        }
    }

    fun marcarColhidoPorGrupo() {
        val currentState = _state.value
        val selectedGrupo = currentState.selectedGrupoId ?: return
        val selectedPlotIds = currentState.selectedPlots

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                marcarColhidoPorGrupoUseCase.marcarColhidoParaRecids(
                    recidList = selectedPlotIds.toList(),
                    colhido = true
                )
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        successMessageGrupo = "Plots do grupo $selectedGrupo marcados como colhidos!"
                    )
                }
                
                hideGroupDialog()
                
                // Clear success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _state.update { it.copy(successMessageGrupo = null) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error marking group as harvested")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Erro ao marcar grupo como colhido"
                    )
                }
            }
        }
    }

    fun resetSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    fun resetSuccessMessageGrupo() {
        _state.update { it.copy(successMessageGrupo = null) }
    }

    fun resetErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun desfazerColheita() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Find the last harvested plot and undo it
                try {
                    val lastHarvestedPlot = plotRepository.getLastHarvestedPlot(fieldId)
                    if (lastHarvestedPlot != null) {
                        plotRepository.updatePlot(lastHarvestedPlot.copy(colhido = false))
                        
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "Colheita do plot ${lastHarvestedPlot.recid} desfeita!"
                            )
                        }
                        
                        // Clear success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _state.update { it.copy(successMessage = null) }
                    } else {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Não há colheitas para desfazer"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Erro ao desfazer: ${e.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error undoing harvest")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Erro ao desfazer colheita"
                    )
                }
            }
        }
    }
}

data class HarvestState(
    val field: Field? = null,
    val recidInput: String = "",
    val totalPlots: Int = 0,
    val harvestedPlots: Int = 0,
    val remainingPlots: Int = 0,
    val availableGrupos: List<String> = emptyList(),
    val groupPlots: List<Plot> = emptyList(),
    val selectedGrupoId: String? = null,
    val selectedPlots: Set<String> = emptySet(),
    val showGroupDialog: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val successMessageGrupo: String? = null,
    val errorMessage: String? = null
)
