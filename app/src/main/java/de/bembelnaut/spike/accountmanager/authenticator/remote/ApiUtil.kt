package de.bembelnaut.spike.accountmanager.authenticator.remote

import de.bembelnaut.spike.accountmanager.authenticator.remote.service.ApiService

class ApiUtil {
    companion object {
        const val BASE_URL = ""
        val apiService: ApiService = RetrofitClient.getApiClient().create(ApiService::class.java)
    }
}