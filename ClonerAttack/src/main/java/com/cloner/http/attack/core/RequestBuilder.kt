package com.cloner.http.attack.core

import android.content.Context
import com.cloner.http.attack.models.TransferData
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
import okio.IOException
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


    fun makeRequest(
        method: String,
        headers: Headers?,
        body: RequestBody?,
        url: String,
        transferData: List<TransferData> = ArrayList(),
        callback: OnRequestListener
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val startTime = System.nanoTime()
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
                val endTime = System.nanoTime()
                val timeValue = (endTime - startTime) / 1_000_000
                val time = formatDuration(timeValue)
                val responseBody = response.body
                val bytes = responseBody.bytes()
                val bodyString = String(bytes)
                val size = bytes.size

                withContext(Dispatchers.Main) {
                    callback.onParseResponse(
                        serverResponse = bodyString,
                        response = response,
                        requestHeaders = mainHeaders,
                        body = body,
                        time = timeValue.toInt(),
                        timeLabel = time,
                        size = "$size bytes",
                        transferData = transferData
                    )
                }
            } catch (e: IOException) {
                val endTime = System.nanoTime()
                val timeValue = (endTime - startTime) / 1_000_000
                val time = formatDuration(timeValue)
                withContext(Dispatchers.Main) {
                    callback.onErrorResponse(
                        error = e.message.toString(),
                        exception = e,
                        requestHeaders = headers,
                        body = body,
                        time = timeValue.toInt(),
                        timeLabel = time,
                        size = "N/A",
                        transferData = transferData
                    )
                }
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        return when {
            ms < 1000 -> "$ms ms"
            ms < 60_000 -> "${ms / 1000.0} s"
            else -> "${ms / 60_000.0} m"
        }
    }

    interface OnRequestListener {
        fun onParseResponse(
            serverResponse: String, response: Response,
            requestHeaders: Headers?, body: RequestBody?,
            time: Int, timeLabel: String, size: String, transferData: List<TransferData>
        )

        fun onErrorResponse(
            error: String,
            exception: IOException,
            requestHeaders: Headers?, body: RequestBody?,
            time: Int, timeLabel: String, size: String, transferData: List<TransferData>
        )
    }
}
