package de.bembelnaut.spike.accountmanager

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * The account helper defines some useful methods to access the account manager.
 */
object AccountHelper {
    private val TAG = "AccountHelper"

    private var accountManager: AccountManager? = null

    private const val ACCOUNT_TYPE = "de.bembelnaut.spike.accountmanager"

    fun initAccountManager(context: Context) {
        accountManager = AccountManager.get(context)
    }

    fun getAccountManager(): AccountManager? = accountManager

    /**
     * Returns all accounts defined with the type [ACCOUNT_TYPE].
     */
    fun getMyAccounts(): Array<out Account>? = accountManager?.getAccountsByType(ACCOUNT_TYPE)

    /**
     * Returns all accounts of the account manager.
     */
    fun getAccounts(): Array<out Account>? = accountManager?.accounts

    /**
     * Add an account with id, password and - if its available - with an auth token.
     */
    fun addAccount(id: String, pw: String, authToken: String? = "") {
        Account(id, ACCOUNT_TYPE).also { account ->

            // TODO: what kind of user data are stored in parameter userdata?
            accountManager?.addAccountExplicitly(account, pw, null)
            setAuthToken(account, authToken)
        }
    }

    /**
     * Set a token to a givven account.
     */
    fun setAuthToken(account: Account?, authToken: String?) {
        if (account != null && !authToken.isNullOrEmpty()) {
            accountManager?.setAuthToken(account, account.type, authToken)
        }
    }

    /**
     * Remove a account from the account manager.
     */
    fun removeAccount(account: Account?) {
        accountManager?.removeAccountExplicitly(account ?: return)
    }

    /**
     * Get an existing token from the account manager, or start an intent to login and retrun an auth token.
     */
    fun getAuthToken(activity: Activity, account: Account?) : String? {
        if (account != null) {
            val token = accountManager?.peekAuthToken(account, account.type)

            if (!token.isNullOrEmpty()) {
                return token
            } else {
                val feature = accountManager?.getAuthToken(
                    account,
                    account.type,
                    Bundle(),
                    activity,
                    OnTokenAcquired(),
                    null
                )

                return try {
                    // TODO: it is a synchronize call and will block the main thread this time...
                    feature?.getResult(5, TimeUnit.SECONDS)?.getString(AccountManager.KEY_AUTHTOKEN)
                } catch (e: Exception) {
                    Log.d(TAG, "getAuthToken: ${e.message}")
                    ""
                }
            }
        } else {
            return  ""
        }
    }

    /**
     * Reset the password of an account
     */
    fun getPassword(account: Account?): String? {
        return try {
            accountManager?.getPassword(account)
        } catch (e: Exception) {
            ""
        }
    }
}