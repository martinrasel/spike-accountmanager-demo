package de.bembelnaut.spike.accountmanager.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * The Service itself to get in interaction with the user and the auth services.
 *
 * The abstract class AbstractAccountAuthenticator defines several abstract methods to implement.
 */
class Authenticator(
    private val context: Context
) : AbstractAccountAuthenticator(context) {

    private val TAG = "Authenticator"
    private val loginService = LoginService()

    /**
     * Function called when method addAccount of AccountManager is called
     */
    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        accountTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        Log.d(TAG, "addAccount()")

        val intent = Intent(context, AuthenticatorActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(AuthenticatorActivity.PARAM_ACCOUNT_TYPE, accountType)
            putExtra(AuthenticatorActivity.PARAM_AUTH_TOKEN_TYPE, accountTokenType)
            putExtra(AuthenticatorActivity.PARAM_NEW_ACCOUNT, true)
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
         // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(Constance.AUTH_TOKEN_TYPE)) {
            return Bundle().also {
                it.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType")
            }
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        val accountManager = AccountManager.get(context)
        var token = accountManager?.peekAuthToken(account, authTokenType)
        Log.d(TAG, "peekAuthToken returned: $token");

        // oh no... the auth token is not available
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "getAuthToken(): Auth token not available. Get a new one...")

            // Lets give another try to authenticate the user
            account?.let { it ->
                val pw = accountManager.getPassword(it)
                if (!pw.isNullOrEmpty()) {
                    loginService.login(
                        userName = account.name,
                        password = pw,
                        onSuccess = { authToken ->
                            token = authToken
                        },
                        onFailure = { /*nothing*/ }
                    )
                }
            }
        }

        // If we get an authToken - we return it
        return if (!token.isNullOrEmpty()) {
            // return the token
            Bundle().also {
                it.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
                it.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
                it.putString(AccountManager.KEY_AUTHTOKEN, token)
            }
        } else {
            // If we get here, then we couldn't access the user's password - so we
            // need to re-prompt them for their credentials. We do that by creating
            // an intent to display our AuthenticatorActivity.
            val intent = Intent(context, AuthenticatorActivity::class.java).apply {
                putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                putExtra(AuthenticatorActivity.PARAM_AUTH_TOKEN_TYPE, authTokenType)

                account?.let {
                    putExtra(AuthenticatorActivity.PARAM_ACCOUNT_TYPE, it.type)
                    putExtra(AuthenticatorActivity.PARAM_ACCOUNT_NAME, it.name)
                }
            }

            Bundle().also {
                it.putParcelable(AccountManager.KEY_INTENT, intent)
            }
        }
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthTokenLabel(p0: String?): String? {
        return null
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle? {
        return null
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null
    }
}