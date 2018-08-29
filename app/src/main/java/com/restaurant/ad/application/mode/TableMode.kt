package com.restaurant.ad.application.mode

import android.content.Context
import com.restaurant.ad.application.app.App
import com.restaurant.ad.application.utils.ConstantStr

/**
 * 餐桌信息
 */
object TableMode {

    fun saveTableNum(num: String) {
        App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).edit().putString(ConstantStr.SP_TABLE_NUM, num).apply()
    }

    fun getTableNum(): String? {
        return App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).getString(ConstantStr.SP_TABLE_NUM, "")
    }

    fun saveRestaurantNum(resNum: String) {
        App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).edit().putString(ConstantStr.SP_RES, resNum).apply()
    }

    fun getResNum(): String? {
        return App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).getString(ConstantStr.SP_RES, "")
    }

    fun saveDeviceNum(resNum: String) {
        App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).edit().putString(ConstantStr.SP_DEVICES_NUM, resNum).apply()
    }

    fun getDeviceNum(): String? {
        return App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).getString(ConstantStr.SP_DEVICES_NUM, "")
    }

    fun getAdList(): String? {
        return App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).getString(ConstantStr.SP_AD_LIST, "")
    }

    fun saveAdList(adList: String) {
        App.instance.getSharedPreferences(ConstantStr.SP_DIR, Context.MODE_PRIVATE).edit().putString(ConstantStr.SP_AD_LIST, adList).apply()
    }
}