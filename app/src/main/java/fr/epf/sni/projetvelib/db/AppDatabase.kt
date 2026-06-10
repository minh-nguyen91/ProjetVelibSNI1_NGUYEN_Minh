package fr.epf.sni.projetvelib.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.epf.sni.projetvelib.db.dao.FavoriteStationDao
import fr.epf.sni.projetvelib.db.entity.FavoriteStation

@Database(entities = [FavoriteStation::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteStationDao(): FavoriteStationDao

    companion object {
        fun getDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "velib_database")
                .fallbackToDestructiveMigration()
                .build()
    }
}
