package com.sayx.hm_cloud.widget

import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
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
import com.sayx.hm_cloud.model.MessageEvent
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.ViewUtils
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus
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
//            LogUtils.d("currentKey:${value}")
        }

    var controllerType: AppVirtualOperateType = AppVirtualOperateType.NONE
        set(value) {
            LogUtils.d("change controllerType = $value")
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
                currentKey = null
                if (view is RouletteKeyView) {
                    view.showRoulette = value
                    view.invalidate()
                } else if (view is ContainerKeyView) {
                    if (value) {
                        view.showItems(true)
                    } else {
                        view.hideItems()
                    }
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
                LogUtils.d("showGamepadView:${gamepadViews.size}")
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
                LogUtils.d("showKeyboardView:${keyboardViews.size}")
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
        removeAllViews(keyboardViews)
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

                else -> {
                    LogUtils.e("initGamepad:$keyInfo")
                }
            }
        }
        currentKey?.let { info ->
            val view = findKeyView(this, info)
            view?.isActivated = true
            view?.invalidate()
        }
    }

    private fun initKeyboard(keyInfoList: List<KeyInfo>) {
        LogUtils.d("initKeyboard:${keyInfoList}")
        editKeyboardKeys.clear()
        removeAllViews(keyboardViews)
        removeAllViews(gamepadViews)
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

                KeyType.KEY_CONTAINER -> {
                    addContainerKey(keyInfo)
                }

                KeyType.KEY_SHOOT -> {
                    addShotKey(keyInfo)
                }

                else -> {
                    LogUtils.e("initGamepad:$keyInfo")
                }
            }
        }
        currentKey?.let { info ->
            val view = findKeyView(this, info)
            view?.isActivated = true
            view?.invalidate()
        }
    }

    /**
     * 创建并添加十字摇杆控件到操作面板
     */
    private fun addCrossRocker(keyInfo: KeyInfo): RockerView {
//        LogUtils.d("addCrossRocker:$keyInfo")
        val rockerView = RockerView(context)
        rockerView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
        rockerView.setBackgroundBitmap(
            ContextCompat.getDrawable(
                context,
                R.drawable.img_rocker_cross_default
            )
        )
        rockerView.setOnClickListener {
            LogUtils.d("addCrossRocker:$keyInfo")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                rockerView.invalidate()
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
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    rockerView.isActivated = true
                    rockerView.invalidate()
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
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(rockerView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(rockerView)
        }
        rockerView.setKeyInfo(keyInfo)
        addView(rockerView)
        rockerView.post {
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
        rockerView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
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
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                rockerView.invalidate()
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            }
        }
        rockerView.positionChangeListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    rockerView.isActivated = true
                    rockerView.invalidate()
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
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(rockerView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(rockerView)
        }
        rockerView.setKeyInfo(keyInfo)
        addView(rockerView)
        rockerView.post {
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
        keyView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
        keyView.setKeyInfo(keyInfo)
        keyView.setOnClickListener {
            LogUtils.d("onKeyClick:$keyInfo, editMode:$controllerStatus")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                keyView.invalidate()
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
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    keyView.isActivated = true
                    keyView.invalidate()
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
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        addView(keyView)
        keyView.post {
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
        keyView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
        keyView.setKeyInfo(keyInfo)
        keyView.setOnClickListener {
//            LogUtils.d("onKeyClick:$keyInfo, editMode:$controllerStatus")
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                keyView.invalidate()
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
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
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    keyView.isActivated = true
                    keyView.invalidate()
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
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        addView(keyView)
        keyView.post {
            keyView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            keyView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return keyView
    }

    /**
     * 创建并添加摇杆控件到面板
     */
    private fun addRouletteKey(keyInfo: KeyInfo): RouletteKeyView {
//        LogUtils.d("addRouletteKey:$keyInfo")
        val keyView = RouletteKeyView(context)
        keyView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
        keyView.setKeyInfo(keyInfo)
        keyView.onKeyEventListener = keyEventListener
        keyView.setOnClickListener {
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                keyView.invalidate()
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            }
        }
        keyView.positionListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    keyView.isActivated = true
                    keyView.invalidate()
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
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        }
        if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        addView(keyView)
        keyView.post {
            keyView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            keyView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return keyView
    }

    private fun addContainerKey(keyInfo: KeyInfo) : ContainerKeyView {
        val keyView = ContainerKeyView(context)
        keyView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
        keyView.setKeyInfo(keyInfo)
        keyView.setOnClickListener {
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                keyView.invalidate()
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
            }
        }
        keyView.keyEventListener = keyEventListener
        keyView.positionListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                if (controllerStatus == ControllerStatus.Edit) {
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    keyView.isActivated = true
                    keyView.invalidate()
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
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            gamepadViews.add(keyView)
        }
        if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            keyboardViews.add(keyView)
        }
        addView(keyView)
        keyView.post {
            keyView.x = AppSizeUtils.convertWidthSize(keyInfo.left).toFloat()
            keyView.y = AppSizeUtils.convertHeightSize(keyInfo.top).toFloat()
        }
        return keyView
    }

    private fun addShotKey(keyInfo: KeyInfo) : ShotKeyView {
        val keyView = ShotKeyView(context)
        keyView.alpha = SPUtils.getInstance().getInt(GameConstants.keyOpacity, 70) / 100f
        keyView.setKeyInfo(keyInfo)
        keyView.tag = keyInfo.id
        keyView.setOnClickListener {
            if (controllerStatus == ControllerStatus.Edit) {
                currentKey?.let { info ->
                    val view = findKeyView(this, info)
                    view?.isActivated = false
                    view?.invalidate()
                }
                it.isActivated = true
                keyView.invalidate()
                currentKey = keyInfo
                currentKey?.let { info ->
                    listener?.onEditKeyClick(info)
                }
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
                    currentKey?.let { info ->
                        val view = findKeyView(this@GameController, info)
                        view?.isActivated = false
                        view?.invalidate()
                    }
                    keyView.isActivated = true
                    keyView.invalidate()
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
        keyboardViews.add(keyView)
        addView(keyView)
        keyView.post {
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

                KeyType.KEY_CONTAINER -> {
                    addContainerKey(keyInfo)
                }

                KeyType.KEY_SHOOT -> {
                    addShotKey(keyInfo)
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
//        LogUtils.d("addKeyButton:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
        }
        val keyView = addKeyButton(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
            view?.isActivated = false
            view?.invalidate()
        }
        keyView.isActivated = true
        keyView.invalidate()
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加摇杆控件
     */
    fun addRocker(keyInfo: KeyInfo, type: AppVirtualOperateType) {
//        LogUtils.d("addRocker:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
        }
        val rockerView = addRocker(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
            view?.isActivated = false
            view?.invalidate()
        }
        rockerView.isActivated = true
        rockerView.invalidate()
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加十字摇杆控件
     */
    fun addCrossRocker(keyInfo: KeyInfo, type: AppVirtualOperateType) {
//        LogUtils.d("addCrossRocker:$type")
        editGamepadKeys.add(keyInfo)
        val rockerView = addCrossRocker(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
            view?.isActivated = false
            view?.invalidate()
        }
        rockerView.isActivated = true
        rockerView.invalidate()
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加组合按键控件
     */
    fun addCombineKey(keyInfo: KeyInfo, type: AppVirtualOperateType) {
//        LogUtils.d("addCombineKey:$type")
        if (type == AppVirtualOperateType.APP_STICK_XBOX) {
            editGamepadKeys.add(keyInfo)
        } else if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
        }
        val keyView = addCombineKey(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
            LogUtils.d("unActivated:${view?.javaClass?.simpleName}")
            view?.isActivated = false
            view?.invalidate()
        }
        keyView.isActivated = true
        keyView.invalidate()
        currentKey = keyInfo
    }

    /**
     * 编辑模式下，添加轮盘控件
     */
    fun addRouletteKey(keyInfo: KeyInfo, type: AppVirtualOperateType) {
//        LogUtils.d("addRouletteKey:$type")
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
        val keyView = addRouletteKey(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
            LogUtils.d("unActivated:${view?.javaClass?.simpleName}")
            view?.isActivated = false
            view?.invalidate()
        }
        keyView.isActivated = true
        keyView.invalidate()
        currentKey = keyInfo
    }

    fun addContainerKey(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        if (type == AppVirtualOperateType.APP_KEYBOARD) {
            editKeyboardKeys.add(keyInfo)
            keyInfo.containerArr?.forEach { keyData ->
                editKeyboardKeys.remove(keyData)
                removeView(findKeyView(this, keyData))
            }
        }
        val keyView = addContainerKey(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
//            LogUtils.d("unActivated:${view?.javaClass?.simpleName}")
            view?.isActivated = false
            view?.invalidate()
        }
        keyView.showItems(true)
        keyView.isActivated = true
        keyView.invalidate()
        currentKey = keyInfo
    }

    fun addShotKey(keyInfo: KeyInfo, type: AppVirtualOperateType) {
        editKeyboardKeys.add(keyInfo)
        val keyView = addShotKey(keyInfo)
        currentKey?.let { info ->
            val view = findKeyView(this@GameController, info)
//            LogUtils.d("unActivated:${view?.javaClass?.simpleName}")
            view?.isActivated = false
            view?.invalidate()
        }
        keyView.isActivated = true
        keyView.invalidate()
        currentKey = keyInfo
    }

    private var controllerInfo : ControllerInfo? = null

    var controllerName : String = ""

    /**
     * 加载操作面板数据
     */
    fun setControllerData(data: ControllerInfo, update: Boolean = false) {
        controllerInfo = data
        controllerName = data.name?: ""
//        LogUtils.d("setKeyData:$data")
        if (data.type == GameConstants.keyboardConfig) {
            // 键鼠
//            LogUtils.d("setKeyData-> keyboard")
            if (update) {
                initKeyboard(data.keyboard)
            } else {
                keyboardKeys.clear()
                keyboardKeys.addAll(data.keyboard)
                initKeyboard(keyboardKeys)
            }
        } else if (data.type == GameConstants.gamepadConfig) {
            // 手柄
//            LogUtils.d("setKeyData-> gamepad")
            if (update) {
                initGamepad(data.keyboard)
            } else {
                gamepadKeys.clear()
                gamepadKeys.addAll(data.keyboard)
                initGamepad(gamepadKeys)
            }
        }
    }

    fun checkAlignment(view: View) {
        val viewCenterX = view.x + view.width / 2f
        val viewCenterY = view.y + view.height / 2f

        val centerX = width / 2f
        val centerY = height / 2f

        dataBinding.layoutMask.isCenterHorizontal =
            abs(viewCenterX - centerX) <= AutoSizeUtils.dp2px(context, 4f)
        dataBinding.layoutMask.isCenterVertical = abs(viewCenterY - centerY) <= AutoSizeUtils.dp2px(context, 4f)
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
                abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewLeft(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val leftRightAlign =
                abs(ViewUtils.getViewLeft(view) - ViewUtils.getViewRight(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val topAlign =
                abs(ViewUtils.getViewTop(view) - ViewUtils.getViewTop(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val topBottomAlign =
                abs(ViewUtils.getViewTop(view) - ViewUtils.getViewBottom(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val rightAlign =
                abs(ViewUtils.getViewRight(view) - ViewUtils.getViewRight(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val rightLeftAlign =
                abs(ViewUtils.getViewRight(view) - ViewUtils.getViewLeft(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val bottomAlign =
                abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewBottom(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val bottomTopAlign =
                abs(ViewUtils.getViewBottom(view) - ViewUtils.getViewTop(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val centerXAlign =
                abs(ViewUtils.getViewCenterX(view) - ViewUtils.getViewCenterX(child)) <= AutoSizeUtils.dp2px(context,
                    4f
                )
            val centerYAlign =
                abs(ViewUtils.getViewCenterY(view) - ViewUtils.getViewCenterY(child)) <= AutoSizeUtils.dp2px(context,
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

    fun updateKey(keyInfo: KeyInfo) {
//        LogUtils.v("updateKey-> \n current:$currentKey \n new:$keyInfo")
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            currentKey = editGamepadKeys.find { info -> info.id == keyInfo.id }
            currentKey?.copyFrom(keyInfo)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            currentKey = editKeyboardKeys.find { info -> info.id == keyInfo.id }
            currentKey?.copyFrom(keyInfo)
        }
        currentKey?.let {
            val view = findKeyView(this, it)
            LogUtils.v("findKeyView:$view")
            view?.run {
                if (view is KeyView) {
                    view.setKeyInfo(it)
                }
                if (view is RockerView) {
                    view.setKeyInfo(it)
                }
                if (view is CombineKeyView) {
                    view.setKeyInfo(it)
                }
                if (view is RouletteKeyView) {
                    view.setKeyInfo(it)
                }
                if (view is ContainerKeyView) {
                    view.setKeyInfo(it)
                    view.showItems(true)
                }
                if (view is ShotKeyView) {
                    view.setKeyInfo(it)
                }
            }
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
                    when (view) {
                        is RouletteKeyView -> {
                            LogUtils.d("remove RouletteKeyView")
                            it.rouArr?.forEach { info ->
                                list.add(info)
                            }
                            list.remove(currentKey)
                            when (controllerType) {
                                AppVirtualOperateType.APP_STICK_XBOX -> {
                                    initGamepad(list.toList())
                                }

                                AppVirtualOperateType.APP_KEYBOARD -> {
                                    initKeyboard(list.toList())
                                }

                                else -> {
                                    LogUtils.e("deleteKey: $controllerType")
                                }
                            }
                        }

                        is ContainerKeyView -> {
                            it.containerArr?.forEach { info ->
                                list.add(info)
                            }
                            list.remove(currentKey)
                            when (controllerType) {
                                AppVirtualOperateType.APP_STICK_XBOX -> {
                                    initGamepad(list.toList())
                                }

                                AppVirtualOperateType.APP_KEYBOARD -> {
                                    initKeyboard(list.toList())
                                }

                                else -> {
                                    LogUtils.e("deleteKey: $controllerType")
                                }
                            }
                        }

                        else -> {
                            list.remove(currentKey)
                        }
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
                is KeyView, is RockerView, is CombineKeyView, is RouletteKeyView, is ContainerKeyView, is ShotKeyView -> {
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
        currentKey = null
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            initGamepad(gamepadKeys)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            initKeyboard(keyboardKeys)
        }
        EventBus.getDefault().post(MessageEvent("restoreSuccess", arg = currentKey))
    }

    /**
     * 退出编辑，展示编辑前数据
     */
    fun restoreOriginal() {
        currentKey = null
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            initGamepad(gamepadKeys)
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            initKeyboard(keyboardKeys)
        }
    }

    fun onEditSuccess() {
        if (controllerInfo?.use == 1) {
            if (controllerInfo?.type == GameConstants.gamepadConfig) {
                gamepadKeys.clear()
                gamepadKeys.addAll(editGamepadKeys)
            } else if (controllerInfo?.type == GameConstants.keyboardConfig) {
                keyboardKeys.clear()
                keyboardKeys.addAll(editKeyboardKeys)
            }
        }
        restoreOriginal()
    }

    /**
     * 保存键位配置
     */
    fun saveKeyConfig() {
        if (controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            controllerInfo?.let {
                val keyList = editGamepadKeys.map { item -> item.copy() }.toList()
                if (it.isOfficial == true) {
                    val info = ControllerInfo("", it.type, it.userId, it.gameId, keyList, controllerName, 0)
                    GameManager.addKeyboardConfig(info)
                } else {
                    val info = ControllerInfo(it.id, it.type, it.userId, it.gameId, keyList, controllerName, it.use)
                    GameManager.updateKeyboardConfig(info)
                }
            }
        } else if (controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            val keyList = editKeyboardKeys.map { item -> item.copy() }.toList()
            controllerInfo?.let {
                if (it.isOfficial == true) {
                    val info = ControllerInfo("", it.type, it.userId, it.gameId, keyList, controllerName, 0)
                    GameManager.addKeyboardConfig(info)
                } else {
                    val info = ControllerInfo(it.id, it.type, it.userId, it.gameId, keyList, controllerName, it.use)
                    GameManager.updateKeyboardConfig(info)
                }
            }
        }
    }

    private fun changViewVisibility(views: ArrayList<View>, visibility: Int) {
        views.forEach {
            it.visibility = visibility
        }
    }

    private fun hideAllKey() {
        children.iterator().forEach {
            if (it is KeyView ||
                it is RockerView ||
                it is CombineKeyView ||
                it is RouletteKeyView ||
                it is ContainerKeyView ||
                it is ShotKeyView
                ) {
                it.visibility = View.INVISIBLE
            }
        }
    }

    private fun removeAllViews(views: ArrayList<View>) {
        LogUtils.d("removeAllViews")
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

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfGlobal(resources)
        }
        return super.generateLayoutParams(attrs)
    }

    fun setKeyOpacity(opacity: Int) {
        when(controllerType) {
            AppVirtualOperateType.APP_STICK_XBOX-> {
                gamepadViews.forEach {
                    it.alpha = opacity / 100f
                }
            }

            AppVirtualOperateType.APP_KEYBOARD -> {
                keyboardViews.forEach {
                    it.alpha = opacity / 100f
                }
            }

            AppVirtualOperateType.NONE -> {

            }
        }
    }
}
