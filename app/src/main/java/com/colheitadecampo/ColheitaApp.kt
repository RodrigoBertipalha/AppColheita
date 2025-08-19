package com.colheitadecampo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.colheitadecampo.util.ReleaseTree
import javax.inject.Inject

@HiltAndroidApp
class ColheitaApp : Application() {
    
    @Inject
    lateinit var releaseTree: ReleaseTree
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(releaseTree)
        }
    }
}
