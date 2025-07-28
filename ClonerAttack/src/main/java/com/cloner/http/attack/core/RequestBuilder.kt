package com.cloner.http.attack.core

import android.content.Context
import com.cloner.http.attack.sessions.AttackCookieJar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.Proxy
import java.util.concurrent.TimeUnit

abstract class RequestBuilder(
    context: Context,
    useCookieJar: Boolean = false,
    useKeepAlive: Boolean = false,
    ignoreSSL: Boolean = false,
    connectionTimeOut: Long = 10,
    readTimeOut: Long = 10,
    writeTimeOut: Long = 10,
    proxy: Proxy? = null,
    proxyUsername: String? = null,
    proxyPassword: String? = null,
) : TLSExecutor(useKeepAlive) {

    private val client: OkHttpClient

    init {
        var builder =
            enableDispatcher(OkHttpClient.Builder())
                .connectionPool(getConnectionPool())
                .connectTimeout(connectionTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)

        if (useCookieJar) {
            builder = builder.cookieJar(AttackCookieJar(context))
        }

        if (ignoreSSL) {
            builder = enableUnsafeSSL(builder)
        }

        if (proxy != null) {
            builder = builder.proxy(proxy)
            if (proxyUsername != null && proxyPassword != null) {
                val proxyAuthenticator = Authenticator { _, response ->
                    val credential = Credentials.basic(proxyUsername, proxyPassword)
                    response.request.newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build()
                }
                builder = builder.proxyAuthenticator(proxyAuthenticator)
            }
        }

        client = builder.build()
    }


    internal fun makeRequest(
        method: String,
        headers: Headers?,
        body: RequestBody?,
        url: String,
        callback: OnRequestListener
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mainHeaders = (headers?.newBuilder() ?: Headers.Builder())
                    .set("Connection", if (useKeepAlive) "keep-alive" else "close")
                    .build()

                val request = Request.Builder()
                    .method(method, body)
                    .headers(mainHeaders)
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body.string()
                withContext(Dispatchers.Main) {
                    callback.onParseResponse(body, response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onErrorResponse(e.message.toString())
                }
            }
        }
    }

    interface OnRequestListener {
        fun onParseResponse(serverResponse: String, response: Response)
        fun onErrorResponse(error: String)
    }
}
