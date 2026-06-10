package fr.epf.sni.projetvelib.ui.map

import fr.epf.sni.projetvelib.model.Station
import org.osmdroid.util.GeoPoint

data class ClusterItem(val stations: List<Station>) {
    val center: GeoPoint
        get() {
            val lat = stations.map { it.lat }.average()
            val lon = stations.map { it.lon }.average()
            return GeoPoint(lat, lon)
        }
    val totalBikes: Int get() = stations.sumOf { it.numBikesAvailable }
}
