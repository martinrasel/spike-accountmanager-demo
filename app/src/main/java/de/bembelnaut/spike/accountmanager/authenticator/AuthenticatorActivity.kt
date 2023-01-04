package de.bembelnaut.spike.accountmanager.authenticator

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import de.bembelnaut.spike.accountmanager.authenticator.remote.ApiUtil
import de.bembelnaut.spike.accountmanager.authenticator.remote.model.Auth
import de.bembelnaut.spike.accountmanager.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * Activity is called to sign in. It is defined as an intend in the Authenticator.
 */
class AuthenticatorActivity : AppCompatActivity() {
    private val TAG = "AuthenticatorActivity"

    companion object {
        const val PARAM_NEW_ACCOUNT = "isNewAccount"
        const val PARAM_USER_PASSWORD = "password"
        const val PARAM_AUTH_TOKEN_TYPE = "authTokenType"
    }

    // lazy init; init will happend when the getter is called
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    // some dummy accounts
    private val credentialRepos = mapOf(
        "demo@example.com" to "demo",
        "foo@example.com" to "foobar",
        "user@example.com" to "pass"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.loginSubmitBtn.setOnClickListener {
            //login()
            val intent =
                signIn(binding.loginIdEdt.text.toString(), binding.loginPwEdt.text.toString())

            intent?.let {
                finishLogin(it)
            }
        }
    }

    private fun login() {
        val accountId = binding.loginIdEdt.text.toString()
        val accountPw = binding.loginPwEdt.text.toString()

        val body = JsonObject().apply {
            addProperty("id", accountId)
            addProperty("pw", accountPw)
        }

        // retrofit call to login
        ApiUtil.apiService.login(body).enqueue(object : Callback<Auth> {
            override fun onResponse(call: Call<Auth>, response: Response<Auth>) {
                if (response.isSuccessful) {
                    val auth = response.body()

                    // if the account is successfully logged in, the auth token is stored in the account manager
                    AccountHelper.AuthenticatorActions.addAccount(
                        accountId,
                        accountPw,
                        auth!!.authToken!!
                    )

                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@AuthenticatorActivity,
                        "", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Auth>, t: Throwable) {
                Log.d(TAG, "login error : ${t.localizedMessage}")

                Toast.makeText(
                    this@AuthenticatorActivity,
                    "", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // a kind of dummy login
    fun signIn(email: String, password: String): Intent? {
        return if (email in credentialRepos
            && credentialRepos[email] == password
        ) {
            val authToken = email + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)

            Toast.makeText(
                this@AuthenticatorActivity,
                "Add account", Toast.LENGTH_SHORT
            ).show()

            val res = Intent()

            res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email)
            res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountHelper.ACCOUNT_TYPE)
            res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
            res.putExtra(PARAM_USER_PASSWORD, password)

            res
        } else {
            Log.d(TAG, "login error")

            Toast.makeText(
                this@AuthenticatorActivity,
                "Login error", Toast.LENGTH_SHORT
            ).show()

            null
        }
    }

    private fun finishLogin(intent: Intent) {
        val accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountPassword = intent.getStringExtra(PARAM_USER_PASSWORD)
        val account = Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
        val authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN)

        if (getIntent().getBooleanExtra(PARAM_NEW_ACCOUNT, false)) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            AccountHelper.AuthenticatorActions.addAccount(
                accountName!!,
                accountPassword!!,
                authToken!!
            )
        } else {
            AccountHelper.AuthenticatorActions.setAuthToken(account, authToken!!)
            AccountHelper.AuthenticatorActions.setPassword(account, accountPassword!!)
        }

        val accountAuthenticatorResponse = getIntent()
            .getParcelableExtra<AccountAuthenticatorResponse>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        accountAuthenticatorResponse?.onResult(intent.extras)

        setResult(RESULT_OK, intent)
        finish()
    }
}