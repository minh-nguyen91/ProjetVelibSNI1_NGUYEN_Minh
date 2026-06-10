package fr.epf.sni.projetvelib.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.sqrt
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.mapsDirectionsUri
import fr.epf.sni.projetvelib.model.Station
import fr.epf.sni.projetvelib.ui.SharedStationViewModel
import fr.epf.sni.projetvelib.ui.detail.StationDetailActivity
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private lateinit var viewModel: MapViewModel
    private lateinit var sharedViewModel: SharedStationViewModel
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var infoWindow: StationInfoWindow

    private lateinit var mapView: MapView
    private lateinit var btnSearch: ImageButton
    private lateinit var searchContainer: MaterialCardView
    private lateinit var searchAutoComplete: AutoCompleteTextView
    private lateinit var btnMyLocation: ImageButton
    private lateinit var btnZoomIn: ImageButton
    private lateinit var btnZoomOut: ImageButton
    private lateinit var btnToggleMarkers: MaterialButton
    private lateinit var btnMode: MaterialButton
    private lateinit var errorText: TextView

    private var userLocation: Pair<Double, Double>? = null
    private var currentStations: List<Station> = emptyList()
    private var centeredOnUser = false
    private var viewReady = false

    private val iconCache = HashMap<String, Drawable>(64)
    private val redrawHandler = Handler(Looper.getMainLooper())
    private val redrawRunnable = Runnable { redrawMarkersNow() }
    private val reusePoint = Point()

    private val isReturnMode get() = sharedViewModel.mode.value == SharedStationViewModel.Mode.RETURN_BIKE
    private var markersVisible = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedStationViewModel::class.java]

        mapView = view.findViewById(R.id.mapView)
        btnSearch = view.findViewById(R.id.btnSearch)
        searchContainer = view.findViewById(R.id.searchContainer)
        searchAutoComplete = view.findViewById(R.id.searchAutoComplete)
        btnMyLocation = view.findViewById(R.id.btnMyLocation)
        btnZoomIn = view.findViewById(R.id.btnZoomIn)
        btnZoomOut = view.findViewById(R.id.btnZoomOut)
        btnToggleMarkers = view.findViewById(R.id.btnToggleMarkers)
        btnMode = view.findViewById(R.id.btnMode)
        errorText = view.findViewById(R.id.errorText)

        viewReady = true

        setupMap()
        setupSearch()

        infoWindow = StationInfoWindow(mapView, { userLocation },
            onVoirPlus = { openDetail(it) },
            onItineraire = { startActivity(Intent(Intent.ACTION_VIEW, it.mapsDirectionsUri())) }
        )
        btnMyLocation.setOnClickListener {
            locationOverlay.myLocation?.let {
                mapView.controller.animateTo(it)
                mapView.controller.setZoom(16.0)
            } ?: tryGetLocation()
        }
        btnMode.setOnClickListener {
            sharedViewModel.setMode(
                if (isReturnMode) SharedStationViewModel.Mode.FIND_BIKE else SharedStationViewModel.Mode.RETURN_BIKE
            )
        }
        btnToggleMarkers.setOnClickListener {
            markersVisible = !markersVisible
            btnToggleMarkers.text = if (markersVisible) "Masquer stations" else "Afficher stations"
            scheduleRedraw()
        }
        tryGetLocation()
        observeViewModel()
    }

    private fun setupMap() {
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(48.8566, 2.3522))
        }
        btnZoomIn.setOnClickListener { mapView.controller.zoomIn() }
        btnZoomOut.setOnClickListener { mapView.controller.zoomOut() }

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?) = false
            override fun onZoom(event: ZoomEvent?): Boolean { scheduleRedraw(); return false }
        })
    }

    private fun setupSearch() {
        btnSearch.setOnClickListener {
            if (searchContainer.isVisible) closeSearch() else openSearch()
        }
        searchAutoComplete.addTextChangedListener { viewModel.setSearchQuery(it?.toString() ?: "") }
        searchAutoComplete.setOnItemClickListener { _, _, _, _ ->
            sharedViewModel.stations.value?.find { it.name == searchAutoComplete.text.toString() }?.let {
                mapView.controller.animateTo(GeoPoint(it.lat, it.lon))
                mapView.controller.setZoom(18.0)
            }
            searchAutoComplete.dismissDropDown()
            imm().hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun openSearch() {
        searchContainer.isVisible = true
        btnSearch.setImageResource(R.drawable.ic_close)
        searchAutoComplete.requestFocus()
        imm().showSoftInput(searchAutoComplete, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeSearch() {
        searchContainer.isVisible = false
        btnSearch.setImageResource(R.drawable.ic_search)
        searchAutoComplete.text.clear()
        viewModel.setSearchQuery("")
        imm().hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun imm() = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    @SuppressLint("MissingPermission")
    fun tryGetLocation() {
        val fineOk = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineOk && !coarseOk) return
        LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
            .addOnSuccessListener { loc ->
                loc ?: return@addOnSuccessListener
                userLocation = loc.latitude to loc.longitude
                if (!centeredOnUser) {
                    mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                    mapView.controller.setZoom(15.0)
                    centeredOnUser = true
                }
            }
    }

    private fun observeViewModel() {
        sharedViewModel.stations.observe(viewLifecycleOwner) { stations ->
            searchAutoComplete.setAdapter(ArrayAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line, stations.map { it.name }))
            applyFilter(stations, viewModel.searchQuery.value ?: "")
        }
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            applyFilter(sharedViewModel.stations.value ?: emptyList(), query)
        }
        sharedViewModel.mode.observe(viewLifecycleOwner) {
            btnMode.text = if (isReturnMode) "Déposer" else "Trouver"
            iconCache.clear()
            scheduleRedraw()
        }
        sharedViewModel.error.observe(viewLifecycleOwner) { error ->
            errorText.isVisible = error != null
            errorText.text = error?.let { "Erreur : $it" }
        }
    }

    private fun applyFilter(stations: List<Station>, query: String) {
        currentStations = if (query.isBlank()) stations else stations.filter { it.name.contains(query, ignoreCase = true) }
        scheduleRedraw()
    }

    private fun scheduleRedraw() {
        redrawHandler.removeCallbacks(redrawRunnable)
        redrawHandler.postDelayed(redrawRunnable, 250)
    }

    private fun redrawMarkersNow() {
        if (!viewReady) return
        if (mapView.width == 0) { mapView.post { redrawMarkersNow() }; return }
        mapView.overlays.removeIf { it !is MyLocationNewOverlay }
        if (markersVisible) computeClusters(currentStations).forEach { item ->
            when (item) {
                is Station -> addStationMarker(item)
                is ClusterItem -> addClusterMarker(item)
            }
        }
        mapView.invalidate()
    }

    private fun computeClusters(stations: List<Station>): List<Any> {
        if (stations.isEmpty()) return emptyList()
        val projection = mapView.projection
        val cells = HashMap<Long, MutableList<Station>>(stations.size)
        stations.forEach { station ->
            projection.toPixels(GeoPoint(station.lat, station.lon), reusePoint)
            val key = (reusePoint.x / 80).toLong().shl(20) or (reusePoint.y / 80).toLong()
            cells.getOrPut(key) { mutableListOf() }.add(station)
        }
        return cells.values.map { if (it.size == 1) it[0] else ClusterItem(it) }
    }

    private fun markerCount(station: Station) =
        if (isReturnMode) station.numDocksAvailable else station.numBikesAvailable

    private fun addStationMarker(station: Station) {
        val count = markerCount(station)
        mapView.overlays.add(Marker(mapView).apply {
            position = GeoPoint(station.lat, station.lon)
            title = station.name
            relatedObject = station
            icon = cachedIcon(stationColor(count, station.isInstalled), count.toString(), 90)
            infoWindow = this@MapFragment.infoWindow
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }

    private fun addClusterMarker(cluster: ClusterItem) {
        val count = if (isReturnMode) cluster.stations.sumOf { it.numDocksAvailable } else cluster.totalBikes
        mapView.overlays.add(Marker(mapView).apply {
            position = cluster.center
            icon = cachedIcon(Color.rgb(33, 150, 243), count.toString(), 110)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setOnMarkerClickListener { _, mv ->
                zoomToDecompose(cluster, mv)
                true
            }
        })
    }

    private fun zoomToDecompose(cluster: ClusterItem, mv: MapView) {
        val stations = cluster.stations
        var minDist = Double.MAX_VALUE
        for (i in stations.indices)
            for (j in i + 1 until stations.size) {
                val d = stations[i].distanceTo(stations[j].lat, stations[j].lon)
                if (d < minDist) minDist = d
            }
        if (minDist <= 0.0) { mv.controller.animateTo(cluster.center); mv.controller.zoomIn(); return }

        val metersPerPixelZ0 = 156543.03392 * cos(cluster.center.latitude * PI / 180.0)
        val targetZoom = ceil(log2(80.0 * sqrt(2.0) * metersPerPixelZ0 / minDist))
            .coerceIn(mv.zoomLevelDouble + 1.0, mv.maxZoomLevel.toDouble())

        mv.controller.animateTo(cluster.center, targetZoom, 600L)
    }

    private fun stationColor(count: Int, isInstalled: Boolean) = when {
        !isInstalled -> Color.GRAY
        count == 0 -> Color.rgb(244, 67, 54)
        count < 5 -> Color.rgb(255, 152, 0)
        else -> Color.rgb(76, 175, 80)
    }

    private fun cachedIcon(color: Int, text: String, size: Int) =
        iconCache.getOrPut("$color|$text|$size") { buildCircleIcon(color, text, size) }

    private fun buildCircleIcon(color: Int, text: String, size: Int): Drawable {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f; val cy = size / 2f; val r = size / 2f - 6

        canvas.drawCircle(cx + 2, cy + 3, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = Color.argb(50, 0, 0, 0) })
        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
        canvas.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f
        })
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = when (text.length) { 1, 2 -> 26f; 3 -> 22f; else -> 18f }
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(text, cx, cy - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        return BitmapDrawable(resources, bitmap)
    }

    private fun openDetail(station: Station) = startActivity(
        Intent(requireContext(), StationDetailActivity::class.java)
            .putExtra(StationDetailActivity.EXTRA_STATION, station)
    )

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) sharedViewModel.loadStations()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationOverlay.enableMyLocation()
        tryGetLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        locationOverlay.disableMyLocation()
        redrawHandler.removeCallbacks(redrawRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewReady = false
        redrawHandler.removeCallbacks(redrawRunnable)
        iconCache.clear()
    }
}
