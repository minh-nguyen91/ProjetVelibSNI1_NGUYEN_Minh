package fr.epf.sni.projetvelib.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.mapsDirectionsUri
import fr.epf.sni.projetvelib.model.Station
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StationDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STATION = "extra_station"
    }

    private lateinit var viewModel: StationDetailViewModel
    private lateinit var station: Station

    private lateinit var textStationName: TextView
    private lateinit var textBikesAvailable: TextView
    private lateinit var textDocksAvailable: TextView
    private lateinit var textMechanical: TextView
    private lateinit var textEbike: TextView
    private lateinit var textCapacity: TextView
    private lateinit var textStatus: TextView
    private lateinit var textLastUpdated: TextView
    private lateinit var textDistance: TextView
    private lateinit var fabFavorite: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_detail)

        station = IntentCompat.getParcelableExtra(intent, EXTRA_STATION, Station::class.java)
            ?: run { finish(); return }

        viewModel = ViewModelProvider(this)[StationDetailViewModel::class.java]

        textStationName = findViewById(R.id.textStationName)
        textBikesAvailable = findViewById(R.id.textBikesAvailable)
        textDocksAvailable = findViewById(R.id.textDocksAvailable)
        textMechanical = findViewById(R.id.textMechanical)
        textEbike = findViewById(R.id.textEbike)
        textCapacity = findViewById(R.id.textCapacity)
        textStatus = findViewById(R.id.textStatus)
        textLastUpdated = findViewById(R.id.textLastUpdated)
        textDistance = findViewById(R.id.textDistance)
        fabFavorite = findViewById(R.id.fabFavorite)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = station.name

        displayStationInfo()
        observeViewModel()
        viewModel.checkFavorite(station.stationId)
        fetchDistance()

        fabFavorite.setOnClickListener {
            viewModel.toggleFavorite(station)
        }

        findViewById<MaterialButton>(R.id.btnItineraire).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, station.mapsDirectionsUri()))
        }
    }

    private fun displayStationInfo() {
        textStationName.text = station.name
        textBikesAvailable.text = station.numBikesAvailable.toString()
        textDocksAvailable.text = station.numDocksAvailable.toString()
        textMechanical.text = station.numMechanicalBikes.toString()
        textEbike.text = station.numEbikes.toString()
        textCapacity.text = getString(R.string.capacity) + " : ${station.capacity}"
        textStatus.text = getString(R.string.status) + " : " + when {
            !station.isInstalled -> getString(R.string.out_of_service)
            !station.isRenting -> getString(R.string.no_renting)
            else -> getString(R.string.in_service)
        }
        val fmt = SimpleDateFormat("HH:mm", Locale.FRANCE)
        textLastUpdated.text =
            getString(R.string.last_updated) + " : " + fmt.format(Date(station.lastReported * 1000))
    }

    private fun observeViewModel() {
        viewModel.isFavorite.observe(this) { isFav ->
            fabFavorite.setImageResource(
                if (isFav) R.drawable.ic_star else R.drawable.ic_star_border
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDistance() {
        val fineOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineOk && !coarseOk) return
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { loc ->
                loc ?: return@addOnSuccessListener
                val dist = station.distanceTo(loc.latitude, loc.longitude)
                textDistance.isVisible = true
                textDistance.text = if (dist < 1000) "${dist.toInt()} m" else "${"%.1f".format(dist / 1000)} km"
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
