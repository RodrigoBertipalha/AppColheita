package com.colheitadecampo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.colheitadecampo.data.local.PlotDao
import com.colheitadecampo.data.model.GroupStats
import com.colheitadecampo.data.model.Plot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlotRepository @Inject constructor(private val plotDao: PlotDao) {
    fun getAllPlotsByFieldId(fieldId: Long): Flow<List<Plot>> = 
        plotDao.getAllPlotsByFieldId(fieldId)

    fun getPagingPlotsByFieldId(fieldId: Long): Flow<PagingData<Plot>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = true,
                maxSize = 100
            )
        ) {
            plotDao.getPagingSourceByFieldId(fieldId)
        }.flow
    }

    fun searchPlotsByRecid(fieldId: Long, query: String): Flow<PagingData<Plot>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = true
            )
        ) {
            plotDao.searchPlotsByRecid(fieldId, query)
        }.flow
    }

    fun getPlotsByGrupo(fieldId: Long, grupoId: String): Flow<List<Plot>> =
        plotDao.getPlotsByGrupo(fieldId, grupoId)

    fun getPagingPlotsByGrupoId(fieldId: Long, grupoId: String): Flow<PagingData<Plot>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = true
            )
        ) {
            plotDao.getPagingSourceByGrupoId(fieldId, grupoId)
        }.flow
    }

    suspend fun getPlotByRecid(recid: String): Plot? {
        // Primeiro, tenta buscar por RECID exato (mais rápido)
        var plot = plotDao.getPlotByRecid(recid)
        
        // Se não encontrou com correspondência exata, tenta com parcial
        if (plot == null) {
            plot = plotDao.getPlotByPartialRecid("%$recid%")
            if (plot != null) {
                Timber.d("Plot encontrado por correspondência parcial: ${plot.recid}")
            }
        }
        
        return plot
    }

    fun getTotalPlotsCount(fieldId: Long): Flow<Int> = plotDao.getTotalPlotsCount(fieldId)

    fun getHarvestedPlotsCount(fieldId: Long): Flow<Int> = plotDao.getHarvestedPlotsCount(fieldId)

    fun getGroupStats(fieldId: Long): Flow<List<GroupStats>> = plotDao.getGroupStats(fieldId)

    fun getDistinctGrupos(fieldId: Long): Flow<List<String>> = plotDao.getDistinctGrupos(fieldId)

    suspend fun insertPlots(plots: List<Plot>) = plotDao.insertAll(plots)

    suspend fun insertPlot(plot: Plot) = plotDao.insert(plot)

    suspend fun updatePlot(plot: Plot) = plotDao.update(plot)

    suspend fun updateColhidoStatus(recid: String, colhido: Boolean) = 
        plotDao.updateColhidoStatus(recid, colhido)

    suspend fun updateColhidoStatusByGrupo(fieldId: Long, grupoId: String, colhido: Boolean) =
        plotDao.updateColhidoStatusByGrupo(fieldId, grupoId, colhido)

    suspend fun updateColhidoStatusForRecids(recidList: List<String>, colhido: Boolean) =
        plotDao.updateColhidoStatusForRecids(recidList, colhido)

    suspend fun deletePlotsByFieldId(fieldId: Long) = plotDao.deletePlotsByFieldId(fieldId)
    
    suspend fun getLastHarvestedPlot(fieldId: Long): Plot? = plotDao.getLastHarvestedPlot(fieldId)
    
    /**
     * Busca otimizada por um plot usando fieldId e RECID
     * Primeiro tenta busca exata, depois busca aproximada
     */
    suspend fun findPlotInFieldByRecid(fieldId: Long, recid: String): Plot? {
        // Primeiro tenta busca exata pelo RECID
        val exactPlot = plotDao.findPlotByExactRecid(fieldId, recid)
        if (exactPlot != null) {
            return exactPlot
        }
        
        // Se não encontrou com busca exata, tenta com todas as parcelas do campo
        val allPlots = plotDao.getAllPlotsByFieldId(fieldId).first()
        
        // Tenta busca aproximada
        return allPlots.find { plotItem ->
            plotItem.recid.contains(recid) || recid.contains(plotItem.recid)
        }
    }
    
    // Métodos relacionados a plots descartados
    fun getDiscardedPlotsCount(fieldId: Long): Flow<Int> = plotDao.getDiscardedPlotsCount(fieldId)
    
    fun getNonDiscardedPlotsByGrupo(fieldId: Long, grupoId: String): Flow<List<Plot>> =
        plotDao.getNonDiscardedPlotsByGrupo(fieldId, grupoId)
    
    fun getNonDiscardedPlots(fieldId: Long): Flow<List<Plot>> = 
        plotDao.getNonDiscardedPlots(fieldId)
        
    fun getDiscardedPlots(fieldId: Long): Flow<List<Plot>> = 
        plotDao.getDiscardedPlots(fieldId)
        
    fun getEligibleRemainingPlotsCount(fieldId: Long): Flow<Int> =
        plotDao.getEligibleRemainingPlotsCount(fieldId)
}
