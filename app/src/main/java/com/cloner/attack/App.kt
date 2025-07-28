package com.cloner.attack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cloner.http.attack.HttpRequestExecutor
import com.cloner.http.attack.core.RequestBuilder
import okhttp3.Response

class App : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        HttpRequestExecutor(
            this, ignoreSSL = false
        )
            .get("https://smartwalletsfinder.com/api/test",
            null, null, object: RequestBuilder.OnRequestListener {
                    override fun onParseResponse(serverResponse: String, response: Response) {
                        val headers = response.code
                        val dsv = ""
                    }

                    override fun onErrorResponse(error: String) {
                        val dv = ""
                    }

                })
    }

}