package com.inzisoft.ibks.data.remote.interceptor

import com.inzisoft.ibks.AppKeySet
import com.inzisoft.ibks.data.repository.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(val localRepository: LocalRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking(Dispatchers.IO) {
        val token = localRepository.getAccessToken().first()

        chain.proceed(
            if (token.isEmpty()) {
                chain.request()
            } else {
                chain.request()
                    .newBuilder()
                    .addHeader(AppKeySet.ACCESS_TOKEN, token)
                    .build()
            }
        )
    }
}