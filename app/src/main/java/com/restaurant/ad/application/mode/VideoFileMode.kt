package com.restaurant.ad.application.mode

import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.restaurant.ad.application.app.App
import com.restaurant.ad.application.mode.VideoFileMode.DownLoadHandle.DownLoadCallBack
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.Okio
import okio.Sink
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
        return File(savePath, getSaveFileName())
    }

    fun getSaveFileName(): String {
        return url!!.substring(url!!.lastIndexOf("/") + 1)
    }

    fun isDownLoadSuccess(): Boolean {
        return getUrlVideoLocal().exists()
    }

    open fun downLoadFile(callBack: DownLoadCallBack) {
        val downLoadHandle = DownLoadHandle(callBack)
        if (!TextUtils.isEmpty(url)) {
            launch {
                val call = retrofit.create(DownLoadApi::class.java).downloadFile(url!!)
                if (isDownLoadSuccess()) {
                    downLoadHandle.sendEmptyMessage(0)
                } else {
                    File(savePath).mkdir()
                    val response = call.execute()
                    if (response != null && response.isSuccessful) {
                        var sink: Sink? = null
                        var bufferedSink: BufferedSink? = null
                        try {
                            sink = Okio.sink(File(savePath, getSaveFileName()))
                            if (sink != null) {
                                bufferedSink = Okio.buffer(sink)
                                bufferedSink.writeAll(response.body()!!.source())
                                bufferedSink.close()
                            }
                            Log.i("DOWNLOAD", "download success")
                            downLoadHandle.sendEmptyMessage(0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            downLoadHandle.sendEmptyMessage(1)
                        } finally {
                            sink?.close()
                            bufferedSink?.close()
                        }
                    } else {
                        downLoadHandle.sendEmptyMessage(1)
                    }
                }
            }
        }
    }

    class DownLoadHandle(private var callBack: DownLoadCallBack?) : Handler() {
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

    private fun writeResponseBodyToDisk(fileName: String, body: ResponseBody?): Boolean {
        try {
            val futureStudioIconFile = File(savePath, fileName)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(1024)
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

    companion object {
        val savePath = App.instance.externalCacheDir?.absolutePath

        fun cleanVideoFile(adList: ArrayList<AdDataBean>) {
            val videoAd = ArrayList<AdDataBean>()
            for (ad in adList) {
                if (ad.isVideo) {
                    videoAd.add(ad)
                }
            }
            val files = File(savePath).list()
            val deleteFile = ArrayList<String>()
            for (f in files) {
                var isSave = false
                for (v in videoAd) {
                    if (TextUtils.equals(f, v.url.substring(v.url.lastIndexOf("/") + 1))) {
                        isSave = true
                        break
                    }
                }
                if (!isSave) {
                    deleteFile.add(f)
                }
            }
            for (delete in deleteFile) {
                File(savePath, delete).delete()
            }
        }
    }
}