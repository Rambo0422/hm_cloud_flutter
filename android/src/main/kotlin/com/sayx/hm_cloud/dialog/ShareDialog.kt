package com.sayx.hm_cloud.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
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
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.DialogDismissListener
import com.sayx.hm_cloud.databinding.DialogShareBinding

class ShareDialog : DialogFragment() {

    private lateinit var dataBinding: DialogShareBinding

    // 剪切板
    private val clipboardManager: ClipboardManager? by lazy {
        context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    }

    var listener : DialogDismissListener? = null

    @SuppressLint("GestureBackNavigation")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.DialogTheme)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
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
            DataBindingUtil.inflate(inflater, R.layout.dialog_share, container, false)
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
        }
        dataBinding.btnWechat.setOnClickListener {
            GameManager.invokeMethod("shareToWx")
            dismiss()
        }
        dataBinding.btnQq.setOnClickListener {
            GameManager.invokeMethod("shareToQq")
            dismiss()
        }
        dataBinding.btnCopy.setOnClickListener {
            clipboardManager?.setPrimaryClip(ClipData.newPlainText(null, "https://play.3ayx.net/"))
            ToastUtils.showShort(R.string.clip_success)
            dismiss()
        }
        dataBinding.ivHandle.setOnClickListener {
            dismiss()
        }
        GameManager.invokeMethod("sharePageShow")
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.gravity = Gravity.END
        windowParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.dimAmount = 0.1f
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDialogDismiss()
    }

    companion object {
        fun show(activity: FragmentActivity, listener: DialogDismissListener?, tag: String? = ShareDialog::class.java.simpleName) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    fragmentTransaction.show(fragment)
                } else {
                    val dialog = ShareDialog()
                    dialog.listener = listener
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
                    fragmentManager.findFragmentByTag(ShareDialog::class.java.simpleName) as DialogFragment?
                fragment?.dismissAllowingStateLoss()
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}