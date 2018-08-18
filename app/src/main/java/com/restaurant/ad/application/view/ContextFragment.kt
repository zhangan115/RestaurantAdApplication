package com.restaurant.ad.application.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.restaurant.ad.application.BuildConfig
import com.restaurant.ad.application.R
import kotlinx.android.synthetic.main.fragment_context.*
import java.lang.ref.WeakReference

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

class ContextFragment : Fragment() {

    //Fragment的View加载完毕的标记
    private var isViewCreated: Boolean = false
    //Fragment对用户可见的标记
    private var isUIVisible: Boolean = false
    //传过来的value
    private var url: String? = null//视频或者图片地址
    private var isVideo: Boolean = true//是否为视频
    private var imageTime: Long = 5000//图片倒计时
    private var notLoad: Boolean = false//是否加载
    //倒计时控制handler
    private var controlHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_PARAM1)
            isVideo = it.getBoolean(ARG_PARAM2)
            imageTime = it.getLong(ARG_PARAM3)
            notLoad = it.getBoolean(ARG_PARAM4)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_context, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            controlHandler = ControlHandler(WeakReference(activity!!))
        }
        isViewCreated = true
        tv_name.text = url
        lazyLoad()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        //isVisibleToUser这个boolean值表示:该Fragment的UI 用户是否可见
        if (isVisibleToUser) {
            isUIVisible = true
            lazyLoad()
        } else {
            isUIVisible = false
        }
    }

    private fun lazyLoad() {
        if (isViewCreated && isUIVisible) {
            loadData()
            //数据加载完毕,恢复标记,防止重复加载
        }
    }

    private fun loadData() {
        if (notLoad) {
            return
        }
        if (BuildConfig.DEBUG)
            Log.d("za", "===${url}播放视频或者展示图片===")
        if (isVideo) {
            //todo 视频播放
        } else {
            //todo 加载图片
            controlHandler?.sendEmptyMessageDelayed(1, imageTime)//开始计时
        }
    }


    override fun onDetach() {
        super.onDetach()
        controlHandler = null
    }


    class ControlHandler internal constructor(private val activity: WeakReference<FragmentActivity>?) : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg != null) {
                activity?.get()?.sendBroadcast(Intent("next"))
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String, isVideo: Boolean, imageTime: Long) =
                ContextFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, url)
                        putBoolean(ARG_PARAM2, isVideo)
                        putLong(ARG_PARAM3, imageTime)
                    }
                }

        @JvmStatic
        fun newInstance(url: String) =
                ContextFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, url)
                        putBoolean(ARG_PARAM2, false)
                        putLong(ARG_PARAM3, 15)
                    }
                }

        @JvmStatic
        fun newInstance(notLoad: Boolean) =
                ContextFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_PARAM4, notLoad)
                    }
                }
    }
}
