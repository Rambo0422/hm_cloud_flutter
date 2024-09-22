package com.sayx.hm_cloud.dialog

import android.annotation.SuppressLint
import android.app.Dialog
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
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.databinding.DialogGameErrorBinding

class GameErrorDialog : DialogFragment() {

    var title: String? = null
    var subTitle: String? = null
    var leftBtnClickListener: View.OnClickListener? = null

    private lateinit var dataBinding: DialogGameErrorBinding

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
            DataBindingUtil.inflate(inflater, R.layout.dialog_game_error, container, false)
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
        showTitle()
        showSubTitle()
        dataBinding.btnLeft.setOnClickListener(leftBtnClickListener)
        dataBinding.btnLeft.setOnTouchListener { v, _ ->
//            leftBtnClickListener?.onClick(v)
            return@setOnTouchListener false
        }
        dataBinding.btnLeft.requestFocusFromTouch()
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.gravity = Gravity.CENTER
        windowParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.dimAmount = 0.01f
        windowParams?.flags  = windowParams?.flags?.or(WindowManager.LayoutParams.FLAG_FULLSCREEN)
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

    private fun showTitle() {
        dataBinding.tvTitle.text = title
    }

    private fun showSubTitle() {
        dataBinding.tvContent.text = subTitle
    }

    class Builder(private val activity: FragmentActivity) {

        private var title: String? = null
        private var subTitle: String? = null
        private var leftListener: View.OnClickListener? = null
        private var appCommonDialog: GameErrorDialog? = null

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setSubTitle(subTitle: String?): Builder {
            this.subTitle = subTitle
            return this
        }

        fun setLeftButtonClickListener(listener: View.OnClickListener?): Builder {
            this.leftListener = listener
            return this
        }

        fun build(): Builder {
            appCommonDialog = GameErrorDialog()
            appCommonDialog?.title = title
            appCommonDialog?.subTitle = subTitle
            appCommonDialog?.leftBtnClickListener = leftListener
            return this
        }

        fun show(tag: String? = GameErrorDialog::class.java.simpleName) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    fragmentTransaction.show(fragment)
                } else {
                    appCommonDialog?.let {
                        fragmentTransaction.add(it, tag).commitAllowingStateLoss()
                    }
                }
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }

    companion object {
        fun hide(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragment =
                    fragmentManager.findFragmentByTag(GameErrorDialog::class.java.simpleName) as DialogFragment?
                fragment?.dismissAllowingStateLoss()
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}