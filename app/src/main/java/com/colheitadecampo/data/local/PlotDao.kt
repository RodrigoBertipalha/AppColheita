package com.colheitadecampo.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.colheitadecampo.data.model.GroupStats
import com.colheitadecampo.data.model.Plot
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plots: List<Plot>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plot: Plot)

    @Update
    suspend fun update(plot: Plot)

    @Query("SELECT * FROM plots WHERE fieldId = :fieldId")
    fun getAllPlotsByFieldId(fieldId: Long): Flow<List<Plot>>

    @Query("SELECT * FROM plots WHERE fieldId = :fieldId")
    fun getPagingSourceByFieldId(fieldId: Long): PagingSource<Int, Plot>

    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND recid LIKE '%' || :searchQuery || '%'")
    fun searchPlotsByRecid(fieldId: Long, searchQuery: String): PagingSource<Int, Plot>

    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND grupoId = :grupoId")
    fun getPlotsByGrupo(fieldId: Long, grupoId: String): Flow<List<Plot>>

    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND grupoId = :grupoId")
    fun getPagingSourceByGrupoId(fieldId: Long, grupoId: String): PagingSource<Int, Plot>

    @Query("SELECT * FROM plots WHERE recid = :recid LIMIT 1")
    suspend fun getPlotByRecid(recid: String): Plot?
    
    @Query("SELECT * FROM plots WHERE recid LIKE :recid LIMIT 1")
    suspend fun getPlotByPartialRecid(recid: String): Plot?

    @Query("SELECT COUNT(*) FROM plots WHERE fieldId = :fieldId")
    fun getTotalPlotsCount(fieldId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM plots WHERE fieldId = :fieldId AND colhido = 1")
    fun getHarvestedPlotsCount(fieldId: Long): Flow<Int>

    @Query("SELECT grupoId, COUNT(*) as total, SUM(CASE WHEN colhido THEN 1 ELSE 0 END) as colhidos FROM plots WHERE fieldId = :fieldId GROUP BY grupoId ORDER BY grupoId")
    fun getGroupStats(fieldId: Long): Flow<List<GroupStats>>

    @Query("SELECT DISTINCT grupoId FROM plots WHERE fieldId = :fieldId ORDER BY grupoId")
    fun getDistinctGrupos(fieldId: Long): Flow<List<String>>

    @Transaction
    @Query("UPDATE plots SET colhido = :colhido WHERE recid = :recid")
    suspend fun updateColhidoStatus(recid: String, colhido: Boolean)

    @Transaction
    @Query("UPDATE plots SET colhido = :colhido WHERE fieldId = :fieldId AND grupoId = :grupoId")
    suspend fun updateColhidoStatusByGrupo(fieldId: Long, grupoId: String, colhido: Boolean)

    @Transaction
    @Query("UPDATE plots SET colhido = :colhido WHERE recid IN (:recidList)")
    suspend fun updateColhidoStatusForRecids(recidList: List<String>, colhido: Boolean)

    @Query("DELETE FROM plots WHERE fieldId = :fieldId")
    suspend fun deletePlotsByFieldId(fieldId: Long)
    
    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND colhido = 1 ORDER BY recid DESC LIMIT 1")
    suspend fun getLastHarvestedPlot(fieldId: Long): Plot?
    
    // Consultas relacionadas a plots descartados
    @Query("SELECT COUNT(*) FROM plots WHERE fieldId = :fieldId AND descartado = 1")
    fun getDiscardedPlotsCount(fieldId: Long): Flow<Int>
    
    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND grupoId = :grupoId AND descartado = 0")
    fun getNonDiscardedPlotsByGrupo(fieldId: Long, grupoId: String): Flow<List<Plot>>
    
    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND descartado = 0")
    fun getNonDiscardedPlots(fieldId: Long): Flow<List<Plot>>
    
    @Query("SELECT * FROM plots WHERE fieldId = :fieldId AND descartado = 1")
    fun getDiscardedPlots(fieldId: Long): Flow<List<Plot>>
    
    @Query("SELECT COUNT(*) FROM plots WHERE fieldId = :fieldId AND colhido = 0 AND descartado = 0")
    fun getEligibleRemainingPlotsCount(fieldId: Long): Flow<Int>
}
