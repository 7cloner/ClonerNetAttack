package com.cloner.attack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cloner.http.attack.HttpRequestExecutor
import com.cloner.http.attack.core.RequestBuilder
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.Response

class App : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        HttpRequestExecutor(
            this, ignoreSSL = false
        )
            .get(
                "http://192.168.25.36:8000/api/test/b",
                null, null, HashMap(),
                object : RequestBuilder.OnRequestListener {
                    override fun onParseResponse(
                        serverResponse: String,
                        response: Response,
                        requestHeaders: Headers?,
                        body: RequestBody?,
                        time: String,
                        size: String,
                        transferData: HashMap<String, Map<String, List<String>>>
                    ) {
                        val headers = response.code
                        val dsv = ""
                    }


                    override fun onErrorResponse(error: String) {
                        val dv = ""
                    }

                })
    }

}