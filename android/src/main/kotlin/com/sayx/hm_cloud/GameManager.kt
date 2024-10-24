package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.media.atkit.AnTongManager
import com.media.atkit.beans.ChannelInfo
import com.media.atkit.listeners.OnGameIsAliveListener
import com.media.atkit.listeners.OnSaveGameCallBackListener
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
import com.sayx.hm_cloud.model.GameParam
import io.flutter.plugin.common.MethodChannel

@SuppressLint("StaticFieldLeak")
object GameManager {

    private lateinit var channel: MethodChannel
    private var handler: Handler? = null
    private var userId = ""

    val gson: Gson by lazy {
        GsonBuilder().disableHtmlEscaping().create()
    }

    private var gameParam: GameParam? = null
    private lateinit var context: Context

    var isPlaying = false

    fun init(channel: MethodChannel, context: Context) {
        this.channel = channel
        this.context = context
        LogUtils.getConfig().also {
            it.isLogSwitch = BuildConfig.DEBUG
            it.globalTag = "GameManager"
        }
    }

    fun getGameParam(): GameParam? {
        return gameParam
    }

    fun startGame(gameParam: GameParam) {
        this.gameParam = gameParam
        val accessKeyId = this.gameParam?.accessKeyId ?: ""
        val channelName = this.gameParam?.channelName ?: ""
        AnTongSDK.initSdk(context, channelName, accessKeyId)

        val userId = this.gameParam?.userId ?: ""
        this.userId = userId

        val userToken = this.gameParam?.userToken ?: ""
        val gameId = this.gameParam?.gameId ?: ""
        val anTongPackageName = this.gameParam?.gamePkName ?: ""
        val sign = this.gameParam?.cToken ?: ""

        AnTongSDK.play(
            context,
            userId,
            userToken,
            gameId,
            anTongPackageName,
            sign,
            object : RequestDeviceSuccess {
                override fun onRequestDeviceSuccess() {
                    runOnUiThread {
                        // 首帧出现，可以跳转activity
                        if (!isPlaying) {
                            isPlaying = true
                            channel.invokeMethod(
                                GameViewConstants.firstFrameArrival, mapOf(
                                    Pair("cid", "")
                                )
                            )
                            Intent().apply {
                                setClass(context, GameActivity::class.java)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(this)
                            }
                        } else {
                            LogUtils.e("The game feeds back the first frame again.")
                        }
                    }
                }

                override fun onRequestDeviceFailed(errorMessage: String) {
                    this@GameManager.gameParam = null
                    runOnUiThread {
                        channel.invokeMethod(
                            "errorInfo",
                            mapOf(Pair("errorCode", "10001"), Pair("errorMsg", errorMessage))
                        )
                    }
                }

                override fun onStopPlay() {
                }
            })
    }

    fun runOnUiThread(r: Runnable) {
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
        }
        handler?.post(r)
    }

    fun checkPlayingGame(callback: MethodChannel.Result, userId: String) {
        AnTongManager.getInstance().checkPlayingGame(userId, object : OnGameIsAliveListener {
            override fun success(channelInfo: ChannelInfo?) {
                if (channelInfo != null) {
                    callback.success(true)
                } else {
                    callback.success(false)
                }
            }

            override fun fail(msg: String?) {
                callback.success(false)
            }
        })
    }

    private var startReleaseTime = 0L

    fun onBackHome() {
        channel.invokeMethod("homeShow", null)

        // 3s 内只允许调用一次
        val currentTime = System.currentTimeMillis()
        if ((currentTime - startReleaseTime) >= 3000) {
            startReleaseTime = currentTime
            // 检测设备，如果有游戏，直接下机
            releaseOldGame(null, this.userId)
        }
    }

    fun releaseGame(finish: String) {
        if (!isPlaying) {
            return
        }
        LogUtils.d("releaseGame:$finish")
        if (finish != "0") {
            // 非切换队列调用此方法，认定为退出游戏
            channel.invokeMethod("exitGame", mapOf(Pair("action", finish)))
        }
        isPlaying = false
        gameParam = null
        userId = ""
        // AnTongSDK.stopGame()
    }

    fun exitGame(data: Map<*, *>) {
        channel.invokeMethod("exitGame", data)
    }

    fun releaseOldGame(callback: MethodChannel.Result?, userId: String) {
        AnTongManager.getInstance().setReleaseByUserId(userId, object : OnSaveGameCallBackListener {
            override fun success(result: Boolean) {
                callback?.success(true)
                isPlaying = false
                gameParam = null
            }

            override fun fail(msg: String?) {
                this@GameManager.userId = ""
                callback?.success(false)
            }
        })
    }
}