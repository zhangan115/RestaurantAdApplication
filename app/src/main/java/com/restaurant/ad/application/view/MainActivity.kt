package com.restaurant.ad.application.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.restaurant.ad.application.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val data = ArrayList<String>()
    private val broadcastReceiver = NextBroadcastReceive()
    private var currentPosition: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_main)
        data.add("http://qiniu-video5.vmoviercdn.com/5b7b7cae8348e.mp4")
        data.add("http://qiniu-video5.vmoviercdn.com/5b7a3c0e8e5ba.mp4")
        data.add("http://cs.vmovier.com/Uploads/cover/2017-02-23/58aec1f65a07f_cut.jpeg@607h_1080w_1e_1c.jpg")
        data.add("http://qiniu-video3.vmoviercdn.com/5b6d535e14e32.mp4")
        data.add("http://cs.vmovier.com/Uploads/cover/2017-02-23/58aebbf9c9d39_cut.jpeg@607h_1080w_1e_1c.jpg")
        data.add("http://qiniu-video3.vmoviercdn.com/5b710c565d522.mp4")
        data.add("http://cs.vmovier.com/Uploads/cover/2016-07-12/5784e8de070ec_cut.jpeg@607h_1080w_1e_1c.jpg")
        data.add("http://qiniu-video5.vmoviercdn.com/5b63129a25b63.mp4")
        data.add("https://cs.vmovier.com/Uploads/cover/2018-08-15/5b740b73d90ca_cut.jpeg")
        val height = resources.displayMetrics.widthPixels / 16 * 9
        noScrollViewPager.layoutParams.height = height
        noScrollViewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        noScrollViewPager.addOnPageChangeListener(MyPageChangeListener())
        noScrollViewPager.offscreenPageLimit = 3
        noScrollViewPager.setCurrentItem(1, false)
        val intentFilter = IntentFilter("next")
        registerReceiver(broadcastReceiver, intentFilter)

        llTime.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                ContextFragment.newInstance(true)
            } else if (position > 0 && position <= data.size) {
                ContextFragment.newInstance(data[position - 1])
            } else {
                ContextFragment.newInstance(true)
            }
        }

        override fun getCount(): Int {
            return data.size + 2
        }
    }

    inner class NextBroadcastReceive : BroadcastReceiver() {

        override fun onReceive(contenxt: Context?, intent: Intent?) {
            if (intent != null) {
                currentPosition = noScrollViewPager.currentItem
                ++currentPosition
                noScrollViewPager.setCurrentItem(currentPosition, true)
            }
        }
    }

    private inner class MyPageChangeListener : ViewPager.OnPageChangeListener {
        private var mPosition: Int = 0
        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (mPosition == noScrollViewPager.adapter!!.count - 1) {
                    noScrollViewPager.setCurrentItem(1, false)
                } else if (mPosition == 0) {
                    noScrollViewPager.setCurrentItem(noScrollViewPager.adapter!!.count - 2, false)
                }
            }
        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

        }

        override fun onPageSelected(position: Int) {
            mPosition = position
        }
    }
}
