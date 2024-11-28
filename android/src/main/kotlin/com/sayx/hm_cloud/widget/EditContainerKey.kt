package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.callback.AddKeyListener
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.HideListener
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewEditContainerKeyBinding
import com.sayx.hm_cloud.utils.AppSizeUtils
import me.jessyan.autosize.utils.AutoSizeUtils
import java.util.UUID

class EditContainerKey @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewEditContainerKeyBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.view_edit_container_key,
        this,
        true
    )

    private var keyList: HashMap<Int, KeyInfo?> = hashMapOf()

    private var full = false

    var isShow = false

    var onHideListener: HideListener? = null

    var addKeyListener: AddKeyListener? = null

    var keyInfo: KeyInfo? = null

    private var tempList: List<KeyInfo>? = null

    init {

        initView()

        visibility = INVISIBLE
    }

    private fun initView() {
        dataBinding.ivBack.setOnClickListener {
            val newData: MutableList<KeyInfo> = mutableListOf()
            keyList.keys.forEach {
                val keyInfo = keyList[it]
                if (keyInfo != null) {
                    newData.add(keyInfo)
                }
            }
            if (keyInfo != null) {
                val oldData = tempList
                val addList = newData.filter { info ->
                    info !in (oldData ?: listOf())
                }
                val removeList = oldData?.filter { info ->
                    info !in newData
                }
                // rou移除的数据会加回列表
                addKeyListener?.rouRemoveData(addList)
                // rou添加的数据会从列表移除
                addKeyListener?.rouAddData(removeList)
                keyInfo?.containerArr = oldData ?: listOf()
            } else {
                newData.forEach { info ->
                    addKeyListener?.onKeyRemove(info)
                }
            }
            hideLayout()
        }
        dataBinding.btnSaveEdit.setOnClickListener {
            saveKey()
        }
        // 初始化添加按键
        for (index in 0..9) {
            val editKeyView = EditKeyView(context)
            keyList[index] = null
            editKeyView.setOnClickListener {
                val data = editKeyView.getData()
                if (data != null) {
                    keyList[index] = null
                    editKeyView.setData(null)
                    full = false
                    addKeyListener?.onKeyRemove(data)
                    if (keyInfo != null) {
                        keyInfo?.containerArr = keyList.filter { item -> item.value != null }.map { item -> item.value!! }.toList()
                    }
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            when (index) {
                0 -> {
                    layoutParams.marginEnd = AutoSizeUtils.dp2px(context, 4.75f)
                }

                9 -> {
                    layoutParams.marginStart = AutoSizeUtils.dp2px(context, 4.75f)
                }

                else -> {
                    layoutParams.marginEnd = AutoSizeUtils.dp2px(context, 4.75f)
                    layoutParams.marginStart = AutoSizeUtils.dp2px(context, 4.75f)
                }
            }
            dataBinding.layoutKey.addView(editKeyView, layoutParams)
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
                controllerStatus = ControllerStatus.Roulette
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
                onHideListener?.onHide(keyInfo)
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
        if (full) {
            return
        }
        LogUtils.d("addKey:$keyInfo, keyList:$keyList")
        addKeyListener?.onKeyAdd(keyInfo)
        keyList.keys.forEach {
            if (keyList[it] == null) {
                keyList[it] = keyInfo
                val view = dataBinding.layoutKey[it]
                if (view is EditKeyView) {
                    view.setData(keyInfo)
                    if (it == dataBinding.layoutKey.childCount - 1) {
                        full = true
                    }
                }
                this@EditContainerKey.keyInfo?.containerArr = keyList.filter { item -> item.value != null }.map { item -> item.value!! }.toList()
                return
            }
        }
    }

    private fun clear() {
        for (index in 0..<dataBinding.layoutKey.childCount) {
            val editKeyView = dataBinding.layoutKey[index] as EditKeyView
            keyList[index] = null
            editKeyView.setData(null)
        }
        full = false
    }

    private fun saveKey() {
        LogUtils.d("saveKey:$keyList")
        val newData: MutableList<KeyInfo> = mutableListOf()
        keyList.keys.forEach {
            val keyInfo = keyList[it]
            if (keyInfo != null) {
                newData.add(keyInfo)
            }
        }
        LogUtils.d("keyInfoList:$newData")
        if (newData.size < 2) {
            ToastUtils.showLong(R.string.save_at_least_two)
            return
        }
        if (keyInfo != null) {
            val oldData = keyInfo!!.containerArr
            val addList = oldData?.filter { info ->
                info !in newData
            }
            val removeList = newData.filter { info ->
                info !in (oldData ?: listOf())
            }
            addKeyListener?.rouRemoveData(addList)
            addKeyListener?.rouAddData(removeList)
            keyInfo?.containerArr = newData
        } else {
            addKeyListener?.onAddKey(
                KeyInfo(
                    UUID.randomUUID(),
                    AppSizeUtils.DESIGN_WIDTH / 2 - 11,
                    AppSizeUtils.DESIGN_HEIGHT / 2 - 24,
                    22,
                    50,
                    "",
                    0,
                    KeyType.KEY_CONTAINER,
                    70,
                    0,
                    0,
                    48,
                    containerArr = newData
                )
            )
        }
        hideLayout()
    }

    fun setContainerKeyInfo(keyInfo: KeyInfo?) {
        this.keyInfo = keyInfo
        keyInfo?.let {
            val keyInfoList = it.containerArr
            tempList = keyInfoList
            if (!keyInfoList.isNullOrEmpty()) {
                for (index in keyInfoList.indices) {
                    val info = keyInfoList[index]
                    info.id = if (info.id == null) UUID.randomUUID() else info.id
                    keyList[index] = info
                    val view = dataBinding.layoutKey[index]
                    if (view is EditKeyView) {
                        view.setData(info)
                    }
                }
                full = keyInfoList.size == 8
            }
        }
    }
}