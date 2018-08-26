package com.restaurant.ad.application.mode

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.Nullable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


class OkHttpManager<T>(val lifecycle: Lifecycle) : LifecycleObserver {
    private val okHttpClient: OkHttpClient
    val retrofit: Retrofit

    init {
        lifecycle.addObserver(this)
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

}