package com.sayx.hm_cloud.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.NoOperateListener
import com.sayx.hm_cloud.databinding.DialogNoOperateOfflineBinding
import com.sayx.hm_cloud.utils.TimeUtils
import java.util.Timer
import java.util.TimerTask

class NoOperateOfflineDialog : DialogFragment() {

    private var enableCancel = false

    private lateinit var dataBinding: DialogNoOperateOfflineBinding

    private var offlineTimer: Timer? = null

    private var countTime = 30 * 1000L

    private var listener: NoOperateListener? = null

    @SuppressLint("GestureBackNavigation")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.DialogTheme)
        dialog.setCanceledOnTouchOutside(enableCancel)
        dialog.setCancelable(enableCancel)
        dialog.setOnKeyListener(fun(_: DialogInterface, keyCode: Int, _: KeyEvent): Boolean {
            return when (keyCode) {
                KeyEvent.KEYCODE_BACK -> true
                else -> false
            }
        })
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_no_operate_offline, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog?.window
        window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            val insetsController = WindowCompat.getInsetsController(it, it.decorView)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // 水滴屏处理
                it.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        dataBinding.layout.setOnClickListener {

        }
        initDialog()
    }

    private fun initDialog() {
        dataBinding.btnLeft.text = getString(R.string.exit_game)
        dataBinding.btnRight.text = getString(R.string.continue_game)
        dataBinding.btnRight.requestFocus()
        dataBinding.tvMessage.text = buildText()
        dataBinding.btnLeft.setOnClickListener {
            dismiss()
            listener?.overtime()
        }
        dataBinding.btnRight.setOnClickListener {
            dismiss()
            listener?.continuePlay()
        }
        startTimer()
    }

    override fun onResume() {
        super.onResume()
        dataBinding.btnRight.requestFocus()
    }

    private fun startTimer() {
        try {
            if (offlineTimer != null) {
                offlineTimer?.cancel()
                offlineTimer = null
            }
            offlineTimer = Timer()
            offlineTimer?.schedule(object : TimerTask() {
                override fun run() {
                    if (countTime <= 0) {
                        offlineTimer?.cancel()
                        offlineTimer = null
                        listener?.overtime()
                        dismiss()
                    } else {
                        countTime -= 1000L
                        dataBinding.tvMessage.post {
                            dataBinding.tvMessage.text = buildText()
                        }
                    }
                }
            }, 0L, 1000L)
        } catch (e: Exception) {
            LogUtils.e("startTimer:${e.message}")
        }
    }

    private fun buildText(): CharSequence {
        val text = "长时间未操作${TimeUtils.getCountTime(countTime)}后自动下机"
        val spannableString = SpannableString(text)
        val start = text.indexOf("作") + 1
        val end = text.lastIndexOf("后")
        val colorSpan = ForegroundColorSpan(Color.parseColor("#FFC6EC4B"))
        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.gravity = Gravity.CENTER
        windowParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.dimAmount = 0.01f
        window?.attributes = windowParams
    }

    override fun onDestroy() {
        try {
            if (offlineTimer != null) {
                offlineTimer?.cancel()
                offlineTimer = null
            }
        } catch (e: Exception) {
           LogUtils.e("onDestroy:${e.message}")
        }
        super.onDestroy()
    }

    companion object {
        fun show(
            activity: FragmentActivity,
            tag: String? = NoOperateOfflineDialog::class.java.simpleName,
            listener: NoOperateListener? = null
        ) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    fragmentTransaction.show(fragment)
                } else {
                    val dialog = NoOperateOfflineDialog()
                    dialog.listener = listener
                    fragmentTransaction.add(dialog, NoOperateOfflineDialog::class.simpleName)
                        .commitAllowingStateLoss()
                }
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}