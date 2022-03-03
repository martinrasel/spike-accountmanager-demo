package de.bembelnaut.spike.accountmanager

import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // AccountHelper is a singelton and must be initialized
        AccountHelper.initAccountManager(this)
    }
}