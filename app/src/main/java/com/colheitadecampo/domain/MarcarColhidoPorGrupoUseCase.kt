package com.colheitadecampo.domain

import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.PlotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for marking plots as harvested by group
 */
@Singleton
class MarcarColhidoPorGrupoUseCase @Inject constructor(
    private val plotRepository: PlotRepository
) {
    suspend fun marcarColhidoPorGrupo(fieldId: Long, grupoId: String, colhido: Boolean): Int = 
        withContext(Dispatchers.IO) {
            try {
                plotRepository.updateColhidoStatusByGrupo(fieldId, grupoId, colhido)
                // Return the number of affected plots
                val plots = plotRepository.getPlotsByGrupo(fieldId, grupoId).first()
                plots.size
            } catch (e: Exception) {
                Timber.e(e, "Error marking group as harvested: $grupoId")
                throw e
            }
        }
    
    suspend fun marcarColhidoParaRecids(recidList: List<String>, colhido: Boolean) = 
        withContext(Dispatchers.IO) {
            try {
                plotRepository.updateColhidoStatusForRecids(recidList, colhido)
            } catch (e: Exception) {
                Timber.e(e, "Error marking plots as harvested")
                throw e
            }
        }
        
    fun getPlotsByGrupo(fieldId: Long, grupoId: String): Flow<List<Plot>> =
        plotRepository.getPlotsByGrupo(fieldId, grupoId)
            .flowOn(Dispatchers.IO)
}
