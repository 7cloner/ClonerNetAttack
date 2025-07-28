package com.cloner.http.attack.models

import android.util.Base64
import okhttp3.Cookie
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

abstract class AttackModel {
    internal fun encodeCookie(cookie: Cookie): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val objectOutputStream = ObjectOutputStream(outputStream)
            objectOutputStream.writeObject(cookie)
            objectOutputStream.close()
            return "cookie--" + Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (_: IOException) {
            return ""
        }
    }

    internal fun decodeCookie(encodedCookie: String): Cookie? {
        return try {
            val bytes = Base64.decode(encodedCookie, Base64.NO_WRAP)
            val inputStream = ByteArrayInputStream(bytes)
            val objectInputStream = ObjectInputStream(inputStream)
            objectInputStream.readObject() as? Cookie
        } catch (_: Exception) {
            null
        }
    }

}