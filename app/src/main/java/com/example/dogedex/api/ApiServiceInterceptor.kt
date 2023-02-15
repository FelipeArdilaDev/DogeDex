package com.example.dogedex.api

import okhttp3.Interceptor
import okhttp3.Response

object ApiServiceInterceptor : Interceptor {
    const val NEEDS_AUTH_HEADER_KEY = "needs_authentication"
    private var sessiontToken:String? = null

    fun setSettionToken(sessiontToken:String){
        this.sessiontToken = sessiontToken
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        if (request.header(NEEDS_AUTH_HEADER_KEY) != null) {
            // needs credential
            if (sessiontToken == null) {
                throw java.lang.RuntimeException("Need to be authenticated to performance")
            } else {
                requestBuilder.addHeader("AUTH_TOKEN", sessiontToken!!)
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}