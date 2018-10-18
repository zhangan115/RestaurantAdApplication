package com.restaurant.ad.application.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SynthesizerListener
import com.restaurant.ad.application.R
import com.restaurant.ad.application.mode.*
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val data = ArrayList<AdDataBean>()
    private val broadcastReceiver = NextBroadcastReceive()
    private var currentPosition: Int = 1
    private var timeHandler: TimeHandler? = null

    private var currentTime = 0L
    private var openSetting = 0
    private var isCalling = false
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    // 语音合成对象
    private var mTts: SpeechSynthesizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_main)
        initTime()
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this) {}
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        noScrollViewPager.adapter = viewPagerAdapter
        noScrollViewPager.addOnPageChangeListener(MyPageChangeListener())
        noScrollViewPager.offscreenPageLimit = 1
        noScrollViewPager.setCurrentItem(1, false)
        val intentFilter = IntentFilter()
        intentFilter.addAction("requestAdList")
        intentFilter.addAction("next")
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
        ivCallBg.visibility = View.INVISIBLE
        callLayout.setOnClickListener {
            if (isCalling) return@setOnClickListener
            if (callService()) {
                ivCallBg.visibility = View.VISIBLE
                val anim = ivCallBg.drawable as AnimationDrawable
                anim.start()
                ivCall.setImageDrawable(applicationContext.resources.getDrawable(R.drawable.call_btn2))
                object : CountDownTimer(6 * 1000L, 1 * 1000L) {

                    override fun onTick(millisUntilFinished: Long) {
                        val time = millisUntilFinished / 1000
                        if (time >= 10) {
                            tvCall.text = "呼叫中\n${time}秒"
                        } else {
                            tvCall.text = "呼叫中\n0${time}秒"
                        }
                    }

                    override fun onFinish() {
                        isCalling = false
                        anim.stop()
                        ivCallBg.visibility = View.INVISIBLE
                        tvCall.text = "服务"
                        ivCall.setImageDrawable(applicationContext.resources.getDrawable(R.drawable.call_background))
                    }
                }.start()
                isCalling = true
            }
        }
        setTime()
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        fullScreen()
    }

    private fun fullScreen() {
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    /**
     * 呼叫服务
     */
    private fun callService(): Boolean {
        val callManager = OkHttpManager<String>(lifecycle)
        val requestMap = HashMap<String, String>()
        if (!TextUtils.isEmpty(TableMode.getDeviceNum())) {
            requestMap["padNum"] = TableMode.getDeviceNum()!!
            callManager.requestData(callManager.retrofit.create(Api::class.java).insertCallLog(requestMap), {}, {})
        }
        val tableName = TableMode.getTableNum()
        if (TextUtils.isEmpty(tableName)) {
            Toast.makeText(this, "请配置桌号", Toast.LENGTH_SHORT).show()
            return false
        }
        val call = "${tableName}号桌的客人呼叫，请提供服务"
        if (mTts == null) return false
        mTts!!.startSpeaking(call, object : SynthesizerListener {

            override fun onBufferProgress(p0: Int, p1: Int, p2: Int, p3: String?) {
                Log.d("za", "onBufferProgress")
            }

            override fun onSpeakBegin() {
                Log.d("za", "onSpeakBegin")
            }

            override fun onSpeakProgress(p0: Int, p1: Int, p2: Int) {
                Log.d("za", "onSpeakProgress")
            }

            override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
                Log.d("za", "onEvent")
            }

            override fun onSpeakPaused() {
                Log.d("za", "onSpeakPaused")
            }

            override fun onSpeakResumed() {
                Log.d("za", "onSpeakResumed")
            }

            override fun onCompleted(p0: SpeechError?) {
                Log.d("za", "onCompleted")
            }

        })
        return true
    }

    private fun initTime() {
        timeHandler = TimeHandler(WeakReference(this))
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                ContextFragment.newInstance(true)
            } else if (position > 0 && position <= data.size) {
                ContextFragment.newInstance(data[position - 1].url, data[position - 1].isVideo, data[position - 1].time)
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
                if (TextUtils.equals(intent.action, "next")) {
                    currentPosition = noScrollViewPager.currentItem
                    ++currentPosition
                    noScrollViewPager.setCurrentItem(currentPosition, true)
                } else if (TextUtils.equals(intent.action, "requestAdList")) {
                    requestAdList()
                }
            }
        }
    }

    private fun requestAdList() {
        val manager = OkHttpManager<List<AdListBean>>(lifecycle)
        val padNum = TableMode.getDeviceNum()
        if (TextUtils.isEmpty(padNum)) return
        val requestMap = HashMap<String, String>()
        requestMap["padNum"] = padNum!!
        manager.requestData(manager.retrofit.create(Api::class.java).advertisingList(requestMap), {
            if (it != null && it.isNotEmpty()) {
                TableMode.saveAdList(Gson().toJson(it))
                setAdList(it)
            } else {
                val adList = Gson().fromJson<List<AdListBean>>(TableMode.getAdList(), object : TypeToken<List<AdListBean>>() {}.type)
                setAdList(adList)
            }
        }, {
            val adList = Gson().fromJson<List<AdListBean>>(TableMode.getAdList(), object : TypeToken<List<AdListBean>>() {}.type)
            setAdList(adList)
        })
    }

    private fun setAdList(it: List<AdListBean>?) {
        if (it == null) {
            return
        }
        data.clear()
        for (ad in it) {
            if (ad.type == 1) {
                val imageList = ArrayList<String>()
                if (!TextUtils.isEmpty(ad.image1)) {
                    imageList.add(ad.image1!!)
                }
                if (!TextUtils.isEmpty(ad.image2)) {
                    imageList.add(ad.image2!!)
                }
                if (!TextUtils.isEmpty(ad.image3)) {
                    imageList.add(ad.image3!!)
                }
                for (image in imageList) {
                    data.add(AdDataBean(image, AD_SHOW_TIME / imageList.size.toLong(), false))
                }
            } else {
                if (!TextUtils.isEmpty(ad.videoUrl)) {
                    data.add(AdDataBean(ad.videoUrl!!, 0L, true))
                }
            }
        }
        if (data.isNotEmpty()) {
            ivHomeBackground.visibility = View.GONE
            viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
            noScrollViewPager.adapter = viewPagerAdapter
            noScrollViewPager.addOnPageChangeListener(MyPageChangeListener())
            noScrollViewPager.offscreenPageLimit = 1
            noScrollViewPager.setCurrentItem(1, false)
            VideoFileMode.cleanVideoFile(data)
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


    @AfterPermissionGranted(Companion.REQUEST_EXTERNAL)
    fun checkPermission() {
        if (!EasyPermissions.hasPermissions(this.applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this, getString(R.string.request_permissions),
                    Companion.REQUEST_EXTERNAL, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            requestAdList()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, @NonNull perms: List<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, @NonNull perms: List<String>) {
        if (requestCode == Companion.REQUEST_EXTERNAL) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.need_save_setting))
                        .setTitle(getString(R.string.request_permissions))
                        .setPositiveButton(getString(R.string.sure))
                        .setNegativeButton(getString(R.string.cancel))
                        .setRequestCode(Companion.REQUEST_EXTERNAL)
                        .build()
                        .show()
            }
        }
    }

    companion object {
         const val REQUEST_EXTERNAL = 10 //内存卡权限
    }
}
