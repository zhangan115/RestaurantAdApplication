package com.restaurant.ad.application.view

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import com.restaurant.ad.application.R
import com.restaurant.ad.application.mode.Api
import com.restaurant.ad.application.mode.City
import com.restaurant.ad.application.mode.OkHttpManager
import kotlinx.android.synthetic.main.activity_restaurant_config.*

class RestaurantConfigActivity : AppCompatActivity() {

    private lateinit var cityRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_restaurant_config)
        initView()
        initData()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun initView() {
        val layout = nav_view.getHeaderView(0) as LinearLayout
        cityRecyclerView = layout.findViewById(R.id.recycleView_city)
        cityRecyclerView.layoutManager = LinearLayoutManager(this)
        recycleView_restaurant.layoutManager = LinearLayoutManager(this)
        btnFilter.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }
        back_btn.setOnClickListener {
            finish()
        }
        btnSearch.setOnClickListener {
            searchRes()
        }
    }

    private fun initData() {
        getCityList()
    }

    private fun getCityList() {
        val cityManager = OkHttpManager<List<City>>(lifecycle)
        cityManager.requestData(cityManager.retrofit.create(Api::class.java).cityList(), {

        }, {

        })
    }

    private fun searchRes() {
        val searchStr = editSearch.text.toString()
        if (TextUtils.isEmpty(searchStr)) return
        val searchManager = OkHttpManager<List<String>>(lifecycle)
        val requestMap = HashMap<String, String>()
        requestMap[""] = searchStr
        searchManager.requestData(searchManager.retrofit.create(Api::class.java).restaurantList(requestMap), {

        }, {

        })
    }

}
