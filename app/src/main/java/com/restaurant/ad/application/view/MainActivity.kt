package com.restaurant.ad.application.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.restaurant.ad.application.R
import com.restaurant.ad.application.app.App
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity() {

    private val data = ArrayList<String>()
    private val broadcastReceiver = NextBroadcastReceive()
    private var currentPosition: Int = 1
    private var timeHandler: TimeHandler? = null
    private val timeBr: TimeBroadcastReceive = TimeBroadcastReceive()

    private var currentTime = 0L
    private var openSetting = 0
    private var isCalling = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_main)
        initTime()

        data.add("http://cs.vmovier.com/Uploads/cover/2017-02-23/58aec1f65a07f_cut.jpeg@607h_1080w_1e_1c.jpg")
        data.add("http://qiniu-video3.vmoviercdn.com/5b6d535e14e32.mp4")
        data.add("http://cs.vmovier.com/Uploads/cover/2017-02-23/58aebbf9c9d39_cut.jpeg@607h_1080w_1e_1c.jpg")
        data.add("http://qiniu-video3.vmoviercdn.com/5b710c565d522.mp4")
        data.add("http://cs.vmovier.com/Uploads/cover/2016-07-12/5784e8de070ec_cut.jpeg@607h_1080w_1e_1c.jpg")
        data.add("http://qiniu-video5.vmoviercdn.com/5b63129a25b63.mp4")
        data.add("https://cs.vmovier.com/Uploads/cover/2018-08-15/5b740b73d90ca_cut.jpeg")
        data.add("http://mp4.vjshi.com/2018-08-25/d7726fed26f1ffa33bf7cf6d438236e2.mp4")

        val height = resources.displayMetrics.widthPixels / 16 * 9
        noScrollViewPager.layoutParams.height = height
        noScrollViewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        noScrollViewPager.addOnPageChangeListener(MyPageChangeListener())
        noScrollViewPager.offscreenPageLimit = 3
        noScrollViewPager.setCurrentItem(1, false)
        val intentFilter = IntentFilter("next")
        registerReceiver(broadcastReceiver, intentFilter)

        llTime.setOnClickListener {
            if (currentTime == 0L) {
                currentTime = System.currentTimeMillis()
                return@setOnClickListener
            } else {
                if (System.currentTimeMillis() - currentTime < 1000L) {
                    openSetting++
                    currentTime = System.currentTimeMillis()
                } else {
                    openSetting = 0
                    currentTime = 0
                }
            }
            if (openSetting == 8) {
                openSetting = 0
                currentTime = 0
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        tv_call.setOnClickListener {
            if (isCalling) return@setOnClickListener
            object : CountDownTimer(15 * 1000L, 1 * 1000L) {

                override fun onTick(millisUntilFinished: Long) {
                    val time = millisUntilFinished / 1000
                    if (time >= 10) {
                        tv_call.text = "呼叫中\n${time}秒"
                    } else {
                        tv_call.text = "呼叫中\n0${time}秒"
                    }
                }

                override fun onFinish() {
                    isCalling = false
                    tv_call.text = "服务"
                    tv_call.background = App.instance.resources.getDrawable(R.drawable.call_background)
                }
            }.start()
            isCalling = true
            tv_call.background = this.resources.getDrawable(R.drawable.call_ing_background)
            //todo 呼叫服务
        }

        setTime()
//        TimeThread().start()
    }

    private fun initTime() {
        timeHandler = TimeHandler(WeakReference(this))
        registerReceiver(timeBr, IntentFilter("Time"))
    }

    inner class TimeThread : Thread() {
        override fun run() {
            super.run()
            while (true) {
                Thread.sleep(60 * 1000)
                timeHandler?.sendEmptyMessage(1)
            }
        }
    }

    class TimeHandler(private val activity: WeakReference<MainActivity>?) : Handler() {

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            val intent = Intent("Time")
            activity?.get()?.sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
            unregisterReceiver(timeBr)
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

    inner class TimeBroadcastReceive : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            setTime()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setTime() {
        tvTimeMin.format12Hour = "hh:mm"
        tvTimeMin.format24Hour = "HH:mm"
        tvTimeYear.format12Hour = "yyyy年MM月dd日EEEE"
        tvTimeYear.format24Hour = "yyyy年MM月dd日EEEE"
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
