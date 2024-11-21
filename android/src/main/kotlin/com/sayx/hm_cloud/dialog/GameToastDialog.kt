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
import com.sayx.hm_cloud.databinding.DialogGameToastBinding

class GameToastDialog : DialogFragment() {

    var title: String? = null
    var subtitle: String? = null
    var iconDrawable: Int? = null

    private lateinit var dataBinding: DialogGameToastBinding

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
            DataBindingUtil.inflate(inflater, R.layout.dialog_game_toast, container, false)
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
        dataBinding.root.setOnClickListener {
            dismiss()
        }
        dataBinding.tvTitle.text = title
        dataBinding.tvSubtitle.text = subtitle
        iconDrawable?.let {
            dataBinding.tvTitle.setDrawableLeft(it)
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.gravity = Gravity.CENTER
        windowParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.dimAmount = 0.0f
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
        dataBinding.root.postDelayed({
            dismiss()
        }, 1000L)
    }



    class Builder(private val activity: FragmentActivity) {

        private var title: String? = null
        private var subTitle: String? = null
        private var drawable: Int? = null
        private var dialog: GameToastDialog? = null

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setSubTitle(subTitle: String?): Builder {
            this.subTitle = subTitle
            return this
        }

        fun setDrawable(drawable: Int): Builder {
            this.drawable = drawable
            return this
        }

        fun build(): Builder {
            dialog = GameToastDialog()
            dialog?.title = title
            dialog?.subtitle = subTitle
            dialog?.iconDrawable = drawable
            return this
        }

        fun show(tag: String? = GameToastDialog::class.java.simpleName) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    fragmentTransaction.show(fragment)
                } else {
                    dialog?.let {
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
                    fragmentManager.findFragmentByTag(GameToastDialog::class.java.simpleName) as DialogFragment?
                fragment?.dismissAllowingStateLoss()
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}