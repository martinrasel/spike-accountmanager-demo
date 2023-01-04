package de.bembelnaut.spike.accountmanager

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.bembelnaut.spike.accountmanager.authenticator.Constance
import de.bembelnaut.spike.accountmanager.authenticator.Constance.ACCOUNT_TYPE
import de.bembelnaut.spike.accountmanager.databinding.ActivityMainBinding

/**
 * Main activity with some buttons to demonstrate the account manager
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var accountName: String? = "demo@example.com"
    private val account = Account(accountName, ACCOUNT_TYPE)
    private var userData: String? = null
    private var token: String? = null

    private lateinit var accountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        accountManager = AccountManager.get(this)

        initView()
    }

    private fun initView() {
        binding.mainLoginBtn.setOnClickListener {
            clearResult()
            showResult("Log in or create...")

            val createNewAccount = initLoginOrCreate()
            if (createNewAccount) {
                accountManager.addAccount(
                    ACCOUNT_TYPE,
                    Constance.AUTH_TOKEN_TYPE,
                    null,
                    null,
                    this,
                    {
                        Log.d("OnTokenAcquired", "OpenTokenAcquired run()")

                        val bundle = it?.result

                        accountName = bundle?.getString(AccountManager.KEY_ACCOUNT_NAME)

                        // TODO: why does not the auth token returned? i guess some fun is filtering it out(?)
                        token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                        userData = bundle?.getString(AccountManager.KEY_USERDATA)

                        Log.d("OnTokenAcquired", "User token : $token")
                        Log.d("OnTokenAcquired", "User data : $userData")

                        showResult("Successfully create account...")
                        Toast.makeText(this, "Account erstellt und eingeloggt!", Toast.LENGTH_SHORT)
                            .show()

                        initLoginOrCreate()
                    }, null
                )
            } else {
                accountManager.getAuthToken(
                    account,
                    Constance.AUTH_TOKEN_TYPE,
                    null,
                    this,
                    {
                        Log.d("OnTokenAcquired", "OpenTokenAcquired run()")

                        val bundle = it?.result

                        accountName = bundle?.getString(AccountManager.KEY_ACCOUNT_NAME)
                        token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                        userData = bundle?.getString(AccountManager.KEY_USERDATA)

                        Log.d("OnTokenAcquired", "User token : $token")
                        Log.d("OnTokenAcquired", "User data : $userData")

                        showResult("Success...")
                        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show()

                        initLoginOrCreate()
                    },
                    null
                )
            }
        }

        binding.mainMyAccountBtn.setOnClickListener {
            clearResult()
            showResult("List of all personal accounts:")
            showResult("Account: $accountName")
            showResult("Token: $token")
            showResult("Userdata: $userData")

            initLoginOrCreate()
        }

        binding.mainNewTokenBtn.setOnClickListener {
            clearResult()
            showResult("Get new token...")

            val authToken = accountManager.peekAuthToken(account, Constance.AUTH_TOKEN_TYPE)

            if (!authToken.isNullOrEmpty()) {
                accountManager.invalidateAuthToken(ACCOUNT_TYPE, authToken)
            }

            accountManager.getAuthToken(
                account,
                Constance.AUTH_TOKEN_TYPE,
                null,
                this,
                {
                    Log.d("OnTokenAcquired", "OpenTokenAcquired run()")

                    val bundle = it?.result

                    accountName = bundle?.getString(AccountManager.KEY_ACCOUNT_NAME)
                    token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                    userData = bundle?.getString(AccountManager.KEY_USERDATA)

                    Log.d("OnTokenAcquired", "User token : $token")
                    Log.d("OnTokenAcquired", "User data : $userData")

                    showResult("Success...")
                    Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show()

                    initLoginOrCreate()
                },
                null
            )
        }

        binding.mainLogoutAccountBtn.setOnClickListener {
            clearResult()
            showResult("Logout")
            val authToken = accountManager.peekAuthToken(account, Constance.AUTH_TOKEN_TYPE)

            if (!authToken.isNullOrEmpty()) {
                accountManager.invalidateAuthToken(ACCOUNT_TYPE, authToken)
            }

            token = ""
            userData = ""
            accountName = "demo@example.com"

            initLoginOrCreate()
        }

        binding.mainListAccountsBtn.setOnClickListener {
            clearResult()
            showResult("List all accounts:")
            accountManager.accounts.forEach { account ->
                showResult("Name : ${account.name}\nType : ${account.type}")
            }

            initLoginOrCreate()
        }

        binding.mainRemoveAccountBtn.setOnClickListener {
            clearResult()
            showResult("Remove personal account...")
            accountManager.getAccountsByType(Constance.ACCOUNT_TYPE).forEach { account ->
                showResult("Remove account name: ${account.name}")
                accountManager.removeAccountExplicitly(account)
            }

            initLoginOrCreate()
        }

    }

    private fun initLoginOrCreate(): Boolean {
        // login
        val createNewAccount = accountManager.getAccountsByType(Constance.ACCOUNT_TYPE).isEmpty()
        binding.mainLoginBtn.text = if (createNewAccount) {
            "Create Account"
        } else {
            "Login"
        }

        return createNewAccount
    }

    private fun clearResult() {
        binding.mainAccountInfoTv.text = ""
    }

    private fun showResult(msg: String) {
        binding.mainAccountInfoTv.append("$msg\n\n")
    }
}