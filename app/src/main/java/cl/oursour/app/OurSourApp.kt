package cl.oursour.app

import android.app.Application
import cl.oursour.app.di.AppContainer

class OurSourApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}


