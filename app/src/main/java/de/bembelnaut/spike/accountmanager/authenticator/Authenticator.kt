package de.bembelnaut.spike.accountmanager.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.gson.JsonObject
import de.bembelnaut.spike.accountmanager.AccountHelper
import de.bembelnaut.spike.accountmanager.remote.ApiUtil

/**
 * The Service itself to get in interaction with the user and the auth services.
 *
 * The abstract class AbstractAccountAuthenticator defines several abstract methods to implement.
 */
class Authenticator(
    private val context: Context
) : AbstractAccountAuthenticator(context) {

    private val TAG = "Authenticator"

    /**
     * Function is called after the method AccountManager.addAccountExplicitly() is called.
     *
     * The function itself returns an intent, that will be started from the account manager.
     */
    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        accountTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        Log.d(TAG, "addAccount()")

        // here is out user - service - interaction
        val intent = Intent(context, AuthenticatorActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        }

        return Bundle().also {
            it.putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    /**
     * Returns the auth token of an account. It is called whenever the account manager needs a auth token.
     *
     * If the token is not available, the function calls the service to receive a new token.
     *
     * TODO: is this a regular use case, loading a new token?
     */
    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        Log.d(TAG, "getAuthToken()")

        val accountManager = AccountHelper.getAccountManager()
        var token = accountManager?.peekAuthToken(account, authTokenType)

        // oh no... the auth token is not available
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "getAuthToken(): Auth token not available. Get a new one...")

            // Get the password
            val pw = AccountHelper.getPassword(account)
            if (!pw.isNullOrEmpty()) {

                // TODO: replace retrofit with dummy.
                val body = JsonObject().apply {
                    addProperty("id", account?.name) // Account.getName() == ID
                    addProperty("pw", pw)
                }

                val apiResponse = ApiUtil.getApiService().login(body).execute()
                if (apiResponse.isSuccessful) {
                    val auth = apiResponse.body()

                    token = auth?.authToken
                }
            }
        }

        // store the token
        return if (!token.isNullOrEmpty()) {
            // TODO: why saving all the time?
            Log.d(TAG, "getAuthToken(): Save the new auth token")
            AccountHelper.setAuthToken(account, token)

            // return the token
            Bundle().also {
                it.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
                it.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
                it.putString(AccountManager.KEY_AUTHTOKEN, token)
            }
        } else {
            // try to login new, e.g. the account manager have the wrong password stored
            val intent = Intent(context, AuthenticatorActivity::class.java).apply {
                putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            }

            Bundle().also {
                it.putParcelable(AccountManager.KEY_INTENT, intent)
            }
        }
    }

    /**
     * TODO: i don't know when it is called and whats the purpose
     */
    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        // TODO: when is this method called?
        Log.d(TAG, "confirmCredentials()")
        return null
    }

    /**
     * TODO: i don't know when it is called and whats the purpose
     */
    override fun getAuthTokenLabel(p0: String?): String? {
        // TODO: when is this method called?
        Log.d(TAG, "getAuthTokenLabel(): $p0")
        return null
    }

    /**
     * TODO: i don't know when it is called and whats the purpose
     */
    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        // TODO: when is this method called?
        Log.d(TAG, "updateCredentials()")
        return null
    }

    /**
     * TODO: i don't know when it is called and whats the purpose
     */
    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle? {
        // TODO: when is this method called?
        Log.d(TAG, "hasFeatures()")
        return null
    }

    /**
     * TODO: i don't know when it is called and whats the purpose
     */
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        // TODO: when is this method called?
        Log.d(TAG, "editProperties()")
        return null
    }
}