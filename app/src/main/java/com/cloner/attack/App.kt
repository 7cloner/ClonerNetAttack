package com.cloner.attack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cloner.http.attack.HttpRequestExecutor
import com.cloner.http.attack.core.RequestBuilder
import com.cloner.http.attack.models.TransferData
import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException

class App : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        HttpRequestExecutor(
            this, ignoreSSL = false
        )
            .get(
                "http://192.168.25.36:8000/api/test/b",
                null, null, emptyList(),
                object : RequestBuilder.OnRequestListener {
                    override fun onParseResponse(
                        serverResponse: String,
                        response: Response,
                        requestHeaders: Headers?,
                        body: RequestBody?,
                        time: Int,
                        timeLabel: String,
                        size: String,
                        transferData: List<TransferData>
                    ) {
                        val headers = response.code
                        val dsv = ""
                    }


                    override fun onErrorResponse(
                        error: String,
                        exception: IOException,
                        requestHeaders: Headers?,
                        body: RequestBody?,
                        time: Int,
                        timeLabel: String,
                        size: String,
                        transferData: List<TransferData>,
                        url: String,
                        method: String
                    ) {
                        val error2 =  ""
                    }
                })
    }

}