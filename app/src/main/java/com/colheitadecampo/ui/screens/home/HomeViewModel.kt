package com.colheitadecampo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    private val plotRepository: PlotRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        observeLastField()
    }
    
    private fun observeLastField() {
        viewModelScope.launch {
            fieldRepository.getLastField().collectLatest { lastField ->
                if (lastField == null) {
                    _state.value = _state.value.copy(
                        lastField = null,
                        totalPlots = 0,
                        percentageHarvested = 0f,
                        isLoading = false
                    )
                } else {
                    loadFieldStats(lastField)
                }
            }
        }
    }
    
    private fun loadFieldStats(field: Field) {
        viewModelScope.launch {
            try {
                plotRepository.getTotalPlotsCount(field.id).collectLatest { totalPlots ->
                    plotRepository.getHarvestedPlotsCount(field.id).collectLatest { harvestedPlots ->
                        val percentageHarvested = if (totalPlots > 0) {
                            harvestedPlots * 100f / totalPlots
                        } else 0f

                        _state.value = _state.value.copy(
                            lastField = field,
                            totalPlots = totalPlots,
                            percentageHarvested = percentageHarvested,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading home data")
                _state.value = _state.value.copy(
                    lastField = field,
                    error = e.localizedMessage,
                    isLoading = false
                )
            }
        }
    }
}

data class HomeState(
    val lastField: Field? = null,
    val totalPlots: Int = 0,
    val percentageHarvested: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)