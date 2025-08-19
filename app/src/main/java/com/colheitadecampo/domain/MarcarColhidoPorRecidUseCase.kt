package com.colheitadecampo.domain

import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.Plot
import com.colheitadecampo.data.repository.FieldRepository
import com.colheitadecampo.data.repository.PlotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for marking plots as harvested by recid
 */
@Singleton
class MarcarColhidoPorRecidUseCase @Inject constructor(
    private val plotRepository: PlotRepository
) {
    suspend fun marcarColhido(recid: String, colhido: Boolean): Plot? = withContext(Dispatchers.IO) {
        try {
            val plot = plotRepository.getPlotByRecid(recid)
            if (plot != null) {
                plotRepository.updateColhidoStatus(recid, colhido)
                return@withContext plot.copy(colhido = colhido)
            }
            null
        } catch (e: Exception) {
            Timber.e(e, "Error marking plot as harvested: $recid")
            throw e
        }
    }
}
