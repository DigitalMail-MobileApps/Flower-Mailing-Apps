package org.lsm.flower_mailing

import android.app.Application
import com.google.firebase.FirebaseApp

class FlowerMailingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}