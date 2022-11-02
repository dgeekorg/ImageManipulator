package org.dgeek.imagemanipulator

import android.app.Application
import org.dgeek.imagemanipulator.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class MainApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            loadKoinModules(mainModule)
        }
    }
}