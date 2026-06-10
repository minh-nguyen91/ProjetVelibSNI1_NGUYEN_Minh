package fr.epf.sni.projetvelib.ui.nearby

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.model.Station

class StationViewHolder(view: View) : RecyclerView.ViewHolder(view)

class StationAdapter(
    private val onItemClick: (Station) -> Unit,
    private val onFavoriteClick: (Station) -> Unit
) : RecyclerView.Adapter<StationViewHolder>() {

    private var items: List<Pair<Station, Double>> = emptyList()

    fun updateList(newItems: List<Pair<Station, Double>>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(vh: StationViewHolder, position: Int) {
        val (station, distance) = items[position]
        vh.itemView.apply {
            val textName = findViewById<TextView>(R.id.textName)
            val textBikes = findViewById<TextView>(R.id.textBikes)
            val textDocks = findViewById<TextView>(R.id.textDocks)
            val textDistance = findViewById<TextView>(R.id.textDistance)
            val imageFavorite = findViewById<ImageButton>(R.id.imageFavorite)

            textName.text = station.name
            textBikes.text = "${station.numBikesAvailable} vélos"
            textDocks.text = "${station.numDocksAvailable} places"
            textDistance.text = if (distance < 1000) "${distance.toInt()} m"
                                 else "${"%.1f".format(distance / 1000)} km"
            imageFavorite.setImageResource(if (station.isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
            imageFavorite.setOnClickListener { onFavoriteClick(station) }
            setOnClickListener { onItemClick(station) }
        }
    }

    override fun getItemCount() = items.size
}
