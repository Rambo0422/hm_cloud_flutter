package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.haima.hmcp.enums.TouchMode
import com.haima.hmcp.widgets.HmcpVideoView
import com.sayx.hm_cloud.BuildConfig
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.GameSettingChangeListener
import com.sayx.hm_cloud.callback.SeekBarChangListenerImp
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.databinding.ViewGameSettingsBinding
import com.sayx.hm_cloud.utils.AppVibrateUtils
import com.sayx.hm_cloud.utils.TimeUtils
import java.util.Timer
import java.util.TimerTask

class GameSettings @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dataBinding: ViewGameSettingsBinding = DataBindingUtil
        .inflate(LayoutInflater.from(context), R.layout.view_game_settings, this, true)

    var gameSettingChangeListener: GameSettingChangeListener? = null

    private val countTimer by lazy {
        Timer()
    }

    private var gameView: HmcpVideoView? = null

    // 用户当前可用高峰时长
    private var playTime: Long = 0

    // 用户当前游戏已玩时长
    private var playedTime: Long = 0

    // 用户会员到期时间
    private var vipTime: Long = 0L

    // 当前游戏可玩时长
    private var currentPlayTime: Long = 0L

    private var taskScheduled = false

    private var peakChannel = false

    private var lastDelay = 0

    private var lastLost = 0.0

    private var currentTouchMode = TouchMode.TOUCH_MODE_SCREEN

    var initialized = false

    private var animated = false

    private var mouseModeEditable = true

    var controllerType: AppVirtualOperateType = AppVirtualOperateType.NONE
        set(value) {
            field = value
            updateControlType(value)
        }

    private var lastControlType: AppVirtualOperateType = AppVirtualOperateType.NONE

    init {
        // 可用时长点击，添加可用时长
        dataBinding.tvAvailableTime.setOnClickListener {
            gameSettingChangeListener?.onAddAvailableTime()
        }
        // 调试码点击，复制调试码到剪切板
        dataBinding.tvDebugCode.setOnClickListener {
            gameSettingChangeListener?.onDebugCodeClick()
        }
        // 控制方法
        dataBinding.btnGamepad.setOnClickListener {
            lastControlType = AppVirtualOperateType.APP_STICK_XBOX
            controllerType = AppVirtualOperateType.APP_STICK_XBOX
            gameSettingChangeListener?.onControlMethodChange(AppVirtualOperateType.APP_STICK_XBOX)
        }
        dataBinding.btnKeyboard.setOnClickListener {
            lastControlType = AppVirtualOperateType.APP_KEYBOARD
            controllerType = AppVirtualOperateType.APP_KEYBOARD
            gameSettingChangeListener?.onControlMethodChange(AppVirtualOperateType.APP_KEYBOARD)
        }
        dataBinding.btnCustom.setOnClickListener {
            hideLayout()
            if (vipTime > (GameManager.getGameParam()?.realTime ?: 0L)) {
                gameSettingChangeListener?.onCustomSettings()
            } else {
                gameSettingChangeListener?.onShowVipDialog()
            }
        }
        // 震动开关
        dataBinding.btnVibrate.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                AppVibrateUtils.vibrate(force = true)
            }
            updateVibrate()
        }
        // 静音开关
        dataBinding.btnMute.setOnClickListener {
            val value = !it.isSelected
            it.isSelected = value
            gameView?.setAudioMute(!value)
            SPUtils.getInstance().put(GameConstants.volumeSwitch, value)
        }
        // 画质选择
        dataBinding.tvQuality.setOnClickListener {
            dataBinding.layoutQuality.visibility = if (dataBinding.layoutQuality.visibility == INVISIBLE) VISIBLE else INVISIBLE
        }
        dataBinding.tvStandardQuality.setOnClickListener {
            dataBinding.layoutQuality.visibility = INVISIBLE
            dataBinding.tvQuality.text = "标清"
            gameView?.resolutionList?.let { list ->
                gameSettingChangeListener?.onImageQualityChange(list.last())
            }
        }
        dataBinding.tvBlueRay.setOnClickListener {
            dataBinding.layoutQuality.visibility = INVISIBLE
            if (vipTime > (GameManager.getGameParam()?.realTime ?: 0L)) {
                dataBinding.tvQuality.text = "蓝光"
                gameView?.resolutionList?.let { list ->
                    gameSettingChangeListener?.onImageQualityChange(list.first())
                }
            } else {
                hideLayout()
                gameSettingChangeListener?.onShowVipDialog()
            }
        }
        // 云游互动开关
        dataBinding.btnInteraction.setOnClickListener {
            if (vipTime > (GameManager.getGameParam()?.realTime ?: 0L)) {
                val value = !dataBinding.btnInteraction.isSelected
                dataBinding.btnInteraction.isSelected = value
                gameSettingChangeListener?.onLiveInteractionChange(value)
            } else {
                hideLayout()
                gameSettingChangeListener?.onShowVipDialog()
            }
        }
        // 鼠标设置
        dataBinding.switchMouseConfig.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                gameView?.touchMode = currentTouchMode
                SPUtils.getInstance().put(GameConstants.mouseMode, currentTouchMode.ordinal)
            } else {
                currentTouchMode = gameView?.touchMode ?: currentTouchMode
                gameView?.touchMode = TouchMode.TOUCH_MODE_NONE
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_NONE.ordinal)
            }
            dataBinding.btnMouseClick.isEnabled = isChecked
            dataBinding.btnTouchClick.isEnabled = isChecked
            dataBinding.btnTouchAttack.isEnabled = isChecked
            dataBinding.sbSensitivity.isEnabled = isChecked
            updateMouseMode()
        }
        // 鼠标点击
        dataBinding.btnMouseClick.setOnClickListener {
            if (!mouseModeEditable) {
                ToastUtils.showLong(R.string.mouse_editable_notice)
                return@setOnClickListener
            }
            val value = !it.isSelected
            it.isSelected = value
            if (value) {
                currentTouchMode = TouchMode.TOUCH_MODE_MOUSE
                gameView?.touchMode = TouchMode.TOUCH_MODE_MOUSE
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_MOUSE.ordinal)
            } else {
                gameView?.touchMode = TouchMode.TOUCH_MODE_NONE
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_NONE.ordinal)
            }
            updateMouseMode()
        }
        // 触控点击
        dataBinding.btnTouchClick.setOnClickListener {
            if (!mouseModeEditable) {
                ToastUtils.showLong(R.string.mouse_editable_notice)
                return@setOnClickListener
            }
            val value = !it.isSelected
            it.isSelected = value
            if (value) {
                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN
                gameView?.touchMode = TouchMode.TOUCH_MODE_SCREEN
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_SCREEN.ordinal)
            } else {
                gameView?.touchMode = TouchMode.TOUCH_MODE_NONE
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_NONE.ordinal)
            }
            updateMouseMode()
        }
        // 触屏攻击
        dataBinding.btnTouchAttack.setOnClickListener {
            if (!mouseModeEditable) {
                ToastUtils.showLong(R.string.mouse_editable_notice)
                return@setOnClickListener
            }
            val value = !it.isSelected
            it.isSelected = value
            if (value) {
                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
                gameView?.touchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_SCREEN_SLIDE.ordinal)
            } else {
                gameView?.touchMode = TouchMode.TOUCH_MODE_NONE
                SPUtils.getInstance().put(GameConstants.mouseMode, TouchMode.TOUCH_MODE_NONE.ordinal)
            }
            updateMouseMode()
        }
        // 退出游戏
        dataBinding.btnExitGame.setOnClickListener {
            hideLayout()
            gameSettingChangeListener?.onExitGame()
        }
        dataBinding.sbLight.setOnSeekBarChangeListener(object : SeekBarChangListenerImp() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                LogUtils.d("onProgressChanged->light=$progress, fromUser=$fromUser")
                gameSettingChangeListener?.onLightChange(progress)
            }
        })
        dataBinding.sbVoice.setOnSeekBarChangeListener(object : SeekBarChangListenerImp() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                LogUtils.d("onProgressChanged->voice=$progress, fromUser=$fromUser")
                gameSettingChangeListener?.onVoiceChange(progress)
                if (initialized) {
                    gameView?.setAudioMute(false)
                    dataBinding.btnMute.isSelected = true
                    SPUtils.getInstance().put(GameConstants.volumeSwitch, true)
                }
            }
        })
        dataBinding.sbSensitivity.setOnSeekBarChangeListener(object : SeekBarChangListenerImp() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                LogUtils.d("onProgressChanged->sensitivity=$progress, fromUser=$fromUser")
                if (fromUser) {
                    gameView?.mouseSensitivity = progress / 10f
                    SPUtils.getInstance().put(GameConstants.mouseSensitivity, progress)
                }
            }
        })
        dataBinding.layoutSettings.setOnClickListener {
            hideLayout()
        }
    }

    // 初始化设置展示
    fun initSettings(
        gameView: HmcpVideoView?,
        volume: Int,
        maxVolume: Int,
        light: Float,
        virtualOperateType: AppVirtualOperateType,
        playTime: Long,
        playStartTime: Long,
        gamePlayTime: Long,
        vipTime: Long,
        peakChannel: Boolean,
        mobileGame: Boolean
    ) {
        this.gameView = gameView
        // 控制方法
        this.lastControlType = virtualOperateType
        this.controllerType = virtualOperateType
        // 是否高峰通道
        this.peakChannel = peakChannel
        // 手游不展示控制方法
        updateControllerMethod(if (mobileGame) View.GONE else VISIBLE)
        // 配置声音/亮度
        updateVoice(maxVolume, volume)
        updateLight((light * 100).toInt())
        // 按键震动
        dataBinding.btnVibrate.isSelected = SPUtils.getInstance().getBoolean(GameConstants.vibrable, true)
        initSensitivity()
        // 鼠标模式
        initMouseMode()
        // 会员处理
        this.vipTime = vipTime
        // 非VIP用户，无法自定义控制方法，切换清晰度，关闭云互动
        if (vipTime <= (GameManager.getGameParam()?.realTime ?: 0)) {
            dataBinding.btnInteraction.isSelected = true
            gameSettingChangeListener?.onLiveInteractionChange(true)
        }
        if (vipTime <= (GameManager.getGameParam()?.realTime ?: 0)) {
            dataBinding.tvQuality.text = "标清"
            gameView?.resolutionList?.let { list ->
                gameSettingChangeListener?.onImageQualityChange(list.last())
            }
        } else {
            dataBinding.tvQuality.text = "蓝光"
            gameView?.resolutionList?.let { list ->
                gameSettingChangeListener?.onImageQualityChange(list.first())
            }
        }
        // 时间处理
        this.playTime = playTime
        updateAvailableTime(playTime)
        this.playedTime = if (playStartTime > 0) ((GameManager.getGameParam()?.realTime ?: 0) - playStartTime) / 1000L else 0L
        this.currentPlayTime = gamePlayTime / 1000L
        LogUtils.d("playTime=$playTime, playedTime=$playedTime, currentPlayTime=$currentPlayTime")
        startCountTime()
        initialized = true
    }

    private fun initSensitivity() {
        val sensitivity = SPUtils.getInstance().getInt(GameConstants.mouseSensitivity, 10)
        dataBinding.sbSensitivity.max = 20
        dataBinding.sbSensitivity.progress = sensitivity
        gameView?.mouseSensitivity = sensitivity / 10f
    }

    private fun updateControllerMethod(visibility: Int) {
        dataBinding.controlMethod.visibility = visibility
        dataBinding.layoutControlMethod.visibility = visibility
    }

    private fun initMouseMode() {
        when (SPUtils.getInstance().getInt(GameConstants.mouseMode, 2)) {
            // 默认模式：鼠标不跟随手指，触屏点击
            TouchMode.TOUCH_MODE_MOUSE.ordinal -> {
                dataBinding.btnMouseClick.isSelected = true
                dataBinding.switchMouseConfig.isChecked = true
                currentTouchMode = TouchMode.TOUCH_MODE_MOUSE
                gameView?.touchMode = TouchMode.TOUCH_MODE_MOUSE
            }
            // 触控模式：鼠标跟随手指，跟随手指
            TouchMode.TOUCH_MODE_SCREEN.ordinal -> {
                dataBinding.btnTouchClick.isSelected = true
                dataBinding.switchMouseConfig.isChecked = true
                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN
                gameView?.touchMode = TouchMode.TOUCH_MODE_SCREEN
            }
            // 滑屏模式：触屏不点击
            TouchMode.TOUCH_MODE_SCREEN_SLIDE.ordinal -> {
                dataBinding.btnTouchAttack.isSelected = true
                dataBinding.switchMouseConfig.isChecked = true
                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
                gameView?.touchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
            }
            // 禁用模式：鼠标不展示，触屏不移动，不点击
            TouchMode.TOUCH_MODE_NONE.ordinal -> {
                gameView?.touchMode = TouchMode.TOUCH_MODE_NONE
                dataBinding.switchMouseConfig.isChecked = false
                dataBinding.btnMouseClick.isEnabled = false
                dataBinding.btnTouchClick.isEnabled = false
                dataBinding.btnTouchAttack.isEnabled = false
                dataBinding.sbSensitivity.isEnabled = false
            }
        }
    }

    fun updateMouseMode(enable: Boolean = true) {
        dataBinding.switchMouseConfig.isEnabled = enable
        dataBinding.sbSensitivity.isEnabled = enable
        mouseModeEditable = enable
        gameView?.touchMode?.let {
            LogUtils.d("updateTouchMode:$it")
            when (it) {
                TouchMode.TOUCH_MODE_MOUSE -> {
                    dataBinding.btnMouseClick.isSelected = true && enable
                    dataBinding.btnTouchClick.isSelected = false
                    dataBinding.btnTouchAttack.isSelected = false
                    dataBinding.switchMouseConfig.isChecked = true && enable
                }

                TouchMode.TOUCH_MODE_SCREEN -> {
                    dataBinding.btnMouseClick.isSelected = false
                    dataBinding.btnTouchClick.isSelected = true && enable
                    dataBinding.btnTouchAttack.isSelected = false
                    dataBinding.switchMouseConfig.isChecked = true && enable
                }

                TouchMode.TOUCH_MODE_SCREEN_SLIDE -> {
                    dataBinding.btnMouseClick.isSelected = false
                    dataBinding.btnTouchClick.isSelected = false
                    dataBinding.btnTouchAttack.isSelected = true && enable
                    dataBinding.switchMouseConfig.isChecked = true && enable
                }

                TouchMode.TOUCH_MODE_NONE -> {
                    dataBinding.btnMouseClick.isSelected = false
                    dataBinding.btnTouchClick.isSelected = false
                    dataBinding.btnTouchAttack.isSelected = false
                    dataBinding.btnMouseClick.isEnabled = false
                    dataBinding.btnTouchClick.isEnabled = false
                    dataBinding.btnTouchAttack.isEnabled = false
                    dataBinding.sbSensitivity.isEnabled = false
                    dataBinding.switchMouseConfig.isChecked = false
                }

                else -> {}
            }
        }
    }

    private fun updateVibrate() {
        SPUtils.getInstance().put(GameConstants.vibrable, dataBinding.btnVibrate.isSelected)
    }

    // 显示可用时长
    private fun updateAvailableTime(time: Long) {
        LogUtils.d("updateAvailableTime:$time")
        dataBinding.tvAvailableTime.text = TimeUtils.getTimeString(time)
    }

    // 切换控制方法
    private fun updateControlType(type: AppVirtualOperateType) {
        LogUtils.d("updateControlType->type=$type")
        dataBinding.btnGamepad.isSelected = type == AppVirtualOperateType.APP_STICK_XBOX
        dataBinding.btnKeyboard.isSelected = type == AppVirtualOperateType.APP_KEYBOARD
    }

    // 声音
    private fun updateVoice(maxValue: Int, value: Int) {
        dataBinding.sbVoice.max = maxValue
        dataBinding.sbVoice.progress = value
        val volumeSwitch = SPUtils.getInstance().getBoolean(GameConstants.volumeSwitch)
        LogUtils.d("updateVoice->maxValue=$maxValue, value=$value, volumeSwitch=$volumeSwitch")
        dataBinding.btnMute.isSelected = volumeSwitch
        gameView?.setAudioMute(!volumeSwitch)
    }

    private fun updateLight(value: Int) {
        LogUtils.d("updateLight->value=$value")
        dataBinding.sbLight.progress = value
    }

    // 游戏计时
    private fun startCountTime() {
        if (taskScheduled) {
            return
        }
        countTimer.schedule(countTask, 0L, 1000L)
    }

    private val countTask: TimerTask = object : TimerTask() {
        override fun run() {
            taskScheduled = true
            post {
                playedTime += 1
                currentPlayTime -= 1
                if (currentPlayTime <= 300) {
                    gameSettingChangeListener?.onPlayTimeLack()
                }
                if (playedTime.toInt() % 2 == 0) {
                    updateNetDelay()
                }
//                if (peakChannel && GameManager.getGameParam()?.) {
//                    if (playTime > 0) {
//                        playTime -= 1
//                        updateAvailableTime(playTime)
//                    } else {
//                        updateAvailableTime(0)
//                    }
//                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNetDelay() {
        val latencyInfo = gameView?.clockDiffVideoLatencyInfo
//        LogUtils.d("updateNetDelay:${latencyInfo}")
        val delay = latencyInfo?.netDelay ?: 999
        val netDelay = if (delay > 450) 450 else delay
//        val netDelay = (40..400).random()
        // 延迟在0~60，展示满信号
        if (netDelay <= 60) {
            if (lastDelay > 60) {
                dataBinding.ivWifi.setImageResource(R.drawable.icon_wifi_full)
                dataBinding.tvNetDelay.setTextColor(Color.parseColor("#00D38E"))
                gameSettingChangeListener?.updateNetSignal(R.drawable.icon_wifi_full)
            }
        } else if (netDelay in 61..200) {
            // 延迟在61~200，展示中信号
            if (lastDelay <= 60 || lastDelay > 200) {
                dataBinding.ivWifi.setImageResource(R.drawable.icon_wifi_middle)
                dataBinding.tvNetDelay.setTextColor(Color.parseColor("#F7DC00"))
                gameSettingChangeListener?.updateNetSignal(R.drawable.icon_wifi_middle)
            }
        } else {
            // 延迟在201~999，展示无信号
            if (lastDelay <= 200) {
                dataBinding.ivWifi.setImageResource(R.drawable.icon_wifi_low)
                dataBinding.tvNetDelay.setTextColor(Color.parseColor("#FF2D2D"))
                gameSettingChangeListener?.updateNetSignal(R.drawable.icon_wifi_low)
            }
        }
        lastDelay = netDelay
        dataBinding.tvNetDelay.text = "${netDelay}ms"
        val packetsLostRate = latencyInfo?.packetsLostRate
        if (packetsLostRate?.isEmpty() != false) {
            return
        }
        val lostRate = packetsLostRate.toDouble()
        if (lostRate < 1.0) {
            if (lastLost >= 1.0) {
                dataBinding.tvLostRatio.setTextColor(Color.parseColor("#00D38E"))
            }
        } else if (lostRate in 1.0..3.0) {
            if (lastLost < 1.0 || lastLost > 3.0) {
                dataBinding.tvLostRatio.setTextColor(Color.parseColor("#F7DC00"))
            }
        } else {
            if (lastLost < 3.0) {
                dataBinding.tvLostRatio.setTextColor(Color.parseColor("#FF2D2D"))
            }
        }
        lastLost = lostRate
        dataBinding.tvLostRatio.text = "$lostRate%"
    }

    fun showLayout() {
        if (animated) {
            return
        }
        animated = true
        val animatorSet = AnimatorSet()

        val topTranslation = ObjectAnimator.ofFloat(dataBinding.layoutStatus, "translationY", -dataBinding.layoutStatus.height.toFloat(), 0.0f)
        topTranslation.duration = 400L
        topTranslation.interpolator = LinearInterpolator()
        val topAlpha = ObjectAnimator.ofFloat(dataBinding.layoutStatus, "alpha", 0.0f, 1.0f)
        topAlpha.duration = 400L
        topAlpha.interpolator = LinearInterpolator()

        val leftTranslation =
            ObjectAnimator.ofFloat(dataBinding.layoutSettingsLeft, "translationX", -dataBinding.layoutSettingsLeft.width.toFloat(), 0.0f)
        leftTranslation.duration = 400L
        leftTranslation.interpolator = LinearInterpolator()
        val leftAlpha = ObjectAnimator.ofFloat(dataBinding.layoutSettingsLeft, "alpha", 0.0f, 1.0f)
        leftAlpha.duration = 400L
        leftAlpha.interpolator = LinearInterpolator()

        val rightTranslation =
            ObjectAnimator.ofFloat(dataBinding.layoutSettingsRight, "translationX", dataBinding.layoutSettingsRight.width.toFloat(), 0.0f)
        rightTranslation.duration = 400L
        rightTranslation.interpolator = LinearInterpolator()
        val rightAlpha = ObjectAnimator.ofFloat(dataBinding.layoutSettingsRight, "alpha", 0.0f, 1.0f)
        rightAlpha.duration = 400L
        rightAlpha.interpolator = LinearInterpolator()

        animatorSet.playTogether(topTranslation, topAlpha, leftTranslation, leftAlpha, rightTranslation, rightAlpha)
        animatorSet.addListener(object : AnimatorListenerImp() {
            override fun onAnimationStart(animation: Animator) {
                dataBinding.layoutSettingsLeft.visibility = VISIBLE
                dataBinding.layoutStatus.visibility = VISIBLE
                dataBinding.layoutSettingsRight.visibility = VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                dataBinding.layoutSettings.isClickable = true
                animated = false
            }
        })
        animatorSet.start()
    }

    private fun hideLayout() {
        if (animated) {
            return
        }
        animated = true
        val animatorSet = AnimatorSet()
        val topTranslation = ObjectAnimator.ofFloat(dataBinding.layoutStatus, "translationY", 0.0f, -dataBinding.layoutStatus.height.toFloat())
        topTranslation.duration = 400L
        topTranslation.interpolator = LinearInterpolator()
        val topAlpha = ObjectAnimator.ofFloat(dataBinding.layoutStatus, "alpha", 1.0f, 0.0f)
        topAlpha.duration = 400L
        topAlpha.interpolator = LinearInterpolator()

        val leftTranslation =
            ObjectAnimator.ofFloat(dataBinding.layoutSettingsLeft, "translationX", 0.0f, -dataBinding.layoutSettingsLeft.width.toFloat())
        leftTranslation.duration = 400L
        leftTranslation.interpolator = LinearInterpolator()
        val leftAlpha = ObjectAnimator.ofFloat(dataBinding.layoutSettingsLeft, "alpha", 1.0f, 0.0f)
        leftAlpha.duration = 400L
        leftAlpha.interpolator = LinearInterpolator()

        val rightTranslation =
            ObjectAnimator.ofFloat(dataBinding.layoutSettingsRight, "translationX", 0.0f, dataBinding.layoutSettingsRight.width.toFloat())
        rightTranslation.duration = 400L
        rightTranslation.interpolator = LinearInterpolator()
        val rightAlpha = ObjectAnimator.ofFloat(dataBinding.layoutSettingsRight, "alpha", 1.0f, 0.0f)
        rightAlpha.duration = 400L
        rightAlpha.interpolator = LinearInterpolator()

        animatorSet.playTogether(topTranslation, topAlpha, leftTranslation, leftAlpha, rightTranslation, rightAlpha)
        animatorSet.addListener(object : AnimatorListenerImp() {

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                dataBinding.layoutSettings.isClickable = false

                dataBinding.layoutSettingsLeft.visibility = INVISIBLE
                dataBinding.layoutStatus.visibility = INVISIBLE
                dataBinding.layoutSettingsRight.visibility = INVISIBLE
                animated = false
                gameSettingChangeListener?.onHideLayout()
            }
        })
        animatorSet.start()
    }

    fun release() {
        initialized = false
        taskScheduled = false
        countTimer.cancel()
        countTimer.purge()
    }
}