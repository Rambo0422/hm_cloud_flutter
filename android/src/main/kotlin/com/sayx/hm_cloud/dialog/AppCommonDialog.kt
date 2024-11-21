package com.sayx.hm_cloud.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.databinding.DialogCommonAppBinding

class AppCommonDialog : DialogFragment() {

    var title: CharSequence? = null
    var titleColor: Int? = null
    var subTitle: CharSequence? = null
    var subTitleColor: Int? = null
    var leftBtnText: CharSequence? = null
    var leftBtnClickListener: View.OnClickListener? = null
    var rightBtnText: CharSequence? = null
    @DrawableRes
    var rightBtnBg: Int? = null
    var rightBtnColor: Int? = null
    var rightBtnClickListener: View.OnClickListener? = null

    var enableCancel = false

    private lateinit var dataBinding: DialogCommonAppBinding

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
        // 设置全屏
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_common_app, container, false)
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
                it.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        showTitle()
        showSubTitle()
        showLeftBtn()
        showRightBtn()
        dataBinding.btnLeft.setOnClickListener(leftBtnClickListener)
//        dataBinding.btnLeft.setOnTouchListener { v, _ ->
////            leftBtnClickListener?.onClick(v)
//            return@setOnTouchListener false
//        }
        dataBinding.btnRight.setOnClickListener(rightBtnClickListener)
//        dataBinding.btnRight.setOnTouchListener { v, _ ->
////            rightBtnClickListener?.onClick(v)
//            return@setOnTouchListener false
//        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.gravity = Gravity.CENTER
        windowParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.dimAmount = 0.8f
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
        dataBinding.tvTitle.setTextColor(titleColor ?: Color.WHITE)
    }

    private fun showSubTitle() {
        if (subTitle != null) {
            dataBinding.tvSubtitle.text = subTitle
            dataBinding.tvSubtitle.setTextColor(subTitleColor ?: Color.parseColor("#FFC6EC4B"))
        } else {
            dataBinding.tvSubtitle.visibility = View.GONE
        }
    }

    private fun showLeftBtn() {
        if (leftBtnText != null) {
            dataBinding.btnLeft.text = leftBtnText
        } else {
            dataBinding.btnLeft.visibility = View.GONE
        }
    }

    private fun showRightBtn() {
        dataBinding.btnRight.text = rightBtnText
        rightBtnBg?.let {
            dataBinding.btnRight.background = AppCompatResources.getDrawable(dataBinding.btnRight.context, it)
        }
        rightBtnColor?.let {
            dataBinding.btnRight.setTextColor(it)
        }
        dataBinding.btnRight.isSelected = true
//        dataBinding.btnRight.requestFocusFromTouch()
    }

    class Builder(private val activity: FragmentActivity) {

        private var title: CharSequence? = null
        private var titleColor: Int? = null
        private var subTitle: CharSequence? = null
        private var subTitleColor: Int? = null
        private var leftBtnText: CharSequence? = null
        private var leftBtnClickListener: View.OnClickListener? = null
        private var rightBtnText: CharSequence? = null
        private var rightBtnColor: Int? = null
        @DrawableRes
        private var rightBtnBg: Int? = null
        private var rightBtnClickListener: View.OnClickListener? = null
        private var enableCancel: Boolean = false
        private var appCommonDialog: AppCommonDialog? = null

        fun setTitle(title: CharSequence?, titleColor: Int? = null): Builder {
            this.title = title
            this.titleColor = titleColor
            return this
        }

        fun setSubTitle(subTitle: CharSequence?, subTitleColor: Int? = null): Builder {
            this.subTitle = subTitle
            this.subTitleColor = subTitleColor
            return this
        }

        fun setLeftButton(leftBtnText: CharSequence?, clickListener: View.OnClickListener? = null): Builder {
            this.leftBtnText = leftBtnText
            this.leftBtnClickListener = clickListener
            return this
        }

        fun setRightButton(rightBtnText: CharSequence?, rightBtnColor: Int? = null, clickListener: View.OnClickListener? = null): Builder {
            this.rightBtnText = rightBtnText
            this.rightBtnColor = rightBtnColor
            this.rightBtnClickListener = clickListener
            return this
        }

        fun setRightButtonBg(@DrawableRes resourceId: Int): Builder {
            this.rightBtnBg = resourceId
            return this
        }

        fun setEnableCancel(enableCancel:Boolean): Builder {
            this.enableCancel = enableCancel
            return this
        }

        fun build(): Builder {
            appCommonDialog = AppCommonDialog()
            appCommonDialog?.title = title
            appCommonDialog?.titleColor = titleColor
            appCommonDialog?.subTitle = subTitle
            appCommonDialog?.subTitleColor = subTitleColor
            appCommonDialog?.leftBtnText = leftBtnText
            appCommonDialog?.rightBtnText = rightBtnText
            appCommonDialog?.rightBtnBg = rightBtnBg
            appCommonDialog?.rightBtnColor = rightBtnColor
            appCommonDialog?.leftBtnClickListener = leftBtnClickListener
            appCommonDialog?.rightBtnClickListener = rightBtnClickListener
            appCommonDialog?.enableCancel = enableCancel
            return this
        }

        fun show(tag: String? = AppCommonDialog::class.java.simpleName) {
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
        fun hideDialog(activity: FragmentActivity, tag: String? = AppCommonDialog::class.java.simpleName) {
            val fragmentManager = activity.supportFragmentManager
            if (!fragmentManager.isDestroyed) {
                val fragment = fragmentManager.findFragmentByTag(tag) as DialogFragment?
                fragment?.dismissAllowingStateLoss()
            } else {
                LogUtils.e("FragmentManager has been destroyed")
            }
        }
    }
}