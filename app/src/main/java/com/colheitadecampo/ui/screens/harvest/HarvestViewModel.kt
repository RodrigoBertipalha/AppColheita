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
                
                // Registrar em log o que está sendo buscado
                Timber.d("Buscando plot com RECID: '${currentState.recidInput}'")
                
                // Usamos a busca otimizada que primeiro tenta pelo ID do campo e RECID exato
                val plot = plotRepository.findPlotInFieldByRecid(fieldId, currentState.recidInput)
                
                if (plot == null) {
                    Timber.e("Plot não encontrado: '${currentState.recidInput}'")
                    
                    // Verifica se existem plots com recid similares para sugerir ao usuário
                    val allPlots = plotRepository.getAllPlotsByFieldId(fieldId).first()
                    val sugestoes = allPlots
                        .filter { plotItem -> 
                            plotItem.recid.lowercase().contains(currentState.recidInput.lowercase()) || 
                            currentState.recidInput.lowercase().contains(plotItem.recid.lowercase())
                        }
                        .take(3)
                        .map { plotItem -> plotItem.recid }
                    
                    val mensagemErro = if (sugestoes.isNotEmpty()) {
                        "Plot não encontrado: ${currentState.recidInput}. Sugestões: ${sugestoes.joinToString(", ")}"
                    } else {
                        "Plot não encontrado: ${currentState.recidInput}"
                    }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = mensagemErro
                        )
                    }
                    return@launch
                }
                
                // Se encontrou por aproximação, mostra qual foi encontrado
                if (plot.recid != currentState.recidInput) {
                    Timber.d("Plot encontrado por aproximação: ${plot.recid}")
                }
                
                // Se o plot estiver descartado, exibir mensagem de erro
                if (plot.descartado) {
                    _state.update { 
                        it.copy(
                            recidInput = "",
                            isLoading = false,
                            lastCheckedPlot = plot,
                            errorMessage = "Plot ${plot.recid} foi DESCARTADO e não pode ser colhido!"
                        )
                    }
                    
                    // Limpar mensagem de erro após 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _state.update { it.copy(errorMessage = null) }
                    return@launch
                }
                
                // Se não estiver descartado, proceder com a colheita normal
                val updatedPlot = marcarColhidoPorRecidUseCase.marcarColhido(
                    recid = plot.recid, // Usar o RECID encontrado (pode ser diferente do input)
                    colhido = true
                )
                
                // Recupera o último plot colhido para mostrar na UI
                val updatedLastColhidoPlot = updatedPlot ?: plot
                
                // Obtém os valores atualizados do banco de dados
                val newTotalPlots = plotRepository.getTotalPlotsCount(fieldId).first()
                val newDiscardedPlots = plotRepository.getDiscardedPlotsCount(fieldId).first()
                val newHarvestedPlots = plotRepository.getHarvestedPlotsCount(fieldId).first()
                val eligiblePlots = newTotalPlots - newDiscardedPlots
                
                // Se foi encontrado por aproximação, mostrar mensagem diferente
                val successMsg = if (plot.recid != currentState.recidInput) {
                    "Plot ${plot.recid} marcado como colhido! (encontrado a partir de ${currentState.recidInput})"
                } else {
                    "Plot ${plot.recid} marcado como colhido!"
                }
                
                _state.update { 
                    it.copy(
                        recidInput = "",
                        isLoading = false,
                        totalPlots = eligiblePlots,
                        harvestedPlots = newHarvestedPlots,
                        remainingPlots = eligiblePlots - newHarvestedPlots,
                        discardedPlots = newDiscardedPlots,
                        lastColhidoPlot = updatedLastColhidoPlot,
                        successMessage = successMsg
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
                
                // Verificando se existem plots para este grupo
                val todosGrupos = plotRepository.getDistinctGrupos(fieldId).first()
                if (!todosGrupos.contains(grupoId)) {
                    Timber.w("Grupo $grupoId não encontrado na lista de grupos: $todosGrupos")
                    _state.update { it.copy(
                        errorMessage = "Grupo $grupoId não encontrado no campo"
                    )}
                    return@launch
                }
                
                // Agora carregue os plots do grupo selecionado, excluindo os descartados
                plotRepository.getNonDiscardedPlotsByGrupo(fieldId, grupoId).collect { plots ->
                    if (plots.isEmpty()) {
                        Timber.w("Nenhum plot encontrado para o grupo $grupoId")
                        _state.update { it.copy(
                            errorMessage = "Nenhum plot disponível para colheita no grupo $grupoId",
                            groupPlots = emptyList()
                        )}
                    } else {
                        _state.update { it.copy(groupPlots = plots) }
                        // Registre para debug
                        Timber.d("Carregados ${plots.size} plots não descartados para o grupo $grupoId")
                        plots.forEach { plot ->
                            Timber.d("Plot [Grupo: $grupoId] - RECID: ${plot.recid}, Colhido: ${plot.colhido}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading group plots")
                _state.update { it.copy(
                    errorMessage = "Erro ao carregar plots do grupo: ${e.localizedMessage}"
                )}
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
                
                Timber.d("Iniciando colheita por grupo para ${selectedPlotIds.size} plots do grupo $selectedGrupo")
                
                if (selectedPlotIds.isEmpty()) {
                    // Se nenhum plot foi selecionado individualmente, marcar todos os plots do grupo
                    Timber.d("Nenhum plot selecionado individualmente, marcando todo o grupo $selectedGrupo")
                    val affectedCount = marcarColhidoPorGrupoUseCase.marcarColhidoPorGrupo(
                        fieldId = fieldId,
                        grupoId = selectedGrupo,
                        colhido = true
                    )
                    Timber.d("$affectedCount plots marcados como colhidos no grupo $selectedGrupo")
                } else {
                    // Marcar apenas os plots selecionados
                    Timber.d("Marcando ${selectedPlotIds.size} plots selecionados como colhidos")
                    marcarColhidoPorGrupoUseCase.marcarColhidoParaRecids(
                        recidList = selectedPlotIds.toList(),
                        colhido = true
                    )
                }
                
                // Obter contadores atualizados após a operação
                val newTotalPlots = plotRepository.getTotalPlotsCount(fieldId).first()
                val newHarvestedPlots = plotRepository.getHarvestedPlotsCount(fieldId).first()
                val newDiscardedPlots = plotRepository.getDiscardedPlotsCount(fieldId).first()
                val eligiblePlots = newTotalPlots - newDiscardedPlots
                
                // Verificar quantos plots foram colhidos
                val colhidosCount = if (selectedPlotIds.isEmpty()) {
                    // Se todos os plots do grupo foram colhidos, contar a quantidade
                    val grupoPlots = plotRepository.getPlotsByGrupo(fieldId, selectedGrupo).first()
                    grupoPlots.count { it.colhido }
                } else {
                    // Se foram plots específicos, contar os selecionados
                    selectedPlotIds.size
                }
                
                // Atualizar o estado com os novos contadores
                _state.update { 
                    it.copy(
                        isLoading = false,
                        totalPlots = eligiblePlots,
                        harvestedPlots = newHarvestedPlots,
                        remainingPlots = eligiblePlots - newHarvestedPlots,
                        discardedPlots = newDiscardedPlots,
                        successMessageGrupo = "$colhidosCount plots do grupo $selectedGrupo marcados como colhidos!"
                    )
                }
                
                // Antes de fechar o diálogo, verifica se ainda existem plots não colhidos
                val grupoNaoColhidos = plotRepository.getPlotsByGrupo(fieldId, selectedGrupo)
                    .first()
                    .filter { !it.colhido && !it.descartado }
                
                // Se ainda existem plots não colhidos no grupo, mantenha o diálogo aberto com os plots atualizados
                if (grupoNaoColhidos.isNotEmpty()) {
                    _state.update {
                        it.copy(groupPlots = grupoNaoColhidos)
                    }
                } else {
                    // Se não há mais plots para colher, feche o diálogo
                    hideGroupDialog()
                }
                
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
    
    // Métodos novos para melhorar a seleção de grupo
    fun determineGroupFromRecid(recid: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Busca plot pelo RECID informado
                val plot = plotRepository.getPlotByRecid(recid)
                
                if (plot == null) {
                    // Se não encontrar o plot, mostra mensagem de erro
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Plot com RECID $recid não encontrado"
                        )
                    }
                    return@launch
                }
                
                // Verifica se o plot está descartado
                if (plot.descartado) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Plot $recid está descartado e não pode ser colhido"
                        )
                    }
                    return@launch
                }
                
                // Se encontrar, abre o diálogo com o grupo do plot selecionado
                Timber.d("Plot encontrado para RECID $recid: grupo ${plot.grupoId}")
                showGroupHarvestDialog(plot.grupoId)
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao determinar grupo a partir do RECID")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao buscar grupo: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
    
    fun prepareGroupSelection() {
        viewModelScope.launch {
            try {
                val grupos = plotRepository.getDistinctGrupos(fieldId).first()
                
                if (grupos.isEmpty()) {
                    _state.update {
                        it.copy(
                            errorMessage = "Nenhum grupo disponível neste campo"
                        )
                    }
                    return@launch
                }
                
                // Mostra diálogo para escolher grupo
                _state.update {
                    it.copy(
                        showGroupSelector = true,
                        availableGruposForSelection = grupos
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao preparar seleção de grupo")
                _state.update {
                    it.copy(
                        errorMessage = "Erro ao carregar grupos: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
    
    fun selectGroupFromList(grupoId: String) {
        _state.update {
            it.copy(
                showGroupSelector = false
            )
        }
        showGroupHarvestDialog(grupoId)
    }
    
    fun hideGroupSelector() {
        _state.update {
            it.copy(
                showGroupSelector = false
            )
        }
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
    val errorMessage: String? = null,
    // Novos campos
    val showGroupSelector: Boolean = false,
    val availableGruposForSelection: List<String> = emptyList()
)
