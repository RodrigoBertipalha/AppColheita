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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
                _state.update { it.copy(isLoading = true) }
                
                val field = fieldRepository.getFieldById(fieldId)
                if (field != null) {
                    _state.update { it.copy(field = field, isLoading = false) }
                } else {
                    _state.update { it.copy(
                        isLoading = false, 
                        errorMessage = "Campo não encontrado. ID: $fieldId"
                    )}
                    Timber.e("Campo não encontrado: fieldId=$fieldId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading field")
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar campo: ${e.localizedMessage ?: e.toString()}"
                )}
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
        plotRepository.getDiscardedPlotsCount(fieldId),
        _state
    ) { totalPlots, harvestedPlots, grupos, discardedPlots, state ->
        // Calcula o total de plots elegíveis (excluindo descartados)
        val eligiblePlots = totalPlots - discardedPlots
        
        state.copy(
            totalPlots = eligiblePlots, // Apenas plots elegíveis
            harvestedPlots = harvestedPlots,
            remainingPlots = eligiblePlots - harvestedPlots,
            discardedPlots = discardedPlots, // Adiciona contagem de plots descartados
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
                
                // Verificar se o plot existe e se está descartado
                val plot = plotRepository.getPlotByRecid(currentState.recidInput)
                
                if (plot == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Plot não encontrado: ${currentState.recidInput}"
                        )
                    }
                    return@launch
                }
                
                // Se o plot estiver descartado, exibir mensagem de erro
                if (plot.descartado) {
                    _state.update { 
                        it.copy(
                            recidInput = "",
                            isLoading = false,
                            lastCheckedPlot = plot,
                            errorMessage = "Plot ${currentState.recidInput} foi DESCARTADO e não pode ser colhido!"
                        )
                    }
                    
                    // Limpar mensagem de erro após 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _state.update { it.copy(errorMessage = null) }
                    return@launch
                }
                
                // Se não estiver descartado, proceder com a colheita normal
                val updatedPlot = marcarColhidoPorRecidUseCase.marcarColhido(
                    recid = currentState.recidInput,
                    colhido = true
                )
                
                // Recupera o último plot colhido para mostrar na UI
                val updatedLastColhidoPlot = updatedPlot ?: plot
                
                // Obtém os valores atualizados do banco de dados
                val newTotalPlots = plotRepository.getTotalPlotsCount(fieldId).first()
                val newDiscardedPlots = plotRepository.getDiscardedPlotsCount(fieldId).first()
                val newHarvestedPlots = plotRepository.getHarvestedPlotsCount(fieldId).first()
                val eligiblePlots = newTotalPlots - newDiscardedPlots
                
                _state.update { 
                    it.copy(
                        recidInput = "",
                        isLoading = false,
                        totalPlots = eligiblePlots,
                        harvestedPlots = newHarvestedPlots,
                        remainingPlots = eligiblePlots - newHarvestedPlots,
                        discardedPlots = newDiscardedPlots,
                        lastColhidoPlot = updatedLastColhidoPlot,
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
                // Primeiro, limpe os plots do grupo anterior
                _state.update { it.copy(groupPlots = emptyList()) }
                
                // Agora carregue os plots do grupo selecionado, excluindo os descartados
                plotRepository.getNonDiscardedPlotsByGrupo(fieldId, grupoId).collect { plots ->
                    _state.update { it.copy(groupPlots = plots) }
                    
                    // Registre para debug
                    Timber.d("Carregados ${plots.size} plots não descartados para o grupo $grupoId")
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
                
                // Marcar os plots selecionados como colhidos
                marcarColhidoPorGrupoUseCase.marcarColhidoParaRecids(
                    recidList = selectedPlotIds.toList(),
                    colhido = true
                )
                
                // Obter contadores atualizados após a operação
                val newTotalPlots = plotRepository.getTotalPlotsCount(fieldId).first()
                val newHarvestedPlots = plotRepository.getHarvestedPlotsCount(fieldId).first()
                val newDiscardedPlots = plotRepository.getDiscardedPlotsCount(fieldId).first()
                val eligiblePlots = newTotalPlots - newDiscardedPlots
                
                // Atualizar o estado com os novos contadores
                _state.update { 
                    it.copy(
                        isLoading = false,
                        totalPlots = eligiblePlots,
                        harvestedPlots = newHarvestedPlots,
                        remainingPlots = eligiblePlots - newHarvestedPlots,
                        discardedPlots = newDiscardedPlots,
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
                        
                        // Obter contadores atualizados após a operação
                        val newTotalPlots = plotRepository.getTotalPlotsCount(fieldId).first()
                        val newHarvestedPlots = plotRepository.getHarvestedPlotsCount(fieldId).first()
                        val newDiscardedPlots = plotRepository.getDiscardedPlotsCount(fieldId).first()
                        val eligiblePlots = newTotalPlots - newDiscardedPlots
                        
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                totalPlots = eligiblePlots,
                                harvestedPlots = newHarvestedPlots,
                                remainingPlots = eligiblePlots - newHarvestedPlots,
                                discardedPlots = newDiscardedPlots,
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
    val discardedPlots: Int = 0,
    val availableGrupos: List<String> = emptyList(),
    val groupPlots: List<Plot> = emptyList(),
    val selectedGrupoId: String? = null,
    val selectedPlots: Set<String> = emptySet(),
    val showGroupDialog: Boolean = false,
    val isLoading: Boolean = false,
    val lastColhidoPlot: Plot? = null,
    val lastCheckedPlot: Plot? = null,
    val successMessage: String? = null,
    val successMessageGrupo: String? = null,
    val errorMessage: String? = null
)
