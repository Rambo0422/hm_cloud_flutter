package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.EditCallback
import com.sayx.hm_cloud.callback.TextWatcherImp
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewControllerEditBinding
import com.sayx.hm_cloud.model.KeyInfo

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
        // 添加按键
        dataBinding.btnAddCombineKey.setOnClickListener {
            callback?.onAddCombineKey()
        }
        // 添加按键
        dataBinding.btnAddRouletteKey.setOnClickListener {
            callback?.onAddRouletteKey()
        }
        // 还原默认
        dataBinding.btnRestoreEdit.setOnClickListener {
            callback?.onRestoreDefault()
        }
        // 保存
        dataBinding.btnSaveEdit.setOnClickListener {
            callback?.onSaveEdit()
        }
        // 增大按键
        dataBinding.btnAddKeySize.setOnClickListener {
            keyInfo?.let {
                if (it.zoom < 100) {
                    it.changeZoom(it.zoom + 10)
                    dataBinding.tvKeySize.text = String.format("%s", "${it.zoom}%")
                    callback?.onAddKeySize()
                }
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 缩小按键
        dataBinding.btnReduceKeySize.setOnClickListener {
            keyInfo?.let {
                if (it.zoom > 10) {
                    it.changeZoom(it.zoom - 10)
                    dataBinding.tvKeySize.text = String.format("%s", "${it.zoom}%")
                    callback?.onReduceKeySize()
                }
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 增加按键透明度
        dataBinding.btnAddKeyOpacity.setOnClickListener {
            keyInfo?.let {
                if (it.opacity < 100) {
                    it.changeOpacity(it.opacity + 10)
                    dataBinding.tvKeyOpacity.text = String.format("%s", "${it.opacity}%")
                    callback?.onAddKeyOpacity()
                }
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 减少按键透明度
        dataBinding.btnReduceKeyOpacity.setOnClickListener {
            keyInfo?.let {
                if (it.opacity > 10) {
                    it.changeOpacity(it.opacity - 10)
                    dataBinding.tvKeyOpacity.text = String.format("%s", "${it.opacity}%")
                    callback?.onReduceKeyOpacity()
                }
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 按键交互:点击
        dataBinding.btnClick.setOnClickListener {
            keyInfo?.let {
                it.click = 0
                dataBinding.btnClick.isSelected = true
                dataBinding.btnPress.isSelected = false
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 按键交互:长按
        dataBinding.btnPress.setOnClickListener {
            keyInfo?.let {
                it.click = 1
                dataBinding.btnClick.isSelected = false
                dataBinding.btnPress.isSelected = true
            } ?: ToastUtils.showLong(R.string.unselect_key)
        }
        // 删除按键
        dataBinding.btnDeleteKey.setOnClickListener {
            keyInfo?.let {
                setKeyInfo(null)
                callback?.onDeleteKey()
            } ?: ToastUtils.showLong(R.string.unselect_key)
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
        // 名称编辑
        dataBinding.etKeyName.addTextChangedListener(object : TextWatcherImp() {
            override fun afterTextChanged(s: Editable?) {
                keyInfo?.changeText(s?.toString())
                callback?.onTextChange()
            }
        })
        // 组合键，轮盘键编辑
        dataBinding.btnEditKey.setOnClickListener {
            keyInfo?.let {
                callback?.onEditCombine(it)
            }
        }
        // 初始状态
        dataBinding.btnClick.isSelected = true
        dataBinding.btnPress.isSelected = false
        dataBinding.btnEditFold.isSelected = false
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
        if (keyInfo != null) {
            dataBinding.tvKeySize.text = String.format("%s", "${keyInfo.zoom}%")
            dataBinding.tvKeyOpacity.text = String.format("%s", "${keyInfo.opacity}%")
            dataBinding.btnClick.isSelected = keyInfo.click == 0
            dataBinding.btnPress.isSelected = keyInfo.click != 0
            keyInfo.text?.let {
                dataBinding.etKeyName.setText(it)
                dataBinding.etKeyName.setSelection(it.length)
            }
            val nameable =
                keyInfo.type == KeyType.KEYBOARD_KEY || keyInfo.type == KeyType.GAMEPAD_SQUARE ||
                        keyInfo.type == KeyType.GAMEPAD_ROUND_MEDIUM || keyInfo.type == KeyType.GAMEPAD_ROUND_SMALL ||
                        keyInfo.type == KeyType.KEY_COMBINE || keyInfo.type == KeyType.GAMEPAD_COMBINE ||
                        keyInfo.type == KeyType.KEY_ROULETTE || keyInfo.type == KeyType.GAMEPAD_ROULETTE
            val clickable =
                keyInfo.type == KeyType.KEYBOARD_MOUSE_LEFT || keyInfo.type == KeyType.KEYBOARD_MOUSE_RIGHT ||
                        keyInfo.type == KeyType.KEYBOARD_MOUSE_UP || keyInfo.type == KeyType.KEYBOARD_MOUSE_DOWN ||
                        keyInfo.type == KeyType.KEYBOARD_MOUSE_MIDDLE || keyInfo.type == KeyType.KEYBOARD_KEY
            val editable =
                keyInfo.type == KeyType.KEY_COMBINE || keyInfo.type == KeyType.GAMEPAD_COMBINE ||
                        keyInfo.type == KeyType.KEY_ROULETTE || keyInfo.type == KeyType.GAMEPAD_ROULETTE
            dataBinding.btnClick.visibility = if (clickable) VISIBLE else GONE
            dataBinding.btnPress.visibility = if (clickable) VISIBLE else GONE
            dataBinding.keyInteract.visibility = if (clickable) VISIBLE else GONE
            dataBinding.keyName.visibility = if (nameable) VISIBLE else GONE
            dataBinding.etKeyName.visibility = if (nameable) VISIBLE else GONE
            dataBinding.btnEditKey.visibility = if (editable) VISIBLE else GONE
        } else {
            dataBinding.tvKeySize.text = String.format("%s", "${0}%")
            dataBinding.tvKeyOpacity.text = String.format("%s", "${0}%")
            dataBinding.btnClick.isSelected = true
            dataBinding.btnPress.isSelected = false
            dataBinding.etKeyName.setText("")
            dataBinding.btnClick.visibility = GONE
            dataBinding.btnPress.visibility = GONE
            dataBinding.keyInteract.visibility = GONE
            dataBinding.keyName.visibility = GONE
            dataBinding.etKeyName.visibility = GONE
            dataBinding.btnEditKey.visibility = GONE
        }
    }
}