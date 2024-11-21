package com.sayx.hm_cloud.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
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
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.ConfigNameCallback
import com.sayx.hm_cloud.databinding.DialogEditControllerNameBinding

class EditControllerNameDialog : DialogFragment() {

    var name: String = ""
    var nameCallback: ConfigNameCallback? = null

    private lateinit var dataBinding: DialogEditControllerNameBinding

    @SuppressLint("GestureBackNavigation")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.DialogTheme)
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
        // 设置全屏
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_edit_controller_name, container, false)
        return dataBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
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
        initView()
    }

    private fun initView() {
        if (!TextUtils.isEmpty(name)) {
            dataBinding.etName.setText(name)
            dataBinding.etName.setSelection(name.length)
        }
        dataBinding.btnClose.setOnClickListener {
            dismiss()
        }
        dataBinding.btnSave.setOnClickListener {
            val text = dataBinding.etName.text?.toString()
            if (TextUtils.isEmpty(text)) {
                ToastUtils.showLong("请输入配置名称")
                return@setOnClickListener
            }
            if ((text?.length ?: 0) > 6) {
                ToastUtils.showLong("配置名称建议为1～6个字符")
                return@setOnClickListener
            }
            nameCallback?.onName(text!!)
            dismiss()
        }
        dataBinding.btnSave.isSelected = true
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.gravity = Gravity.CENTER
        windowParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.dimAmount = 0.0f
        windowParams?.flags = windowParams?.flags?.or(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window?.attributes = windowParams
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    companion object {

        fun show(
            activity: FragmentActivity,
            name: String,
            callback: ConfigNameCallback,
            tag: String? = EditControllerNameDialog::class.java.simpleName
        ) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    fragmentTransaction.show(fragment)
                } else {
                    val dialog = EditControllerNameDialog()
                    dialog.name = name
                    dialog.nameCallback = callback
                    fragmentTransaction.add(dialog, tag).commitAllowingStateLoss()
                }
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }

        fun hide(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragment =
                    fragmentManager.findFragmentByTag(EditControllerNameDialog::class.java.simpleName) as DialogFragment?
                fragment?.dismissAllowingStateLoss()
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}