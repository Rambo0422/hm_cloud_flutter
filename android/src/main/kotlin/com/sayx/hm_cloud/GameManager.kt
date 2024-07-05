package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.haima.hmcp.Constants
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.PlayNotification
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.beans.UserInfo2
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.haima.hmcp.listeners.OnSaveGameCallBackListener
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.sayx.hm_cloud.model.GameError
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.utils.GameUtils
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object GameManager : HmcpPlayerListener {

    private var gameSdkInt = false

    private lateinit var channel: MethodChannel

    val gson: Gson by lazy {
        GsonBuilder().disableHtmlEscaping().create()
    }

    private var gameParam: GameParam? = null

    var gameView: HmcpVideoView? = null

    private lateinit var context: Context

    lateinit var flutterEngine: FlutterEngine

    var activity: Activity? = null

    var isPlaying = false

    fun init(channel: MethodChannel, context: Context) {
        this.channel = channel
        this.context = context
    }

    fun getGameParam(): GameParam? {
        return gameParam
    }

    fun startGame(gameParam: GameParam) {
        this.gameParam = gameParam
        if (gameSdkInt) {
            prepareGame()
        } else {
            initGameSdk()
        }
    }

    fun initGameSdk() {
        try {
            LogUtils.d("init haiMaSDK:${gson.toJson(gameParam)}")
            val config: Bundle = Bundle().also {
                it.putString(HmcpManager.ACCESS_KEY_ID, gameParam?.accessKeyId)
                it.putString(HmcpManager.CHANNEL_ID, "app_cloud_game")
            }
            Constants.IS_DEBUG = false
            Constants.IS_ERROR = false
            Constants.IS_INFO = false
            LogUtils.d("init haiMaSDK:${gameParam?.accessKeyId}")
            HmcpManager.getInstance().init(config, context, object : OnInitCallBackListener {
                override fun success() {
                    LogUtils.d("haiMaSDK success:${HmcpManager.getInstance().sdkVersion}")
                    gameSdkInt = true
                    prepareGame()
                }

                override fun fail(msg: String?) {
                    LogUtils.e("haiMaSDK fail:$msg")
                    channel.invokeMethod(
                        "errorInfo",
                        mapOf(
                            Pair("errorCode", GameError.gameInitErrorCode),
                            Pair("cid", HmcpManager.getInstance().cloudId),
                        )
                    )
                }
            }, true)
        } catch (e: Exception) {
            LogUtils.e("haiMaSDK fail:${e.message}")
            channel.invokeMethod(
                "errorInfo",
                mapOf(
                    Pair("errorCode", GameError.gameParamErrorCode),
                    Pair("cid", HmcpManager.getInstance().cloudId),
                )
            )
        }
    }

    /**
     * 准备进入游戏队列
     */
    private fun prepareGame(cid: String? = null) {
        LogUtils.d("prepareGame:${cid}, priority:${gameParam?.priority}")
        try {
            val bundle = Bundle().also {
                // 横屏
                it.putSerializable(HmcpVideoView.ORIENTATION, ScreenOrientation.LANDSCAPE)
                // 可玩时间
                val playTime: Long = gameParam?.playTime ?: 0
                it.putInt(
                    HmcpVideoView.PLAY_TIME,
                    if (playTime > Int.MAX_VALUE) Int.MAX_VALUE else playTime.toInt()
                )
                // 排队优先级：1~48
                it.putInt(HmcpVideoView.PRIORITY, 48)
                // 游戏包名：GTAV
                it.putString(HmcpVideoView.APP_NAME, gameParam?.gamePkName)
                // 渠道商名称：szlk
                it.putString(HmcpVideoView.APP_CHANNEL, gameParam?.channelName)
                // cToken
                it.putString(HmcpVideoView.C_TOKEN, gameParam?.cToken)
//                it.putString(HmcpVideoView.EXTRA_ID, AppConstants.extraId)
                // 是否存档
//                it.putBoolean(HmcpVideoView.ARCHIVED, true)
                it.putString(
                    HmcpVideoView.PAY_PROTO_DATA,
                    GameUtils.getProtoData(
                        gson,
                        gameParam?.userId,
                        gameParam?.gameId,
                        gameParam?.priority ?: 0
                    )
                )
                // 码率
//                it.putInt(HmcpVideoView.INTERNET_SPEED, 300)
                // 清晰度挡位：1流畅，2标清，3高清，4超清
                it.putInt(HmcpVideoView.RESOLUTION_ID, 4)
                // 显示剩余时间
//                it.putBoolean(HmcpVideoView.IS_SHOW_TIME, true)
                // 背景色
//                it.putInt(HmcpVideoView.VERTICAL_BACKGROUND, Color.BLACK)
                // 分辨率宽高
//                it.putInt(HmcpVideoView.VIEW_RESOLUTION_WIDTH, 1920)
//                it.putInt(HmcpVideoView.VIEW_RESOLUTION_HEIGHT, 1080)
                // 流类型 0：表示RTMP 1：表示WEBRTC, 默认0
                it.putInt(HmcpVideoView.STREAM_TYPE, 1)
                // rtmp解码类型 0：软解码 1：硬解码, 默认1
//                it.putInt(HmcpVideoView.DECODE_TYPE, 1)
                // 输入法类型 0：表示云端键盘 1：表示本地键盘
//                it.putInt(HmcpVideoView.IME_TYPE, 1)
            }
            LogUtils.d("prepareGame->bundle:$bundle")
            playGame(bundle)
        } catch (e: Exception) {
            LogUtils.e("game error:${e.message}")
            // 数据错误，退出游戏
            channel.invokeMethod(
                "errorInfo",
                mapOf(
                    Pair("errorCode", GameError.gameConfigErrorCode),
                    Pair("cid", HmcpManager.getInstance().cloudId),
                )
            )
        }
    }

    private fun playGame(bundle: Bundle?) {
        LogUtils.d("playGame:$gameView")
        if (gameView != null) {
            releaseGame(finish = "0", bundle)
        } else {
            gameView = HmcpVideoView(context)
            gameView?.setUserInfo(UserInfo().also {
                it.userId = gameParam?.userId
                it.userToken = gameParam?.userToken
            })
            gameView?.setConfigInfo("configInfo")
            gameView?.hmcpPlayerListener = this
            gameView?.virtualDeviceType = VirtualOperateType.NONE
            gameView?.play(bundle)
        }
    }

    override fun HmcpPlayerStatusCallback(statusData: String?) {
        LogUtils.d("playerStatusCallback:$statusData, cid:${HmcpManager.getInstance().cloudId}")
        statusData?.let {
            val data = JSONObject(it)
            when (val status = data.getInt(StatusCallbackUtil.STATUS)) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    gameView?.play()
                }

                Constants.STATUS_WAIT_CHOOSE -> {
                    gameView?.entryQueue()
                }

                Constants.STATUS_TIPS_CHANGE_WIFI_TO_4G -> {
                    gameView?.reconnection()
                }

                Constants.STATUS_OPERATION_INTERVAL_TIME -> {
                    val dataStr = data.getString(StatusCallbackUtil.DATA)
                    if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                        val resultData = gson.fromJson(dataStr, Map::class.java)
                        channel.invokeMethod(
                            "queueInfo",
                            mapOf(
                                Pair("queueTime", resultData["time"])
                            )
                        )
                    } else {
                        LogUtils.e("queue info error:$dataStr");
                    }
                }

                Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                    if (!isPlaying) {
                        isPlaying = true
                        gameView?.virtualDeviceType = VirtualOperateType.NONE
                        channel.invokeMethod(
                            GameViewConstants.firstFrameArrival, mapOf(
                                Pair("cid", HmcpManager.getInstance().cloudId)
                            )
                        )
                        Intent().apply {
                            setClass(context, GameActivity::class.java)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(this)
                        }
                    } else {
                        LogUtils.d("game already play")
                    }
                }

                Constants.STATUS_OPERATION_GAME_TIME_COUNT_DOWN -> {
                    val dataStr = data.getString(StatusCallbackUtil.DATA)
                    if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                        val resultData = gson.fromJson(dataStr, Map::class.java)
                        LogUtils.d("下线倒计时:${resultData["ahead"]}");
                    } else {
                        LogUtils.e("gameTimeCountDown error:$dataStr");
                    }
                }
                // 9,连接失败
                Constants.STATUS_CONNECTION_ERROR,
                    // 10,排队人数过多
                Constants.STATUS_OPERATION_REFUSE_QUEUE,
                    // 11,长时间无操作
                Constants.STATUS_TOAST_NO_INPUT,
                    // 15,游戏时间到
                Constants.STATUS_OPERATION_GAME_OVER,
                    // 18,服务器开始维护
                Constants.STATUS_OPERATION_PAUSE_SAAS_SERVER,
                    // 19,服务维护中
                Constants.STATUS_OPERATION_PAUSED_SAAS_SERVER,
                    // 23,获取流地址超时
                Constants.STATUS_TIME_OUT,
                    // 24,Token过期服务终⽌
                Constants.STATUS_OPERATION_FORCED_OFFLINE,
                    // 26,测速结果低于服务下限
                Constants.STATUS_SPEED_LOWER_BITRATE,
                    // 27,游戏多开
                Constants.STATUS_OPERATION_OPEN_MORE_SAME_GAME,
                    // 29,服务连接错误
                Constants.STATUS_OPERATION_HMCP_ERROR,
                    // 40,获取控制权失败
                Constants.STATUS_GET_CONTRON_ERROR,
                    // 42,接⼊⽅连接服务端结束游戏
                Constants.STATUS_OPERATION_STATE_CHANGE_REASON -> {
                    val dataStr = data.getString(StatusCallbackUtil.DATA)
                    LogUtils.d("errorInfo:$dataStr")
                    var errorCode = ""
                    var errorMsg = ""
                    if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                        val resultData = gson.fromJson(dataStr, Map::class.java)
                        var errorCodeWithoutCid = ""
                        try {
                            errorCodeWithoutCid =
                                if (resultData["errorCodeWithoutCid"].toString() == "null") "$status" else resultData["errorCodeWithoutCid"].toString()
                        } catch (e: Exception) {
                            LogUtils.e("${e.message}")
                        }
                        errorCode =
                            if (TextUtils.isEmpty(errorCodeWithoutCid)) "$status" else errorCodeWithoutCid
                        try {
                            errorMsg =
                                if (resultData["errorMessage"].toString() == "null") resultData["errorMsg"].toString() else resultData["errorMessage"].toString()
                        } catch (e: Exception) {
                            LogUtils.e("${e.message}")
                        }
                    }
                    EventBus.getDefault().post(GameErrorEvent(errorCode, errorMsg))
                    channel.invokeMethod(
                        "errorInfo",
                        mapOf(Pair("errorCode", errorCode), Pair("errorMsg", errorMsg))
                    )
                }

                else -> {}
            }
        }
    }

    override fun onCloudDeviceStatus(status: String?) {
        LogUtils.d("onCloudDeviceStatus:$status")
    }

    override fun onInterceptIntent(intentData: String?) {
        LogUtils.d("onInterceptIntent:$intentData")
    }

    override fun onCloudPlayerKeyboardStatusChanged(keyboardStatus: CloudPlayerKeyboardStatus?) {
        LogUtils.d("onCloudPlayerKeyboardStatusChanged:${keyboardStatus?.name}")
    }

    override fun onError(errorType: ErrorType?, errorMsg: String?) {
        LogUtils.e("onError-> errorType:$errorType, errorMsg:$errorMsg")
    }

    override fun onSuccess() {
        LogUtils.d("onSuccess")
    }

    override fun onExitQueue() {
        LogUtils.d("onExitQueue")
    }

    override fun onMessage(msg: String?) {
        LogUtils.d("onMessage:$msg")
    }

    override fun onSceneChanged(sceneMessage: String?) {
        LogUtils.d("onSceneChanged:$sceneMessage")
    }

    override fun onNetworkChanged(networkState: NetWorkState?) {
        LogUtils.d("onNetworkChanged:$networkState")
    }

    override fun onPlayStatus(status: Int, value: Long, data: String?) {
        LogUtils.d("onPlayStatus->status:$status, value:$value, data:$data")
    }

    override fun onPlayerError(errorCode: String?, errorMsg: String?) {
        LogUtils.e("onPlayerError->errorCode:$errorCode, errorMsg:$errorMsg")
    }

    override fun onInputMessage(msg: String?) {
        LogUtils.d("onInputMessage:$msg")
    }

    override fun onInputDevice(device: Int, operationType: Int) {
        LogUtils.d("onInputDevice-> device:$device, operationType:$operationType")
    }

    override fun onPermissionNotGranted(msg: String?) {
        LogUtils.e("onPermissionNotGranted:$msg")
    }

    override fun onMiscResponse(msg: String?) {
        LogUtils.d("onInputMessage:$msg")
    }

    override fun onAccProxyConnectStateChange(connectState: Int) {
        super.onAccProxyConnectStateChange(connectState)
        LogUtils.d("onAccProxyConnectStateChange:$connectState")
    }

    override fun onPlayNotification(playNotification: PlayNotification?) {
        super.onPlayNotification(playNotification)
        LogUtils.d("onPlayNotification")
    }

    override fun onSwitchConnectionCallback(statusCode: Int, networkType: Int) {
        super.onSwitchConnectionCallback(statusCode, networkType)
        LogUtils.d("onSwitchConnectionCallback:$statusCode, $networkType")
    }

    fun releaseGame(finish: String, bundle: Bundle?) {
        if (finish != "0") {
            channel.invokeMethod("exitGame", mapOf(Pair("action", finish)))
        }
        val cloudId = HmcpManager.getInstance().cloudId
        if (TextUtils.isEmpty(cloudId)) {
            LogUtils.d("undo releaseGame, cid is empty")
            if (finish == "0") {
                gameView?.onDestroy()
                gameView = null
                playGame(bundle)
            } else {
                isPlaying = false
            }
            return
        }
        HmcpManager.getInstance().setReleaseCid(
            gameParam?.gamePkName, cloudId, gameParam?.cToken, gameParam?.channelName,
            UserInfo2().also {
                it.userId = gameParam?.userId
                it.userToken = gameParam?.userToken
            },
            object : OnSaveGameCallBackListener {
                override fun success(result: Boolean) {
                    LogUtils.d("releaseGame:$result")
                    if (finish == "0") {
                        gameView?.onDestroy()
                        gameView = null
                        playGame(bundle)
                    } else {
                        isPlaying = false
                    }
                }

                override fun fail(error: String?) {
                    LogUtils.e("releaseGame:$error")
                    channel.invokeMethod(
                        "errorInfo",
                        mapOf(
                            Pair("errorCode", GameError.gameReleaseErrorCode),
                            Pair("cid", cloudId)
                        )
                    )
                }
            }
        )
    }

    fun exitGame(data: Map<*, *>) {
        channel.invokeMethod("exitGame", data)
    }
}