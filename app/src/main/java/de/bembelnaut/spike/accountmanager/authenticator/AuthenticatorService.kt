package de.bembelnaut.spike.accountmanager.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * This class binds the service to android system. The binding is defined in the authenticator.xml
 * (attribute 'accountType') and in the configured intent of class Authenticator.
 */
class AuthenticatorService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        // if the bind is called the returned authenticator is available
        val authenticator = Authenticator(this)
        return authenticator.iBinder
    }
}