package fr.epf.sni.projetvelib.repository

import fr.epf.sni.projetvelib.api.VelibApiService
import fr.epf.sni.projetvelib.db.dao.FavoriteStationDao
import fr.epf.sni.projetvelib.db.entity.FavoriteStation
import fr.epf.sni.projetvelib.model.Station
import fr.epf.sni.projetvelib.toFavoriteStation

class StationRepository(
    private val api: VelibApiService,
    private val dao: FavoriteStationDao
) {
    val favoriteStations = dao.getAllFavorites()

    suspend fun fetchAllStations(): List<Station> {
        val info = api.getStationInfo().data.stations
        val statusMap = api.getStationStatus().data.stations.associateBy { it.station_id }
        val favoriteIds = dao.getAllFavoriteIds().toSet()
        return info.mapNotNull { i ->
            val s = statusMap[i.station_id] ?: return@mapNotNull null
            Station(
                stationId = i.station_id, name = i.name, lat = i.lat, lon = i.lon,
                capacity = i.capacity,
                numBikesAvailable = s.numBikesAvailable,
                numDocksAvailable = s.numDocksAvailable,
                numMechanicalBikes = s.num_bikes_available_types.sumOf { it.mechanical },
                numEbikes = s.num_bikes_available_types.sumOf { it.ebike },
                isInstalled = s.is_installed == 1, isRenting = s.is_renting == 1,
                isReturning = s.is_returning == 1, lastReported = s.last_reported,
                isFavorite = i.station_id in favoriteIds
            )
        }
    }

    suspend fun addFavorite(station: Station) = dao.insert(station.toFavoriteStation())
    suspend fun removeFavorite(stationId: Long) = dao.deleteById(stationId)
    suspend fun isFavorite(stationId: Long) = dao.getFavoriteById(stationId) != null
    suspend fun getAllFavoriteIds() = dao.getAllFavoriteIds()
}
