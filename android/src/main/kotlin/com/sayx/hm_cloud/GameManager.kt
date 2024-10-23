package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.media.atkit.AnTongManager
import com.media.atkit.beans.ChannelInfo
import com.media.atkit.listeners.OnGameIsAliveListener
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
import com.sayx.hm_cloud.model.GameParam
import io.flutter.plugin.common.MethodChannel

@SuppressLint("StaticFieldLeak")
object GameManager {

    private lateinit var channel: MethodChannel
    private var handler: Handler? = null

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
            it.isLogSwitch = true
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

    fun onBackHome() {
        channel.invokeMethod("homeShow", null)
    }

    fun releaseGame(finish: String, bundle: Bundle?) {
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

        AnTongSDK.stopGame()
    }

    fun exitGame(data: Map<*, *>) {
        channel.invokeMethod("exitGame", data)
    }
}