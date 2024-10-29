package com.sayx.hm_cloud.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.media.atkit.widgets.AnTongVideoView
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

    private var gameView: View? = null

    // 用户当前可用高峰时长（单位：秒）
    private var playTime: Long = 0

    // 当前游戏可玩时长（单位：秒）
    private var currentPlayTime: Long = 0L

    // 当前游戏游玩时长（单位：秒）
    private var gamePlayTime: Long = 0L

    private var taskScheduled = false

    private var peakChannel = false

    private var lastDelay = 0

    private var lastLost = 0.0

    private var currentTouchMode = TouchMode.TOUCH_MODE_MOUSE

    private var animated = false

    private var mouseModeEditable = true

    var controllerType: AppVirtualOperateType = AppVirtualOperateType.NONE
        set(value) {
            field = value
            updateControlType(value)
        }

    init {
        // 可用时长点击，添加可用时长
        dataBinding.tvAvailableTime.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "加号",
            ))
            hideLayout()
            gameSettingChangeListener?.onAddAvailableTime()
        }
        dataBinding.btnRecharge.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "充值",
            ))
            gameSettingChangeListener?.onAddAvailableTime()
        }
        // 调试码点击，复制调试码到剪切板
        dataBinding.tvDebugCode.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "调试码",
            ))
            gameSettingChangeListener?.onDebugCodeClick()
        }
        // 控制方法
        dataBinding.btnGamepad.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "手柄",
            ))
            controllerType = AppVirtualOperateType.APP_STICK_XBOX
            gameSettingChangeListener?.onControlMethodChange(AppVirtualOperateType.APP_STICK_XBOX)
        }
        dataBinding.btnKeyboard.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "键盘",
            ))
            controllerType = AppVirtualOperateType.APP_KEYBOARD
            gameSettingChangeListener?.onControlMethodChange(AppVirtualOperateType.APP_KEYBOARD)
        }
        dataBinding.btnCustom.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "自定义",
            ))
            hideLayout()
            if (GameManager.getGameParam()?.isVip() == true) {
                gameSettingChangeListener?.onCustomSettings()
            } else {
                gameSettingChangeListener?.onShowVipDialog()
            }
        }
        // 震动开关
        dataBinding.btnVibrate.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "震动",
            ))
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                AppVibrateUtils.vibrate(force = true)
            }
            updateVibrate()
        }
        // 静音开关
        dataBinding.btnMute.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "静音",
            ))
            val value = !it.isSelected
            it.isSelected = value
            if (gameView is HmcpVideoView) {
                (gameView as HmcpVideoView).setAudioMute(!value)
            } else if (gameView is AnTongVideoView) {
                (gameView as AnTongVideoView).setAudioMute(!value)
            }
            SPUtils.getInstance().put(GameConstants.volumeSwitch, value)
        }
        // 画质选择
        dataBinding.tvQuality.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "画质切换",
            ))
            dataBinding.layoutQuality.visibility =
                if (dataBinding.layoutQuality.visibility == INVISIBLE) VISIBLE else INVISIBLE
        }
        dataBinding.tvStandardQuality.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "画质切换-标清",
            ))
            dataBinding.layoutQuality.visibility = INVISIBLE
            dataBinding.tvQuality.text = context.getString(R.string.standard_quality)
            if (gameView is HmcpVideoView) {
                (gameView as HmcpVideoView).resolutionList?.let { list ->
                    gameSettingChangeListener?.onImageQualityChange(list.last())
                }
            } else if (gameView is AnTongVideoView) {
                (gameView as AnTongVideoView).onSwitchResolution(4)
            }
        }
        dataBinding.tvBlueRay.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "画质切换-蓝光",
            ))
            dataBinding.layoutQuality.visibility = INVISIBLE
            if (GameManager.getGameParam()?.isVip() == true) {
                dataBinding.tvQuality.text = context.getString(R.string.blue_ray)
                if (gameView is HmcpVideoView) {
                    (gameView as HmcpVideoView).resolutionList?.let { list ->
                        gameSettingChangeListener?.onImageQualityChange(list.first())
                    }
                } else if (gameView is AnTongVideoView) {
                    (gameView as AnTongVideoView).onSwitchResolution(1)
                }
            } else {
                hideLayout()
                gameSettingChangeListener?.onShowVipDialog()
            }
        }
        // 云游互动开关
        dataBinding.btnInteraction.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "云游互动",
            ))
            if (gameView is AnTongVideoView) {
                return@setOnClickListener
            }
            if (GameManager.getGameParam()?.isVip() == true) {
                val value = !dataBinding.btnInteraction.isSelected
                dataBinding.btnInteraction.isSelected = value
                gameSettingChangeListener?.onLiveInteractionChange(value)
            } else {
                hideLayout()
                gameSettingChangeListener?.onShowVipDialog()
            }
        }
        dataBinding.btnPlayParty.setOnClickListener {
            hideLayout()
            gameSettingChangeListener?.onShowPlayParty()
        }
        // 鼠标点击
        dataBinding.btnMouseClick.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "鼠标点击",
            ))
            if (!mouseModeEditable) {
                ToastUtils.showLong(R.string.mouse_editable_notice)
            } else if (!it.isSelected) {
                it.isSelected = true
                currentTouchMode = TouchMode.TOUCH_MODE_MOUSE
                updateMouseMode(currentTouchMode)
            }
        }
        // 触控点击
        dataBinding.btnTouchClick.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "触控点击",
            ))
            if (!mouseModeEditable) {
                ToastUtils.showLong(R.string.mouse_editable_notice)
            } else if (!it.isSelected) {
                it.isSelected = true
                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN
                updateMouseMode(currentTouchMode)
            }
        }
        // 触屏攻击
        dataBinding.btnTouchAttack.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "触屏不攻击",
            ))
            if (!mouseModeEditable) {
                ToastUtils.showLong(R.string.mouse_editable_notice)
            } else if (!it.isSelected) {
                it.isSelected = true
                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
                updateMouseMode(currentTouchMode)
            }
        }
        // 退出游戏
        dataBinding.btnExitGame.setOnClickListener {
            GameManager.gameStat("游戏界面-点击", "click", mapOf(
                "gamepage-type" to "设置页面",
                "clickdpopup-content" to "退出游戏",
            ))
            hideLayout()
            gameSettingChangeListener?.onExitGame()
        }
        // 点击空白区域
        dataBinding.layoutSettings.setOnClickListener {
            hideLayout()
        }
    }

    // 初始化设置展示
    fun initSettings(
        gameView: View?,
        volume: Int,
        maxVolume: Int,
        light: Float,
        virtualOperateType: AppVirtualOperateType,
        userPeakTime: Long,
        playStartTime: Long,
        gamePlayTime: Long,
        peakChannel: Boolean,
        mobileGame: Boolean
    ) {
//        LogUtils.d("initSettings")
        this.gameView = gameView
        // 控制方法
        this.controllerType = virtualOperateType
        // 是否高峰通道
        this.peakChannel = peakChannel
        // 手游不展示控制方法
        updateControllerMethod(if (mobileGame) View.GONE else VISIBLE)
        // 配置声音/亮度
        updateVoice(maxVolume, volume)
        updateLight((light * 100).toInt())
        // 按键震动
        dataBinding.btnVibrate.isSelected =
            SPUtils.getInstance().getBoolean(GameConstants.vibrable, true)
        // 鼠标灵敏度
        initSensitivity()
        // 鼠标模式
        initMouseMode()
        if (gameView is HmcpVideoView) {
            // 默认开启云游互动
            dataBinding.btnInteraction.isSelected = true
        } else {
            dataBinding.btnInteraction.setDrawableTop(R.drawable.icon_interaction_normal)
            dataBinding.btnInteraction.setTextColor(Color.parseColor("#FF444855"))
            dataBinding.ivInteractionSign.visibility = View.INVISIBLE
            dataBinding.tvInteractionSign.visibility = View.VISIBLE
        }
        gameSettingChangeListener?.onLiveInteractionChange(true)
        // 非VIP用户，无法自定义控制方法，切换清晰度，关闭云互动
        // 非VIP用户默认使用标清，VIP用户默认蓝光
        if (GameManager.getGameParam()?.isVip() != true) {
            dataBinding.tvQuality.text = context.getString(R.string.standard_quality)
            if (gameView is HmcpVideoView) {
                gameView.resolutionList?.let { list ->
                    gameSettingChangeListener?.onImageQualityChange(list.last())
                }
            } else if (gameView is AnTongVideoView) {
                gameView.onSwitchResolution(4)
            }
        } else {
            dataBinding.tvQuality.text = context.getString(R.string.blue_ray)
            if (gameView is HmcpVideoView) {
                gameView.resolutionList?.let { list ->
                    gameSettingChangeListener?.onImageQualityChange(list.first())
                }
            } else if (gameView is AnTongVideoView) {
                gameView.onSwitchResolution(1)
            }
        }

        // 时间处理
        this.playTime = userPeakTime
        updateAvailableTime(userPeakTime)
        this.currentPlayTime = gamePlayTime / 1000L
        LogUtils.d("playTime=$userPeakTime, currentPlayTime=$currentPlayTime")
        // 如果是派对吧，且如果是游客，则不显示游戏倒计时

        if (GameManager.isPartyPlay) {
            if (GameManager.isPartyPlayOwner) {
                startCountTime()
            }
            dataBinding.btnPlayParty.visibility = View.VISIBLE
        } else {
            startCountTime()
            // 当前不是派对吧的情况下，隐藏派对吧条目
            dataBinding.btnPlayParty.visibility = View.GONE
        }
        initStatusListener()
    }

    private fun initStatusListener() {
        // 鼠标设置
        dataBinding.switchMouseConfig.setOnCheckedChangeListener { _, isChecked ->
//            LogUtils.d("MouseMode change:$isChecked, touchMode:${gameView?.touchMode}, currentTouchMode:$currentTouchMode, mouseModeEditable:$mouseModeEditable")
            if (mouseModeEditable) {
                if (isChecked) {
                    updateMouseMode(currentTouchMode)
                } else {
                    if (gameView is HmcpVideoView) {
                        currentTouchMode = (gameView as HmcpVideoView).touchMode
                    } else if (gameView is AnTongVideoView) {
                        when((gameView as AnTongVideoView).touchMode) {
                            com.media.atkit.enums.TouchMode.TOUCH_MODE_MOUSE -> {
                                currentTouchMode = TouchMode.TOUCH_MODE_MOUSE
                            }
                            com.media.atkit.enums.TouchMode.TOUCH_MODE_SCREEN -> {
                                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN
                            }
                            com.media.atkit.enums.TouchMode.TOUCH_MODE_SCREEN_SLIDE -> {
                                currentTouchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
                            }
                            com.media.atkit.enums.TouchMode.TOUCH_MODE_NONE -> {
                                currentTouchMode = TouchMode.TOUCH_MODE_NONE
                            }
                            else -> {

                            }
                        }
                    }
                    updateMouseMode(TouchMode.TOUCH_MODE_NONE)
                }
                dataBinding.sbSensitivity.isEnabled = isChecked
            }
        }
        // 亮度状态变更
        dataBinding.sbLight.setOnSeekBarChangeListener(object : SeekBarChangListenerImp() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                LogUtils.d("onProgressChanged->light=$progress, fromUser=$fromUser")
                gameSettingChangeListener?.onLightChange(progress)
            }
        })
        // 声音状态变更
        dataBinding.sbVoice.setOnSeekBarChangeListener(object : SeekBarChangListenerImp() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                LogUtils.d("onProgressChanged->voice=$progress, fromUser=$fromUser")
                gameSettingChangeListener?.onVoiceChange(progress)
                if (gameView is HmcpVideoView) {
                    (gameView as HmcpVideoView).setAudioMute(false)
                } else if (gameView is AnTongVideoView) {
                    (gameView as AnTongVideoView).setAudioMute(false)
                }
                dataBinding.btnMute.isSelected = true
                SPUtils.getInstance().put(GameConstants.volumeSwitch, true)
            }
        })
        // 鼠标灵敏度状态变更
        dataBinding.sbSensitivity.setOnSeekBarChangeListener(object : SeekBarChangListenerImp() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                LogUtils.d("onProgressChanged->sensitivity=$progress, fromUser=$fromUser")
                if (fromUser) {
                    if (gameView is HmcpVideoView) {
                        (gameView as HmcpVideoView).mouseSensitivity = progress / 10f
                    } else if (gameView is AnTongVideoView) {
                        (gameView as AnTongVideoView).setMouseSensitivity(progress / 10f)
                    }
                    SPUtils.getInstance().put(GameConstants.mouseSensitivity, progress)
                }
            }
        })
    }

    private fun initSensitivity() {
        if (gameView is HmcpVideoView) {
            val sensitivity = SPUtils.getInstance().getInt(GameConstants.mouseSensitivity, 10)
            dataBinding.sbSensitivity.max = 20
            dataBinding.sbSensitivity.progress = sensitivity
            (gameView as HmcpVideoView).mouseSensitivity = sensitivity / 10f
        } else if (gameView is AnTongVideoView) {
            val sensitivity = SPUtils.getInstance().getInt(GameConstants.mouseSensitivity, 30)
            dataBinding.sbSensitivity.max = 60
            dataBinding.sbSensitivity.progress = sensitivity
            (gameView as AnTongVideoView).setMouseSensitivity(sensitivity / 10f)
        }
    }

    private fun updateControllerMethod(visibility: Int) {
        dataBinding.controlMethod.visibility = visibility
        dataBinding.layoutControlMethod.visibility = visibility
    }

    private fun initMouseMode() {
        dataBinding.btnMouseClick.isSelected = true
        dataBinding.switchMouseConfig.isChecked = true
        currentTouchMode = TouchMode.TOUCH_MODE_MOUSE
        updateMouseMode(currentTouchMode)
    }

    fun setPCMouseMode(enable: Boolean) {
        dataBinding.switchMouseConfig.isEnabled = enable
        dataBinding.sbSensitivity.isEnabled = enable
        mouseModeEditable = enable
    }

    private fun updateMouseMode(touchMode: TouchMode) {
        LogUtils.d("updateMouseMode:$touchMode")
        when (touchMode) {
            TouchMode.TOUCH_MODE_MOUSE -> {
                dataBinding.btnMouseClick.isSelected = true
                dataBinding.btnTouchClick.isSelected = false
                dataBinding.btnTouchAttack.isSelected = false
                if (!dataBinding.switchMouseConfig.isChecked) {
                    dataBinding.switchMouseConfig.isChecked = true
                }

                if (gameView is HmcpVideoView) {
                    (gameView as HmcpVideoView).touchMode = TouchMode.TOUCH_MODE_MOUSE
                } else if (gameView is AnTongVideoView) {
                    (gameView as AnTongVideoView).touchMode = com.media.atkit.enums.TouchMode.TOUCH_MODE_MOUSE
                }
            }

            TouchMode.TOUCH_MODE_SCREEN -> {
                dataBinding.btnMouseClick.isSelected = false
                dataBinding.btnTouchClick.isSelected = true
                dataBinding.btnTouchAttack.isSelected = false
                if (!dataBinding.switchMouseConfig.isChecked) {
                    dataBinding.switchMouseConfig.isChecked = true
                }

                if (gameView is HmcpVideoView) {
                    (gameView as HmcpVideoView).touchMode = TouchMode.TOUCH_MODE_SCREEN
                } else if (gameView is AnTongVideoView) {
                    (gameView as AnTongVideoView).touchMode = com.media.atkit.enums.TouchMode.TOUCH_MODE_SCREEN
                }
            }

            TouchMode.TOUCH_MODE_SCREEN_SLIDE -> {
                dataBinding.btnMouseClick.isSelected = false
                dataBinding.btnTouchClick.isSelected = false
                dataBinding.btnTouchAttack.isSelected = true
                if (!dataBinding.switchMouseConfig.isChecked) {
                    dataBinding.switchMouseConfig.isChecked = true
                }

                if (gameView is HmcpVideoView) {
                    (gameView as HmcpVideoView).touchMode = TouchMode.TOUCH_MODE_SCREEN_SLIDE
                } else if (gameView is AnTongVideoView) {
                    (gameView as AnTongVideoView).touchMode = com.media.atkit.enums.TouchMode.TOUCH_MODE_SCREEN_SLIDE
                }
            }

            TouchMode.TOUCH_MODE_NONE -> {
                dataBinding.btnMouseClick.isSelected = false
                dataBinding.btnTouchClick.isSelected = false
                dataBinding.btnTouchAttack.isSelected = false

                dataBinding.sbSensitivity.isEnabled = false
                if (dataBinding.switchMouseConfig.isChecked) {
                    dataBinding.switchMouseConfig.isChecked = false
                }

                if (gameView is HmcpVideoView) {
                    (gameView as HmcpVideoView).touchMode = TouchMode.TOUCH_MODE_NONE
                } else if (gameView is AnTongVideoView) {
                    (gameView as AnTongVideoView).touchMode = com.media.atkit.enums.TouchMode.TOUCH_MODE_NONE
                }
            }

            else -> {}
        }
    }

    private fun updateVibrate() {
        SPUtils.getInstance().put(GameConstants.vibrable, dataBinding.btnVibrate.isSelected)
    }

    // 显示可用高峰时长
    fun updateAvailableTime(time: Long) {
        LogUtils.d("updateAvailableTime:$time")
        dataBinding.tvAvailableTime.text = TimeUtils.getTimeString(time)
    }

    // 切换控制方法
    private fun updateControlType(type: AppVirtualOperateType) {
//        LogUtils.d("updateControlType->type=$type")
        dataBinding.btnGamepad.isSelected = type == AppVirtualOperateType.APP_STICK_XBOX
        dataBinding.btnKeyboard.isSelected = type == AppVirtualOperateType.APP_KEYBOARD
    }

    // 声音
    private fun updateVoice(maxValue: Int, value: Int) {
        dataBinding.sbVoice.max = maxValue
        dataBinding.sbVoice.progress = value
        val volumeSwitch = SPUtils.getInstance().getBoolean(GameConstants.volumeSwitch, true)
        LogUtils.d("updateVoice->maxValue=$maxValue, value=$value, volumeSwitch=$volumeSwitch")
        dataBinding.btnMute.isSelected = volumeSwitch
        if (gameView is HmcpVideoView) {
            (gameView as HmcpVideoView).setAudioMute(!volumeSwitch)
        } else if (gameView is AnTongVideoView) {
            (gameView as AnTongVideoView).setAudioMute(!volumeSwitch)
        }
    }

    private fun updateLight(value: Int) {
        LogUtils.d("updateLight->value=$value")
        dataBinding.sbLight.progress = value
    }

    fun updatePlayTime(time: Long) {
        currentPlayTime = time / 1000
    }

    // 游戏计时
    private fun startCountTime() {
        if (taskScheduled) {
            return
        }
        countTimer.schedule(countTask, 0L, 1000L)
    }

    val timeList = listOf(0L, 60L, 300L, 600L, 900L, 1800L, 3600L)

    private val countTask: TimerTask = object : TimerTask() {
        override fun run() {
            taskScheduled = true
            post {
                currentPlayTime -= 1
                if (timeList.contains(gamePlayTime) || gamePlayTime % 3600 == 0L) {
                    GameManager.statGameTime(if (gamePlayTime > 3600 * 24) 3600 * 24 else gamePlayTime)
                }
                if (gamePlayTime % 60 == 0L) {
                    GameManager.statGamePlay()
                }
                gamePlayTime += 1L
                if (currentPlayTime <= 300L) {
                    // 临下机还有5分钟
                    gameSettingChangeListener?.onPlayTimeLack(true)
                } else {
                    gameSettingChangeListener?.onPlayTimeLack(false)
                }
                updateNetDelay()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNetDelay() {
        val delay = gameSettingChangeListener?.getNetDelay() ?: 999


        val netDelay = if (delay > 450) 450 else delay
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
        val packetsLostRate = gameSettingChangeListener?.getPacketsLostRate() ?: ""

        if (packetsLostRate.isEmpty()) {
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

    @SuppressLint("SetTextI18n")
    fun showGameOffNotice() {
        val text = "本次游玩即将结束:${TimeUtils.getCountTime(currentPlayTime)}"
        val spannableString = SpannableString(text)
        val start = text.indexOf(":") + 1
        val colorSpan = ForegroundColorSpan(Color.parseColor("#FFC6EC4B"))
        spannableString.setSpan(colorSpan, start, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        dataBinding.tvGameOffNotice.text = spannableString
        if (dataBinding.layoutGameOffNotice.visibility == VISIBLE) {
            return
        }
        GameManager.gameStat("游戏界面", "show", mapOf(
            "api-platform" to "海马云",
            "gamepage-type" to "时长不足",
        ))
        val animatorSet = AnimatorSet()
        val translation = ObjectAnimator.ofFloat(
            dataBinding.layoutGameOffNotice,
            "translationY",
            -dataBinding.layoutGameOffNotice.height.toFloat(),
            0.0f
        )
        translation.duration = 400L
        translation.interpolator = LinearInterpolator()
        val alpha = ObjectAnimator.ofFloat(dataBinding.layoutGameOffNotice, "alpha", 0.0f, 1.0f)
        alpha.duration = 400L
        alpha.interpolator = LinearInterpolator()
        animatorSet.playTogether(translation, alpha)
        animatorSet.addListener(object : AnimatorListenerImp() {
            override fun onAnimationStart(animation: Animator) {
                dataBinding.layoutGameOffNotice.visibility = VISIBLE
            }
        })
        animatorSet.start()
    }

    fun hideGameOffNotice() {
        if (dataBinding.layoutGameOffNotice.visibility != VISIBLE) {
            return
        }
        dataBinding.layoutGameOffNotice.visibility = INVISIBLE
    }

    fun showLayout() {
        if (animated) {
            return
        }
        animated = true
        val animatorSet = AnimatorSet()

        val topTranslation = ObjectAnimator.ofFloat(
            dataBinding.layoutStatus,
            "translationY",
            -dataBinding.layoutStatus.height.toFloat(),
            0.0f
        )
        topTranslation.duration = 400L
        topTranslation.interpolator = LinearInterpolator()
        val topAlpha = ObjectAnimator.ofFloat(dataBinding.layoutStatus, "alpha", 0.0f, 1.0f)
        topAlpha.duration = 400L
        topAlpha.interpolator = LinearInterpolator()

        val leftTranslation =
            ObjectAnimator.ofFloat(
                dataBinding.layoutSettingsLeft,
                "translationX",
                -dataBinding.layoutSettingsLeft.width.toFloat(),
                0.0f
            )
        leftTranslation.duration = 400L
        leftTranslation.interpolator = LinearInterpolator()
        val leftAlpha = ObjectAnimator.ofFloat(dataBinding.layoutSettingsLeft, "alpha", 0.0f, 1.0f)
        leftAlpha.duration = 400L
        leftAlpha.interpolator = LinearInterpolator()

        val rightTranslation =
            ObjectAnimator.ofFloat(
                dataBinding.layoutSettingsRight,
                "translationX",
                dataBinding.layoutSettingsRight.width.toFloat(),
                0.0f
            )
        rightTranslation.duration = 400L
        rightTranslation.interpolator = LinearInterpolator()
        val rightAlpha =
            ObjectAnimator.ofFloat(dataBinding.layoutSettingsRight, "alpha", 0.0f, 1.0f)
        rightAlpha.duration = 400L
        rightAlpha.interpolator = LinearInterpolator()

        animatorSet.playTogether(
            topTranslation,
            topAlpha,
            leftTranslation,
            leftAlpha,
            rightTranslation,
            rightAlpha
        )
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
        val topTranslation = ObjectAnimator.ofFloat(
            dataBinding.layoutStatus,
            "translationY",
            0.0f,
            -dataBinding.layoutStatus.height.toFloat()
        )
        topTranslation.duration = 400L
        topTranslation.interpolator = LinearInterpolator()
        val topAlpha = ObjectAnimator.ofFloat(dataBinding.layoutStatus, "alpha", 1.0f, 0.0f)
        topAlpha.duration = 400L
        topAlpha.interpolator = LinearInterpolator()

        val leftTranslation =
            ObjectAnimator.ofFloat(
                dataBinding.layoutSettingsLeft,
                "translationX",
                0.0f,
                -dataBinding.layoutSettingsLeft.width.toFloat()
            )
        leftTranslation.duration = 400L
        leftTranslation.interpolator = LinearInterpolator()
        val leftAlpha = ObjectAnimator.ofFloat(dataBinding.layoutSettingsLeft, "alpha", 1.0f, 0.0f)
        leftAlpha.duration = 400L
        leftAlpha.interpolator = LinearInterpolator()

        val rightTranslation =
            ObjectAnimator.ofFloat(
                dataBinding.layoutSettingsRight,
                "translationX",
                0.0f,
                dataBinding.layoutSettingsRight.width.toFloat()
            )
        rightTranslation.duration = 400L
        rightTranslation.interpolator = LinearInterpolator()
        val rightAlpha =
            ObjectAnimator.ofFloat(dataBinding.layoutSettingsRight, "alpha", 1.0f, 0.0f)
        rightAlpha.duration = 400L
        rightAlpha.interpolator = LinearInterpolator()

        animatorSet.playTogether(
            topTranslation,
            topAlpha,
            leftTranslation,
            leftAlpha,
            rightTranslation,
            rightAlpha
        )
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
        taskScheduled = false
        countTimer.cancel()
        countTimer.purge()
    }
}