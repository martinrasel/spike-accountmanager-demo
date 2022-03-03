package de.bembelnaut.spike.accountmanager

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.os.Bundle
import android.util.Log

class OnTokenAcquired : AccountManagerCallback<Bundle> {
    private val TAG = "OnTokenAcquired"

    // Callback is called after the account manager gets its token
    override fun run(result: AccountManagerFuture<Bundle>?) {
        Log.d(TAG, "OpenTokenAcquired run()")

        val bundle = result?.result

        val token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)

        Log.d(TAG, "User token : $token")
    }
}