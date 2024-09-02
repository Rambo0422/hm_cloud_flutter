package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar.hasNavigationBar
import com.gyf.immersionbar.ktx.immersionBar
import com.haima.hmcp.HmcpManager
//import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.sayx.hm_cloud.callback.NoOperateListener
import com.sayx.hm_cloud.databinding.ActivityGameBinding
import com.sayx.hm_cloud.dialog.AppCommonDialog
import com.sayx.hm_cloud.dialog.GameErrorDialog
import com.sayx.hm_cloud.dialog.NoOperateOfflineDialog
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameOverEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Timer
import java.util.TimerTask

class GameActivity : AppCompatActivity() {

    private val noOperateTime = 240 * 1000L

    private lateinit var dataBinding: ActivityGameBinding

    private var gameTimer: Timer? = null

    private var countTime = noOperateTime

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
        initView()
    }

    private fun initView() {
        GameManager.gameView?.let { gameView ->
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
//        GameManager.gameView?.onSwitchResolution(
//            0,
//            GameManager.gameView?.resolutionList?.first(),
//            0
//        )
        LogUtils.d("initGameSettings:${GameManager.gameView?.resolutionList}");
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        countTime = noOperateTime
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

    private var lastDelay = 0

    @SuppressLint("SetTextI18n")
    private fun updateNetDelay() {
        val latencyInfo = GameManager.gameView?.clockDiffVideoLatencyInfo
//        LogUtils.d("updateNetDelay:${latencyInfo}")
        val delay = latencyInfo?.netDelay ?: 999
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
//        dataBinding.tvLossPacket.text =
//            "netDelay:${latencyInfo?.netDelay}\n" +
//                    "decodeDelay:${latencyInfo?.decodeDelay}\n" +
//                    "renderDelay:${latencyInfo?.renderDelay}\n" +
//                    "videoFps:${latencyInfo?.videoFps}\n" +
//                    "bitRate:${latencyInfo?.bitRate}\n" +
//                    "packetsLostRate:${latencyInfo?.packetsLostRate}\n" +
//                    "receivedBitrate:${latencyInfo?.receivedBitrate}\n" +
//                    "audioBitrate:${latencyInfo?.audioBitrate}\n"

        val cloudId = HmcpManager.getInstance().cloudId
        if (!TextUtils.isEmpty(cloudId)) {
            dataBinding.tvCid.text = cloudId
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
                    GameManager.releaseGame(finish = "1", bundle = null)
                    finish()
                }
            }

            override fun continuePlay() {
                // 继续游戏，刷新无操作时间,重新开始计时
                countTime = noOperateTime
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
            "cid:${GameManager.cid},uid:${GameManager.getGameParam()?.userId}"
        LogUtils.d("exitGame:$str")
        if (errorCode != "0") {
            showErrorDialog(errorCode, errorMsg)
        } else {
            showExitGameDialog()
        }
    }

    private fun showErrorDialog(errorCode: String, errorMsg: String? = null) {
        GameManager.gameView?.onDestroy()
        GameManager.gameView = null
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
                finish()
            }
            .build().show()
    }

    override fun onStart() {
        GameManager.gameView?.onStart()
        super.onStart()
    }

    override fun onResume() {
        GameManager.gameView?.onResume()
        super.onResume()
    }

    override fun onRestart() {
        GameManager.gameView?.onRestart(0)
        super.onRestart()
    }

    override fun onPause() {
        GameManager.gameView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        GameManager.gameView?.onStop()
        GameManager.isPlaying = false
        try {
            gameTimer?.purge()
            gameTimer?.cancel()
            gameTimer = null
        } catch (e: Exception) {
            LogUtils.e("cancel Timer error:${e.message}")
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
//        LogUtils.v("dispatchGenericMotionEvent:$event")
        countTime = noOperateTime
        return super.dispatchGenericMotionEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
//        LogUtils.v("dispatchTouchEvent:$event")
        countTime = noOperateTime
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        LogUtils.v("dispatchKeyEvent:$event")
        countTime = noOperateTime
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        LogUtils.v("onKeyDown:$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
//        LogUtils.v("onKeyUp:$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return !(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }
}