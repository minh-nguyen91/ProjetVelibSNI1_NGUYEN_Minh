package fr.epf.sni.projetvelib.model

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

data class Station(
    val stationId: Long,
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
    val isFavorite: Boolean = false
) : Parcelable {

    fun distanceTo(lat: Double, lon: Double): Double {
        val result = FloatArray(1)
        Location.distanceBetween(this.lat, this.lon, lat, lon, result)
        return result[0].toDouble()
    }

    constructor(parcel: Parcel) : this(
        stationId = parcel.readLong(),
        name = parcel.readString()!!,
        lat = parcel.readDouble(),
        lon = parcel.readDouble(),
        capacity = parcel.readInt(),
        numBikesAvailable = parcel.readInt(),
        numDocksAvailable = parcel.readInt(),
        numMechanicalBikes = parcel.readInt(),
        numEbikes = parcel.readInt(),
        isInstalled = parcel.readByte() != 0.toByte(),
        isRenting = parcel.readByte() != 0.toByte(),
        isReturning = parcel.readByte() != 0.toByte(),
        lastReported = parcel.readLong(),
        isFavorite = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(stationId)
        parcel.writeString(name)
        parcel.writeDouble(lat)
        parcel.writeDouble(lon)
        parcel.writeInt(capacity)
        parcel.writeInt(numBikesAvailable)
        parcel.writeInt(numDocksAvailable)
        parcel.writeInt(numMechanicalBikes)
        parcel.writeInt(numEbikes)
        parcel.writeByte(if (isInstalled) 1 else 0)
        parcel.writeByte(if (isRenting) 1 else 0)
        parcel.writeByte(if (isReturning) 1 else 0)
        parcel.writeLong(lastReported)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel) = Station(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Station>(size)
    }
}
