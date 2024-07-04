package com.sayx.hm_cloud.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
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
import com.sayx.hm_cloud.callback.OnTypeListener
import com.sayx.hm_cloud.databinding.DialogControllerTypeBinding

class ControllerTypeDialog : DialogFragment() {

    private lateinit var dataBinding: DialogControllerTypeBinding

    var listener: OnTypeListener? = null

    @SuppressLint("GestureBackNavigation")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.DialogTheme)
        dialog.setCanceledOnTouchOutside(false)
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
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_controller_type, container, false)
        dataBinding.lifecycleOwner = this
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog?.window
        window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            it.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            val insetsController = WindowCompat.getInsetsController(it, it.decorView)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // 水滴屏处理
                it.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        dataBinding.btnGamepadType.setOnClickListener {
            listener?.onGamepadType()
        }
        dataBinding.btnKeyboardType.setOnClickListener {
            listener?.onKeyboardType()
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.dimAmount = 0.8f
        window?.attributes = windowParams
    }

    companion object {
        fun hideDialog(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragment =
                    fragmentManager.findFragmentByTag(ControllerTypeDialog::class.java.simpleName) as DialogFragment?
                fragment?.dismissAllowingStateLoss()
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }

        fun showDialog(activity: FragmentActivity, listener: OnTypeListener) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentByTag(ControllerTypeDialog::class.java.simpleName)
                if (fragment != null) {
                    fragmentTransaction.show(fragment)
                } else {
                    val dialog = ControllerTypeDialog()
                    dialog.listener = listener
                    fragmentTransaction.add(dialog, ControllerTypeDialog::class.simpleName).commitAllowingStateLoss()
                }
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}