package com.restaurant.ad.application.app

import android.app.Application
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility
import com.tencent.bugly.Bugly

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=594a2e41")
        super.onCreate()
        instance = this
//        CrashReport.initCrashReport(this,"01c50ad647",false)
        Bugly.init(applicationContext, "01c50ad647", false)
    }

}