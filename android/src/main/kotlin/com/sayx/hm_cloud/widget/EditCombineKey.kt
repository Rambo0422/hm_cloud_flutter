package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.AddKeyListener
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.HideListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewEditCombineKeyBinding
import com.sayx.hm_cloud.utils.AppSizeUtils
import java.util.UUID

class EditCombineKey @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewEditCombineKeyBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.view_edit_combine_key,
        this,
        true
    )

    private var keyList: HashMap<Int, KeyInfo?> = hashMapOf()

    private var full = false

    var isShow = false

    var onHideListener: HideListener? = null

    var addKeyListener: AddKeyListener? = null

    var keyInfo: KeyInfo? = null

    init {

        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
        dataBinding.ivBack.setOnClickListener {
            hideLayout()
        }
        dataBinding.btnSaveEdit.setOnClickListener {
            saveKey()
        }
        // 初始化添加按键
        for (index in 0..6) {
            val view: View = if (index % 2 == 0) {
                // 按钮
                val editKeyView = EditKeyView(context)
                keyList[index] = null
                editKeyView.setOnClickListener {
                    val data = editKeyView.getData()
                    if (data != null) {
                        keyList[index] = null
                        editKeyView.setData(null)
                        full = false
                    }
                }
                editKeyView
            } else {
                // 加号
                AppCompatImageView(context).also {
                    it.setImageResource(R.drawable.img_add_combine)
                }
            }
            val layoutParams = if (index % 2 == 0) {
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } else {
                FrameLayout.LayoutParams(SizeUtils.dp2px(25f), SizeUtils.dp2px(25f))
            }
            dataBinding.layoutKey.addView(view, layoutParams)
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
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            showBoard()
        }
    }

    fun showBoard() {
        isShow = true
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
                controllerStatus = ControllerStatus.Combine
            }
        })
        animator.start()
    }

    private fun hideLayout() {
        isShow = false
        val animator = ObjectAnimator.ofFloat(
            dataBinding.root,
            "translationY",
            0.0f,
            -dataBinding.root.height.toFloat()
        )
        animator.duration = 500L
        animator.interpolator = AccelerateInterpolator()
        animator.addListener(object : AnimatorListenerImp() {
            override fun onAnimationEnd(animation: Animator) {
                clear()
                onHideListener?.onHide()
            }
        })
        animator.start()
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

    fun addKey(keyInfo: KeyInfo) {
        LogUtils.d("addKey:$keyInfo, keyList:$keyList")
        if (full) {
            return
        }
        keyList.keys.forEach {
            if (keyList[it] == null) {
                keyList[it] = keyInfo
                val view = dataBinding.layoutKey[it]
                if (view is EditKeyView) {
                    view.setData(keyInfo)
                    if (it == dataBinding.layoutKey.childCount - 1) {
                        full = true
                    }
                    return
                }
            }
        }
    }

    private fun clear() {
        for (index in 0..<dataBinding.layoutKey.childCount) {
            val view = dataBinding.layoutKey[index]
            if (view is EditKeyView) {
                keyList[index] = null
                view.setData(null)
            }
        }
        full = false
    }

    private fun saveKey() {
        LogUtils.d("saveKey:$keyList")
        val keyInfoList: MutableList<KeyInfo> = mutableListOf()
        keyList.keys.forEach {
            val keyInfo = keyList[it]
            if (keyInfo != null) {
                keyInfoList.add(keyInfo)
            }
        }
        if (keyInfoList.size < 2) {
            ToastUtils.showLong(R.string.save_at_least_two)
            return
        }
        if (keyInfo == null) {
            addKeyListener?.onAddKey(
                KeyInfo(
                    UUID.randomUUID(),
                    AppSizeUtils.DESIGN_WIDTH / 2 - 15,
                    AppSizeUtils.DESIGN_HEIGHT / 2 - 15,
                    30,
                    60,
                    context.getString(R.string.combine_key),
                    0,
                    KeyType.KEY_COMBINE,
                    60,
                    0,
                    0,
                    30,
                    composeArr = keyInfoList
                )
            )
        } else {
            keyInfo?.changeList(composeArr = keyInfoList)
            addKeyListener?.onUpdateKey()
        }
        hideLayout()
    }

    fun setCombineKeyInfo(keyInfo: KeyInfo?) {
        this.keyInfo = keyInfo
        keyInfo?.let {
            val keyInfoList = it.composeArr
            if (!keyInfoList.isNullOrEmpty()) {
                for (index in keyInfoList.indices) {
                    val info = keyInfoList[index]
                    keyList[index * 2] = info
                    val view = dataBinding.layoutKey[index * 2]
                    if (view is EditKeyView) {
                        view.setData(info)
                    }
                }
                full = keyInfoList.size == 4
            }
        }
    }
}