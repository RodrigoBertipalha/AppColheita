package com.colheitadecampo.data.repository

import com.colheitadecampo.data.local.dao.PlotDao
import com.colheitadecampo.data.model.GroupStats
import com.colheitadecampo.data.model.Plot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class PlotRepositoryTest {
    
    private lateinit var plotDao: PlotDao
    private lateinit var repository: PlotRepository
    
    @Before
    fun setup() {
        plotDao = mockk()
        repository = PlotRepositoryImpl(plotDao)
    }
    
    @Test
    fun `getPlotByRecid should return plot when exists`() = runTest {
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
            colhido = false
        )
        coEvery { plotDao.getPlotByRecid(recid) } returns plot
        
        // When
        val result = repository.getPlotByRecid(recid)
        
        // Then
        assertEquals(plot, result)
        coVerify { plotDao.getPlotByRecid(recid) }
    }
    
    @Test
    fun `getPlotByRecid should return null when plot does not exist`() = runTest {
        // Given
        val recid = "NONEXISTENT"
        coEvery { plotDao.getPlotByRecid(recid) } returns null
        
        // When
        val result = repository.getPlotByRecid(recid)
        
        // Then
        assertNull(result)
        coVerify { plotDao.getPlotByRecid(recid) }
    }
    
    @Test
    fun `getAllPlotsByFieldId should return list of plots`() = runTest {
        // Given
        val fieldId = 1L
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId),
            Plot(recid = "2", fieldId = fieldId)
        )
        coEvery { plotDao.getAllPlotsByFieldId(fieldId) } returns plots
        
        // When
        val result = repository.getAllPlotsByFieldId(fieldId)
        
        // Then
        assertEquals(plots, result)
        coVerify { plotDao.getAllPlotsByFieldId(fieldId) }
    }
    
    @Test
    fun `getPlotsByFieldIdAndGrupoId should return list of plots`() = runTest {
        // Given
        val fieldId = 1L
        val grupoId = "G1"
        val plots = listOf(
            Plot(recid = "1", fieldId = fieldId, grupoId = grupoId),
            Plot(recid = "2", fieldId = fieldId, grupoId = grupoId)
        )
        coEvery { plotDao.getPlotsByFieldIdAndGrupoId(fieldId, grupoId) } returns plots
        
        // When
        val result = repository.getPlotsByFieldIdAndGrupoId(fieldId, grupoId)
        
        // Then
        assertEquals(plots, result)
        coVerify { plotDao.getPlotsByFieldIdAndGrupoId(fieldId, grupoId) }
    }
    
    @Test
    fun `updateColhidoStatus should call dao method`() = runTest {
        // Given
        val recid = "TEST123"
        val colhido = true
        coEvery { plotDao.updateColhidoStatus(recid, colhido) } returns Unit
        
        // When
        repository.updateColhidoStatus(recid, colhido)
        
        // Then
        coVerify { plotDao.updateColhidoStatus(recid, colhido) }
    }
    
    @Test
    fun `updateColhidoStatusForGrupo should return count of updated plots`() = runTest {
        // Given
        val fieldId = 1L
        val grupoId = "G1"
        val colhido = true
        val updatedCount = 5
        coEvery { plotDao.updateColhidoStatusForGrupo(fieldId, grupoId, colhido) } returns updatedCount
        
        // When
        val result = repository.updateColhidoStatusForGrupo(fieldId, grupoId, colhido)
        
        // Then
        assertEquals(updatedCount, result)
        coVerify { plotDao.updateColhidoStatusForGrupo(fieldId, grupoId, colhido) }
    }
    
    @Test
    fun `getFieldColheita should return field harvest stats`() = runTest {
        // Given
        val fieldId = 1L
        val groupStats = listOf(
            GroupStats(grupoId = "G1", total = 10, colhidos = 5),
            GroupStats(grupoId = "G2", total = 8, colhidos = 2)
        )
        coEvery { plotDao.getGroupStatsForField(fieldId) } returns flowOf(groupStats)
        
        // When
        val result = repository.getFieldColheita(fieldId).toList().first()
        
        // Then
        assertEquals(groupStats, result)
        coVerify { plotDao.getGroupStatsForField(fieldId) }
    }
    
    @Test
    fun `insertPlots should call dao method`() = runTest {
        // Given
        val plots = listOf(
            Plot(recid = "1", fieldId = 1),
            Plot(recid = "2", fieldId = 1)
        )
        coEvery { plotDao.insertPlots(plots) } returns Unit
        
        // When
        repository.insertPlots(plots)
        
        // Then
        coVerify { plotDao.insertPlots(plots) }
    }
    
    @Test
    fun `deletePlotsWithRecids should call dao method`() = runTest {
        // Given
        val recids = listOf("1", "2", "3")
        coEvery { plotDao.deletePlotsWithRecids(recids) } returns Unit
        
        // When
        repository.deletePlotsWithRecids(recids)
        
        // Then
        coVerify { plotDao.deletePlotsWithRecids(recids) }
    }
    
    @Test
    fun `deleteAllPlotsByFieldId should call dao method`() = runTest {
        // Given
        val fieldId = 1L
        coEvery { plotDao.deleteAllPlotsByFieldId(fieldId) } returns Unit
        
        // When
        repository.deleteAllPlotsByFieldId(fieldId)
        
        // Then
        coVerify { plotDao.deleteAllPlotsByFieldId(fieldId) }
    }
}
