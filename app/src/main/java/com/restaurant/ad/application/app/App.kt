package com.restaurant.ad.application.app

import android.app.Application
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=594a2e41")
        super.onCreate()
        instance = this
    }
}