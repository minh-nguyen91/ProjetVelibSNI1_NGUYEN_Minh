package fr.epf.sni.projetvelib.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.epf.sni.projetvelib.api.VelibApiService
import fr.epf.sni.projetvelib.db.AppDatabase
import fr.epf.sni.projetvelib.model.Station
import fr.epf.sni.projetvelib.repository.StationRepository
import kotlinx.coroutines.launch

class SharedStationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StationRepository(
        VelibApiService.create(),
        AppDatabase.getDatabase(application).favoriteStationDao()
    )

    enum class Mode { FIND_BIKE, RETURN_BIKE }

    val stations = MutableLiveData<List<Station>>(emptyList())
    val mode = MutableLiveData(Mode.FIND_BIKE)
    val error = MutableLiveData<String?>(null)

    fun setMode(m: Mode) { if (mode.value != m) mode.value = m }

    init { loadStations() }

    fun loadStations() {
        viewModelScope.launch {
            error.value = null
            try {
                stations.value = repository.fetchAllStations()
            } catch (e: Exception) {
                error.value = "${e.javaClass.simpleName}: ${e.message}"
            }
        }
    }

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            if (station.isFavorite) repository.removeFavorite(station.stationId)
            else repository.addFavorite(station)
            loadStations()
        }
    }
}
