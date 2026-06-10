package fr.epf.sni.projetvelib

import android.app.Application
import org.osmdroid.config.Configuration

class VelibApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            load(this@VelibApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}
