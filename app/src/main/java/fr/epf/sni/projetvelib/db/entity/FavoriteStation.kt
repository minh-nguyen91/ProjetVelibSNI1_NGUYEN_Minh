package fr.epf.sni.projetvelib.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStation(
    @PrimaryKey val stationId: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val numBikesAvailable: Int,
    val numDocksAvailable: Int,
    val numMechanicalBikes: Int,
    val numEbikes: Int,
    val isInstalled: Boolean,
    val isRenting: Boolean,
    val isReturning: Boolean,
    val lastReported: Long,
    val savedAt: Long = System.currentTimeMillis()
)
