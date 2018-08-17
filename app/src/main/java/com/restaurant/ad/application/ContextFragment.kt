package com.restaurant.ad.application

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_context.*
import java.lang.ref.WeakReference

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class ContextFragment : Fragment() {

    private var url: String? = null
    private var isVideo: Boolean = true
    private var imageTime: Long = 15
    private var listener: OnFragmentInteractionListener? = null
    private var controlHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_PARAM1)
            isVideo = it.getBoolean(ARG_PARAM2)
            imageTime = it.getLong(ARG_PARAM3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_context, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null) {
            controlHandler = ControlHandler(WeakReference(activity!!))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_name.text = url
        if (isVideo) {

        } else {
            ControlThread().start()
        }
    }

    fun onShowFinish() {
        listener?.onFragmentInteraction()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null

    }


    interface OnFragmentInteractionListener {

        fun onFragmentInteraction()

    }

    inner class ControlThread : Thread() {
        override fun run() {
            while (true) {
                Thread.sleep(imageTime)
                controlHandler?.sendEmptyMessage(1)
            }
        }
    }

    class ControlHandler internal constructor(private val activity: WeakReference<FragmentActivity>?) : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg != null) {
                val intent = Intent("next")
                activity?.get()?.sendBroadcast(intent)
            }
        }
    }

    inner class NextBroadcastReceive : BroadcastReceiver() {

        override fun onReceive(contenxt: Context?, intent: Intent?) {
            if (intent != null) {
                onShowFinish()
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
    }
}
