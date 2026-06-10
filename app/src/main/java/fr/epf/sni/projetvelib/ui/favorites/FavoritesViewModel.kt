package fr.epf.sni.projetvelib.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.epf.sni.projetvelib.api.VelibApiService
import fr.epf.sni.projetvelib.db.AppDatabase
import fr.epf.sni.projetvelib.model.Station
import fr.epf.sni.projetvelib.repository.StationRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StationRepository(
        VelibApiService.create(),
        AppDatabase.getDatabase(application).favoriteStationDao()
    )

    val favorites = repository.favoriteStations.asLiveData()

    fun removeFavorite(stationId: Long) {
        viewModelScope.launch { repository.removeFavorite(stationId) }
    }

    fun refreshFavorites(stations: List<Station>) {
        viewModelScope.launch {
            val currentIds = repository.getAllFavoriteIds().toSet()
            if (currentIds.isEmpty()) return@launch
            stations.filter { it.stationId in currentIds }.forEach { repository.addFavorite(it) }
        }
    }
}
