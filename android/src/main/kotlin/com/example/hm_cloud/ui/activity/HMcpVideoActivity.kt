@file:Suppress("DEPRECATION")

package com.example.hm_cloud.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.hm_cloud.R
import com.example.hm_cloud.databinding.ActivityHmcpVideoBinding
import com.example.hm_cloud.manage.HmcpVideoManage
import com.haima.hmcp.Constants
import com.haima.hmcp.beans.Message
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.utils.CryptoUtils
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import org.json.JSONObject

class HMcpVideoActivity : AppCompatActivity(), View.OnSystemUiVisibilityChangeListener {

    private lateinit var hmcpVideoView: HmcpVideoView
    private lateinit var binding: ActivityHmcpVideoBinding

    companion object {
        private val TAG: String = HMcpVideoActivity::class.java.getSimpleName()
        fun start(context: Context) {
            val intent = Intent(context, HMcpVideoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
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

        initView()
        initHmc()
    }

    private fun initView() {
        binding.button.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.rightDrawerLayout)
        }

        binding.buttonExitFull.setOnClickListener {

        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            Log.e(TAG, "switchSound: $isChecked")
        }
    }

    private fun initHmc() {
        HmcpVideoManage.getInstance().addView(this, binding.layoutHmcContainer)
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
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        uiOptions = if (Build.VERSION.SDK_INT >= 19) {
            uiOptions or 0x00001000
        } else {
            uiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        }
        window.decorView.systemUiVisibility = uiOptions
    }
}