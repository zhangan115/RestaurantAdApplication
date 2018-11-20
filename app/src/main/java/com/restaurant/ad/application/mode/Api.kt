package com.restaurant.ad.application.mode

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface Api {

    /**
     * 1,权限认证接口
     */
    @GET("api/user/login.json")
    fun userLogin(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<User>>

    /**
     * 3,模糊搜索餐厅
     */
    @GET("api/restaurant/list.json")
    fun restaurantList(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<List<Restaurant>>>

    /**
     * 4，城市列表 根据城市查找餐厅
     */
    @GET("api/city/list.json")
    fun cityList(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<List<City>>>

    /**
     * 5，分配桌号
     */
    @GET("api/pad/tableNumSetting.json")
    fun tableNumSetting(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<String>>


    /**
     * 6,获取到一个餐厅已经配置的桌号
     */
    @GET("api/restaurant/tableNums.json")
    fun restaurantTableNum(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<Restaurant>>

    /**
     * 7,获取广告列表
     */
    @GET("api/advertising/list.json")
    fun advertisingList(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<List<AdListBean>>>

    /**
     * 8,记录呼叫次数
     */
    @GET("api/restaurant/insertCallLog.json")
    fun insertCallLog(@QueryMap() requestMap: Map<String, String>): Call<BaseEntity<String>>

    /**
     * 9,获取设备列表
     */
    @GET("api/pad/getNoSetTableNumPadList.json")
    fun getDeviceList(): Call<BaseEntity<List<Device>>>

}