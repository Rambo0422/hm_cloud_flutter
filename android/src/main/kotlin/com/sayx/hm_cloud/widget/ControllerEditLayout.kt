package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.EditCallback
import com.sayx.hm_cloud.callback.TextWatcherImp
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewControllerEditBinding
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.model.MessageEvent
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus

class ControllerEditLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewControllerEditBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.view_controller_edit,
        this,
        true
    )

    private var callback: EditCallback? = null

    private var keyInfo: KeyInfo? = null

    var controllerType: AppVirtualOperateType? = null
        set(value) {
            field = value
            dataBinding.btnAddRouletteKey.visibility = if (value == AppVirtualOperateType.APP_STICK_XBOX) GONE else VISIBLE
        }

    init {

        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
        // 退出按钮
        dataBinding.btnExitEdit.setOnClickListener {
            callback?.onExitEdit()
        }
        // 添加按键
        dataBinding.btnAddKey.setOnClickListener {
            callback?.onAddKey()
        }
        // 添加组合按键
        dataBinding.btnAddCombineKey.setOnClickListener {
            callback?.onAddCombineKey()
        }
        // 添加轮盘按键
        dataBinding.btnAddRouletteKey.setOnClickListener {
            callback?.onAddRouletteKey()
        }
        // 展示更多
        dataBinding.btnEditMore.setOnClickListener {
            showMore(it)
        }
        // 编辑单个入口
        dataBinding.btnEdit.setOnClickListener {
            keyInfo?.let {
                EventBus.getDefault().post(MessageEvent("editKey", arg = it))
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 保存
        dataBinding.btnSaveEdit.setOnClickListener {
            callback?.onSaveEdit()
        }
        // 编辑菜单隐藏/显示
        dataBinding.btnEditFold.setOnClickListener {
            val selected = !dataBinding.btnEditFold.isSelected
            dataBinding.btnEditFold.isSelected = selected
            if (selected) {
                foldMenu()
            } else {
                unfoldMenu()
            }
        }
        dataBinding.btnEditFold.isSelected = false
    }

    @SuppressLint("InflateParams")
    private fun showMore(anchor: View) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.popup_edit_more, null)
        val width = AutoSizeUtils.dp2px(context,120f)
        val popupWindow = PopupWindow(view, width, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.apply {
            isOutsideTouchable = true
            isFocusable = true
            isTouchable = true
            showAsDropDown(anchor, -width / 2 + anchor.width / 2, AutoSizeUtils.dp2px(context, 5f), Gravity.CENTER)
        }
        view.findViewById<View>(R.id.btn_edit_name).setOnClickListener {
            callback?.onEditName()
            popupWindow.dismiss()
        }
        view.findViewById<View>(R.id.btn_restore).setOnClickListener {
            popupWindow.dismiss()
            callback?.onRestoreDefault()
        }
        view.findViewById<View>(R.id.btn_delete_key).setOnClickListener {
            keyInfo?.let {
                popupWindow.dismiss()
                setKeyInfo(null)
                callback?.onDeleteKey()
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
    }

    private fun unfoldMenu() {
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            -dataBinding.layoutBoard.height.toFloat(),
            0.0f
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    private fun foldMenu() {
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            0.0f,
            -dataBinding.layoutBoard.height.toFloat()
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    fun showLayout() {
        controllerStatus = ControllerStatus.Edit
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            -dataBinding.root.height.toFloat(),
            0.0f
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerImp() {
            override fun onAnimationStart(animation: Animator) {
                visibility = VISIBLE
            }
        })
        animator.start()
    }

    fun hideLayout(listener: AnimatorListenerImp) {
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            0.0f,
            -dataBinding.root.height.toFloat()
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(listener)
        animator.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            showLayout()
        }
    }

    fun setCallback(callback: EditCallback) {
        this.callback = callback
    }

    fun setKeyInfo(keyInfo: KeyInfo?) {
        this.keyInfo = keyInfo
    }
}