package com.cloner.http.attack.models

import android.content.Context
import okhttp3.Cookie

class AttackCookieModel(context: Context):
    AttackDBModel<AttackCookieModel>(context, AttackCookieModel::class.java, 1) {

    var name = ""
    var value = ""
    var host = ""
    var cookie: Cookie? = null

}