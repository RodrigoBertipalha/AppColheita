package com.colheitadecampo.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.GroupStats
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    private val plotRepository: PlotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fieldId: Long = checkNotNull(savedStateHandle["fieldId"])
    
    private val _state = MutableStateFlow(DashboardState())
    
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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _selectedGrupoId = MutableStateFlow<String?>(null)
    val selectedGrupoId = _selectedGrupoId.asStateFlow()
    
    val state: StateFlow<DashboardState> = combine(
        plotRepository.getTotalPlotsCount(fieldId),
        plotRepository.getHarvestedPlotsCount(fieldId),
        plotRepository.getGroupStats(fieldId),
        plotRepository.getDistinctGrupos(fieldId),
        _state
    ) { totalPlots, harvestedPlots, groupStats, grupos, state ->
        val percentageHarvested = if (totalPlots > 0) {
            harvestedPlots * 100f / totalPlots
        } else 0f
        
        state.copy(
            totalPlots = totalPlots,
            harvestedPlots = harvestedPlots,
            percentageHarvested = percentageHarvested,
            groupStats = groupStats,
            availableGrupos = grupos,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardState(isLoading = true)
    )
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedPlots: Flow<PagingData<Plot>> = combine(
        _searchQuery.debounce(300),
        _selectedGrupoId
    ) { query, grupoId ->
        Pair(query, grupoId)
    }.distinctUntilChanged().flatMapLatest { (query, grupoId) ->
        when {
            !query.isBlank() -> plotRepository.searchPlotsByRecid(fieldId, query)
            grupoId != null -> plotRepository.getPagingPlotsByGrupoId(fieldId, grupoId)
            else -> plotRepository.getPagingPlotsByFieldId(fieldId)
        }
    }.cachedIn(viewModelScope)
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectGrupoId(grupoId: String?) {
        _selectedGrupoId.value = grupoId
    }
    
    fun getField(): Field? {
        return state.value.field
    }
    
    fun getGroupStats(): List<GroupStats> {
        return state.value.groupStats
    }
}

data class DashboardState(
    val field: Field? = null,
    val totalPlots: Int = 0,
    val harvestedPlots: Int = 0,
    val percentageHarvested: Float = 0f,
    val groupStats: List<GroupStats> = emptyList(),
    val availableGrupos: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
