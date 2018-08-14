package com.restaurant.ad.application

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val data = ArrayList<String>()
    private var controlHandler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        data.add("1")
        data.add("2")
        data.add("3")
        data.add("4")
        noScrollViewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        ControlThread().start()
        controlHandler = ControlHandler()
    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return VideoFragment()
        }

        override fun getCount(): Int {
            return data.size
        }
    }

    inner class ControlThread : Thread() {
        private var position = 0
        override fun run() {
            while (true) {
                Thread.sleep(2000)
                controlHandler?.sendEmptyMessage(position)
                position++
            }
        }
    }

    inner class ControlHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg != null) {
                noScrollViewPager.setCurrentItem(msg.what, true)
            }
        }
    }
}
