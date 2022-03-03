package de.bembelnaut.spike.accountmanager.authenticator

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import de.bembelnaut.spike.accountmanager.AccountHelper
import de.bembelnaut.spike.accountmanager.databinding.ActivityLoginBinding
import de.bembelnaut.spike.accountmanager.remote.ApiUtil
import de.bembelnaut.spike.accountmanager.remote.model.Auth
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
        const val LOGIN_REQUEST_CODE = 1234
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
            signIn(binding.loginIdEdt.text.toString(), binding.loginPwEdt.text.toString())
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
        ApiUtil.getApiService().login(body).enqueue(object : Callback<Auth> {
            override fun onResponse(call: Call<Auth>, response: Response<Auth>) {
                if (response.isSuccessful) {
                    val auth = response.body()

                    // if the account is successfully logged in, the auth token is stored in the account manager
                    AccountHelper.addAccount(accountId, accountPw, auth?.authToken)

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
    fun signIn(email: String, password: String) {
        if (email in credentialRepos
            && credentialRepos[email] == password
        ) {
            val auth = email + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)

            // if the account is successfully logged in, the auth token is stored in the account manager
            AccountHelper.addAccount(email, password, auth)

            Toast.makeText(
                this@AuthenticatorActivity,
                "Add account", Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.d(TAG, "login error")

            Toast.makeText(
                this@AuthenticatorActivity,
                "Login error", Toast.LENGTH_SHORT
            ).show()
        }
    }
}