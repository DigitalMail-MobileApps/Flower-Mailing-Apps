package org.lsm.flower_mailing.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.lsm.flower_mailing.data.UserPreferencesRepository
import org.lsm.flower_mailing.data.source.remote.api.AuthApi
import org.lsm.flower_mailing.data.source.remote.api.FileApi
import org.lsm.flower_mailing.data.source.remote.api.IncomingLetterApi
import org.lsm.flower_mailing.data.source.remote.api.OutgoingLetterApi
import org.lsm.flower_mailing.data.source.remote.api.SettingsApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton client to provide Retrofit API instances.
 *
 * Access individual service interfaces via the provided helper methods. Example:
 * `RetrofitClient.getAuthApi(context)`
 */
object RetrofitClient {

    private const val BASE_URL = "http://13.250.142.48:8080/api/"

    @Volatile private var retrofit: Retrofit? = null

    /** Returns a thread-safe singleton instance of Retrofit. */
    private fun getRetrofit(context: Context): Retrofit {
        return retrofit
                ?: synchronized(this) {
                    val instance = buildRetrofit(context.applicationContext)
                    retrofit = instance
                    instance
                }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val repository = UserPreferencesRepository(context)
        val authInterceptor = AuthInterceptor(repository)

        // Add logging for debug builds
        val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val okHttpClient =
                OkHttpClient.Builder()
                        .addInterceptor(authInterceptor)
                        .addInterceptor(loggingInterceptor)
                        .build()

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
    }

    // --- Modular API Providers ---

    fun getAuthApi(context: Context): AuthApi {
        return getRetrofit(context).create(AuthApi::class.java)
    }

    fun getOutgoingLetterApi(context: Context): OutgoingLetterApi {
        return getRetrofit(context).create(OutgoingLetterApi::class.java)
    }

    fun getIncomingLetterApi(context: Context): IncomingLetterApi {
        return getRetrofit(context).create(IncomingLetterApi::class.java)
    }

    fun getFileApi(context: Context): FileApi {
        return getRetrofit(context).create(FileApi::class.java)
    }

    fun getSettingsApi(context: Context): SettingsApi {
        return getRetrofit(context).create(SettingsApi::class.java)
    }

    // @Deprecated("Use specific API providers instead.")
    fun getInstance(context: Context): ApiService {
        return getRetrofit(context).create(ApiService::class.java)
    }
}
