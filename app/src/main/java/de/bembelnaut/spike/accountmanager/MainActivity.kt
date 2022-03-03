package de.bembelnaut.spike.accountmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.bembelnaut.spike.accountmanager.authenticator.AuthenticatorActivity
import de.bembelnaut.spike.accountmanager.databinding.ActivityMainBinding

/**
 * Main activity with some buttons to demonstrate the account manager
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.mainMyAccountBtn.setOnClickListener {
            clearResult()
            showResult("List of all personal accounts:")
            AccountHelper.getMyAccounts()?.forEach { account ->
                showResult("Name : ${account.name}\nType : ${account.type}")
            } ?: showResult("No personal accounts found!")
        }

        binding.mainAllAccountBtn.setOnClickListener {
            clearResult()
            showResult("List all accounts:")
            AccountHelper.getAccounts()?.forEach { account ->
                showResult("Name : ${account.name}\nType : ${account.type}")
            } ?: showResult("No accounts found!")
        }

        binding.mainLoginBtn.setOnClickListener {
            clearResult()
            showResult("Login with main...")
            startActivityForResult(
                Intent(this, AuthenticatorActivity::class.java),
                AuthenticatorActivity.LOGIN_REQUEST_CODE
            )
        }

        binding.mainRemoveAccountBtn.setOnClickListener {
            clearResult()
            showResult("Remove all personal accounts...")
            AccountHelper.getMyAccounts()?.forEach { account ->
                showResult("Remove account name: ${account.name}")
                AccountHelper.removeAccount(account)
            }
        }

        binding.mainGetTokenBtn.setOnClickListener {
            clearResult()
            showResult("Show personal tokens accounts...")
            AccountHelper.getMyAccounts()?.forEach { account ->
                val token = AccountHelper.getAuthToken(this, account)
                showResult("AuthToken account name : ${account.name}\ntoken : token")
            }
        }
    }

    private fun clearResult() {
        binding.mainAccountInfoTv.text = ""
    }

    private fun showResult(msg: String) {
        binding.mainAccountInfoTv.append("$msg\n\n")
    }
}