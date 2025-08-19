package com.colheitadecampo.data.model

import android.graphics.Color
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupStatsTest {

    @Test
    fun `test colheitaPercentage calculation`() {
        // Given
        val stats1 = GroupStats(grupoId = "G1", total = 100, colhidos = 50)
        val stats2 = GroupStats(grupoId = "G2", total = 100, colhidos = 0)
        val stats3 = GroupStats(grupoId = "G3", total = 100, colhidos = 100)
        val stats4 = GroupStats(grupoId = "G4", total = 0, colhidos = 0)

        // When / Then
        assertEquals(50.0f, stats1.colheitaPercentage)
        assertEquals(0.0f, stats2.colheitaPercentage)
        assertEquals(100.0f, stats3.colheitaPercentage)
        assertEquals(0.0f, stats4.colheitaPercentage)
    }

    @Test
    fun `test colheitaColor calculation`() {
        // Given
        val stats1 = GroupStats(grupoId = "G1", total = 100, colhidos = 0)
        val stats2 = GroupStats(grupoId = "G2", total = 100, colhidos = 50)
        val stats3 = GroupStats(grupoId = "G3", total = 100, colhidos = 100)
        
        // When / Then
        assertEquals(Color.RED, stats1.colheitaColor)
        assertEquals(Color.YELLOW, stats2.colheitaColor)
        assertEquals(Color.GREEN, stats3.colheitaColor)
    }

    @Test
    fun `test isFullyHarvested`() {
        // Given
        val stats1 = GroupStats(grupoId = "G1", total = 100, colhidos = 50)
        val stats2 = GroupStats(grupoId = "G2", total = 100, colhidos = 100)
        val stats3 = GroupStats(grupoId = "G3", total = 0, colhidos = 0)

        // When / Then
        assertFalse(stats1.isFullyHarvested())
        assertTrue(stats2.isFullyHarvested())
        assertFalse(stats3.isFullyHarvested())
    }

    @Test
    fun `test isNotHarvestedAtAll`() {
        // Given
        val stats1 = GroupStats(grupoId = "G1", total = 100, colhidos = 0)
        val stats2 = GroupStats(grupoId = "G2", total = 100, colhidos = 1)
        val stats3 = GroupStats(grupoId = "G3", total = 0, colhidos = 0)

        // When / Then
        assertTrue(stats1.isNotHarvestedAtAll())
        assertFalse(stats2.isNotHarvestedAtAll())
        assertTrue(stats3.isNotHarvestedAtAll())
    }

    @Test
    fun `test colheitaStatusText`() {
        // Given
        val stats1 = GroupStats(grupoId = "G1", total = 100, colhidos = 0)
        val stats2 = GroupStats(grupoId = "G2", total = 100, colhidos = 50)
        val stats3 = GroupStats(grupoId = "G3", total = 100, colhidos = 100)
        val stats4 = GroupStats(grupoId = "G4", total = 0, colhidos = 0)

        // When / Then
        assertEquals("Não iniciado", stats1.colheitaStatusText)
        assertEquals("Em andamento", stats2.colheitaStatusText)
        assertEquals("Concluído", stats3.colheitaStatusText)
        assertEquals("Sem plots", stats4.colheitaStatusText)
    }
}
