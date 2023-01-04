package de.bembelnaut.spike.accountmanager.authenticator

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import de.bembelnaut.spike.accountmanager.databinding.ActivityLoginBinding


/**
 * Activity is called to sign in. It is defined as an intend in the Authenticator.
 */
class AuthenticatorActivity : AppCompatActivity() {

    private val TAG = "AuthenticatorActivity"

    companion object {
        const val PARAM_NEW_ACCOUNT = "isNewAccount"
        const val PARAM_ACCOUNT_NAME = "accountName"
        const val PARAM_ACCOUNT_PASSWORD = "accountPassword"
        const val PARAM_ACCOUNT_TYPE = "accountType"
        const val PARAM_AUTH_TOKEN_TYPE = "resultAuthTokenType"
    }

    // lazy init; init will happend when the getter is called
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private lateinit var accountManager: AccountManager
    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var mResultBundle: Bundle? = null

    private val loginService = LoginService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        accountManager = AccountManager.get(this)

        val key = AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE
        accountAuthenticatorResponse = intent.getParcelableExtra(key)
        accountAuthenticatorResponse?.onRequestContinued()

        binding.loginSubmitBtn.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val accountName = binding.loginIdEdt.text.toString()
        val accountPassword = binding.loginPwEdt.text.toString()

        loginService.login(
            userName = accountName,
            password = accountPassword,
            onSuccess = { authToken ->
                val accountType = intent.getStringExtra(PARAM_ACCOUNT_TYPE)

                val nextIntent = Intent()

                val data = Bundle()
                data.putString(AccountManager.KEY_ACCOUNT_NAME, accountName)
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
                data.putString(AccountManager.KEY_AUTHTOKEN, authToken)
                data.putString(PARAM_AUTH_TOKEN_TYPE, authToken)
                data.putString(PARAM_ACCOUNT_PASSWORD, accountPassword)
                nextIntent.putExtras(data)

                finishLogin(nextIntent)
            },
            onFailure = { error ->
                Log.d(TAG, "login error : $error")

                val nextIntent = Intent()

                val data = Bundle()
                data.putString(AccountManager.KEY_ERROR_MESSAGE, "Login Error: $error");
                nextIntent.putExtras(data)

                finishLogin(nextIntent)
            }
        )
    }

    private fun finishLogin(nextIntent: Intent) {
        val accountName = nextIntent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountPassword = nextIntent.getStringExtra(PARAM_ACCOUNT_PASSWORD)
        val accountType = nextIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        val account = Account(accountName, accountType)
        val authToken = nextIntent.getStringExtra(AccountManager.KEY_AUTHTOKEN)

        if (intent.getBooleanExtra(PARAM_NEW_ACCOUNT, false)) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            val result = accountManager.addAccountExplicitly(account, accountPassword, null)
            accountManager.setAuthToken(account, Constance.AUTH_TOKEN_TYPE, authToken);
            println("Account created: $result")
        } else {
            accountManager.setPassword(account, accountPassword!!)
        }

        val bee = Bundle()
        bee.putString(AccountManager.KEY_ACCOUNT_NAME, accountName)
        bee.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        bee.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        val resultIntent = Intent()
        resultIntent.putExtras(bee)

        setAccountAuthenticatorResult(bee)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun setAccountAuthenticatorResult(result: Bundle) {
        mResultBundle = result
    }

    override fun finish() {
        accountAuthenticatorResponse?.let {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                it.onResult(mResultBundle);
            } else {
                it.onError(
                    AccountManager.ERROR_CODE_CANCELED,
                    "canceled"
                );
            }
            accountAuthenticatorResponse = null;
        }
        super.finish()
    }
}