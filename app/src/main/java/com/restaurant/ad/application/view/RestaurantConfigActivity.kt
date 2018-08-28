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
import com.restaurant.ad.application.mode.Restaurant
import com.restaurant.ad.application.view.adapter.ResAdapter
import com.restaurant.ad.application.view.adapter.RestaurantChoose
import com.restaurant.ad.application.view.adapter.SimpleTreeRecyclerAdapter
import com.restaurant.ad.application.widget.tree.Node
import kotlinx.android.synthetic.main.activity_restaurant_config.*

class RestaurantConfigActivity : AppCompatActivity() {

    private lateinit var cityRecyclerView: RecyclerView
    private val restaurantList = ArrayList<Restaurant>()
    private val nodeList = ArrayList<Node<String, City>>()
    private var currentNode: Node<String, City>? = null
    private var currentRestaurant: Restaurant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_restaurant_config)
        initView()
        getCityList()
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
        val adapter = ResAdapter(restaurantList, this, object : RestaurantChoose {

            override fun resChoose(res: Restaurant) {
                currentRestaurant = res
                getRestaurantInfo()
            }

        })
        recycleView_restaurant.adapter = adapter
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


    /**
     * 获取城市列表
     */
    private fun getCityList() {
        val cityManager = OkHttpManager<List<City>>(lifecycle)
        val requestMap = HashMap<String, String>()
        requestMap["parentCityId"] = "0"
        cityManager.requestData(cityManager.retrofit.create(Api::class.java).cityList(requestMap), {
            if (it != null && it.isNotEmpty()) {
                nodeList.clear()
                for (city1 in it) {
                    val node1 = Node(city1.cityId.toString(), city1.parentCityId.toString(), city1.city, city1)
                    node1.level = 1
                    if (city1.childList != null) {
                        for (city2 in city1.childList!!) {
                            val node2 = Node(city2.cityId.toString(), city2.parentCityId.toString(), city2.city, city2)
                            node2.level = 2
                            nodeList.add(node2)
                            if (city2.childList != null) {
                                for (city3 in city2.childList!!) {
                                    val node3 = Node(city3.cityId.toString(), city3.parentCityId.toString(), city3.city, city3)
                                    node3.level = 3
                                    nodeList.add(node3)
                                }
                            }
                        }
                    }
                    nodeList.add(node1)
                }
                val mAdapter = SimpleTreeRecyclerAdapter(cityRecyclerView, this,
                        nodeList, 0, R.drawable.navigation_icon_top, R.drawable.navigation_icon_down)
                cityRecyclerView.adapter = mAdapter
                mAdapter.setOnTreeNodeClickListener { node, position ->
                    if (node.isLeaf) {
                        currentNode = node as Node<String, City>?
                        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                            drawer_layout.closeDrawer(GravityCompat.START)
                        }
                        for (n in nodeList) {
                            n.isChecked = TextUtils.equals(n.id, currentNode?.id)
                        }
                        mAdapter.notifyDataSetChanged()
                        searchRes()
                    }
                }
            }
        }, {})
    }

    /**
     * 查找餐厅
     */
    private fun searchRes() {
        val requestMap = HashMap<String, String>()
        val searchStr = editSearch.text.toString()
        if (!TextUtils.isEmpty(searchStr)) {
            requestMap["restaurantName"] = searchStr
        }
        val searchManager = OkHttpManager<List<Restaurant>>(lifecycle)
        if (currentNode != null) {
            when (currentNode?.level) {
                0 -> {
                    requestMap["provinceId"] = currentNode?.id.toString()
                }
                1 -> {
                    requestMap["cityId"] = currentNode?.id.toString()
                }
                2 -> {
                    requestMap["districtId"] = currentNode?.id.toString()
                }
            }
        }
        restaurantList.clear()
        recycleView_restaurant.adapter?.notifyDataSetChanged()
        searchManager.requestData(searchManager.retrofit.create(Api::class.java).restaurantList(requestMap), {
            if (it != null && it.isNotEmpty()) {
                restaurantList.addAll(it)
                recycleView_restaurant.adapter?.notifyDataSetChanged()
            } else {

            }
        }, {

        })
    }

    /**
     * 获取餐厅信息
     */
    private fun getRestaurantInfo() {
        if (currentRestaurant == null) return
        val resNumManager = OkHttpManager<Restaurant>(lifecycle)
        val requestMap = HashMap<String, String>()
        requestMap["restaurantId"] = currentRestaurant?.restaurantId.toString()
        resNumManager.requestData(resNumManager.retrofit.create(Api::class.java).restaurantTableNum(requestMap), {}, {})
    }

}
