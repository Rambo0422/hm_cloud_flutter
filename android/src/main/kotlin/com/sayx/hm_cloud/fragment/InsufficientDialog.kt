package com.sayx.hm_cloud.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.dialog.PayDialog
import com.sayx.hm_cloud.utils.TVUtils

/**
 * 时长不足续费弹窗
 */
class InsufficientDialog : DialogFragment(), DialogInterface.OnKeyListener {

    private lateinit var tvTime: TextView
    private var countdownTimer: CountDownTimer? = null
    private var exit = false
    private var payDialog: PayDialog? = null

    companion object {
        fun newInstance(): InsufficientDialog {
            val insufficientDialog = InsufficientDialog()
            return insufficientDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnKeyListener(this)
        val view = inflater.inflate(R.layout.insufficient_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 倒计时 5 分钟（300000 毫秒）
        tvTime = view.findViewById(R.id.time)
        startCountdown(5 * 60 * 1000)
    }

    override fun onStart() {
        super.onStart()
        // 获取 Dialog 的窗口对象
        val window = dialog?.window
        if (window != null) {
            // 设置宽度和高度为全屏
            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT

            // 设置窗口为全屏显示，去除边距
            params.gravity = Gravity.CENTER
            window.attributes = params

            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#BF000000")))
        }
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                // 下机退出 (其实只要跳转首页就行了)
                if (!exit) {
                    exit = true
                    context?.let {
                        TVUtils.toTVHome(it)
                    }
                }
            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
                dismiss()
            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
                if (payDialog == null) {
                    payDialog = PayDialog.newInstance()
                    payDialog?.show(parentFragmentManager, "PayDialog")
                    payDialog?.setPayOderListener(object : PayDialog.PayOderListener {
                        override fun onPaySuccess() {
                            this@InsufficientDialog.dismiss()
                        }

                        override fun onDismiss() {
                            payDialog = null
                        }
                    })
                }
            }
            return true
        }

        return true
    }

    override fun onDestroyView() {
        dialog?.setOnKeyListener(null)
        countdownTimer?.cancel()
        countdownTimer = null
        super.onDestroyView()
    }

    private fun startCountdown(millisInFuture: Long) {
        // 初始化倒计时器
        countdownTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 格式化剩余时间为 mm:ss
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val timeLeft = String.format("%02d:%02d", minutes, seconds)
                tvTime.text = timeLeft
            }

            override fun onFinish() {
                context?.let {
                    TVUtils.toTVHome(it)
                }
            }
        }

        // 启动倒计时
        countdownTimer?.start()
    }
}