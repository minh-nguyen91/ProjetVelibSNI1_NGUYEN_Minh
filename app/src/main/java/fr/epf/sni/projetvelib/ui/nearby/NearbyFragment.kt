package fr.epf.sni.projetvelib.ui.nearby

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.model.Station
import fr.epf.sni.projetvelib.ui.SharedStationViewModel
import fr.epf.sni.projetvelib.ui.detail.StationDetailActivity

class NearbyFragment : Fragment() {

    private lateinit var viewModel: NearbyViewModel
    private lateinit var sharedViewModel: SharedStationViewModel
    private lateinit var adapter: StationAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var modeToggleGroup: MaterialButtonToggleGroup
    private lateinit var radiusSlider: Slider
    private lateinit var radiusLabel: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipMechanical: Chip
    private lateinit var chipEbike: Chip
    private lateinit var emptyText: TextView

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getLastLocation()
        } else {
            Snackbar.make(requireView(), R.string.location_denied, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_nearby, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NearbyViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedStationViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerView)
        modeToggleGroup = view.findViewById(R.id.modeToggleGroup)
        radiusSlider = view.findViewById(R.id.radiusSlider)
        radiusLabel = view.findViewById(R.id.radiusLabel)
        chipGroup = view.findViewById(R.id.chipGroup)
        chipAll = view.findViewById(R.id.chipAll)
        chipMechanical = view.findViewById(R.id.chipMechanical)
        chipEbike = view.findViewById(R.id.chipEbike)
        emptyText = view.findViewById(R.id.emptyText)

        setupRecyclerView()
        setupModeToggle()
        setupRadiusSlider()
        setupBikeFilter()
        observeViewModel()
        requestLocation()
    }

    private fun setupRecyclerView() {
        adapter = StationAdapter(
            onItemClick = { openDetail(it) },
            onFavoriteClick = { sharedViewModel.toggleFavorite(it) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupModeToggle() {
        modeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) sharedViewModel.setMode(
                if (checkedId == R.id.btnModeReturnBike) SharedStationViewModel.Mode.RETURN_BIKE
                else SharedStationViewModel.Mode.FIND_BIKE
            )
        }
    }

    private fun setupRadiusSlider() {
        radiusSlider.addOnChangeListener { _, value, _ ->
            val radius = value.toInt()
            radiusLabel.text = getString(R.string.radius_label, radius)
            viewModel.setRadius(radius.toDouble())
        }
        radiusLabel.text = getString(R.string.radius_label, radiusSlider.value.toInt())
    }

    private fun setupBikeFilter() {
        chipAll.setOnClickListener { viewModel.bikeFilter = NearbyViewModel.BikeFilter.ALL }
        chipMechanical.setOnClickListener { viewModel.bikeFilter = NearbyViewModel.BikeFilter.MECHANICAL }
        chipEbike.setOnClickListener { viewModel.bikeFilter = NearbyViewModel.BikeFilter.EBIKE }
    }

    private fun observeViewModel() {
        sharedViewModel.stations.observe(viewLifecycleOwner) { viewModel.updateStations(it) }
        sharedViewModel.mode.observe(viewLifecycleOwner) { mode ->
            val isReturn = mode == SharedStationViewModel.Mode.RETURN_BIKE
            viewModel.isReturnMode = isReturn
            chipGroup.isVisible = !isReturn
            modeToggleGroup.check(
                if (isReturn) R.id.btnModeReturnBike else R.id.btnModeFindBike
            )
        }
        viewModel.nearbyStations.observe(viewLifecycleOwner) { stations ->
            adapter.updateList(stations)
            emptyText.isVisible = stations.isEmpty()
            recyclerView.isVisible = stations.isNotEmpty()
        }
        sharedViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(requireView(), getString(R.string.loading_error, it), Snackbar.LENGTH_LONG)
                    .setAction("Réessayer") { sharedViewModel.loadStations() }
                    .show()
            }
        }
    }

    fun onTabResumed() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) getLastLocation()
        sharedViewModel.loadStations()
    }

    private fun requestLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getLastLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
            .addOnSuccessListener { location ->
                if (location != null) viewModel.setLocation(location.latitude, location.longitude)
                else Snackbar.make(requireView(), R.string.location_unavailable, Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun openDetail(station: Station) {
        startActivity(
            Intent(requireContext(), StationDetailActivity::class.java)
                .putExtra(StationDetailActivity.EXTRA_STATION, station)
        )
    }
}
