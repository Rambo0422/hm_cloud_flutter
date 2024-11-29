package com.sayx.hm_cloud.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.sayx.hm_cloud.R

/**
 * 手柄操作提示弹窗
 */
class HandleOperationDialog : DialogFragment(), DialogInterface.OnKeyListener {

    companion object {
        fun newInstance(): HandleOperationDialog {
            val dialog = HandleOperationDialog()
            return dialog
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
        val view = inflater.inflate(R.layout.fragment_handle_operation, container, false)
        return view
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
        }
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                dismiss()
            }
        }
        return true
    }
}