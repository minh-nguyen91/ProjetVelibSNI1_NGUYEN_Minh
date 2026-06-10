package fr.epf.sni.projetvelib

import android.net.Uri
import fr.epf.sni.projetvelib.db.entity.FavoriteStation
import fr.epf.sni.projetvelib.model.Station

fun Station.toFavoriteStation() = FavoriteStation(
    stationId = stationId, name = name, lat = lat, lon = lon, capacity = capacity,
    numBikesAvailable = numBikesAvailable, numDocksAvailable = numDocksAvailable,
    numMechanicalBikes = numMechanicalBikes, numEbikes = numEbikes,
    isInstalled = isInstalled, isRenting = isRenting, isReturning = isReturning,
    lastReported = lastReported
)

fun FavoriteStation.toStation() = Station(
    stationId = stationId, name = name, lat = lat, lon = lon, capacity = capacity,
    numBikesAvailable = numBikesAvailable, numDocksAvailable = numDocksAvailable,
    numMechanicalBikes = numMechanicalBikes, numEbikes = numEbikes,
    isInstalled = isInstalled, isRenting = isRenting, isReturning = isReturning,
    lastReported = lastReported, isFavorite = true
)

fun Station.mapsDirectionsUri(): Uri = Uri.parse(
    "https://www.google.com/maps/dir/?api=1&destination=$lat,$lon&travelmode=walking"
)
