package com.cloner.http.attack.sessions

import android.content.Context
import com.cloner.http.attack.models.AttackCookieModel
import com.cloner.http.attack.models.DBWhere
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class AttackCookieJar(private val context: Context): CookieJar {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>
    ) {
        scope.launch {
            mutex.withLock {
                val host = url.host
                AttackCookieModel(context).delete("host = '$host'")

                cookies.forEach {
                    val cookieModel = AttackCookieModel(context)
                    cookieModel.host = host
                    cookieModel.value = it.value
                    cookieModel.name = it.name
                    cookieModel.cookie = it

                    cookieModel.save()
                }
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val items: MutableList<Cookie> = ArrayList()
        AttackCookieModel(context).get(
            listOf("*"),
            wheres = listOf(DBWhere("host", url.host)),
            orderBy = "DESC",
        ).forEach { item ->
            if(item.cookie != null){
                items.add(item.cookie!!)
            }
        }

        return items
    }
}