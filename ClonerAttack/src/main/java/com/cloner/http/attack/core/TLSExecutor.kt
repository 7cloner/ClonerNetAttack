package com.cloner.http.attack.core

import android.annotation.SuppressLint
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

abstract class TLSExecutor(internal val useKeepAlive: Boolean) {


    internal fun getConnectionPool(): ConnectionPool {
        return if (useKeepAlive)
            ConnectionPool(10, 2, TimeUnit.DAYS)
        else
            ConnectionPool(0, 1, TimeUnit.MILLISECONDS)
    }

    internal fun enableDispatcher(builder: OkHttpClient.Builder): OkHttpClient.Builder{
        val dispatcher = Dispatcher().apply {
            maxRequests = 5000
            maxRequestsPerHost = 5000
        }

        return builder.dispatcher(dispatcher)
    }

    internal fun enableUnsafeSSL(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val trustAllCerts = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }

        val sslSocketFactory = sslContext.socketFactory
        return builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    }

}