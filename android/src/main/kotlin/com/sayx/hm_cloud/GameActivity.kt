package com.sayx.hm_cloud

import android.animation.Animator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.JsonObject
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.navigationBarHeight
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.ResolutionInfo
import com.haima.hmcp.listeners.OnLivingListener
import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.noober.background.drawable.DrawableCreator
import com.sayx.hm_cloud.callback.AddKeyListenerImp
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.ControllerEventCallback
import com.sayx.hm_cloud.callback.EditCallback
import com.sayx.hm_cloud.callback.GameSettingChangeListener
import com.sayx.hm_cloud.callback.HideListener
import com.sayx.hm_cloud.callback.OnEditClickListener
import com.sayx.hm_cloud.callback.OnTypeListener
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.OnKeyEventListenerImp
import com.sayx.hm_cloud.constants.OnRockerOperationListenerImp
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ActivityGameBinding
import com.sayx.hm_cloud.dialog.AppCommonDialog
import com.sayx.hm_cloud.dialog.ControllerTypeDialog
import com.sayx.hm_cloud.dialog.GameErrorDialog
import com.sayx.hm_cloud.model.ControllerChangeEvent
import com.sayx.hm_cloud.model.ControllerConfigEvent
import com.sayx.hm_cloud.model.ControllerEditEvent
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.model.PCMouseEvent
import com.sayx.hm_cloud.model.PlayPartyRoomInfoEvent
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.widget.AddGamepadKey
import com.sayx.hm_cloud.widget.AddKeyboardKey
import com.sayx.hm_cloud.widget.ControllerEditLayout
import com.sayx.hm_cloud.widget.EditCombineKey
import com.sayx.hm_cloud.widget.EditRouletteKey
import com.sayx.hm_cloud.widget.GameSettings
import com.sayx.hm_cloud.widget.PlayPartyGameView
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Timer
import java.util.TimerTask

class GameActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityGameBinding

    private var gameSettings: GameSettings? = null

    private var controllerEditLayout: ControllerEditLayout? = null

    private var addKeyboardKey: AddKeyboardKey? = null

    private var addGamepadKey: AddGamepadKey? = null

    private var editCombineKey: EditCombineKey? = null

    private var editRouletteKey: EditRouletteKey? = null

    // 编辑状态无响应处理
    private var inputTimer: Timer? = null

    // 声音控制
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // 剪切板
    private val clipboardManager: ClipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 全屏
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        // 设置屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 事件监听
        EventBus.getDefault().register(this)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_game)
        dataBinding.lifecycleOwner = this

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
        AppSizeUtils.navigationBarHeight = navigationBarHeight
        initView()
    }

    private fun initView() {
        GameManager.gameView?.let {
            dataBinding.gameController.addView(
                it,
                0,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        GameManager.gameView?.setAttachContext(this)
        GameManager.gameView?.virtualDeviceType = VirtualOperateType.NONE
        // 游戏设置
        dataBinding.btnGameSettings.setOnClickListener {
            val showGuide = SPUtils.getInstance().getBoolean(GameConstants.showGuide)
            if (!showGuide) {
                dataBinding.layoutGuide.visibility = View.GONE
                dataBinding.guideMaskView.visibility = View.GONE
                dataBinding.layoutGuide.clearAnimation()
                SPUtils.getInstance().put(GameConstants.showGuide, true)
            }
            showGameSetting()
        }
        dataBinding.btnVirtualKeyboard.setOnClickListener {
            LogUtils.d("显示游戏输入法键盘")
            try {
                GameManager.gameView?.switchKeyboard(true)
            } catch (e: Exception) {
                LogUtils.e(e.message)
            }
        }
        dataBinding.gameController.listener = object : OnEditClickListener {
            override fun onEditKeyClick(keyInfo: KeyInfo) {
                // 自定义按钮编辑状态点击
                controllerEditLayout?.setKeyInfo(keyInfo)
            }
        }
        // 游戏控制器数据反馈
        dataBinding.gameController.controllerCallback = object : ControllerEventCallback {
            override fun getDefaultKeyboardData() {
                // 需要默认键盘配置
                GameManager.getDefaultKeyboardData()
            }

            override fun getKeyboardData() {
                // 需要键盘配置
                GameManager.getKeyboardData()
            }

            override fun getDefaultGamepadData() {
                // 需要默认手柄配置
                GameManager.getDefaultGamepadData()
            }

            override fun getGamepadData() {
                // 需要手柄配置
                GameManager.getGamepadData()
            }

            override fun updateKeyboardData(data: JsonObject) {
                // 需要更新配置
                GameManager.updateKeyboardData(data)
            }
        }
        // 游戏控制器按键操作处理
        dataBinding.gameController.keyEventListener = object : OnKeyEventListenerImp() {}

        // 游戏控制器摇杆操作处理
        dataBinding.gameController.rockerListener = object : OnRockerOperationListenerImp() {}

        // 检查是否展示引导
        checkGuideShow()

        // 初始化设置面板
        initGameSettings()

        initPlayPartyView()
    }

    private var playPartyGameView: PlayPartyGameView? = null

    private fun initPlayPartyView() {
        playPartyGameView = PlayPartyGameView(this)
        playPartyGameView?.visibility = View.GONE
        // 将设置面板控件加入主面板
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dataBinding.layoutGame.addView(playPartyGameView, layoutParams)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    private fun checkGuideShow() {
        val showGuide = SPUtils.getInstance().getBoolean(GameConstants.showGuide)
        if (!showGuide) {
            showGuideView()
        }
    }

    /**
     * 引导面板动画
     */
    private fun showGuideView() {
        dataBinding.layoutGuide.visibility = View.VISIBLE
        dataBinding.guideMaskView.visibility = View.VISIBLE
        val animation = ScaleAnimation(
            1.0f,
            0.9f,
            1.0f,
            0.9f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        animation.duration = 1000
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        animation.interpolator = LinearInterpolator()
        dataBinding.layoutGuide.startAnimation(animation)
    }

    private fun initGameSettings() {
        if (gameSettings != null) {
            // 避免二次初始化
            return
        }
        // 获取系统音量
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        // 获取系统最高音量
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // 获取系统屏幕亮度值
        val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        // 取得亮度比
        val light = brightness / 255f
        LogUtils.d("screenBrightness:$light, systemBrightness:$brightness")
        // 设置当前window亮度
        val attributes = window.attributes
        attributes.screenBrightness = light
        window.attributes = attributes

        // 创建设置面板控件
        gameSettings = GameSettings(this)
        configSettingCallback()
        gameSettings?.initSettings(
            GameManager.gameView,
            volume,
            maxVolume,
            light,
            AppVirtualOperateType.NONE,
            // 用户高峰时长
            GameManager.getGameParam()?.peakTime ?: 0L,
            // 本次游戏开始时间（重连获取游戏记录可获取本次游戏开始时间，目前重连存在问题，待完善）
            0,
            // 本次游戏可玩时长
            GameManager.getGameParam()?.playTime ?: 0L,
            // 本次是否使用高峰通道进入
            GameManager.getGameParam()?.isPeakChannel ?: false,
            // 当前是否海马手游
            false
        )
        // 将设置面板控件加入主面板
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dataBinding.layoutGame.post {
            dataBinding.layoutGame.addView(gameSettings, layoutParams)
        }

        // 页面初始化完成，获取数据根据数据进行同步处理
        GameManager.getGameData()
        if (GameManager.isPartyPlay) {
            GameManager.updatePlayPartyRoomInfo()
        }
    }

    private fun configSettingCallback() {
        // 游戏设置监听
        gameSettings?.gameSettingChangeListener = object : GameSettingChangeListener {
            override fun onAddAvailableTime() {
                LogUtils.d("onAddAvailableTime")
                // 前往购买时长
                GameManager.openBuyPeakTime()
            }

            override fun onDebugCodeClick() {
                val str =
                    "cid:${HmcpManager.getInstance().cloudId},uid:${GameManager.getGameParam()?.userId}"
                LogUtils.d("onDebugCodeClick:$str")
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, str))
                ToastUtils.showShort(R.string.clip_success)
            }

            override fun onControlMethodChange(operateType: AppVirtualOperateType) {
                LogUtils.d("onControlMethodChange:$operateType")
                when (operateType) {
                    AppVirtualOperateType.APP_STICK_XBOX -> {
                        dataBinding.gameController.controllerType =
                            AppVirtualOperateType.APP_STICK_XBOX
                    }

                    AppVirtualOperateType.APP_KEYBOARD -> {
                        dataBinding.gameController.controllerType =
                            AppVirtualOperateType.APP_KEYBOARD
                    }

                    else -> {
                        dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
                    }
                }
            }

            override fun onLiveInteractionChange(status: Boolean) {
                LogUtils.d("onLiveInteractionChange:$status")
                val cloudId = HmcpManager.getInstance().cloudId
                if (status) {
                    // 开启直播
                    val liveUrl = "rtmp://push-cg.3ayx.net/live/$cloudId"
                    GameManager.gameView
                        ?.startLiving(cloudId, liveUrl, object : OnLivingListener {
                            override fun start(success: Boolean, msg: String?) {
                                LogUtils.d("startLiving:$success, $msg, url:$liveUrl")
                                GameManager.openInteraction(cloudId, true)
                            }

                            override fun stop(success: Boolean, msg: String?) {
                                LogUtils.d("startLiving:$success, $msg")
                            }
                        })
                } else {
                    // 停止直播
                    GameManager.openInteraction(cloudId, false)
                }
            }

            override fun onImageQualityChange(resolution: ResolutionInfo) {
                LogUtils.d("onImageQualityChange:$resolution")
                GameManager.gameView?.onSwitchResolution(0, resolution, 0)
            }

            override fun onLightChange(light: Int) {
                val attributes = window.attributes
                val brightness = light / 100f
                LogUtils.d("onLightChange:$brightness")
                attributes.screenBrightness = brightness
                window.attributes = attributes
            }

            override fun onVoiceChange(volume: Int) {
                LogUtils.d("onVoiceChange:$volume")
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            }

            override fun onExitGame() {
                LogUtils.d("onExitGame")
                showExitGameDialog()
            }

            override fun onCustomSettings() {
                showChooseControllerDialog()
            }

            override fun onShowVipDialog() {
                showJoinVipDialog()
            }

            override fun onHideLayout() {
                dataBinding.btnGameSettings.visibility = View.VISIBLE
                dataBinding.btnVirtualKeyboard.visibility = View.VISIBLE
            }

            override fun onPlayTimeLack() {
                runOnUiThread {
                    gameSettings?.showGameOffNotice()
                }
            }

            override fun updateNetSignal(icon: Int) {
                dataBinding.btnGameSettings.setImageResource(icon)
            }

            override fun onShowPlayParty() {
                playPartyGameView?.show()
            }
        }
    }

    private fun showGameSetting() {
        dataBinding.btnGameSettings.visibility = View.INVISIBLE
        dataBinding.btnVirtualKeyboard.visibility = View.INVISIBLE
        gameSettings?.showLayout()
    }

    private fun showChooseControllerDialog() {
        ControllerTypeDialog.showDialog(this, object : OnTypeListener {
            override fun onKeyboardType() {
                ControllerTypeDialog.hideDialog(this@GameActivity)
                showControllerEdit(AppVirtualOperateType.APP_KEYBOARD)
            }

            override fun onGamepadType() {
                ControllerTypeDialog.hideDialog(this@GameActivity)
                showControllerEdit(AppVirtualOperateType.APP_STICK_XBOX)
            }
        })
    }

    private fun showControllerEdit(type: AppVirtualOperateType) {
        if (controllerEditLayout != null) {
            dataBinding.layoutGame.removeView(controllerEditLayout)
        }
        controllerEditLayout = ControllerEditLayout(this)
        configControllerEditCallback()
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dataBinding.layoutGame.post {
            dataBinding.layoutGame.addView(controllerEditLayout, layoutParams)
            // 进入编辑模式，隐藏游戏设置按钮与键盘呼出按钮
            dataBinding.btnGameSettings.visibility = View.INVISIBLE
            dataBinding.btnVirtualKeyboard.visibility = View.INVISIBLE
            // 展示自定义控制面板，让游戏画面无法触摸操作
            dataBinding.gameController.controllerType = type
            dataBinding.gameController.maskEnable = true
            // 进入编辑模式，防止操作过久，游戏出现无操作退出，每5分钟重置无操作时间，后台设置无操作下线时间为10分钟
            updateInputTimer()
        }
    }

    private fun updateInputTimer() {
        try {
            if (inputTimer != null) {
                inputTimer?.cancel()
                inputTimer = null
            }
            inputTimer = Timer()
            inputTimer?.schedule(object : TimerTask() {
                override fun run() {
                    val result = GameManager.gameView?.resetInputTimer()
                    LogUtils.d("resetInputTimer:$result")
                }
            }, 0L, 5 * 60 * 1000L)
        } catch (e: Exception) {
            LogUtils.e("updateInputTimer:${e.message}")
        }
    }

    private fun configControllerEditCallback() {
        controllerEditLayout?.setCallback(object : EditCallback {
            override fun onExitEdit() {
                showExitCustomDialog()
            }

            override fun onSaveEdit() {
                showSaveCustomDialog()
            }

            override fun onAddKey() {
                showKeyBoard(true)
            }

            override fun onAddCombineKey() {
                // 隐藏编辑栏，打开组合键编辑，按键面板
                controllerEditLayout?.hideLayout(object : AnimatorListenerImp() {
                    override fun onAnimationEnd(animation: Animator) {
                        showEditCombineKeyLayout()
                    }
                })
            }

            override fun onAddRouletteKey() {
                // 隐藏编辑栏，打开轮盘键编辑，按键面板
                controllerEditLayout?.hideLayout(object : AnimatorListenerImp() {
                    override fun onAnimationEnd(animation: Animator) {
                        showEditRouletteKeyLayout()
                    }
                })
            }

            override fun onRestoreDefault() {
                showRestoreCustomDialog()
            }

            override fun onAddKeySize() {
                dataBinding.gameController.updateKey()
            }

            override fun onReduceKeySize() {
                dataBinding.gameController.updateKey()
            }

            override fun onAddKeyOpacity() {
                dataBinding.gameController.updateKey()
            }

            override fun onReduceKeyOpacity() {
                dataBinding.gameController.updateKey()
            }

            override fun onTextChange() {
                dataBinding.gameController.updateKey()
            }

            override fun onDeleteKey() {
                dataBinding.gameController.deleteKey()
            }

            override fun onEditCombine(keyInfo: KeyInfo) {
                LogUtils.d("onEditCombine:$keyInfo")
                controllerEditLayout?.hideLayout(object : AnimatorListenerImp() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (keyInfo.type == KeyType.KEY_COMBINE || keyInfo.type == KeyType.GAMEPAD_COMBINE) {
                            showEditCombineKeyLayout(keyInfo)
                        } else if (keyInfo.type == KeyType.KEY_ROULETTE || keyInfo.type == KeyType.GAMEPAD_ROULETTE) {
                            showEditRouletteKeyLayout(keyInfo)
                        }
                    }
                })
            }
        })
    }

    private fun showEditCombineKeyLayout(keyInfo: KeyInfo? = null) {
        if (editCombineKey == null) {
            editCombineKey = EditCombineKey(this)
            editCombineKey?.setCombineKeyInfo(keyInfo)
            editCombineKey?.onHideListener = object : HideListener {
                override fun onHide() {
                    controllerEditLayout?.showLayout()
                    hideKeyBoard()
                }
            }
            editCombineKey?.addKeyListener = object : AddKeyListenerImp() {
                override fun onAddKey(keyInfo: KeyInfo) {
                    if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_KEYBOARD) {
                        keyInfo.type = KeyType.KEY_COMBINE
                    } else if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
                        keyInfo.type = KeyType.GAMEPAD_COMBINE
                    }
                    dataBinding.gameController.addCombineKey(
                        keyInfo,
                        dataBinding.gameController.controllerType
                    )
                    controllerEditLayout?.setKeyInfo(keyInfo)
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dataBinding.layoutGame.post {
                dataBinding.layoutGame.addView(editCombineKey, layoutParams)
            }
        } else if (editCombineKey?.isShow != true) {
            editCombineKey?.setCombineKeyInfo(keyInfo)
            editCombineKey?.showBoard()
        }
        showKeyBoard(false)
    }

    private fun showEditRouletteKeyLayout(keyInfo: KeyInfo? = null) {
        if (editRouletteKey == null) {
            editRouletteKey = EditRouletteKey(this)
            editRouletteKey?.setRouletteKeyInfo(keyInfo)
            editRouletteKey?.onHideListener = object : HideListener {
                override fun onHide() {
                    controllerEditLayout?.showLayout()
                    hideKeyBoard()
                }
            }
            editRouletteKey?.addKeyListener = object : AddKeyListenerImp() {
                override fun onAddKey(keyInfo: KeyInfo) {
                    if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_KEYBOARD) {
                        keyInfo.type = KeyType.KEY_ROULETTE
                    } else if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
                        keyInfo.type = KeyType.GAMEPAD_ROULETTE
                    }
                    dataBinding.gameController.addRouletteKey(
                        keyInfo,
                        dataBinding.gameController.controllerType
                    )
                    controllerEditLayout?.setKeyInfo(keyInfo)
                }

                override fun onUpdateKey() {
                    dataBinding.gameController.updateKey()
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dataBinding.layoutGame.post {
                dataBinding.layoutGame.addView(editRouletteKey, layoutParams)
            }
        } else if (editRouletteKey?.isShow != true) {
            editRouletteKey?.setRouletteKeyInfo(keyInfo)
            editRouletteKey?.showBoard()
        }
        showKeyBoard(false)
    }

    private fun showExitCustomDialog() {
        AppCommonDialog.Builder(this)
            .setTitle(getString(R.string.title_exit_custom))
            .setLeftButton(getString(R.string.leave)) {
                AppCommonDialog.hideDialog(this)
                dataBinding.gameController.restoreOriginal()
                exitCustom()
            }
            .setRightButton(getString(R.string.save)) {
                AppCommonDialog.hideDialog(this)
                dataBinding.gameController.saveKeyConfig()
            }
            .setEnableCancel(true)
            .build().show()
    }

    private fun showKeyBoard(showRocker: Boolean) {
        LogUtils.d("showKeyBoard:$showRocker, ${dataBinding.gameController.controllerType}")
        if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            showAddKeyboardView(showRocker)
        } else if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            showAddGamepadView(showRocker)
        }
    }

    private fun hideKeyBoard() {
        LogUtils.d("hideKeyBoard")
        if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_KEYBOARD) {
            addKeyboardKey?.hideBoard(null)
        } else if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_STICK_XBOX) {
            addGamepadKey?.hideBoard(null)
        }
    }

    private fun showAddKeyboardView(showRocker: Boolean) {
        if (addKeyboardKey == null) {
            addKeyboardKey = AddKeyboardKey(this)
            addKeyboardKey?.showRocker = showRocker
            addKeyboardKey?.listener = object : AddKeyListenerImp() {
                override fun onAddKey(keyInfo: KeyInfo) {
                    if (controllerStatus == ControllerStatus.Edit) {
                        LogUtils.d("onEditKeyClick:$keyInfo")
                        when (keyInfo.type) {
                            KeyType.KEYBOARD_KEY, KeyType.KEYBOARD_MOUSE_UP, KeyType.KEYBOARD_MOUSE_DOWN, KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                                dataBinding.gameController.addKeyButton(
                                    keyInfo,
                                    AppVirtualOperateType.APP_KEYBOARD
                                )
                            }

                            KeyType.ROCKER_LETTER, KeyType.ROCKER_ARROW -> {
                                dataBinding.gameController.addRocker(
                                    keyInfo,
                                    AppVirtualOperateType.APP_KEYBOARD
                                )
                            }
                        }
                        controllerEditLayout?.setKeyInfo(keyInfo)
                    } else if (controllerStatus == ControllerStatus.Combine) {
                        addCombineKey(keyInfo)
                    }
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.BOTTOM
            dataBinding.layoutGame.post {
                dataBinding.layoutGame.addView(addKeyboardKey, layoutParams)
            }
            return
        }
        if (addKeyboardKey?.isShow == true) {
            addKeyboardKey?.hideBoard(object : AnimatorListenerImp() {
                override fun onAnimationEnd(animation: Animator) {
                    addKeyboardKey?.showRocker = showRocker
                    addKeyboardKey?.showBoard()
                }
            })
        } else {
            addKeyboardKey?.showRocker = showRocker
            addKeyboardKey?.showBoard()
        }
    }

    private fun showAddGamepadView(showRocker: Boolean) {
        if (addGamepadKey == null) {
            addGamepadKey = AddGamepadKey(this)
            addGamepadKey?.showRocker = showRocker
            addGamepadKey?.listener = object : AddKeyListenerImp() {
                override fun onAddKey(keyInfo: KeyInfo) {
                    if (controllerStatus == ControllerStatus.Edit) {
                        LogUtils.d("onEditKeyClick:$keyInfo")
                        when (keyInfo.type) {
                            KeyType.GAMEPAD_SQUARE, KeyType.GAMEPAD_ELLIPTIC, KeyType.GAMEPAD_ROUND_MEDIUM, KeyType.GAMEPAD_ROUND_SMALL -> {
                                dataBinding.gameController.addKeyButton(
                                    keyInfo,
                                    AppVirtualOperateType.APP_STICK_XBOX
                                )
                            }

                            KeyType.ROCKER_RIGHT, KeyType.ROCKER_LEFT -> {
                                dataBinding.gameController.addRocker(
                                    keyInfo,
                                    AppVirtualOperateType.APP_STICK_XBOX
                                )
                            }

                            KeyType.ROCKER_CROSS -> {
                                dataBinding.gameController.addCrossRocker(
                                    keyInfo,
                                    AppVirtualOperateType.APP_STICK_XBOX
                                )
                            }
                        }
                        controllerEditLayout?.setKeyInfo(keyInfo)
                    } else if (controllerStatus == ControllerStatus.Combine) {
                        addCombineKey(keyInfo)
                    }
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.BOTTOM
            dataBinding.layoutGame.post {
                dataBinding.layoutGame.addView(addGamepadKey, layoutParams)
            }
            return
        }

        if (addGamepadKey?.isShow == true) {
            addGamepadKey?.hideBoard(object : AnimatorListenerImp() {
                override fun onAnimationEnd(animation: Animator) {
                    addGamepadKey?.showRocker = showRocker
                    addGamepadKey?.showBoard()
                }
            })
        } else {
            addGamepadKey?.showRocker = showRocker
            addGamepadKey?.showBoard()
        }
    }

    private fun addCombineKey(keyInfo: KeyInfo) {
        if (editCombineKey?.isShow == true) {
            editCombineKey?.addKey(keyInfo)
        } else if (editRouletteKey?.isShow == true) {
            editRouletteKey?.addKey(keyInfo)
        }
    }

    private fun showRestoreCustomDialog() {
        AppCommonDialog.Builder(this)
            .setTitle(getString(R.string.title_restore_custom))
            .setLeftButton(getString(R.string.cancel)) {
                AppCommonDialog.hideDialog(this)
            }
            .setRightButton(getString(R.string.confirm_restore)) {
                AppCommonDialog.hideDialog(this)
                dataBinding.gameController.restoreDefault()
            }
            .build().show()
    }

    private fun showSaveCustomDialog() {
        AppCommonDialog.Builder(this)
            .setTitle(getString(R.string.title_save_custom))
            .setLeftButton(getString(R.string.cancel)) {
                AppCommonDialog.hideDialog(this)
            }
            .setRightButton(getString(R.string.confirm_save)) {
                AppCommonDialog.hideDialog(this)
                dataBinding.gameController.saveKeyConfig()
            }
            .build().show()
    }

    private fun showJoinVipDialog() {
        AppCommonDialog.Builder(this)
            .setTitle(getString(R.string.title_join_vip))
            .setSubTitle(getString(R.string.subtitle_join_vip), Color.GRAY)
            .setLeftButton(getString(R.string.cancel)) {
                AppCommonDialog.hideDialog(
                    this,
                    "hideJoinVipDialog"
                )
            }
            .setRightButton(getString(R.string.join_vip)) {
                AppCommonDialog.hideDialog(this, "hideJoinVipDialog")
                GameManager.openBuyVip()
            }
            .build().show("hideJoinVipDialog")
    }

    private fun exitCustom() {
        // 移除编辑面板
        controllerEditLayout?.let {
            dataBinding.layoutGame.removeView(it)
            controllerEditLayout = null
        }
        // 移除添加按键面板
        addGamepadKey?.let {
            dataBinding.layoutGame.removeView(it)
            addGamepadKey = null
        }
        addKeyboardKey?.let {
            dataBinding.layoutGame.removeView(it)
            addKeyboardKey = null
        }
        editCombineKey?.let {
            dataBinding.layoutGame.removeView(it)
            editCombineKey = null
        }
        dataBinding.btnGameSettings.visibility = View.VISIBLE
        dataBinding.btnVirtualKeyboard.visibility = View.VISIBLE

        gameSettings?.controllerType?.let { dataBinding.gameController.controllerType = it }

        controllerStatus = ControllerStatus.Normal

        dataBinding.gameController.maskEnable = false

        try {
            inputTimer?.cancel()
            inputTimer?.purge()
            inputTimer = null
        } catch (e: Exception) {
            LogUtils.e("exitCustom:${e.message}")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onControllerChangeEvent(event: ControllerChangeEvent) {
        when (event.type) {
            0 -> {
                dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
                gameSettings?.controllerType = AppVirtualOperateType.NONE
            }

            1 -> {
                dataBinding.gameController.controllerType = AppVirtualOperateType.APP_KEYBOARD
                gameSettings?.controllerType = AppVirtualOperateType.APP_KEYBOARD
            }

            2 -> {
                dataBinding.gameController.controllerType = AppVirtualOperateType.APP_STICK_XBOX
                gameSettings?.controllerType = AppVirtualOperateType.APP_STICK_XBOX
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPCMouseEvent(event: PCMouseEvent) {
        LogUtils.d("onPCMouseEvent:${event.open}")
        if (event.open) {
            dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
            gameSettings?.controllerType = AppVirtualOperateType.NONE
            gameSettings?.updateMouseMode(false)
        } else {
            gameSettings?.updateMouseMode(true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onControllerConfigEvent(event: ControllerConfigEvent) {
        dataBinding.gameController.setControllerData(event.data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onControllerEditEvent(event: ControllerEditEvent) {
        dataBinding.gameController.controllerChange(event.type)
        exitCustom()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGameError(event: GameErrorEvent) {
        exitGame(errorCode = event.errorCode, errorMsg = event.errorMsg)
    }

    private fun exitGame(errorCode: String = "0", errorMsg: String? = null) {
        val str =
            "cid:${HmcpManager.getInstance().cloudId},uid:${GameManager.getGameParam()?.userId}"
        LogUtils.d("exitGame:$str")
        if (errorCode != "0") {
            showErrorDialog(errorCode, errorMsg)
        } else {
            showExitGameDialog()
        }
    }

    private fun showErrorDialog(errorCode: String, errorMsg: String? = null) {
        gameSettings?.release()
        GameManager.isPlaying = false
        GameManager.releaseGame(finish = errorCode, bundle = null)
        try {
            val title = StringBuilder()
            if (TextUtils.isEmpty(errorMsg) || errorMsg == "null") {
                title.append(getString(R.string.title_game_error))
            } else {
                title.append(errorMsg)
            }
            if (!TextUtils.isEmpty(errorCode)) {
                title.append("\n").append("[$errorCode]")
            }

            val content =
                StringBuilder().append("游戏名称:").append(GameManager.getGameParam()?.gameName)
                    .append("\n")
                    .append("CID:").append(HmcpManager.getInstance().cloudId).append("\n")
                    .append("UID:").append(GameManager.getGameParam()?.userId).append("\n")
                    .append("无法重连可截图联系客服QQ:3107321871")
            GameErrorDialog.Builder(this)
                .setTitle(title.toString())
                .setSubTitle(content.toString())
                .setLeftButtonClickListener {
                    LogUtils.d("exitGameForError")
                    finish()
                }
                .build().show()
        } catch (e: Exception) {
            LogUtils.e("showErrorDialog:${e.message}")
        }
    }

    private fun showExitGameDialog() {
        AppCommonDialog.Builder(this)
            .setTitle(getString(R.string.title_exit_game))
            .setLeftButton(getString(R.string.continue_game)) { AppCommonDialog.hideDialog(this@GameActivity) }
            .setRightButton(getString(R.string.confirm)) {
                LogUtils.d("exitGameByUser")
                GameManager.releaseGame(finish = "1", bundle = null)
                gameSettings?.release()
                finish()
            }
            .build().show()
    }

    override fun onDestroy() {
        try {
            inputTimer?.cancel()
            inputTimer?.purge()
            inputTimer = null
        } catch (e: Exception) {
            LogUtils.e("exitCustom:${e.message}")
        }
        EventBus.getDefault().unregister(this)
        GameManager.gameView?.onDestroy()
        if (GameManager.isPlaying) {
            GameManager.exitGame(mapOf(Pair("action", "")))
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        LogUtils.d("onKeyDown:$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
//        LogUtils.d("onKeyUp:$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayPartyRoomInfoEvent(event: PlayPartyRoomInfoEvent) {
        val roomInfo = event.roomInfo
        val controlInfos = event.controlInfos
        playPartyGameView?.onPlayPartyRoomInfoEvent(roomInfo, controlInfos)

        // 判断我自己是否有权限，如果没权限，就显示，有权限就隐藏
        val position = controlInfos.find {
            it.uid == GameManager.userId
        }?.position ?: 0
        if (position == 0) {
            initWantPlayView()
        } else {
            // 如果有权限，则需要removeView
            dataBinding.gameController.findViewById<View>(wantPlayViewId)?.let {
                dataBinding.gameController.removeView(it)
            }
        }
    }

    private val wantPlayViewId = View.generateViewId()

    private fun initWantPlayView() {
        // 校验是否已经拥有
        val view = dataBinding.gameController.findViewById<View>(wantPlayViewId)
        if (view != null) {
            return
        }

        val layoutWidth = AutoSizeUtils.dp2px(this, 100f)
        val layoutHeight = AutoSizeUtils.dp2px(this, 40f)
        val linearLayout = LinearLayout(this).apply {
            id = wantPlayViewId
            background = DrawableCreator.Builder()
                .setCornersRadius(AutoSizeUtils.dp2px(this@GameActivity, 21f).toFloat())
                .setSolidColor(Color.parseColor("#FFC6EC4B"))
                .setStrokeColor(Color.parseColor("#FFA8C93E"))
                .setStrokeWidth(AutoSizeUtils.dp2px(this@GameActivity, 2f).toFloat())
                .build()

            layoutParams = ConstraintLayout.LayoutParams(
                layoutWidth,
                layoutHeight
            ).apply {
                rightToRight = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID

                bottomMargin = AutoSizeUtils.dp2px(this@GameActivity, 20f)
                marginEnd = AutoSizeUtils.dp2px(this@GameActivity, 20f)
            }
            gravity = Gravity.CENTER
        }

        linearLayout.setOnClickListener {
            GameManager.wantPlay(GameManager.userId)
        }

        val imageView = ImageView(this).apply {
            val width = AutoSizeUtils.dp2px(this@GameActivity, 20f)
            layoutParams = LinearLayout.LayoutParams(width, width)
            setImageResource(R.drawable.icon_play_party_want_play)
        }
        linearLayout.addView(imageView)

        val textView = TextView(this).apply {
            text = "我要玩"
            setTextColor(Color.parseColor("#FF000000"))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSizeUtils.sp2px(this@GameActivity, 13f).toFloat())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = AutoSizeUtils.dp2px(this@GameActivity, 5f)
            }
            setTypeface(typeface, Typeface.BOLD)
        }
        linearLayout.addView(textView)

        // 将 textView 添加到 ConstraintLayout
        dataBinding.gameController.addView(linearLayout)
    }
}