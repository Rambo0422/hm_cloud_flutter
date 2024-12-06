package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.GsonUtils
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
        AnTongSDK.initSdk(context.applicationContext, channelName, accessKeyId)

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

    fun checkPlayingGame(callback: MethodChannel.Result, arguments: HashMap<*, *>) {
        // 先调用 init
        val userId = arguments["userId"].toString()
        val gameId = arguments["gameId"].toString()

        AnTongManager.getInstance().checkPlayingGame(userId, object : OnGameIsAliveListener {
            override fun success(channelInfo: ChannelInfo?) {
                if (channelInfo != null) {
                    val businessGameId = channelInfo.businessGameId
                    if (businessGameId == gameId) {
                        // 代表连接的是同一个游戏，则重连
                        callback.success(false)
                    } else {
                        // 代表连接的不是同一个游戏
                        callback.success(true)
                    }
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
        AnTongSDK.onDestroy()
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
        AnTongSDK.onDestroy()
    }

    fun exitGame(data: Map<*, *>) {
        channel.invokeMethod("exitGame", data)
    }

    private var releaseOldGameCallback: MethodChannel.Result? = null

    fun releaseOldGame(callback: MethodChannel.Result?, userId: String) {
        releaseOldGameCallback = callback
        AnTongManager.getInstance().setReleaseByUserId(userId, object : OnSaveGameCallBackListener {
            override fun success(result: Boolean) {
                isPlaying = false
                gameParam = null

                // 这里做一个调整，只要判断成功了，不再检测
                releaseOldGameCallback?.success(true)
                releaseOldGameCallback = null
            }

            override fun fail(msg: String?) {
                this@GameManager.userId = ""
                releaseOldGameCallback?.success(false)
                releaseOldGameCallback = null
            }
        })
    }

    private fun stopReleaseCheckTimer() {
        handler?.removeCallbacksAndMessages(null)
    }

    fun getOldGameInfo(callback: MethodChannel.Result, userId: String) {
        AnTongManager.getInstance().checkPlayingGame(userId, object : OnGameIsAliveListener {
            override fun success(deviceInfo: ChannelInfo?) {
                if (deviceInfo != null) {
                    callback.success(GsonUtils.toJson(deviceInfo))
                } else {
                    callback.success(null)
                }
            }

            override fun fail(msg: String?) {
                callback.success(null)
            }
        })
    }

    fun leaveQueue() {
        AnTongSDK.leaveQueue()
        stopReleaseCheckTimer()

        // 假设这个时候碰到了 releaseOldGame 的情况下，所以，需要释放 releaseOldGame 的channel
        releaseOldGameCallback = null
    }

    fun getUserInfo(cache: Int) {
        val arguments = mapOf("cache" to cache)
        channel.invokeMethod("get_user_info", arguments)
    }

    fun requestPayData() {
        channel.invokeMethod("requestPayData", null)
    }

    fun createOrder(orderId: String) {
        channel.invokeMethod("createOrder", orderId)
    }

    fun checkOrderStatus(orderNo: String, price: Number) {
        val arguments = mapOf("orderNo" to orderNo, "price" to price)
        channel.invokeMethod("checkOrderStatus", arguments)
    }

    /**
     * Dau（登录成功）、充值人数、充值金额、消耗完毕，游玩15分钟。
     *
     * play_fifteen_minutes     游玩十五分钟
     * insufficient             余额不足
     *
     */
    fun xlStat(params: Map<String, Any>) {
        /**
         * event: "pay"         "offli"
         * price: 0.01
         */
        channel.invokeMethod("xl-stat", params)
    }
}