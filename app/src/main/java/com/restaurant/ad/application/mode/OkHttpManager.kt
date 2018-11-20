package com.restaurant.ad.application.mode

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.Nullable
import android.text.TextUtils
import com.restaurant.ad.application.app.App
import com.restaurant.ad.application.utils.ConstantStr
import com.restaurant.ad.application.utils.SPHelper
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class OkHttpManager<T>(val lifecycle: Lifecycle) : LifecycleObserver {
    private val okHttpClient: OkHttpClient
    val retrofit: Retrofit

    companion object {
        private var cookie: Cookie? = null
    }

    init {
        lifecycle.addObserver(this)
        okHttpClient = OkHttpClient().newBuilder()
                .cookieJar(object : CookieJar {

                    override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
                        if (cookies != null && cookies.size > 0) {
                            cookie = cookies[0]
                            SPHelper.write(App.instance, ConstantStr.SP_DIR, ConstantStr.COOKIE_DOMAIN, cookie!!.domain())
                            SPHelper.write(App.instance, ConstantStr.SP_DIR, ConstantStr.COOKIE_NAME, cookie!!.name())
                            SPHelper.write(App.instance, ConstantStr.SP_DIR, ConstantStr.COOKIE_VALUE, cookie!!.value())
                        }
                    }

                    override fun loadForRequest(url: HttpUrl?): MutableList<Cookie> {
                        val cookies = ArrayList<Cookie>()
                        if (cookie == null) {
                            val doMin = SPHelper.readString(App.instance, ConstantStr.SP_DIR, ConstantStr.COOKIE_DOMAIN)
                            val name = SPHelper.readString(App.instance, ConstantStr.SP_DIR, ConstantStr.COOKIE_NAME)
                            val value = SPHelper.readString(App.instance, ConstantStr.SP_DIR, ConstantStr.COOKIE_VALUE)
                            if (!TextUtils.isEmpty(doMin) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                                cookie = Cookie.Builder().domain(doMin).name(name).value(value).build()
                            }
                        }
                        if (cookie != null) {
                            cookies.add(Cookie.Builder().domain(cookie!!.domain()).name(cookie!!.name()).value(cookie!!.value()).build())
                        }
                        return cookies
                    }

                })
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl("http://47.110.73.71/lycm/")
                .client(okHttpClient)
                .addConverterFactory(ProtoConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Nullable
    fun requestData(call: Call<BaseEntity<T>>, successCallBack: (T?) -> Unit, failCallBack: (String?) -> Unit) {
        try {
            call.enqueue(object : Callback<BaseEntity<T>> {

                override fun onFailure(call: Call<BaseEntity<T>>?, t: Throwable?) {
                    failCallBack(t?.message)
                }

                override fun onResponse(call: Call<BaseEntity<T>>?, response: Response<BaseEntity<T>>?) {
                    if (response != null && response.isSuccessful) {
                        val result = response.body()
                        if (result?.errorCode == 0) {
                            requestSuccess(successCallBack, result.data)
                        } else {
                            requestFail(failCallBack, result?.message)
                        }
                    } else {
                        requestFail(failCallBack, response?.message())
                    }
                }

            })
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