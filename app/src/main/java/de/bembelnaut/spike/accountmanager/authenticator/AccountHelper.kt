package de.bembelnaut.spike.accountmanager.authenticator

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.app.Activity
import android.content.Context
import android.os.Bundle

/**
 * The account helper defines some useful methods to access the account manager.
 */
object AccountHelper {
    private var accountManager: AccountManager? = null

    const val ACCOUNT_NAME = "Bembelnaut Account"
    const val ACCOUNT_TYPE = "de.bembelnaut.spike.accountmanager"
    const val AUTH_TOKEN_TYPE = "de.bembelnaut.spike.accountmanager.token"

    fun initAccountManager(context: Context) {
        accountManager = AccountManager.get(context)
    }

    fun getAccountManager(): AccountManager? = accountManager

    object ClientActions {
        /**
         * Returns all accounts defined with the type [ACCOUNT_TYPE].
         */
        fun getMyAccounts(): Array<out Account>? = accountManager?.getAccountsByType(ACCOUNT_TYPE)

        /**
         * Returns all accounts of the account manager.
         */
        fun getAccounts(): Array<out Account>? = accountManager?.accounts

        /**
         * Create a new account.
         */
        fun createAccount(
            activity: Activity,
            onSuccess: AccountManagerCallback<Bundle?>
        ) {
            accountManager?.addAccount(
                ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                null,
                null,
                activity,
                onSuccess,
                null
            )
        }

        /**
         * Get an existing token from the account manager, or start an intent to login and retrun an auth token.
         */
        fun getAuthToken(
            activity: Activity,
            account: Account,
            onSuccess: AccountManagerCallback<Bundle?>
        ) {
            accountManager?.getAuthToken(
                account,
                AUTH_TOKEN_TYPE,
                null,
                activity,
                onSuccess,
                null
            )
        }

        /**
         * Get an new token from the account manager, or start an intent to login and retrun an auth token.
         */
        fun getNewAuthToken(
            activity: Activity,
            account: Account,
            onSuccess: AccountManagerCallback<Bundle?>
        ): String? {
            val token = accountManager?.peekAuthToken(account, AUTH_TOKEN_TYPE)

            if (!token.isNullOrEmpty()) {
                accountManager?.invalidateAuthToken(ACCOUNT_TYPE, token)
            }

            getAuthToken(activity, account, onSuccess)
            return accountManager?.peekAuthToken(account, AUTH_TOKEN_TYPE)
        }

        /**
         * Get an new token from the account manager, or start an intent to login and retrun an auth token.
         */
        fun invalidateToken(
            account: Account,
            onSuccess: () -> Unit,
        ) {
            val token = accountManager?.peekAuthToken(account, AUTH_TOKEN_TYPE)

            if (!token.isNullOrEmpty()) {
                accountManager?.let {
                    it.invalidateAuthToken(ACCOUNT_TYPE, token)
                    onSuccess()
                }
            }
        }
    }

    object AuthenticatorActions {
        /**
         * Add an account with id, password and - if its available - with an auth token.
         */
        fun addAccount(
            name: String,
            pw: String,
            authToken: String = "",
            userData: Bundle? = null
        ) {
            Account(name, ACCOUNT_TYPE).also { account ->
                accountManager?.addAccountExplicitly(account, pw, userData)
                setAuthToken(account, authToken)
            }
        }

        /**
         * Set a token to a givven account.
         */
        fun setAuthToken(
            account: Account,
            authToken: String
        ) {
            accountManager?.setAuthToken(account, AUTH_TOKEN_TYPE, authToken)
        }

        /**
         * Remove a account from the account manager.
         */
        fun removeAccount(account: Account) {
            accountManager?.removeAccountExplicitly(account)
        }

        /**
         * Reset the password of an account
         */
        fun getPassword(account: Account): String? {
            return accountManager?.getPassword(account)
        }

        /**
         * Reset the password of an account
         */
        fun setPassword(account: Account, password: String) {
            accountManager?.setPassword(account, password)
        }
    }
}