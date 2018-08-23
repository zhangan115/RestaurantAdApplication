package com.restaurant.ad.application.mode

import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.restaurant.ad.application.mode.VideoFileMode.DownLoadHandle.DownLoadCallBack
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.*
import java.util.concurrent.TimeUnit


open class VideoFileMode(var url: String?) {

    interface DownLoadApi {
        @Streaming
        @GET
        fun downloadFile(@Url fileUrl: String): Call<ResponseBody>
    }

    private val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    private val retrofit: Retrofit

    init {
        retrofit = Retrofit.Builder()
                .baseUrl("http:192.168.2.100:8080/App/")
                .client(okHttpClient)
                .addConverterFactory(ProtoConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    fun getUrlVideoLocal(): File {
        val fileName = url!!.substring(url!!.lastIndexOf("/") + 1)
        val filePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_MOVIES
        return File(filePath + File.separator + fileName)
    }

    open fun downLoadFile(callBack: DownLoadCallBack) {
        val downLoadHandle = DownLoadHandle(callBack)
        if (!TextUtils.isEmpty(url)) {
            val call = retrofit.create(DownLoadApi::class.java).downloadFile(url!!)
            if (!getUrlVideoLocal().exists()) {
                getUrlVideoLocal().mkdir()
            } else {
                downLoadHandle.sendEmptyMessage(0)
                return
            }
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    t?.printStackTrace()
                    downLoadHandle.sendEmptyMessage(1)
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    if (response != null && response.isSuccessful && writeResponseBodyToDisk(getUrlVideoLocal().absolutePath, response.body())) {
                        downLoadHandle.sendEmptyMessage(0)
                    } else {
                        downLoadHandle.sendEmptyMessage(1)
                    }
                }
            })
        }
    }

    class DownLoadHandle(var callBack: DownLoadCallBack?) : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg?.what == 0) {
                callBack?.downSuccess()
            }
        }

        interface DownLoadCallBack {
            fun downSuccess()
        }
    }

    private fun writeResponseBodyToDisk(filePath: String, body: ResponseBody?): Boolean {
        try {
            val futureStudioIconFile = File(filePath)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                inputStream = body?.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read = inputStream!!.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
                return true
            } catch (e: IOException) {
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            return false
        }
    }
}