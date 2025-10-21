package com.kevin.receipttrackr

import android.app.Application
import com.kevin.receipttrackr.debug.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReceiptTrackrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init()
        Logger.d("ReceiptTrackrApp", "Application started")
    }
}
