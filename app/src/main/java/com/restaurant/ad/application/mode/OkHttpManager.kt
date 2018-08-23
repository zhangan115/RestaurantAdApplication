package com.restaurant.ad.application.mode

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.Nullable

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


class OkHttpManager<T>(val lifecycle: Lifecycle) : LifecycleObserver {
    private val okHttpClient: OkHttpClient
    private val netInterceptor: Interceptor
    val retrofit: Retrofit

    init {
        lifecycle.addObserver(this)
        netInterceptor = Interceptor { chain ->
            val request: Request = chain.request()
            val httpUr: HttpUrl = chain.request()
                    .url()
                    .newBuilder()
                    .addQueryParameter("key", "9403ec902e1f43ef89ab17000271fa4e")
                    .build()
            val builder: Request = request.newBuilder().url(httpUr).addHeader("contentType", "text/json").build()
            val response: Response = chain.proceed(builder)
            response
        }
        okHttpClient = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl("http:192.168.2.100:8080/App/")
                .client(okHttpClient)
                .addConverterFactory(ProtoConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Nullable
    fun requestData(call: Call<BaseEntity<T>>, successCallBack: (T?) -> Unit, failCallBack: (String?) -> Unit) {
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.errorCode == 0) {
                    requestSuccess(successCallBack, result.data)
                } else {
                    requestFail(failCallBack, result?.message)
                }
            } else {
                requestFail(failCallBack, response.message())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            requestFail(failCallBack, e.message)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun requestSuccess(successCallBack: (T?) -> Unit, t: T) {

        successCallBack(t)

    }

    private fun requestFail(failCallBack: (String?) -> Unit, message: String?) {

        failCallBack(message)

    }

    fun uploadFile(file: File, type: String, successCallBack: (String) -> Unit, failCallBack: (String?) -> Unit) {
        try {
            val builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            builder.addFormDataPart("file", file.name, requestFile)
            val parts = builder.build().parts()
            val response = retrofit.create(Api::class.java).uploadFile(parts, type).execute()
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.errorCode == 0) {
                    if (result.data.isNotEmpty()) {
                        successCallBack(result.data[0])
                    }
                } else {
                    failCallBack(result?.message)
                }
            } else {
                failCallBack(response.message())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            failCallBack(e.message)
        }

    }

}