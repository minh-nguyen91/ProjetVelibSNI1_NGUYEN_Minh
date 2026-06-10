package fr.epf.sni.projetvelib.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.epf.sni.projetvelib.api.VelibApiService
import fr.epf.sni.projetvelib.db.AppDatabase
import fr.epf.sni.projetvelib.model.Station
import fr.epf.sni.projetvelib.repository.StationRepository
import kotlinx.coroutines.launch

class StationDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StationRepository(
        VelibApiService.create(),
        AppDatabase.getDatabase(application).favoriteStationDao()
    )

    private val _isFavorite = MutableLiveData(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    fun checkFavorite(stationId: Long) {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(stationId)
        }
    }

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            if (_isFavorite.value == true) {
                repository.removeFavorite(station.stationId)
                _isFavorite.value = false
            } else {
                repository.addFavorite(station)
                _isFavorite.value = true
            }
        }
    }
}
