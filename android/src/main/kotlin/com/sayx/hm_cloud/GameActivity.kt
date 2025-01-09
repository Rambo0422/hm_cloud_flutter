package com.sayx.hm_cloud

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.hardware.input.InputManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.gyf.immersionbar.ktx.navigationBarHeight
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.ResolutionInfo
import com.haima.hmcp.listeners.OnLivingListener
import com.haima.hmcp.rtc.widgets.beans.RtcVideoDelayInfo
import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.sayx.hm_cloud.callback.AddKeyListenerImp
import com.sayx.hm_cloud.callback.AnimatorListenerImp
import com.sayx.hm_cloud.callback.ConfigNameCallback
import com.sayx.hm_cloud.callback.ControllerEventCallback
import com.sayx.hm_cloud.callback.EditCallback
import com.sayx.hm_cloud.callback.GameSettingChangeListener
import com.sayx.hm_cloud.callback.HideListener
import com.sayx.hm_cloud.callback.KeyEditCallback
import com.sayx.hm_cloud.callback.OnEditClickListener
import com.sayx.hm_cloud.callback.OnPositionChangeListener
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.constants.ControllerStatus
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.OnKeyEventListenerImp
import com.sayx.hm_cloud.constants.OnRockerOperationListenerImp
import com.sayx.hm_cloud.constants.controllerStatus
import com.sayx.hm_cloud.databinding.ActivityGameBinding
import com.sayx.hm_cloud.dialog.AppCommonDialog
import com.sayx.hm_cloud.dialog.EditControllerNameDialog
import com.sayx.hm_cloud.dialog.GameErrorDialog
import com.sayx.hm_cloud.dialog.GameToastDialog
import com.sayx.hm_cloud.dialog.ShareDialog
import com.sayx.hm_cloud.http.bean.BaseObserver
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.repository.AppRepository
import com.sayx.hm_cloud.model.ControllerConfigEvent
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.ErrorConfigInfo
import com.sayx.hm_cloud.model.ExitGameEvent
import com.sayx.hm_cloud.model.GameConfig
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameNotice
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.model.MessageEvent
import com.sayx.hm_cloud.model.PartyPlayWantPlay
import com.sayx.hm_cloud.model.PlayPartyRoomInfoEvent
import com.sayx.hm_cloud.model.PlayPartyRoomSoundAndMicrophoneStateEvent
import com.sayx.hm_cloud.model.TimeUpdateEvent
import com.sayx.hm_cloud.model.UserRechargeStatusEvent
import com.sayx.hm_cloud.utils.AppSizeUtils
import com.sayx.hm_cloud.utils.GameUtils
import com.sayx.hm_cloud.utils.TimeUtils
import com.sayx.hm_cloud.widget.AddGamepadKey
import com.sayx.hm_cloud.widget.AddKeyboardKey
import com.sayx.hm_cloud.widget.ControllerEditLayout
import com.sayx.hm_cloud.widget.EditCombineKey
import com.sayx.hm_cloud.widget.EditContainerKey
import com.sayx.hm_cloud.widget.EditRouletteKey
import com.sayx.hm_cloud.widget.GameNoticeView
import com.sayx.hm_cloud.widget.GameSettings
import com.sayx.hm_cloud.widget.KeyEditView
import com.sayx.hm_cloud.widget.KeyboardListView
import com.sayx.hm_cloud.widget.PlayPartyGameView
import com.sayx.hm_cloud.widget.PlayPartyPermissionView
import com.sayx.hm_cloud.widget.PlayPartyUserAvatarView
import com.sayx.hm_cloud.widget.PlayPartyWantPlayView
import com.sayx.hm_cloud.widget.TouchEventDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSizeCompat
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

    private var editContainerKey: EditContainerKey? = null

    private var keyEditView: KeyEditView? = null

    // 编辑状态无响应处理
    private var inputTimer: Timer? = null
    private var pinCodeTimer: Timer? = null

    private var live = false

    // 声音控制
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // 剪切板
    private val clipboardManager: ClipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    // 设备连接监听
    private val inputManager : InputManager by lazy {
        getSystemService(Context.INPUT_SERVICE) as InputManager
    }

    // 隐藏软键盘
    private val inputMethodManager : InputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameManager.openGame = false
        // 全屏
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        controllerStatus = ControllerStatus.Normal
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
            val parent = it.parent
            if (parent != null && parent is ViewGroup) {
                parent.removeView(it)
            }
            TouchEventDispatcher.registerView(it)
            dataBinding.gameController.addView(
                it,
                0,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        if (BuildConfig.DEBUG) {
            dataBinding.tvCloudId.text = HmcpManager.getInstance().cloudId
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

                checkTipsShow()
            } else {
                showGameSetting()
            }
        }
        dataBinding.btnGameSettings.positionListener = object : OnPositionChangeListener {
            override fun onPositionChange(left: Int, top: Int, right: Int, bottom: Int) {
                SPUtils.getInstance().put(GameConstants.settingsLeft, left)
                SPUtils.getInstance().put(GameConstants.settingsTop, top)
            }
        }
        val x = SPUtils.getInstance().getInt(GameConstants.settingsLeft, -1)
        val y = SPUtils.getInstance().getInt(GameConstants.settingsTop, -1)
        if (x > 0 && y > 0) {
            dataBinding.btnGameSettings.post {
                dataBinding.btnGameSettings.x = x.toFloat()
                dataBinding.btnGameSettings.y = y.toFloat()
            }
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
                LogUtils.d("onEditKeyClick->status:$controllerStatus, keyInfo:$keyInfo")
                if (controllerStatus == ControllerStatus.Edit) {
                    // 自定义按钮编辑状态点击
                    controllerEditLayout?.setKeyInfo(keyInfo)
                } else if (controllerStatus == ControllerStatus.Roulette) {
                    addCombineKey(keyInfo)
                }
            }
        }
        // 游戏控制器数据反馈
        dataBinding.gameController.controllerCallback = object : ControllerEventCallback {

            override fun getKeyboardData() {
                // 需要键盘配置
                GameManager.initKeyboardData()
            }

            override fun getGamepadData() {
                // 需要手柄配置
                GameManager.initGamepadData()
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

        if (GameManager.isPartyPlay) {
            if (GameManager.isPartyPlayOwner) {
                GameManager.queryControlUsers()
                GameManager.getPinCode()
            }
            GameManager.sendCurrentCid()
            initPlayPartyView()
        }
    }

    private var playPartyGameView: PlayPartyGameView? = null
    private var playPartyUser: PlayPartyUserAvatarView? = null

    private fun initPlayPartyView() {
        playPartyGameView = PlayPartyGameView(this)
        playPartyGameView?.visibility = View.GONE

        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dataBinding.layoutGame.addView(playPartyGameView, layoutParams)

        // 右上角派对吧用户头像
        playPartyUser = PlayPartyUserAvatarView(this)
        // 将设置面板控件加入主面板
        playPartyUser?.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
            marginEnd = AutoSizeUtils.dp2px(this@GameActivity, 56f)
            topMargin = AutoSizeUtils.dp2px(this@GameActivity, 6f)
        }
        dataBinding.layoutGame.addView(playPartyUser)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        LogUtils.d("${this}: onNewIntent")
        this.intent = intent
    }

    private fun checkGuideShow() {
        val showGuide = SPUtils.getInstance().getBoolean(GameConstants.showGuide)
        if (!showGuide) {
            showGuideView()
        } else {
            checkTipsShow()
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
            // 用户高峰时长
            GameManager.getGameParam()?.peakTime ?: 0L,
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
        if (GameManager.isPartyPlay) {
            GameManager.updatePlayPartyRoomInfo()
        }
        checkInputDevices()
    }

    private fun checkTipsShow() {
        val string = SPUtils.getInstance().getString("showNoticeGame")
        if (!string.isNullOrEmpty()) {
            val gameRecord = GameManager.gson.fromJson(string, Map::class.java)
            val gameId = gameRecord["gameId"]
            val time = gameRecord["time"]
            // 同一游戏，同一天不重复展示
            if (gameId == GameManager.getGameParam()?.gameId && TimeUtils.isSameDay(time, System.currentTimeMillis())) {
                return
            }
        }
        AppRepository.requestGameConfig(object : BaseObserver<HttpResponse<GameConfig>>() {

            override fun onNext(response: HttpResponse<GameConfig>) {
                response.data?.let {
                    val gameNotice = it.list.findLast { item -> item.gameId == GameManager.getGameParam()?.gameId }
                    gameNotice?.let { info ->
                        showGameNotice(info)
                    }
                }
            }
        })
    }

    private fun showGameNotice(info: GameNotice) {
        val tipsView = GameNoticeView(this)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tipsView.setNoticeData(info)
        tipsView.setOnClickListener {
            dataBinding.layoutGame.removeView(tipsView)
        }
        SPUtils.getInstance().put("showNoticeGame", GameManager.gson.toJson(mapOf("gameId" to info.gameId, "time" to System.currentTimeMillis())))
        dataBinding.layoutGame.post {
            dataBinding.layoutGame.addView(tipsView, layoutParams)
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

            override fun onShareClick() {
                ShareDialog.show(this@GameActivity)
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
                    if (live) {
                        GameManager.openInteraction(cloudId, true)
                    } else {
                        // 开启直播
                        val liveUrl = "rtmp://push-cg.3ayx.net/live/$cloudId"
                        GameManager.gameView
                            ?.startLiving(cloudId, liveUrl, object : OnLivingListener {
                                override fun start(success: Boolean, msg: String?) {
                                    LogUtils.d("startLiving:$success, $msg, url:$liveUrl")
                                    live = true
                                    GameManager.openInteraction(cloudId, true)
                                }

                                override fun stop(success: Boolean, msg: String?) {
                                    live = false
                                    LogUtils.d("startLiving:$success, $msg")
                                }
                            })
                    }
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

            override fun onMoreKeyboard() {
                showKeyboardList()
            }

            override fun onShowVipDialog() {
                showJoinVipDialog()
            }

            override fun onHideLayout() {
                dataBinding.btnGameSettings.visibility = View.VISIBLE
                dataBinding.btnVirtualKeyboard.visibility = View.VISIBLE
            }

            override fun onPlayTimeLack(lack: Boolean) {
                runOnUiThread {
                    if (lack) {
                        gameSettings?.showGameOffNotice()
                    } else {
                        gameSettings?.hideGameOffNotice()
                    }
                }
            }

            override fun updateNetSignal(icon: Int) {
                dataBinding.btnGameSettings.setImageResource(icon)
            }

            override fun onShowPlayParty() {
                playPartyGameView?.show()
            }

            @SuppressLint("SetTextI18n")
            override fun onDelayChange(delayInfo: Any?) {
                if (BuildConfig.DEBUG) {
                    if (delayInfo is RtcVideoDelayInfo) {
                        if (BuildConfig.DEBUG) {
                            dataBinding.tvInfo.text =
                                "netDelay:${delayInfo.netDelay}\n" +
                                        "decodeDelay: ${delayInfo.decodeDelay}\n" +
                                        "renderDelay: ${delayInfo.renderDelay}\n" +
                                        "videoFps: ${delayInfo.videoFps}\n" +
                                        "bitRate: ${delayInfo.bitRate}\n" +
                                        "packetsLostRate: ${delayInfo.packetsLostRate}\n"
                        } else {
                            dataBinding.tvInfo.text = "Fps:${delayInfo.videoFps}"
                        }
                    }
                }
            }

            override fun getNetDelay(): Int {
                return GameManager.gameView?.clockDiffVideoLatencyInfo?.netDelay ?: 999
            }

            override fun getPacketsLostRate(): String {
                return GameManager.gameView?.clockDiffVideoLatencyInfo?.packetsLostRate ?: ""
            }

            override fun onOpacityChange(opacity: Int) {
                dataBinding.gameController.setKeyOpacity(opacity)
            }
        }
    }

    private fun showGameSetting() {
        dataBinding.btnGameSettings.visibility = View.INVISIBLE
        dataBinding.btnVirtualKeyboard.visibility = View.INVISIBLE
        gameSettings?.showLayout()
    }

    private fun showKeyboardList() {
        KeyboardListView.show(dataBinding.root as ViewGroup)
    }

    private fun showControllerEdit(type: AppVirtualOperateType) {
        if (controllerEditLayout != null) {
            dataBinding.layoutGame.removeView(controllerEditLayout)
        }
        controllerEditLayout = ControllerEditLayout(this)
        controllerEditLayout?.controllerType = type
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
            dataBinding.gameController.maskEnable = true
            dataBinding.gameController.controllerType = type
            controllerStatus = ControllerStatus.Edit
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

            override fun onAddContainerKey() {
                controllerEditLayout?.hideLayout(object : AnimatorListenerImp() {
                    override fun onAnimationEnd(animation: Animator) {
                        showEditContainerKeyLayout()
                    }
                })
            }

            override fun onRestoreDefault() {
                showRestoreCustomDialog()
            }

            override fun onDeleteKey() {
                dataBinding.gameController.deleteKey()
            }

            override fun onEditName() {
                showEditConfigName()
            }
        })
    }

    private fun showEditCombineKeyLayout(keyInfo: KeyInfo? = null) {
        if (editCombineKey == null) {
            editCombineKey = EditCombineKey(this)
            editCombineKey?.setCombineKeyInfo(keyInfo)
            editCombineKey?.onHideListener = object : HideListener {
                override fun onHide(keyInfo: KeyInfo?) {
                    keyInfo?.let {
                        showKeyEditView(it)
                    }
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
                override fun onHide(keyInfo: KeyInfo?) {
                    keyInfo?.let {
                        showKeyEditView(keyInfo)
                    }
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

                override fun rouAddData(list: List<KeyInfo>?) {
                    if (!list.isNullOrEmpty()) {
                        dataBinding.gameController.removeKeys(list)
                    }
                }

                override fun rouRemoveData(list: List<KeyInfo>?) {
                    if (!list.isNullOrEmpty()) {
                        dataBinding.gameController.addKeys(list)
                    }
                }

                override fun onKeyAdd(keyInfo: KeyInfo) {
                    dataBinding.gameController.removeKeys(listOf(keyInfo))
                }

                override fun onKeyRemove(keyInfo: KeyInfo) {
                    dataBinding.gameController.addKey(keyInfo)
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
    }

    private fun showEditContainerKeyLayout(keyInfo: KeyInfo? = null) {
        if (editContainerKey == null) {
            editContainerKey = EditContainerKey(this)
            editContainerKey?.setContainerKeyInfo(keyInfo)
            editContainerKey?.onHideListener = object : HideListener {
                override fun onHide(keyInfo: KeyInfo?) {
                    keyInfo?.let {
                        showKeyEditView(keyInfo)
                    }
                    controllerEditLayout?.showLayout()
                    hideKeyBoard()
                }
            }
            editContainerKey?.addKeyListener = object : AddKeyListenerImp() {
                override fun onAddKey(keyInfo: KeyInfo) {
                    if (dataBinding.gameController.controllerType == AppVirtualOperateType.APP_KEYBOARD) {
                        keyInfo.type = KeyType.KEY_CONTAINER
                    }
                    dataBinding.gameController.addContainerKey(
                        keyInfo,
                        dataBinding.gameController.controllerType
                    )
                    controllerEditLayout?.setKeyInfo(keyInfo)
                }

                override fun rouAddData(list: List<KeyInfo>?) {
                    if (!list.isNullOrEmpty()) {
                        dataBinding.gameController.removeKeys(list)
                    }
                }

                override fun rouRemoveData(list: List<KeyInfo>?) {
                    if (!list.isNullOrEmpty()) {
                        dataBinding.gameController.addKeys(list)
                    }
                }

                override fun onKeyAdd(keyInfo: KeyInfo) {
                    dataBinding.gameController.removeKeys(listOf(keyInfo))
                }

                override fun onKeyRemove(keyInfo: KeyInfo) {
                    dataBinding.gameController.addKey(keyInfo)
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dataBinding.layoutGame.post {
                dataBinding.layoutGame.addView(editContainerKey, layoutParams)
            }
        } else if (editContainerKey?.isShow != true) {
            editContainerKey?.setContainerKeyInfo(keyInfo)
            editContainerKey?.showBoard()
        }
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
                        LogUtils.d("onAddKey:$keyInfo")
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
                            KeyType.KEY_SHOOT -> {
                                dataBinding.gameController.addShotKey(
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
                        LogUtils.d("onAddKey:$keyInfo")
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
        } else if (editContainerKey?.isShow == true) {
            editContainerKey?.addKey(keyInfo)
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
            .setSubTitle("已有记录只保存最后配置信息", Color.GRAY)
            .setLeftButton(getString(R.string.cancel)) {
                AppCommonDialog.hideDialog(this)
            }
            .setRightButton(getString(R.string.save)) {
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
        keyEditView?.let {
            dataBinding.layoutGame.removeView(it)
            keyEditView = null
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
    fun onMessageEvent(event: MessageEvent) {
        when(event.msg) {
            "showVIP" -> {
                showJoinVipDialog()
            }
            "addKeyboard" -> {
                event.arg?.let {
                    when (it) {
                        GameConstants.gamepadConfig -> {
                            dataBinding.gameController.setControllerData(GameManager.gamepadList[0], true)
                            showControllerEdit(AppVirtualOperateType.APP_STICK_XBOX)
                        }
                        GameConstants.keyboardConfig -> {
                            dataBinding.gameController.setControllerData(GameManager.keyboardList[0], true)
                            showControllerEdit(AppVirtualOperateType.APP_KEYBOARD)
                        }
                    }
                }
            }
            "updateKeyboard" -> {
                event.arg?.let {
                    if (it is ControllerInfo) {
                        LogUtils.d("updateKeyboard:${it.type}")
                        dataBinding.gameController.setControllerData(it, true)
                        when (it.type) {
                            GameConstants.gamepadConfig -> {
                                showControllerEdit(AppVirtualOperateType.APP_STICK_XBOX)
                            }
                            GameConstants.keyboardConfig -> {
                                showControllerEdit(AppVirtualOperateType.APP_KEYBOARD)
                            }
                        }
                    }
                }
            }
            "deleteKeyboard" -> {
                AppCommonDialog.Builder(this)
                    .setTitle("确认删除吗?")
                    .setSubTitle("删除后按键将无法恢复!", Color.parseColor("#FFA3ACBD"))
                    .setLeftButton("取消") {
                        AppCommonDialog.hideDialog(this, tag = "deleteKeyboard")
                    }
                    .setRightButton("确认删除", Color.parseColor("#FFFFFFFF")) {
                        AppCommonDialog.hideDialog(this, tag = "deleteKeyboard")
                        event.arg?.let {
                            GameManager.deleteKeyboardConfig(it as ControllerInfo)
                        }
                    }
                    .setRightButtonBg(R.drawable.shape_delete_keyboard_bg)
                    .build()
                    .show(tag = "deleteKeyboard")
            }
            "editKey" -> {
                showKeyEditView(event.arg as KeyInfo)
            }
            "useSuccess" -> {
                val type = when (event.arg) {
                    GameConstants.gamepadConfig -> {
                        "手柄"
                    }
                    GameConstants.keyboardConfig -> {
                        "键鼠"
                    }
                    else -> {
                        ""
                    }
                }
                GameToastDialog.Builder(this)
                    .setTitle("使用成功")
                    .setSubTitle("请在操作方法中选择“$type”使用")
                    .setDrawable(R.drawable.icon_toast_success)
                    .build()
                    .show()
            }
            "restoreSuccess" -> {
                if (event.arg is KeyInfo) {
                    keyEditView?.setKeyInfo(event.arg)
                } else {
                    controllerEditLayout?.setKeyInfo(null)
                }
                GameToastDialog.Builder(this)
                    .setTitle("还原成功")
                    .setSubTitle("继续编辑最适合你的按键配置吧！")
                    .setDrawable(R.drawable.icon_toast_success)
                    .build()
                    .show()
            }
            "addSuccess", "updateSuccess" -> {
                exitCustom()
                KeyboardListView.show(dataBinding.layoutGame)
                if (GameManager.getGameParam()?.isVip() != true) {
                    dataBinding.gameController.restoreOriginal()
                } else {
                    dataBinding.gameController.onEditSuccess()
                }
            }
        }
    }

    private fun showKeyEditView(keyInfo: KeyInfo) {
        LogUtils.d("showKeyEditView:$keyInfo")
        if (keyEditView != null) {
            dataBinding.layoutGame.removeView(keyEditView)
            keyEditView = null
        }
        keyEditView = KeyEditView(this)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        keyEditView?.setKeyInfo(keyInfo)
        keyEditView?.callback = object : KeyEditCallback {
            override fun onKeyDelete() {
                dataBinding.gameController.deleteKey()
            }

            override fun onSaveKey(keyInfo: KeyInfo, windowToken: IBinder) {
                hideSoftKeyBoard(windowToken)
                dataBinding.gameController.updateKey(keyInfo)
            }

            override fun onCombineKeyEdit(keyInfo: KeyInfo) {
                controllerEditLayout?.hideLayout(object : AnimatorListenerImp() {
                    override fun onAnimationEnd(animation: Animator) {
                        when (keyInfo.type) {
                            KeyType.KEY_COMBINE, KeyType.GAMEPAD_COMBINE -> {
                                showEditCombineKeyLayout(keyInfo)
                            }
                            KeyType.KEY_ROULETTE, KeyType.GAMEPAD_ROULETTE -> {
                                showEditRouletteKeyLayout(keyInfo)
                            }
                            KeyType.KEY_CONTAINER -> {
                                showEditContainerKeyLayout(keyInfo)
                            }
                        }
                    }
                })
            }

            override fun onViewHide() {
                dataBinding.layoutGame.removeView(keyEditView)
                keyEditView = null
            }
        }
        dataBinding.layoutGame.post {
            dataBinding.layoutGame.addView(keyEditView, layoutParams)
        }
    }

    fun hideSoftKeyBoard(windowToken: IBinder) {
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onControllerConfigEvent(event: ControllerConfigEvent) {
        LogUtils.d("onControllerConfigEvent:${event.data}")
        dataBinding.gameController.setControllerData(event.data)
        gameSettings?.controllerType = if (event.data.type == GameConstants.gamepadConfig)
            AppVirtualOperateType.APP_STICK_XBOX
        else
            AppVirtualOperateType.APP_KEYBOARD
        if (event.data.use != 1) {
            GameManager.useKeyboardData(event.data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateUserRechargeStatusEvent(event: UserRechargeStatusEvent) {
        gameSettings?.updateUserRechargeStatus(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGameError(event: GameErrorEvent) {
        exitGame(errorCode = event.errorCode, errorMsg = event.errorMsg)
    }

    private fun showEditConfigName() {
        EditControllerNameDialog.show(this, dataBinding.gameController.controllerName, object : ConfigNameCallback {
            override fun onName(name: String) {
                dataBinding.gameController.controllerName = name
            }
        })
    }

    private fun exitGame(errorCode: String = "0", errorMsg: String? = null) {
        // 判断后台配置的弹窗是否开启
        val errorDialogConfig = GameManager.getErrorDialogConfig()
        val enable = errorDialogConfig?.enable ?: false

        val str =
            "cid:${HmcpManager.getInstance().cloudId},uid:${GameManager.getGameParam()?.userId}"
        LogUtils.d("exitGame:$str")
        if (!enable) {
            handleExitGameWithoutDialog(errorCode, errorMsg)
            return
        }

        // 判断对应的 errorCode 是否能够找到对应的弹窗配置
        val configInfo = errorDialogConfig?.list?.find { it.androidCode == errorCode }

        if (configInfo == null) {
            handleExitGameWithoutDialog(errorCode, errorMsg)
            return
        }

        // 找到了对应的配置，显示弹窗
        showConfiguredDialog(configInfo)
    }

    /**
     * 原来的弹窗逻辑，保留不做修改
     */
    private fun handleExitGameWithoutDialog(errorCode: String, errorMsg: String?) {
        when {
            errorCode == "11" || errorCode == "15" || errorCode == "42" || errorCode == "401" -> {
                showWarningDialog(errorCode)
            }

            errorCode != "0" -> {
                showErrorDialog(errorCode, errorMsg)
            }

            else -> {
                showExitGameDialog()
            }
        }
    }

    private fun showWarningDialog(errorCode: String) {
        gameSettings?.release()
        GameManager.isPlaying = false
        GameManager.releaseGame(finish = errorCode, bundle = null)

        AppCommonDialog.Builder(this)
            .setTitle(getWarningDialogTitle(errorCode))
            .setSubTitle(getWarningDialogSubtitle(errorCode), Color.parseColor("#FF555A69"))
            .setLeftButton(getLeftButtonText(errorCode)) {
                finish()
            }
            .setRightButton(getRightButtonText(errorCode)) {
                LogUtils.d("exitGameForError:$errorCode")
                AppCommonDialog.hideDialog(this, "warningDialog")
                when(errorCode) {
                    "42" -> {
                        GameManager.invokeMethod("openRecharge")
                    }
                    else -> {
                    }
                }
                finish()
            }
            .build().show("warningDialog")
    }

    private fun getWarningDialogTitle(errorCode: String): String {
        return when (errorCode) {
            "401" -> {
                "设备限制"
            }
            "42" -> {
                "账号时长已消耗完毕"
            }
            else -> {
                "游戏结束\n[$errorCode]"
            }
        }
    }

    private fun getWarningDialogSubtitle(errorCode: String): String {
        return when (errorCode) {
            "11" -> {
                "游戏长时间无操作"
            }
            "42" -> {
                "你可以通过每日签到或充值获取时长"
            }
            "401" -> {
                "游戏已在其他设备运行"
            }
            else -> {
                "游戏结束"
            }
        }
    }

    private fun getLeftButtonText(errorCode: String): String? {
        return when(errorCode) {
            "42" -> {
                "退出"
            }
            else -> {
                null
            }
        }
    }

    private fun getRightButtonText(errorCode: String): String {
        return when(errorCode) {
            "42" -> {
                "去充值"
            }
            else -> {
                "退出游戏"
            }
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

            val content = StringBuilder()
                .append("游戏名称:")
                .append(GameManager.getGameParam()?.gameName).append("\n")
                .append("CID:")
                .append(HmcpManager.getInstance().cloudId).append("\n")
                .append("UID:")
                .append(GameManager.getGameParam()?.userId).append("\n")
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

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
//        LogUtils.d("$this->dispatchTouchEvent:$event")
        event?.let {
            if (controllerStatus == ControllerStatus.Edit) {
                return super.dispatchTouchEvent(event)
            }
            if (GameUtils.isGamePadEvent(it) ||
                GameUtils.isKeyBoardEvent(it) ||
                GameUtils.isMouseEvent(it)
            ) {
//                LogUtils.d("外设输入:$it")
                if (dataBinding.gameController.controllerType != AppVirtualOperateType.NONE) {
                    dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
                    gameSettings?.controllerType = AppVirtualOperateType.NONE
                }
            } else {
//                LogUtils.d("其他输入:$it")
//                if (dataBinding.btnGameSettings.visibility == View.VISIBLE && dataBinding.gameController.controllerType != GameManager.lastControllerType) {
//                    dataBinding.gameController.controllerType = GameManager.lastControllerType
//                    gameSettings?.controllerType = GameManager.lastControllerType
//                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
//        LogUtils.d("$this->dispatchGenericMotionEvent:$event")
        event?.let {
            if (controllerStatus == ControllerStatus.Edit) {
                return super.dispatchGenericMotionEvent(event)
            }
            if (GameUtils.isGamePadEvent(it) ||
                GameUtils.isKeyBoardEvent(it) ||
                GameUtils.isMouseEvent(it)
            ) {
//                LogUtils.d("外设输入:$it")
                if (dataBinding.gameController.controllerType != AppVirtualOperateType.NONE) {
                    dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
                    gameSettings?.controllerType = AppVirtualOperateType.NONE
                }
            } else {
//                LogUtils.d("其他输入:$it")
            }
        }
        return super.dispatchGenericMotionEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        LogUtils.d("$this->dispatchKeyEvent:$event")
        event.let {
            if (controllerStatus == ControllerStatus.Edit) {
                return super.dispatchKeyEvent(event)
            }
            if (GameUtils.isGamePadEvent(it) ||
                GameUtils.isKeyBoardEvent(it) ||
                GameUtils.isMouseEvent(it)
            ) {
                //                LogUtils.d("外设输入:$it")
                if (dataBinding.gameController.controllerType != AppVirtualOperateType.NONE) {
                    dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
                    gameSettings?.controllerType = AppVirtualOperateType.NONE
                }
            }
        }
        return super.dispatchKeyEvent(event)
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

    override fun onDestroy() {
        try {
            inputTimer?.cancel()
            inputTimer?.purge()
            inputTimer = null
        } catch (e: Exception) {
            LogUtils.e("exitCustom:${e.message}")
        }

        KeyboardListView.destroy()

        stopUpdatePinCode()

        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTimeUpdateEvent(event: TimeUpdateEvent) {
        val params: GameParam = event.param
        gameSettings?.updatePlayTime(params.playTime)
        gameSettings?.updateAvailableTime(params.peakTime)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayPartyRoomInfoEvent(event: PlayPartyRoomInfoEvent) {
        val roomInfo = event.roomInfo
        val controlInfos = event.controlInfos
        playPartyGameView?.onPlayPartyRoomInfoEvent(roomInfo, controlInfos)

        // 判断我自己是否有权限，如果没权限，就显示，有权限就隐藏
        val position = controlInfos.find {
            it.uid == GameManager.getGameParam()?.userId
        }?.position ?: 0
        if (position == 0) {
            GameManager.hasPremission = false
            dataBinding.gameController.controllerType = AppVirtualOperateType.NONE
            gameSettings?.controllerType = AppVirtualOperateType.NONE
            initWantPlayView()
        } else {
            GameManager.hasPremission = true
            checkInputDevices()
            // 如果有权限，则需要removeView
            dataBinding.gameController.findViewById<View>(wantPlayViewId)?.let {
                dataBinding.gameController.removeView(it)
            }
        }

        playPartyUser?.setUserInfo(roomInfo, controlInfos)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPartyPlayWantPlay(partyPlayWantPlay: PartyPlayWantPlay) {
        showPlayPartyPermissionView(partyPlayWantPlay)
        // 同时派对吧游戏页面去申请权限
        playPartyGameView?.onPartyPlayWantPlay(partyPlayWantPlay)
    }

    private val wantPlayViewId = View.generateViewId()

    private fun initWantPlayView() {
        // 校验是否已经拥有
        val view = dataBinding.gameController.findViewById<View>(wantPlayViewId)
        if (view != null) {
            return
        }

        val partyWantPlayView = PlayPartyWantPlayView(this)
        partyWantPlayView.id = wantPlayViewId
        // 将 textView 添加到 ConstraintLayout
        dataBinding.gameController.addView(partyWantPlayView)
    }

    private fun showPlayPartyPermissionView(partyPlayWantPlay: PartyPlayWantPlay) {
        val permissionView = PlayPartyPermissionView(partyPlayWantPlay, this).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
            }
        }

        dataBinding.gameController.addView(permissionView)
        permissionView.show()
    }

    private fun stopUpdatePinCode() {
        pinCodeTimer?.cancel()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayPartyExitGame(event: ExitGameEvent) {
        GameManager.releasePlayPartyGame()
        gameSettings?.release()
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayPartyRoomSoundAndMicrophoneStateEvent(event: PlayPartyRoomSoundAndMicrophoneStateEvent) {
        playPartyGameView?.setSoundAndMicrophoneState(event.soundState, event.microphoneState)
    }

    private fun showConfiguredDialog(configInfo: ErrorConfigInfo) {
        AppCommonDialog.Builder(this)
            .setTitle(configInfo.title)
            .setSubTitle(configInfo.subtitle, Color.parseColor("#FF555A69"))
            .setLeftButton(getString(R.string.exit)) {
                AppCommonDialog.hideDialog(this)
                GameManager.releaseGame(finish = "1", bundle = null)
                gameSettings?.release()
                finish()
            }
            .setRightButton("吐槽一下") {
                lifecycleScope.launch {
                    AppCommonDialog.hideDialog(this@GameActivity)
                    // 这里通过协程添加延时的原因是如果hide上一个弹窗的同时，马上去显示下一个弹窗
                    // 会导致下一个弹窗无法显示，所以这里加一个延时
                    delay(30)
                    showFeedbackSubmissionSuccessAlert(configInfo)
                }
            }
            .build().show()
    }

    /**
     * 吐槽弹窗
     */
    private fun showFeedbackSubmissionSuccessAlert(configInfo: ErrorConfigInfo) {
        AppCommonDialog.Builder(this)
            .setTitle("提交成功")
            .setSubTitle(
                "已提交相关人员处理你的吐槽，如果在后续过程中有\r\n任何问题,不要犹豫，立即联系客服哦",
                Color.parseColor("#FF555A69")
            )
            .setRightButton("知道了") {
                AppCommonDialog.hideDialog(this)
                GameManager.releaseGame(finish = "1", bundle = null)
                gameSettings?.release()
                finish()
            }
            .build().show()
    }

    override fun onStart() {
        super.onStart()
        inputManager.registerInputDeviceListener(inputDeviceListener, Handler(Looper.getMainLooper()))
    }

    override fun onStop() {
        super.onStop()
        inputManager.unregisterInputDeviceListener(inputDeviceListener)
    }

    private val inputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            checkInputDevices()
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            LogUtils.v("检测到设备移除:$deviceId")
            checkInputDevices()
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            checkInputDevices()
        }
    }

    private fun checkInputDevices() {
        val inputDeviceIds = inputManager.inputDeviceIds
        var pcMouseMode = false
        if (inputDeviceIds.isNotEmpty()) {
            inputDeviceIds.forEach { deviceId ->
                val inputDevice = inputManager.getInputDevice(deviceId)
                inputDevice?.let {
                    when {
                        GameUtils.isGamePadController(it) -> {
//                            LogUtils.v("检测到外设手柄:$deviceId, device:${inputDevice.name}")
                            pcMouseMode = true
                        }

                        GameUtils.isKeyBoardController(it) -> {
//                            LogUtils.v("检测到外设键盘:$deviceId, device:${inputDevice.name}")
                            pcMouseMode = true
                        }

                        GameUtils.isMouseController(it) -> {
//                            LogUtils.v("检测到外设鼠标:$deviceId, device:${inputDevice.name}")
                            pcMouseMode = true
                        }

                        else -> {
//                            LogUtils.d("checkInputDevice->other:$inputDevice")
                        }
                    }
                }
            }
        }
        GameManager.gameView?.setPCMouseMode(pcMouseMode)
        gameSettings?.setPCMouseMode(!pcMouseMode)
        var controllerType = AppVirtualOperateType.NONE
        if (!pcMouseMode && GameManager.hasPremission) {
            if (GameManager.lastControllerType == AppVirtualOperateType.NONE) {
                when(GameManager.getGameParam()?.defaultOperation ?: GameConstants.keyboardControl) {
                    GameConstants.keyboardControl -> {
                        controllerType = AppVirtualOperateType.APP_KEYBOARD
                    }
                    GameConstants.gamepadControl -> {
                        controllerType = AppVirtualOperateType.APP_STICK_XBOX
                    }
                }
            } else {
                controllerType = GameManager.lastControllerType
            }
        }
        dataBinding.gameController.controllerType = controllerType
        gameSettings?.controllerType = controllerType
    }

    override fun getResources(): Resources {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        }
        return super.getResources()
    }
}