package fr.epf.sni.projetvelib.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface VelibApiService {

    @GET("station_information.json")
    suspend fun getStationInfo(): StationInfoResponse

    @GET("station_status.json")
    suspend fun getStationStatus(): StationStatusResponse

    companion object {
        fun create(): VelibApiService {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "fr.epf.sni.projetvelib/1.0 (Android)")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://velib-metropole-opendata.smovengo.cloud/opendata/Velib_Metropole/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(VelibApiService::class.java)
        }
    }
}

data class StationInfoResponse(val data: StationInfoData, val lastUpdatedOther: Long, val ttl: Int)
data class StationInfoData(val stations: List<StationInfo>)
data class StationInfo(val station_id: Long, val name: String, val lat: Double, val lon: Double, val capacity: Int)

data class StationStatusResponse(val data: StationStatusData, val lastUpdatedOther: Long, val ttl: Int)
data class StationStatusData(val stations: List<StationStatus>)
data class StationStatus(
    val station_id: Long,
    val numBikesAvailable: Int,
    val numDocksAvailable: Int,
    val num_bikes_available_types: List<BikeTypeCount>,
    val is_installed: Int,
    val is_renting: Int,
    val is_returning: Int,
    val last_reported: Long
)
data class BikeTypeCount(val ebike: Int = 0, val mechanical: Int = 0)
