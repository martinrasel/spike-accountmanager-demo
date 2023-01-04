package de.bembelnaut.spike.accountmanager.authenticator.remote.service

import com.google.gson.JsonObject
import de.bembelnaut.spike.accountmanager.authenticator.remote.model.Auth
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // Postman Mock Server Request
    @POST("/auth/login")
    fun login(@Body userData: JsonObject): Call<Auth>
}