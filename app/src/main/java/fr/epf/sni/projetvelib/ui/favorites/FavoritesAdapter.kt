package fr.epf.sni.projetvelib.ui.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.epf.sni.projetvelib.R
import fr.epf.sni.projetvelib.db.entity.FavoriteStation

class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view)

class FavoritesAdapter(
    private val onItemClick: (FavoriteStation) -> Unit,
    private val onRemoveFavorite: (FavoriteStation) -> Unit
) : RecyclerView.Adapter<FavoriteViewHolder>() {

    private var items: List<FavoriteStation> = emptyList()

    fun updateList(newItems: List<FavoriteStation>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(vh: FavoriteViewHolder, position: Int) {
        val fav = items[position]
        vh.itemView.apply {
            val textName = findViewById<TextView>(R.id.textName)
            val textBikes = findViewById<TextView>(R.id.textBikes)
            val textDocks = findViewById<TextView>(R.id.textDocks)
            val textDistance = findViewById<TextView>(R.id.textDistance)
            val imageFavorite = findViewById<ImageButton>(R.id.imageFavorite)

            textName.text = fav.name
            textBikes.text = "${fav.numBikesAvailable} vélos"
            textDocks.text = "${fav.numDocksAvailable} places"
            textDistance.visibility = View.GONE
            imageFavorite.setImageResource(R.drawable.ic_star)
            imageFavorite.setOnClickListener { onRemoveFavorite(fav) }
            setOnClickListener { onItemClick(fav) }
        }
    }

    override fun getItemCount() = items.size
}
