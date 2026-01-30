package org.lsm.flower_mailing.remote

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.lsm.flower_mailing.data.UserPreferencesRepository

class AuthInterceptor(private val repository: UserPreferencesRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { repository.accessTokenFlow.first() }
        val request = chain.request()

        if (token != null) {
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }
        return chain.proceed(request)
    }
}