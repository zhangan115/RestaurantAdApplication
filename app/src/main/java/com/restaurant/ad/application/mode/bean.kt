package com.restaurant.ad.application.mode

data class User(var realName: String, var userName: String, var userPhone: String)

data class City(var city: String, var cityId: Long, var shouzimu: String, var parentCityId: Long, var childList: ArrayList<City>?)

data class Restaurant(var createTime: Long, var endTime: Long, var latitude: Float, var longitude: Float
                      , var phone: String, var phone2: String, var restaurantAddress: String
                      , var restaurantId: Int, var restaurantLinkman: String, var restaurantName: String
                      , var startTime: Long, var tableNums: String, var isChoose: Boolean)