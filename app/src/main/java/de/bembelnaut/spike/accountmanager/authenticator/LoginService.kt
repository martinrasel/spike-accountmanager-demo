package de.bembelnaut.spike.accountmanager.authenticator

class LoginService {

    // some dummy accounts
    private val credentialRepos = mapOf(
        "demo@example.com" to "demo",
        "foo@example.com" to "foobar",
        "user@example.com" to "pass"
    )

    fun login(
        userName: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        if (credentialRepos[userName] == password) {
            onSuccess("userName_${System.currentTimeMillis()}")
        } else {
            onFailure("Login error!")
        }
    }
}