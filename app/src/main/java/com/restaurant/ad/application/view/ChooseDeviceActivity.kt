package com.restaurant.ad.application.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.restaurant.ad.application.R
import com.restaurant.ad.application.mode.Api
import com.restaurant.ad.application.mode.Device
import com.restaurant.ad.application.mode.OkHttpManager
import kotlinx.android.synthetic.main.activity_choose_device.*

class ChooseDeviceActivity : AppCompatActivity() {

    lateinit var manager: OkHttpManager<List<Device>>
    val data = ArrayList<Device>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_choose_device)
        manager = OkHttpManager(lifecycle)
        manager.requestData(manager.retrofit.create(Api::class.java).getDeviceList(), {
            if (it != null) {
                this.data.clear()
                this.data.addAll(it)
            }
            recycleViewDevice.adapter?.notifyDataSetChanged()
        }, {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        recycleViewDevice.layoutManager = LinearLayoutManager(this)
        recycleViewDevice.adapter = Adapter(data, this)
        back_btn.setOnClickListener {
            finish()
        }
    }

    private class Adapter(private val dataList: ArrayList<Device>, private val content: Context)
        : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(content).inflate(R.layout.item_choose_device, parent, false)
            val text = view.findViewById<TextView>(R.id.deviceName)
            return ViewHolder(view, text)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = dataList[position].padNum
            holder.itemView.setOnClickListener {
                if (content is ChooseDeviceActivity) {
                    val intent = Intent()
                    intent.putExtra("padNum", dataList[position].padNum)
                    content.setResult(Activity.RESULT_OK, intent)
                    content.finish()
                }
            }
        }

    }

    private class ViewHolder(itemView: View, val text: TextView)
        : RecyclerView.ViewHolder(itemView)

}