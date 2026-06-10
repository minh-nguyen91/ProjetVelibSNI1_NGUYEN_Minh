package fr.epf.sni.projetvelib.ui.map

import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.model.Station
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class StationInfoWindow(
    mapView: MapView,
    private val getUserLocation: () -> Pair<Double, Double>?,
    private val onVoirPlus: (Station) -> Unit,
    private val onItineraire: (Station) -> Unit
) : InfoWindow(R.layout.map_info_window, mapView) {

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val station = marker.relatedObject as? Station ?: return

        mView.findViewById<ImageButton>(R.id.btnCloseInfo).setOnClickListener { close() }

        mView.findViewById<TextView>(R.id.infoName).text = station.name
        mView.findViewById<TextView>(R.id.infoMechanical).text = "🚲 ${station.numMechanicalBikes} méca"
        mView.findViewById<TextView>(R.id.infoEbike).text = "⚡ ${station.numEbikes} élec"

        val loc = getUserLocation()
        mView.findViewById<TextView>(R.id.infoDistance).text = if (loc != null) {
            val dist = station.distanceTo(loc.first, loc.second)
            if (dist < 1000) "📍 ${dist.toInt()} m" else "📍 ${"%.1f".format(dist / 1000)} km"
        } else {
            "📍 Position inconnue"
        }

        mView.findViewById<Button>(R.id.btnItineraire).setOnClickListener {
            close()
            onItineraire(station)
        }

        mView.findViewById<Button>(R.id.btnVoirPlus).setOnClickListener {
            close()
            onVoirPlus(station)
        }
    }

    override fun onClose() {}
}
