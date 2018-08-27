package com.restaurant.ad.application.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.restaurant.ad.application.R
import com.restaurant.ad.application.mode.Api
import com.restaurant.ad.application.mode.OkHttpManager
import com.restaurant.ad.application.mode.User
import kotlinx.android.synthetic.main.activity_login_setting.*

class LoginActivity : AppCompatActivity() {

    var requestMap = HashMap<String, String>()
    lateinit var manager: OkHttpManager<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)// 设置全屏
        setContentView(R.layout.activity_login_setting)
        back_btn.setOnClickListener {
            finish()
        }
        manager = OkHttpManager(lifecycle)
        btnLogin.setOnClickListener {
            val userName = editUserName.text.toString()
            val userPass = editUserPass.text.toString()
            if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPass)) {
                Toast.makeText(this, "请输入账号或者密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requestMap["userName"] = userName
            requestMap["userPwd"] = userPass
            userLogin()
        }
    }

    private fun userLogin() {
        manager.requestData(manager.retrofit.create(Api::class.java).userLogin(requestMap), {
            startActivity(Intent(this, RestaurantConfigActivity::class.java))
            finish()
        }, {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }
}