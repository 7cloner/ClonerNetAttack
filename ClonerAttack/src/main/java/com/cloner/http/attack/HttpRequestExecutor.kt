package com.cloner.http.attack

import android.content.Context
import com.cloner.http.attack.core.RequestBuilder
import okhttp3.Headers
import okhttp3.RequestBody
import java.net.Proxy

class HttpRequestExecutor(
    context: Context,
    ignoreSSL: Boolean = false,
    useCookieJar: Boolean = false,
    useKeepAlive: Boolean = false,
    connectionTimeOut: Long = 10,
    readTimeOut: Long = 10,
    writeTimeOut: Long = 10,
    proxy: Proxy? = null,
    proxyUsername: String? = null,
    proxyPassword: String? = null,
) : RequestBuilder(
    context = context,
    ignoreSSL = ignoreSSL,
    useCookieJar = useCookieJar,
    useKeepAlive = useKeepAlive,
    connectionTimeOut = connectionTimeOut,
    readTimeOut = readTimeOut,
    writeTimeOut = writeTimeOut,
    proxy = proxy,
    proxyUsername = proxyUsername,
    proxyPassword = proxyPassword
) {

    companion object {
        private const val GET = "GET"
        private const val POST = "POST"
        private const val PUT = "PUT"
        private const val PATCH = "PATCH"
        private const val DELETE = "DELETE"
        private const val HEAD = "HEAD"
        private const val OPTIONS = "OPTIONS"
        private const val TRACE = "TRACE"
        private const val CONNECT = "CONNECT"
    }

    fun get(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(GET, headers, body, url, callback)
    }

    fun post(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(POST, headers, body, url, callback)
    }

    fun put(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(PUT, headers, body, url, callback)
    }

    fun patch(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(PATCH, headers, body, url, callback)
    }

    fun delete(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(DELETE, headers, body, url, callback)
    }

    fun head(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(HEAD, headers, body, url, callback)
    }

    fun options(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(OPTIONS, headers, body, url, callback)
    }

    fun trace(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(TRACE, headers, body, url, callback)
    }

    fun connect(url: String, body: RequestBody?, headers: Headers?, callback: OnRequestListener) {
        makeRequest(CONNECT, headers, body, url, callback)
    }

}
