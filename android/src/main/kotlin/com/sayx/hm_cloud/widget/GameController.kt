package com.sayx.hm_cloud.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.ControllerEventCallback
import com.sayx.hm_cloud.callback.OnEditClickListener
import com.sayx.hm_cloud.callback.OnKeyEventListener
import com.sayx.hm_cloud.callback.OnKeyTouchListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.callback.OnRockerOperationListener
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ViewGameControllerBinding
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.Direction
import com.sayx.hm_cloud.model.DirectionMode
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.ViewUtils
import kotlin.math.abs

class GameController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewGameControllerBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.view_game_controller,
        this,
        true
    )

    // 初始数据：1，默认数据；2，用户自定义数据
    private val keyboardKeys: MutableList<KeyInfo> = mutableListOf()
    private val gamepadKeys: MutableList<KeyInfo> = mutableListOf()

    // 编辑数据：
    // 1，首次进入时为初始数据，可编辑
    // 2，编辑退出，还原初始数据
    // 3，编辑还原默认，获取默认数据，替换编辑数据为默认数据，等待继续编辑
    // 4，编辑保存，将当前编辑数据保存
    private val editKeyboardKeys: MutableList<KeyInfo> = mutableListOf()
    private val editGamepadKeys: MutableList<KeyInfo> = mutableListOf()

    private var keyboardViews: ArrayList<View> = arrayListOf()
    private var gamepadViews: ArrayList<View> = arrayListOf()

    var listener: OnEditClickListener? = null
    var keyEventListener: OnKeyEventListener? = null
    var rockerListener: OnRockerOperationListener? = null

    var controllerCallback: ControllerEventCallback? = null

    private var currentKey: KeyInfo? = null
        set(value) {
            field = value
//            LogUtils.d("currentKey:${value?.id}")
        }

    var controllerType: AppVirtualOperateType = AppVirtualOperateType.NONE
        set(value) {
//            LogUtils.d("change controllerType = $value")
            if (value == field) {
                return
            }
            field = value
            when (value) {
                AppVirtualOperateType.APP_STICK_XBOX -> {
                    GameManager.lastControllerType = value
                    showGamepad()
                }

                AppVirtualOperateType.APP_KEYBOARD -> {
                    GameManager.lastControllerType = value
                    showKeyboard()
                }

                else -> {
                    hideAllKey()
                }
            }
        }

    // 编辑模式下，通过蒙板，让GameView无法操作
    var maskEnable: Boolean = false
        set(value) {
            field = value
            dataBinding.layoutMask.isClickable = value
            dataBinding.layoutMask.isFocusable = value
            dataBinding.layoutMask.clearLine()
            for (index in 0..<childCount) {
                val view = get(index)
                if (view is RouletteKeyView) {
                    view.showRoulette = value
                    view.invalidate()
                } else if (view.visibility == VISIBLE) {
                    view.invalidate()
                }
            }
        }

    init {
        dataBinding.layoutMask.isClickable = false
        dataBinding.layoutMask.isFocusable = false
    }

    private fun showGamepad() {
        LogUtils.d("showGamepad:${gamepadKeys.size}")
        try {
            changViewVisibility(keyboardViews, INVISIBLE)
            changViewVisibility(gamepadViews, VISIBLE)
            if (gamepadKeys.isEmpty()) {
                controllerCallback?.getGamepadData()
            } else {
                LogUtils.d("showGamepad:${gamepadViews.size}")
                // 加载Views
                if (gamepadViews.size == 0) {
                    initGamepad(gamepadKeys)
                }
            }
        } catch (e: Exception) {
            LogUtils.e(e.message)
        }
    }

    private fun showKeyboard() {
        LogUtils.d("showKeyboard:${keyboardKeys.size}")
        try {
            changViewVisibility(gamepadViews, INVISIBLE)
            changViewVisibility(keyboardViews, VISIBLE)
            if (keyboardKeys.isEmpty()) {
                controllerCallback?.getKeyboardData()
            } else {
                LogUtils.d("showKeyboard:${keyboardViews.size}")
                // 加载Views
                if (keyboardViews.size == 0) {
                    initKeyboard(keyboardKeys)
                }
            }
        } catch (e: Exception) {
            LogUtils.e(e.message)
        }
    }

    private fun initGamepad(keyInfoList: List<KeyInfo>) {
        LogUtils.d("initGamepad:${keyInfoList}")
        editGamepadKeys.clear()
        removeAllViews(gamepadViews)
        for (item in keyInfoList) {
            val keyInfo = item.copy()
            editGamepadKeys.add(keyInfo)
            when (keyInfo.type) {
                KeyType.GAMEPAD_SQUARE, KeyType.GAMEPAD_ELLIPTIC, KeyType.GAMEPAD_ROUND_MEDIUM,
                KeyType.GAMEPAD_ROUND_SMALL -> {
                    addKeyButton(keyInfo)
                }

                KeyType.ROCKER_RIGHT, KeyType.ROCKER_LEFT -> {
                    addRocker(keyInfo)
                }

                KeyType.ROCKER_CROSS -> {
                    addCrossRocker(keyInfo)
                }

                KeyType.GAMEPAD_COMBINE -> {
                    addCombineKey(keyInfo)
                }

                KeyType.GAMEPAD_ROULETTE -> {
                    addRouletteKey(keyInfo)
                }

                else -> {
                    LogUtils.e("initGamepad:$keyInfo")
                }
            }
        }
    }

    private fun initKeyboard(keyInfoList: List<KeyInfo>) {
        LogUtils.d("initKeyboard:${keyInfoList}")
        editKeyboardKeys.clear()
        removeAllViews(keyboardViews)
        for (item in keyInfoList) {
            val keyInfo = item.copy()
            editKeyboardKeys.add(keyInfo)
            when (keyInfo.type) {
                KeyType.KEYBOARD_KEY, KeyType.KEYBOARD_MOUSE_UP, KeyType.KEYBOARD_MOUSE_DOWN,
                KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                    addKeyButton(keyInfo)
                }

                KeyType.ROCKER_LETTER, KeyType.ROCKER_ARROW -> {
                    addRocker(keyInfo)
                }

                KeyType.KEY_COMBINE -> {
                    addCombineKey(keyInfo)
                }

                KeyType.KEY_ROULETTE -> {
                    addRouletteKey(keyInfo)
                }

                else -> {
                    LogUtils.e("initGamepad:$keyInfo")
                }
            }
        }
    }

    /**
     * 创建并添加十字摇杆控件到操作面板
     */
    private fun addCrossRocker(keyInfo: KeyInfo): RockerView {
//        LogUtils.d("addCrossRocker:$keyInfo")
        val rockerView = RockerView(context)
        rockerView.setBackgroundBitmap(
            ContextCompat.getDrawable(
                context,
                R.drawable.img_rocker_cross_default
            )
        )
        rockerView.setOnClickListener {
            LogUtils.d("addCrossRocker:$keyInfo")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            }
        }
        rockerView.setOnShakeListener(
            DirectionMode.DIRECTION_8,
            object : RockerView.OnShakeListener {
                override fun onStart() {
                }

                override fun direction(direction: Direction?) {
                    when (direction) {
                        Direction.DIRECTION_LEFT -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_left
                                )
                            )
                        }

                        Direction.DIRECTION_UP -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_up
                                )
                            )
                        }

                        Direction.DIRECTION_RIGHT -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_right
                                )
                            )
                        }

                        Direction.DIRECTION_DOWN -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_down
                                )
                            )
                        }

                        Direction.DIRECTION_UP_LEFT -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_left_up
                                )
                            )
                        }

                        Direction.DIRECTION_UP_RIGHT -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_right_up
                                )
                            )
                        }

                        Direction.DIRECTION_DOWN_LEFT -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_left_down
                                )
                            )
                        }

                        Direction.DIRECTION_DOWN_RIGHT -> {
                            rockerView.setBackgroundBitmap(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.img_rocker_cross_right_down
                                )
                            )
                        }

                        else -> {}
                    }
                    rockerListener?.onRockerDirection(keyInfo, direction)
                }

                override fun onFinish() {
                    rockerView.setBackgroundBitmap(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.img_rocker_cross_default
                        )
                    )
                    rockerListener?.onRockerDirection(keyInfo, Direction.DIRECTION_CENTER)
                }
            })
        rockerView.positionChangeListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey = keyInfo
                    currentKey?.let { info ->
                        info.changePosition(
                            AppSizeUtils.reconvertWidthSize(left),
                            AppSizeUtils.reconvertHeightSize(top),
                        )
                        listener?.onEditKeyClick(info)
                    }
                }
            }
        }

        rockerView.tag = keyInfo.id
        rockerView.alpha = keyInfo.opacity / 100f
        val layoutParams = FrameLayout.LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(rockerView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(rockerView)
        }
        addView(rockerView, layoutParams)
        invalidate()
        rockerView.post {
//            val x = if (keyInfo.left < AppSizeUtils.DESIGN_WIDTH / 2) {
//                AppSizeUtils.convertViewSize(keyInfo.left)
//            } else {
//                val rightMargin = AppSizeUtils.DESIGN_WIDTH - keyInfo.left - keyInfo.getKeyWidth()
//                width - AppSizeUtils.convertViewSize(keyInfo.getKeyWidth() + rightMargin)
//            }
//            rockerView.x = x.toFloat()
//            rockerView.y = AppSizeUtils.convertViewSize(keyInfo.top).toFloat()
            rockerView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            rockerView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return rockerView
    }

    /**
     * 创建并添加摇杆控件到操作面板
     */
    private fun addRocker(keyInfo: KeyInfo): RockerView {
//        LogUtils.d("addRocker:$keyInfo")
        val rockerView = RockerView(context)
        when (keyInfo.type) {
            KeyType.ROCKER_RIGHT -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_bg
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_r
                    )
                )
                rockerView.positionOffsetListener = object : RockerView.OnPositionOffsetListener {
                    override fun onPosition(positionX: Float, positionY: Float) {
                        rockerListener?.onRockerMove(keyInfo, positionX, positionY)
                    }
                }
            }

            KeyType.ROCKER_LEFT -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_bg
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_l
                    )
                )
                rockerView.positionOffsetListener = object : RockerView.OnPositionOffsetListener {
                    override fun onPosition(positionX: Float, positionY: Float) {
                        rockerListener?.onRockerMove(keyInfo, positionX, positionY)
                    }
                }
            }

            KeyType.ROCKER_LETTER -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_letter_pad
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_default
                    )
                )
                rockerView.setOnShakeListener(
                    DirectionMode.DIRECTION_8,
                    object : RockerView.OnShakeListener {
                        override fun onStart() {
                        }

                        override fun direction(direction: Direction?) {
                            rockerListener?.onRockerDirection(keyInfo, direction)
                        }

                        override fun onFinish() {
                            rockerListener?.onRockerDirection(keyInfo, Direction.DIRECTION_CENTER)
                        }
                    })
            }

            KeyType.ROCKER_ARROW -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_arrow_pad
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_default
                    )
                )
                rockerView.setOnShakeListener(
                    DirectionMode.DIRECTION_8,
                    object : RockerView.OnShakeListener {
                        override fun onStart() {
                        }

                        override fun direction(direction: Direction?) {
                            rockerListener?.onRockerDirection(keyInfo, direction)
                        }

                        override fun onFinish() {
                            rockerListener?.onRockerDirection(keyInfo, Direction.DIRECTION_CENTER)
                        }
                    })
            }
        }
        rockerView.setOnClickListener {
//            LogUtils.d("onRockerClick:$keyInfo, status:${controllerStatus}")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            }
        }
        rockerView.positionChangeListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey = keyInfo
                    currentKey?.let { info ->
                        info.changePosition(
                            AppSizeUtils.reconvertWidthSize(left),
                            AppSizeUtils.reconvertHeightSize(top),
                        )
                        listener?.onEditKeyClick(info)
                    }
                }
            }
        }

        rockerView.tag = keyInfo.id
        rockerView.alpha = keyInfo.opacity / 100f
        val layoutParams = FrameLayout.LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(rockerView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(rockerView)
        }
        addView(rockerView, layoutParams)
        invalidate()
        rockerView.post {
//            val x = if (keyInfo.left < AppSizeUtils.DESIGN_WIDTH / 2) {
//                AppSizeUtils.convertViewSize(keyInfo.left)
//            } else {
//                val rightMargin = AppSizeUtils.DESIGN_WIDTH - keyInfo.left - keyInfo.getKeyWidth()
//                width - AppSizeUtils.convertViewSize(keyInfo.getKeyWidth() + rightMargin)
//            }
//            rockerView.x = x.toFloat()
//            rockerView.y = AppSizeUtils.convertViewSize(keyInfo.top).toFloat()
            rockerView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            rockerView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return rockerView
    }

    /**
     * 创建并添加按键控件到操作面板
     */
    private fun addKeyButton(keyInfo: KeyInfo): KeyView {
//        LogUtils.d("addKeyButton:$keyInfo")
        val keyView = KeyView(context)
        keyView.setKeyInfo(keyInfo)
        keyView.setOnClickListener {
            LogUtils.d("onKeyClick:$keyInfo, editMode:$controllerStatus")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            } else if (controllerStatus == ControllerStatus.Roulette) {
                listener?.onEditKeyClick(keyInfo)
            }
        }
        keyView.onKeyTouchListener = object : OnKeyTouchListener {
            override fun onKeyTouch(touch: Boolean) {
                if (keyInfo.click == 0) {
                    keyView.longClick = touch
                    keyEventListener?.onButtonPress(keyInfo, touch)
                } else {
                    if (touch) {
                        keyView.longClick = !keyView.longClick
                        keyEventListener?.onButtonPress(keyInfo, true)
                    } else {
                        if (!keyView.longClick) {
                            keyEventListener?.onButtonPress(keyInfo, false)
                        }
                    }
                }
            }
        }
        keyView.positionListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey = keyInfo
                    currentKey?.let { info ->
                        info.changePosition(
                            AppSizeUtils.reconvertWidthSize(left),
                            AppSizeUtils.reconvertHeightSize(top),
                        )
                        listener?.onEditKeyClick(info)
                    }
                }
            }
        }

        keyView.tag = keyInfo.id
        keyView.alpha = keyInfo.opacity / 100f
        val layoutParams = FrameLayout.LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        addView(keyView, layoutParams)
        invalidate()
        keyView.post {
//            val x = if (keyInfo.left < AppSizeUtils.DESIGN_WIDTH / 2) {
//                AppSizeUtils.convertViewSize(keyInfo.left)
//            } else {
//                val rightMargin = AppSizeUtils.DESIGN_WIDTH - keyInfo.left - keyInfo.getKeyWidth()
//                width - AppSizeUtils.convertViewSize(keyInfo.getKeyWidth() + rightMargin)
//            }
//            keyView.x = x.toFloat()
//            keyView.y = AppSizeUtils.convertViewSize(keyInfo.top).toFloat()
            keyView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            keyView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return keyView
    }

    /**
     * 创建并添加组合按键控件到操作面板
     */
    private fun addCombineKey(keyInfo: KeyInfo): CombineKeyView {
//        LogUtils.d("addCombineKey:$keyInfo")
        val keyView = CombineKeyView(context)
        keyView.setKeyInfo(keyInfo)
        keyView.isClickable = true
        keyView.isFocusable = true
        keyView.setOnClickListener {
            LogUtils.d("onKeyClick:$keyInfo, editMode:$controllerStatus")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            } else if (controllerStatus == ControllerStatus.Roulette) {
//                ToastUtils.showShort("无法在轮盘中添加")
            }
        }
        keyView.onKeyTouchListener = object : OnKeyTouchListener {
            override fun onKeyTouch(touch: Boolean) {
                keyEventListener?.onButtonPress(keyInfo, touch)
            }
        }
        keyView.positionListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey = keyInfo
                    currentKey?.let { info ->
                        info.changePosition(
                            AppSizeUtils.reconvertWidthSize(left),
                            AppSizeUtils.reconvertHeightSize(top),
                        )
                        listener?.onEditKeyClick(info)
                    }
                }
            }
        }
        keyView.tag = keyInfo.id
        keyView.alpha = keyInfo.opacity / 100f
        val layoutParams = FrameLayout.LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        addView(keyView, layoutParams)
        keyView.post {
//            val x = if (keyInfo.left < AppSizeUtils.DESIGN_WIDTH / 2) {
//                AppSizeUtils.convertViewSize(keyInfo.left)
//            } else {
//                val rightMargin = AppSizeUtils.DESIGN_WIDTH - keyInfo.left - keyInfo.getKeyWidth()
//                width - AppSizeUtils.convertViewSize(keyInfo.getKeyWidth() + rightMargin)
//            }
//            keyView.x = x.toFloat()
//            keyView.y = AppSizeUtils.convertViewSize(keyInfo.top).toFloat()
            keyView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            keyView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return keyView
    }

    /**
     * 创建并添加摇杆控件到面板
     */
    private fun addRouletteKey(keyInfo: KeyInfo): RouletteKeyView {
        LogUtils.d("addRouletteKey:$keyInfo")
        val keyView = RouletteKeyView(context)
        keyView.setKeyInfo(keyInfo)
        keyView.onKeyEventListener = keyEventListener
        keyView.positionListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey = keyInfo
                    currentKey?.let { info ->
                        info.changePosition(
                            AppSizeUtils.reconvertWidthSize(left),
                            AppSizeUtils.reconvertHeightSize(top),
                        )
                        listener?.onEditKeyClick(info)
                    }
                }
            }
        }
        keyView.tag = keyInfo.id
        keyView.alpha = keyInfo.opacity / 100f
        val layoutParams = FrameLayout.LayoutParams(
//            SizeUtils.dp2px(keyInfo.getKeyWidth().toFloat()),
//            SizeUtils.dp2px(keyInfo.getKeyHeight().toFloat())
            AppSizeUtils.convertViewSize(keyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(keyInfo.getKeyHeight())
        )
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        }
        if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        keyView.layoutParams = layoutParams
        addView(keyView)
        invalidate()
        keyView.post {
//            val x = if (keyInfo.left < AppSizeUtils.DESIGN_WIDTH / 2) {
//                AppSizeUtils.convertViewSize(keyInfo.left)
//            } else {
//                val rightMargin = AppSizeUtils.DESIGN_WIDTH - keyInfo.left - keyInfo.getKeyWidth()
//                width - AppSizeUtils.convertViewSize(keyInfo.getKeyWidth() + rightMargin)
//            }
//            keyView.x = x.toFloat()
//            keyView.y = AppSizeUtils.convertViewSize(keyInfo.top).toFloat()
            keyView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            keyView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return keyView
    }

    fun addKey(keyInfo: KeyInfo) {
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
            when (keyInfo.type) {
                KeyType.GAMEPAD_SQUARE, KeyType.GAMEPAD_ELLIPTIC, KeyType.GAMEPAD_ROUND_MEDIUM,
                KeyType.GAMEPAD_ROUND_SMALL -> {
                    addKeyButton(keyInfo)
                }

                KeyType.ROCKER_RIGHT, KeyType.ROCKER_LEFT -> {
                    addRocker(keyInfo)
                }

                KeyType.ROCKER_CROSS -> {
                    addCrossRocker(keyInfo)
                }

                KeyType.GAMEPAD_COMBINE -> {
                    addCombineKey(keyInfo)
                }

                KeyType.GAMEPAD_ROULETTE -> {
                    addRouletteKey(keyInfo)
                }

                else -> {
                    LogUtils.e("initGamepad:$keyInfo")
                }
            }
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
            when (keyInfo.type) {
                KeyType.KEYBOARD_KEY, KeyType.KEYBOARD_MOUSE_UP, KeyType.KEYBOARD_MOUSE_DOWN,
                KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                    addKeyButton(keyInfo)
                }

                KeyType.ROCKER_LETTER, KeyType.ROCKER_ARROW -> {
                    addRocker(keyInfo)
                }

                KeyType.KEY_COMBINE -> {
                    addCombineKey(keyInfo)
                }

                KeyType.KEY_ROULETTE -> {
                    addRouletteKey(keyInfo)
                }

                else -> {
                    LogUtils.e("initGamepad:$keyInfo")
                }
            }
        }
    }

    /**
     * 编辑模式下，添加按键控件
     */
    fun addKeyButton(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        LogUtils.d("addKeyButton:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
        }
        addKeyButton(keyInfo)
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加摇杆控件
     */
    fun addRocker(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        LogUtils.d("addRocker:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
        }
        addRocker(keyInfo)
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加十字摇杆控件
     */
    fun addCrossRocker(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        LogUtils.d("addCrossRocker:$type")
        editGamepadKeys.add(keyInfo)
        addCrossRocker(keyInfo)
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加组合按键控件
     */
    fun addCombineKey(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        LogUtils.d("addCombineKey:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
        }
        addCombineKey(keyInfo)
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加轮盘控件
     */
    fun addRouletteKey(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        LogUtils.d("addRouletteKey:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
            keyInfo.rouArr?.forEach { rouKey ->
                editGamepadKeys.remove(rouKey)
                removeView(findKeyView(this, rouKey))
            }
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
            keyInfo.rouArr?.forEach { rouKey ->
                editKeyboardKeys.remove(rouKey)
                removeView(findKeyView(this, rouKey))
            }
        }
        addRouletteKey(keyInfo)
        currentKey = keyInfo
    }

    /**
     * 加载操作面板数据
     */
    fun setControllerData(data: ControllerInfo) {
        LogUtils.d("setKeyData:$data")
        if (data.type == GameConstants.keyboardConfig) {
            // 键鼠
            LogUtils.d("setKeyData-> keyboard")
            keyboardKeys.clear()
            keyboardKeys.addAll(data.keyboard)
            initKeyboard(data.keyboard)
        } else if (data.type == GameConstants.gamepadConfig) {
            // 手柄
            LogUtils.d("setKeyData-> gamepad")
            gamepadKeys.clear()
            gamepadKeys.addAll(data.keyboard)
            initGamepad(data.keyboard)
        }
    }

    fun checkAlignment(view: View) {
        val viewCenterX = view.x + view.width / 2f
        val viewCenterY = view.y + view.height / 2f

        val centerX = width / 2f
        val centerY = height / 2f

        dataBinding.layoutMask.isCenterHorizontal =
            abs(viewCenterX - centerX) <= SizeUtils.dp2px(4f)
        dataBinding.layoutMask.isCenterVertical = abs(viewCenterY - centerY) <= SizeUtils.dp2px(4f)
        if (dataBinding.layoutMask.isCenterHorizontal || dataBinding.layoutMask.isCenterVertical) {
            // 居中对齐画居中对齐线
            dataBinding.layoutMask.drawCenterLine(view)
            return
        }
        val viewList =
            if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) gamepadViews else keyboardViews
        val alignViews = mutableListOf<View>()
        for (index in 0..<viewList.size) {
            val child = viewList[index]
            if (child == view || child.visibility != VISIBLE) {
                continue
            }
            val leftAlign =
                abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewLeft(child)) <= SizeUtils.dp2px(
                    4f
                )
            val leftRightAlign =
                abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewRight(child)) <= SizeUtils.dp2px(
                    4f
                )
            val topAlign =
                abs(ViewUtils.getViewTop(view) - ViewUtils.getViewTop(child)) <= SizeUtils.dp2px(
                    4f
                )
            val topBottomAlign =
                abs(ViewUtils.getViewTop(view) - ViewUtils.getViewBottom(child)) <= SizeUtils.dp2px(
                    4f
                )
            val rightAlign =
                abs(ViewUtils.getViewRight(view) - ViewUtils.getViewRight(child)) <= SizeUtils.dp2px(
                    4f
                )
            val rightLeftAlign =
                abs(ViewUtils.getViewRight(view) - ViewUtils.getViewLeft(child)) <= SizeUtils.dp2px(
                    4f
                )
            val bottomAlign =
                abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewBottom(child)) <= SizeUtils.dp2px(
                    4f
                )
            val bottomTopAlign =
                abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewTop(child)) <= SizeUtils.dp2px(
                    4f
                )
            val centerXAlign =
                abs(ViewUtils.getViewCenterX(view) - ViewUtils.getViewCenterX(child)) <= SizeUtils.dp2px(
                    4f
                )
            val centerYAlign =
                abs(ViewUtils.getViewCenterY(view) - ViewUtils.getViewCenterY(child)) <= SizeUtils.dp2px(
                    4f
                )
            if (leftAlign || topAlign || rightAlign || bottomAlign || centerXAlign ||
                centerYAlign || leftRightAlign || topBottomAlign || rightLeftAlign || bottomTopAlign
            ) {
                // 关联对齐
                alignViews.add(child)
            }
        }
        if (alignViews.isEmpty()) {
            dataBinding.layoutMask.clearLine()
        } else {
            val matchView: View = findMostMatchView(view, alignViews)
            dataBinding.layoutMask.drawRelation(view, matchView)
        }
    }

    private fun findMostMatchView(targetView: View, views: List<View>): View {
        var resultView = views.first()
        var minDistance = ViewUtils.getViewDistance(targetView, resultView)
        for (view in views) {
            if (view == resultView) {
                continue
            }
            val distance = ViewUtils.getViewDistance(targetView, view)
            if (distance < minDistance) {
                minDistance = distance
                resultView = view
            }
        }
        return resultView
    }

    fun clearLine() {
        dataBinding.layoutMask.clearLine()
    }

    /**
     * 操作面板编辑数据成功返回
     */
    fun onControllerEditSuccess(keyType: Int) {
        if (keyType == GameConstants.gamepadConfig) {
            gamepadKeys.clear()
            gamepadKeys.addAll(editGamepadKeys)
            initGamepad(gamepadKeys)
        } else if (keyType == GameConstants.keyboardConfig) {
            keyboardKeys.clear()
            keyboardKeys.addAll(editKeyboardKeys)
            initKeyboard(keyboardKeys)
        }
    }

    /**
     * 编辑修改按键属性
     */
    fun updateKey() {
        currentKey?.let {
            val view = findKeyView(this, it)
            view?.run {
                if (it.textChange) {
                    if (view is KeyView) {
                        view.updateText(it.text)
                    }
                    if (view is CombineKeyView) {
                        view.updateText(it.text)
                    }
                    if (view is RouletteKeyView) {
                        view.updateText(it.text)
                    }
                }
                if (it.zoomChange) {
                    val targetWidth = AppSizeUtils.convertViewSize(it.getKeyWidth())
                    val scale = targetWidth / view.width.toFloat()
                    view.scaleX = scale
                    view.scaleY = scale
                    LogUtils.d("changeZoom:${it.zoom}")
                }
                if (it.opacityChange) {
                    view.alpha = it.opacity / 100f
                    LogUtils.d("changeOpacity:${it.opacity}")
                }
                if (it.listChange) {
                    if (view is RouletteKeyView) {
                        view.setKeyInfo(it)
//                        LogUtils.d("listChange:${it.rouArr}")
                    }
                    if (view is CombineKeyView) {
                        view.setKeyInfo(it)
//                        LogUtils.d("listChange:${it.composeArr}")
                    }
                }
                it.updateChange(false)
            } ?: LogUtils.e("updateKey: View not found")
        }
    }

    /**
     * 删除按键
     */
    fun deleteKey() {
        currentKey?.let {
            val view = findKeyView(this, it)
            view?.run {
                if (view.parent is ViewGroup) {
                    (view.parent as ViewGroup).removeView(view)
                }
                val currentEditKeys = getCurrentEditKeys()
                currentEditKeys?.let { list ->
                    if (view is RouletteKeyView) {
                        LogUtils.d("remove RouletteKeyView")
                        it.rouArr?.forEach { info ->
                            list.add(info)
                        }
                        list.remove(currentKey)
                        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
                            initGamepad(list.toList())
                        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
                            initKeyboard(list.toList())
                        } else {
                            LogUtils.e("deleteKey: $controllerType")
                        }
                    } else {
                        list.remove(currentKey)
                    }
                }
                currentKey = null
                LogUtils.d("deleteKey")
            } ?: LogUtils.e("deleteKey: View not found")
        }
    }

    /**
     * 查找对应的按键控件
     */
    private fun findKeyView(viewGroup: ViewGroup, keyInfo: KeyInfo): View? {
        val childCount = viewGroup.childCount
//        LogUtils.d("findKeyView:$viewGroup, childCount:${viewGroup.childCount}")
        for (index in 0..<childCount) {
            when (val childView = viewGroup.getChildAt(index)) {
                is KeyView, is RockerView, is CombineKeyView, is RouletteKeyView -> {
//                    LogUtils.d("childView:{tag:${childView.tag}}, keyInfo:{id:${keyInfo.id}}")
                    if (childView.tag == keyInfo.id) {
//                        LogUtils.d("findKeyView:${childView.tag}, $keyInfo")
                        return childView
                    }
                }
            }
        }
        return null
    }

    /**
     * 获取当前正在编辑的按键信息列表
     */
    private fun getCurrentEditKeys(): MutableList<KeyInfo>? {
        LogUtils.d("getCurrentEditKeys:$controllerType")
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            return editGamepadKeys
        }
        if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            return editKeyboardKeys
        }
        return null
    }

    /**
     * 点击还原默认
     */
    fun restoreDefault() {
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
//            controllerCallback?.getDefaultGamepadData()
            initGamepad(gamepadKeys)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
//            controllerCallback?.getDefaultKeyboardData()
            initKeyboard(keyboardKeys)
        }
    }

    /**
     * 退出编辑，展示编辑前数据
     */
    fun restoreOriginal() {
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            initGamepad(gamepadKeys)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            initKeyboard(keyboardKeys)
        }
    }

    /**
     * 保存键位配置
     */
    fun saveKeyConfig() {
        val data = JsonObject()
        val gson = GsonBuilder()
            // 仅序列化有 @Expose 标记的字段
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            val toJsonTree = gson.toJsonTree(editGamepadKeys)
            LogUtils.d("saveKeyConfig:$toJsonTree")
            data.add("keyboard", toJsonTree)
            data.addProperty("type", GameConstants.gamepadConfig)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            val toJsonTree = gson.toJsonTree(editKeyboardKeys)
            LogUtils.d("saveKeyConfig:$toJsonTree")
            data.add("keyboard", toJsonTree)
            data.addProperty("type", GameConstants.keyboardConfig)
        }
        controllerCallback?.updateKeyboardData(data)
    }

    fun controllerChange(keyType: Int) {
        if (keyType == GameConstants.gamepadConfig) {
            gamepadKeys.clear()
            gamepadKeys.addAll(editGamepadKeys)
            initGamepad(gamepadKeys)
        } else if (keyType == GameConstants.keyboardConfig) {
            keyboardKeys.clear()
            keyboardKeys.addAll(editKeyboardKeys)
            initKeyboard(keyboardKeys)
        }
    }

    private fun changViewVisibility(views: ArrayList<View>, visibility: Int) {
        views.forEach {
            it.visibility = visibility
        }
    }

    private fun hideAllKey() {
        children.iterator().forEach {
            if ((it is KeyView) or (it is RockerView) or (it is CombineKeyView) or (it is RouletteKeyView)) {
                it.visibility = View.INVISIBLE
            }
        }
    }

    private fun removeAllViews(views: ArrayList<View>) {
        views.forEach {
            this.removeView(it)
        }
        views.clear()
    }

    fun removeKeys(list: List<KeyInfo>) {
        list.forEach {
            if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
                editGamepadKeys.remove(it)
            } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
                editKeyboardKeys.remove(it)
            }
            val findKeyView = findKeyView(this, it)
            if (findKeyView != null) {
                removeView(findKeyView)
            }
        }
    }

    fun addKeys(list: List<KeyInfo>) {
        list.forEach {
            addKey(it)
        }
    }
}
