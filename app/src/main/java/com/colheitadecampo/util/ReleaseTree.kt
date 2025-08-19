package com.colheitadecampo.util

import android.util.Log
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custom Timber tree for logging in release builds
 */
@Singleton
class ReleaseTree @Inject constructor() : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            // Only log warnings and errors in release
            Log.println(priority, tag, message)
        }
    }
}
