package fr.epf.sni.projetvelib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.epf.sni.projetvelib.ui.favorites.FavoritesFragment
import fr.epf.sni.projetvelib.ui.map.MapFragment
import fr.epf.sni.projetvelib.ui.nearby.NearbyFragment

class MainActivity : AppCompatActivity() {

    private val mapFragment = MapFragment()
    private val nearbyFragment = NearbyFragment()
    private val favoritesFragment = FavoritesFragment()
    private var activeFragment: Fragment = mapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, favoritesFragment, "favorites").hide(favoritesFragment)
                .add(R.id.fragmentContainer, nearbyFragment, "nearby").hide(nearbyFragment)
                .add(R.id.fragmentContainer, mapFragment, "map")
                .commit()
        } else {
            activeFragment = when (bottomNavigation.selectedItemId) {
                R.id.nav_nearby -> nearbyFragment
                R.id.nav_favorites -> favoritesFragment
                else -> mapFragment
            }
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val target = when (item.itemId) {
                R.id.nav_map -> mapFragment
                R.id.nav_nearby -> {
                    nearbyFragment.onTabResumed()
                    nearbyFragment
                }
                R.id.nav_favorites -> favoritesFragment
                else -> return@setOnItemSelectedListener false
            }
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
            true
        }
    }
}
