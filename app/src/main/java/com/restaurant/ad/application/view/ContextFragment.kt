package com.restaurant.ad.application.view

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.restaurant.ad.application.R
import com.restaurant.ad.application.app.GlideApp
import com.restaurant.ad.application.mode.VideoFileMode
import com.restaurant.ad.application.utils.GlideBlurTransformation
import kotlinx.android.synthetic.main.fragment_context.*
import java.lang.ref.WeakReference

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

class ContextFragment : Fragment(), VideoFileMode.DownLoadHandle.DownLoadCallBack {

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
    private var videoMode: VideoFileMode? = null

    private var videoPlayPosition = 0
    private var isPlaying = false

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
        if (isVideo) {
            image_view.visibility = View.GONE
            video_view.visibility = View.VISIBLE
            videoMode = VideoFileMode(url)
            iv_call_background.setImageDrawable(this.resources.getDrawable(R.drawable.app_home_bg))
            videoMode?.downLoadFile(this)
        } else {
            image_view.visibility = View.VISIBLE
            video_view.visibility = View.GONE
            GlideApp.with(this).load(url)
                    .placeholder(R.drawable.shape_image_background)
                    .into(image_view)
            Glide.with(this).load(url)
                    .apply(RequestOptions.bitmapTransform(GlideBlurTransformation(activity))).into(iv_call_background);
        }
        lazyLoad()
    }

    override fun downSuccess() {

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
        if (isVideo) {
            video_view.setOnErrorListener { _, _, _ ->
                controlHandler?.sendEmptyMessage(1)//开始计时
                true
            }
            video_view.setOnPreparedListener {
                it?.setOnInfoListener { _, what, _ ->
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
                        video_view.setBackgroundColor(Color.TRANSPARENT);
                    true
                }
                it?.setVolume(0f, 0f)
            }
            video_view.setOnCompletionListener {
                video_view.stopPlayback()
                controlHandler?.sendEmptyMessage(1)//开始计时
            }
            if (videoMode != null) {
                if (videoMode!!.isDownLoadSuccess()) {
                    video_view.setVideoPath(videoMode!!.getUrlVideoLocal().absolutePath)
                } else {
                    video_view.setVideoPath(url)
                }
//                video_view.setVideoPath(url)
            }
            video_view.start()
        } else {
            controlHandler?.sendEmptyMessageDelayed(1, imageTime)//开始计时
        }
    }

    override fun onResume() {
        super.onResume()
        if (videoMode != null) {
            video_view.requestFocus()
        }
        if (isVideo && !video_view.isPlaying && isPlaying) {
            video_view.seekTo(videoPlayPosition)
            video_view.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (video_view.isPlaying) {
            isPlaying = true
            videoPlayPosition = video_view.currentPosition
        } else {
            isPlaying = false
        }
    }


    override fun onDetach() {
        super.onDetach()
        controlHandler = null
    }


    class ControlHandler internal constructor(private val activity: WeakReference<FragmentActivity>?) : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg != null) {
                Log.d("ZA", "next")
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
                        putBoolean(ARG_PARAM2, url.endsWith(".mp4"))
                        putLong(ARG_PARAM3, 5000)
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
