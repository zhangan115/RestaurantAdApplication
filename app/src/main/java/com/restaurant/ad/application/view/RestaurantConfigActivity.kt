package com.restaurant.ad.application.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import com.restaurant.ad.application.R
import com.restaurant.ad.application.mode.*
import com.restaurant.ad.application.view.adapter.ResAdapter
import com.restaurant.ad.application.view.adapter.RestaurantChoose
import com.restaurant.ad.application.view.adapter.SimpleTreeRecyclerAdapter
import com.restaurant.ad.application.view.adapter.TableAdapter
import com.restaurant.ad.application.widget.tree.Node
import kotlinx.android.synthetic.main.activity_restaurant_config.*

class RestaurantConfigActivity : AppCompatActivity() {

    private lateinit var cityRecyclerView: RecyclerView
    private val restaurantList = ArrayList<Restaurant>()
    private val tableList = ArrayList<String>()
    private val nodeList = ArrayList<Node<String, City>>()
    private var currentNode: Node<String, City>? = null
    private var currentRestaurant: Restaurant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                setResToView()
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
        btnBind.setOnClickListener {
            val deviceNum = editDeviceNum.text.toString()
            val tableNum = editTableNum.text.toString()
            bindDeviceTable(deviceNum, tableNum)
        }
        val tableAdapter = TableAdapter(tableList, this)
        recycleView_table_num.layoutManager = GridLayoutManager(this, 5)
        recycleView_table_num.adapter = tableAdapter
    }

    private fun bindDeviceTable(deviceNum: String?, tableNum: String?) {
        if (TextUtils.isEmpty(deviceNum) || TextUtils.isEmpty(tableNum)) {
            Toast.makeText(this, "请输入设备编号和桌号", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentRestaurant == null) {
            Toast.makeText(this, "请选择餐厅", Toast.LENGTH_SHORT).show()
            return
        }
        val requestMap = HashMap<String, String>()
        requestMap["padNum"] = deviceNum!!
        requestMap["tableNum"] = tableNum!!
        requestMap["restaurantId"] = currentRestaurant!!.restaurantId.toString()
        val binManager = OkHttpManager<String>(lifecycle)
        binManager.requestData(binManager.retrofit.create(Api::class.java).tableNumSetting(requestMap), {
            TableMode.saveDeviceNum(deviceNum)
            TableMode.saveTableNum(tableNum)
            TableMode.saveRestaurantNum(currentRestaurant?.restaurantId.toString())
            Toast.makeText(this, "绑定成功", Toast.LENGTH_SHORT).show()
            sendBroadcast(Intent("requestAdList"))
            finish()
        }, {
            if (!TextUtils.isEmpty(it)) {
                Toast.makeText(this, it!!, Toast.LENGTH_SHORT).show()
            }
        })
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
     * 展示餐厅信息
     */
    private fun setResToView() {
        if (currentRestaurant == null) {
            tableList.clear()
            recycleView_table_num.adapter?.notifyDataSetChanged()
            tv_table_num.visibility = View.VISIBLE
            return
        } else {
            tableList.clear()
            tableList.addAll(currentRestaurant!!.tableNums.split(","))
        }
        if (tableList.isEmpty()) {
            tv_table_num.visibility = View.VISIBLE
        } else {
            tv_table_num.visibility = View.GONE
            recycleView_table_num.adapter?.notifyDataSetChanged()
        }
        res_name.text = currentRestaurant?.restaurantName
    }
}
