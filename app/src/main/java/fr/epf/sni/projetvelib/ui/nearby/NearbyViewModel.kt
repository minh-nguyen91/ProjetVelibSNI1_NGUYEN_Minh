package fr.epf.sni.projetvelib.ui.nearby

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.epf.sni.projetvelib.model.Station

class NearbyViewModel : ViewModel() {

    private var allStations: List<Station> = emptyList()

    val nearbyStations = MutableLiveData<List<Pair<Station, Double>>>()

    private var radiusMeters = 500.0
    private var userLat = Double.NaN
    private var userLon = Double.NaN

    enum class BikeFilter { ALL, MECHANICAL, EBIKE }

    var bikeFilter = BikeFilter.ALL
        set(value) { field = value; filterStations() }

    var isReturnMode = false
        set(value) { field = value; filterStations() }

    fun updateStations(stations: List<Station>) { allStations = stations; filterStations() }
    fun setLocation(lat: Double, lon: Double) { userLat = lat; userLon = lon; filterStations() }
    fun setRadius(meters: Double) { radiusMeters = meters; filterStations() }

    private fun filterStations() {
        if (userLat.isNaN() || userLon.isNaN()) return
        nearbyStations.value = allStations.mapNotNull { station ->
            val available = if (isReturnMode) {
                station.numDocksAvailable > 0
            } else {
                when (bikeFilter) {
                    BikeFilter.ALL -> station.numBikesAvailable > 0
                    BikeFilter.MECHANICAL -> station.numMechanicalBikes > 0
                    BikeFilter.EBIKE -> station.numEbikes > 0
                }
            }
            if (!available) return@mapNotNull null
            val dist = station.distanceTo(userLat, userLon)
            if (dist > radiusMeters) return@mapNotNull null
            station to dist
        }.sortedBy { it.second }
    }
}
