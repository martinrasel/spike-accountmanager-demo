package de.bembelnaut.spike.accountmanager

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.bembelnaut.spike.accountmanager.authenticator.AccountHelper
import de.bembelnaut.spike.accountmanager.authenticator.AccountHelper.ACCOUNT_NAME
import de.bembelnaut.spike.accountmanager.authenticator.AccountHelper.ACCOUNT_TYPE
import de.bembelnaut.spike.accountmanager.databinding.ActivityMainBinding

/**
 * Main activity with some buttons to demonstrate the account manager
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val account = Account(ACCOUNT_NAME, ACCOUNT_TYPE)
    private var userData: String? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        val createNewAccount = initLoginOrCreate()

        binding.mainLoginBtn.setOnClickListener {
            clearResult()
            showResult("Log in or create...")

            if (createNewAccount) {
                AccountHelper.ClientActions
                    .createAccount(
                        activity = this,
                        onSuccess = {
                            Log.d("OnTokenAcquired", "OpenTokenAcquired run()")

                            val bundle = it?.result

                            token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                            userData = bundle?.getString(AccountManager.KEY_USERDATA)

                            Log.d("OnTokenAcquired", "User token : $token")
                            Log.d("OnTokenAcquired", "User data : $userData")

                            showResult("Successfully create account...")
                            Toast.makeText(this, "Account erstellt und eingeloggt!", Toast.LENGTH_SHORT).show()
                        }
                    )
            } else {
                AccountHelper.ClientActions
                    .getAuthToken(
                        activity = this,
                        account = account,
                        onSuccess = {
                            Log.d("OnTokenAcquired", "OpenTokenAcquired run()")

                            val bundle = it?.result

                            token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                            userData = bundle?.getString(AccountManager.KEY_USERDATA)

                            Log.d("OnTokenAcquired", "User token : $token")
                            Log.d("OnTokenAcquired", "User data : $userData")

                            showResult("Success...")
                            Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show()
                        }
                    )
            }
        }

        binding.mainMyAccountBtn.setOnClickListener {
            clearResult()
            showResult("List of all personal accounts:")
            showResult("Token: $token")
            showResult("Userdata: $userData")
        }

        binding.mainNewTokenBtn.setOnClickListener {
            clearResult()
            showResult("Get new token...")

            AccountHelper.ClientActions
                .getNewAuthToken(
                    activity = this,
                    account = account,
                    onSuccess = {
                        Log.d("OnTokenAcquired", "OpenTokenAcquired run()")

                        val bundle = it?.result

                        token = bundle?.getString(AccountManager.KEY_AUTHTOKEN)
                        userData = bundle?.getString(AccountManager.KEY_USERDATA)

                        Log.d("OnTokenAcquired", "User token : $token")
                        Log.d("OnTokenAcquired", "User data : $userData")

                        showResult("Success...")
                        Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show()
                    }
                )
        }

        binding.mainLogoutAccountBtn.setOnClickListener {
            clearResult()
            showResult("List of all personal accounts:")
            AccountHelper.ClientActions
                .invalidateToken(
                    account = account,
                    onSuccess = {
                        token = null
                        userData = null
                    }
                )
        }

        binding.mainListAccountsBtn.setOnClickListener {
            clearResult()
            showResult("List all accounts:")
            AccountHelper.ClientActions.getAccounts()?.forEach { account ->
                showResult("Name : ${account.name}\nType : ${account.type}")
            } ?: showResult("No accounts found!")
        }

        binding.mainRemoveAccountBtn.setOnClickListener {
            clearResult()
            showResult("Remove personal account...")
            AccountHelper.ClientActions.getMyAccounts()?.forEach { account ->
                showResult("Remove account name: ${account.name}")
                AccountHelper.AuthenticatorActions.removeAccount(account)
            }

            initLoginOrCreate()
        }

    }

    private fun initLoginOrCreate(): Boolean {
        // login
        val createNewAccount = AccountHelper.ClientActions.getMyAccounts().isNullOrEmpty()
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