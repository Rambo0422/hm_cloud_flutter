package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.sayx.hm_cloud.callback.AvailableTimeEvent
import com.sayx.hm_cloud.callback.NoOperateListener
import com.sayx.hm_cloud.callback.StopPlayEvent
import com.sayx.hm_cloud.databinding.ActivityGameBinding
import com.sayx.hm_cloud.dialog.AppCommonDialog
import com.sayx.hm_cloud.dialog.GameErrorDialog
import com.sayx.hm_cloud.dialog.NoOperateOfflineDialog
import com.sayx.hm_cloud.fragment.HandleOperationDialog
import com.sayx.hm_cloud.fragment.InsufficientDialog
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameOverEvent
import com.sayx.hm_cloud.mvp.GameContract
import com.sayx.hm_cloud.mvp.GamePresenter
import com.sayx.hm_cloud.utils.TVUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Timer
import java.util.TimerTask

class GameActivity : AppCompatActivity(), GameContract.IGameView {

    // 未操作的时间
    private val NO_OPERATE_TIME = 240 * 1000L

    private lateinit var dataBinding: ActivityGameBinding

    private var gameTimer: Timer? = null

    private var countTime = NO_OPERATE_TIME
    private var lastDelay = 0
    private lateinit var presenter: GameContract.IGamePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        LogUtils.d("GameActivity onCreate")
        // 全屏
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
        // 设置屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 事件监听
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_game)
        dataBinding.lifecycleOwner = this

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        presenter = GamePresenter(this)
        presenter.onCreate(this)

        initView()

        val operationDialog = HandleOperationDialog.newInstance()
        operationDialog.show(supportFragmentManager, "HandleOperationDialog")
    }

    private fun initView() {
        AnTongSDK.anTongVideoView?.let { gameView ->
            val parent = gameView.parent
            parent?.let {
                (it as ViewGroup).removeView(gameView)
            }
            dataBinding.layoutGame.addView(
                gameView,
                0,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        initGameSettings()
        startTimer()
    }

    /**
     * 初始化大屏游戏配置
     * 1，设置PC鼠标模式
     * 2，设置隐藏虚拟按键
     * 3，设置为超清
     */
    private fun initGameSettings() {
        // LogUtils.d("initGameSettings:${GameManager.gameView?.resolutionList}");
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        countTime = NO_OPERATE_TIME
        LogUtils.d("GameActivity onNewIntent")
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        startTimer()
    }

    private fun startTimer() {
        try {
            if (gameTimer != null) {
                gameTimer?.purge()
                gameTimer?.cancel()
                gameTimer = null
            }
            gameTimer = Timer()
            gameTimer?.schedule(object : TimerTask() {
                override fun run() {
//                    LogUtils.d("startTimer->countTime:$countTime")
                    if (countTime == 0L) {
                        gameTimer?.purge()
                        gameTimer?.cancel()
                        gameTimer = null
                        runOnUiThread {
                            LogUtils.e("No operate timeout!")
                            // 3分钟无操作，提示下线
                            showNoOperateDialog()
                        }
                    } else {
                        countTime -= 1000L
                        runOnUiThread {
                            updateNetDelay()
                        }
                    }
                }
            }, 0L, 1000L)
        } catch (e: Exception) {
            LogUtils.e("startTimer:${e.message}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNetDelay() {
        val latencyInfo = AnTongSDK.anTongVideoView?.clockDiffVideoLatencyInfo
//        val latencyInfo = GameManager.gameView?.clockDiffVideoLatencyInfo
//        LogUtils.d("updateNetDelay:${latencyInfo}")
        val delay = latencyInfo?.netDelay?.toInt() ?: 999
        val netDelay = if (delay > 450) 450 else delay
//        val netDelay = (40..400).random()
        // 延迟在0~60，展示满信号
        if (netDelay <= 60) {
            if (lastDelay > 60) {
                dataBinding.ivNetStatus.setImageResource(R.drawable.icon_wifi_full)
            }
        } else if (netDelay in 61..200) {
            // 延迟在61~200，展示中信号
            if (lastDelay <= 60 || lastDelay > 200) {
                dataBinding.ivNetStatus.setImageResource(R.drawable.icon_wifi_middle)
            }
        } else {
            // 延迟在201~999，展示无信号
            if (lastDelay <= 200) {
                dataBinding.ivNetStatus.setImageResource(R.drawable.icon_wifi_low)
            }
        }
        lastDelay = netDelay
        dataBinding.tvLossPacket.text =
            "netDelay:${latencyInfo?.netDelay}\n" +
                    "decodeDelay: ${latencyInfo?.decodeDelay}\n" +
                    "renderDelay: ${latencyInfo?.renderDelay}\n" +
                    "videoFps: ${latencyInfo?.videoFps}\n" +
                    "bitRate: ${latencyInfo?.bitrate}\n" +
                    "decodeDelayAvg: ${latencyInfo?.decodeDelayAvg}\n" +
                    "packetsLostRate: ${latencyInfo?.packetsLostRate}\n" +
                    "freezeCount: ${latencyInfo?.freezeCount}\n" +
                    "freezeDuration: ${latencyInfo?.freezeDuration}\n"

        val userId = GameManager.getGameParam()?.userId
        if (!TextUtils.isEmpty(userId)) {
            dataBinding.tvCid.text = userId
        }
    }

    private fun showNoOperateDialog() {
        NoOperateOfflineDialog.show(this, listener = object : NoOperateListener {
            override fun overtime() {
                // 提示下线30秒无操作，下线处理
                LogUtils.w("No operation overtime")
                runOnUiThread {
                    gameTimer?.purge()
                    gameTimer?.cancel()
                    gameTimer = null
                    GameManager.releaseGame(finish = "1")
                    finish()
                }
            }

            override fun continuePlay() {
                // 继续游戏，刷新无操作时间,重新开始计时
                countTime = NO_OPERATE_TIME
                startTimer()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGameError(event: GameErrorEvent) {
        exitGame(errorCode = event.errorCode, errorMsg = event.errorMsg)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGameOver(event: GameOverEvent) {
        finish()
    }

    private fun exitGame(errorCode: String = "0", errorMsg: String? = null) {
        val str =
            "uid:${GameManager.getGameParam()?.userId}"
        LogUtils.d("exitGame:$str")
        if (errorCode != "0") {
            showErrorDialog(errorCode, errorMsg)
        } else {
            showExitGameDialog()
        }
    }

    private fun showErrorDialog(errorCode: String, errorMsg: String? = null) {
//        GameManager.gameView?.onDestroy()
//        GameManager.gameView = null
        AnTongSDK.onDestroy()
        GameManager.isPlaying = false
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
//                    .append("CID:").append(HmcpManager.getInstance().cloudId).append("\n")
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
                GameManager.releaseGame(finish = "1")
                finish()
            }
            .build().show()
    }

    override fun onStart() {
        // GameManager.gameView?.onStart()
        AnTongSDK.anTongVideoView?.onStart()
        super.onStart()
    }

    override fun onResume() {
        AnTongSDK.anTongVideoView?.onResume()
        super.onResume()
    }

    override fun onRestart() {
        AnTongSDK.anTongVideoView?.onRestart(0)
        LogUtils.d("onRestart")
        super.onRestart()
    }

    override fun onPause() {
        super.onPause()
        AnTongSDK.anTongVideoView?.onPause()
        LogUtils.d("onPause")
//        AnTongSDK.anTongVideoView?.stopGame()
        finish()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        AnTongSDK.anTongVideoView?.onStop()
        GameManager.isPlaying = false
        try {
            gameTimer?.purge()
            gameTimer?.cancel()
            gameTimer = null
        } catch (e: Exception) {
            LogUtils.e("cancel Timer error:${e.message}")
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
//        LogUtils.v("dispatchTouchEvent:$event")
        countTime = NO_OPERATE_TIME
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        LogUtils.v("dispatchKeyEvent:$event")
        countTime = NO_OPERATE_TIME
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        LogUtils.v("onKeyDown:$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        LogUtils.v("onKeyUp:$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }

        val eventSource = event.source
        if (((eventSource and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
            || ((eventSource and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
        ) {
            // 判断是手柄，且 keyCode 是home
            if (keyCode == KeyEvent.KEYCODE_BUTTON_MODE && event.scanCode == 316) {
                toTVHome()
                return true
            }
        }


        return !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        countTime = NO_OPERATE_TIME
        return super.dispatchGenericMotionEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        AnTongSDK.onDestroy()
        presenter.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        LogUtils.d("onUserLeaveHint")
    }

    /**
     * 写这个的目的是，有些手柄点击home键是不会自动跳转的，所以这里单独进行处理跳转
     */
    private fun toTVHome() {
        TVUtils.toTVHome(this)
    }

    override fun getUserInfo() {
        GameManager.getUserInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAvailableTimeEvent(event: AvailableTimeEvent) {
        val availableTime = event.availableTime
        if (availableTime > 8 * 60) {
            presenter.onUserInfoReceived(availableTime)
        } else {
            // 余额不足八分钟
            // 显示充值弹窗
            val insufficientFragment = InsufficientDialog.newInstance()
            insufficientFragment.show(supportFragmentManager, "InsufficientFragment")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStopPlayEvent(event: StopPlayEvent) {
        AnTongSDK.onDestroy()
        // toTVHome()
    }
}