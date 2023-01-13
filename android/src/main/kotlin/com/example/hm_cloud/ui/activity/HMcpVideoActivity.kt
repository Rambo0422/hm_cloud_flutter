@file:Suppress("DEPRECATION")

package com.example.hm_cloud.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.hm_cloud.databinding.ActivityHmcpVideoBinding
import com.example.hm_cloud.manage.HmcpVideoManage
import com.example.hm_cloud.manage.MethodChannelManage
import com.example.hm_cloud.pluginconstant.ChannelConstant
import com.example.hm_cloud.ui.view.HorizontalSettingIcon

class HMcpVideoActivity : AppCompatActivity(), View.OnSystemUiVisibilityChangeListener {

    private lateinit var binding: ActivityHmcpVideoBinding

    companion object {
        const val REQUEST_CODE = 100
        const val FULL_RESULT_CODE = 956545

        fun startActivityForResult(activity: Activity) {
            val intent = Intent(activity, HMcpVideoActivity::class.java)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.decorView.setOnSystemUiVisibilityChangeListener(this)
        binding = ActivityHmcpVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MethodChannelManage.getInstance().invokeMethod(ChannelConstant.METHOD_START_SUCCESS)

        initView()
        initHmc()
    }

    private fun initView() {
        binding.buttonExitFull.setOnClickListener {
            exitFullScreen()
        }

        binding.switchSound.setOnCheckedChangeListener { _, _ ->
            MethodChannelManage.getInstance().invokeMethod(ChannelConstant.METHOD_CHANGE_SOUND)
        }

        initFloatView()
    }

    private fun initFloatView() {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val settingIcon = HorizontalSettingIcon(this)
        settingIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.rightDrawerLayout)
        }
        binding.layoutContent.addView(settingIcon, params)
    }

    private fun initHmc() {
        HmcpVideoManage.getInstance().apply {
            addView(binding.layoutHmcContainer)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onSystemUiVisibilityChange(visibility: Int) {
        setHideVirtualKey()
    }

    private fun setHideVirtualKey() {
        //保持布局状态
        var uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or  //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN or  //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        uiOptions = if (Build.VERSION.SDK_INT >= 19) {
            uiOptions or 0x00001000
        } else {
            uiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        }
        window.decorView.systemUiVisibility = uiOptions
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 屏蔽返回按钮
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun exitFullScreen() {
        // 移除 hmcpView
        HmcpVideoManage.getInstance().removeView()
        setResult(FULL_RESULT_CODE)
        finish()
    }
}