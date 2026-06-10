package fr.epf.sni.projetvelib.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.db.entity.FavoriteStation
import fr.epf.sni.projetvelib.toStation
import fr.epf.sni.projetvelib.ui.SharedStationViewModel
import fr.epf.sni.projetvelib.ui.detail.StationDetailActivity

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var sharedViewModel: SharedStationViewModel
    private lateinit var adapter: FavoritesAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var offlineBanner: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedStationViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerView)
        emptyText = view.findViewById(R.id.emptyText)
        offlineBanner = view.findViewById(R.id.offlineBanner)

        adapter = FavoritesAdapter(
            onItemClick = { openDetail(it) },
            onRemoveFavorite = { viewModel.removeFavorite(it.stationId) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            adapter.updateList(favorites)
            emptyText.isVisible = favorites.isEmpty()
            recyclerView.isVisible = favorites.isNotEmpty()
        }
        sharedViewModel.stations.observe(viewLifecycleOwner) { viewModel.refreshFavorites(it) }
        sharedViewModel.error.observe(viewLifecycleOwner) { offlineBanner.isVisible = it != null }
    }

    private fun openDetail(fav: FavoriteStation) {
        startActivity(
            Intent(requireContext(), StationDetailActivity::class.java)
                .putExtra(StationDetailActivity.EXTRA_STATION, fav.toStation())
        )
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) sharedViewModel.loadStations()
    }
}
