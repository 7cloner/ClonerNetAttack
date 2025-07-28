package com.cloner.http.attack.models

import okhttp3.Cookie

data class DBWhere(
    private val whereKey: String,
    private val whereValue: Any,
    private val operator: String = "=",
    private val before: String = "",
    private val after: String = "",
): AttackModel() {

    private fun getValue(): String {
        when (whereValue) {
            is Int -> {
                return whereValue.toString()
            }

            is Boolean -> {
                return if(whereValue) "1" else "0"
            }

            else -> {
                val wV = whereValue as? String
                    ?: if (whereValue is Cookie)
                        encodeCookie(whereValue)
                    else ""
                return "'$wV'"
            }
        }
    }

    fun buildQuery(): String {
        val query = StringBuilder()
        query.append(before)
        query.append(whereKey)
        query.append(operator)
        query.append(getValue())
        query.append(after)

        return query.toString()
    }
}