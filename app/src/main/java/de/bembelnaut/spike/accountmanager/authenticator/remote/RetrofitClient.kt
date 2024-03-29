package de.bembelnaut.spike.accountmanager.authenticator.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var apiClient: Retrofit? = null

    fun getApiClient(): Retrofit {
        if (apiClient == null) {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .addInterceptor {
                    val request = it.request().newBuilder()
//                    .addHeader()
                        .build()

                    it.proceed(request)
                }

            apiClient = Retrofit.Builder()
                .baseUrl(ApiUtil.BASE_URL)
                .client(client.build())
                .build()
        }

        return apiClient!!
    }
}