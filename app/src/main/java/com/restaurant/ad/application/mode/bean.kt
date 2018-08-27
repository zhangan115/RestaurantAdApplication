package com.restaurant.ad.application.mode

data class User(var realName: String, var userName: String, var userPhone: String)

data class City(var city: String, var cityId: Long, var shouzimu: String, var parentCityId: Long)