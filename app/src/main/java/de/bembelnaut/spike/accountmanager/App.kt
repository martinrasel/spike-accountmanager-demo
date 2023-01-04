package de.bembelnaut.spike.accountmanager

import android.app.Application
import de.bembelnaut.spike.accountmanager.authenticator.AccountHelper

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // AccountHelper is a singelton and must be initialized
        AccountHelper.initAccountManager(this)
    }
}