package org.lsm.flower_mailing.remote

import android.content.Context
import okhttp3.OkHttpClient
import org.lsm.flower_mailing.data.UserPreferencesRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://13.250.142.48:8080/api/"

    @Volatile
    private var INSTANCE: ApiService? = null

    fun getInstance(context: Context): ApiService {
        return INSTANCE ?: synchronized(this) {
            val instance = buildService(context.applicationContext)
            INSTANCE = instance
            instance
        }
    }

    private fun buildService(context: Context): ApiService {
        val repository = UserPreferencesRepository(context)
        val authInterceptor = AuthInterceptor(repository)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}