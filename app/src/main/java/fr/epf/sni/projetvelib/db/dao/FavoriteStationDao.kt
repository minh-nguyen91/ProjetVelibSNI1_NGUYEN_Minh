package fr.epf.sni.projetvelib.db.dao

import androidx.room.*
import fr.epf.sni.projetvelib.db.entity.FavoriteStation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    @Query("SELECT * FROM favorite_stations ORDER BY name ASC")
    fun getAllFavorites(): Flow<List<FavoriteStation>>

    @Query("SELECT stationId FROM favorite_stations")
    suspend fun getAllFavoriteIds(): List<Long>

    @Query("SELECT * FROM favorite_stations WHERE stationId = :stationId")
    suspend fun getFavoriteById(stationId: Long): FavoriteStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStation)

    @Query("DELETE FROM favorite_stations WHERE stationId = :stationId")
    suspend fun deleteById(stationId: Long)
}
