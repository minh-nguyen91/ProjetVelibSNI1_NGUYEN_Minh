package fr.epf.sni.projetvelib.ui.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    val searchQuery = MutableLiveData("")
    fun setSearchQuery(query: String) { searchQuery.value = query }
}
