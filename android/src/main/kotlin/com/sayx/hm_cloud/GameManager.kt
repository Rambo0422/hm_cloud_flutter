package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.haima.hmcp.Constants
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.CheckCloudServiceResult
import com.haima.hmcp.beans.IntentExtraData
import com.haima.hmcp.beans.PlayNotification
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.beans.UserInfo2
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.listeners.OnGameIsAliveListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.haima.hmcp.listeners.OnSaveGameCallBackListener
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.sayx.hm_cloud.model.AccountInfo
import com.sayx.hm_cloud.model.GameError
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.model.PCMouseEvent
import com.sayx.hm_cloud.utils.GameUtils
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object GameManager : HmcpPlayerListener {

    private lateinit var channel: MethodChannel

    val gson: Gson by lazy {
        GsonBuilder().disableHtmlEscaping().create()
    }

    private var gameParam: GameParam? = null

    var gameView: HmcpVideoView? = null

    private lateinit var context: Context

    lateinit var flutterEngine: FlutterEngine

    var isPlaying = false

    fun init(channel: MethodChannel, context: Context) {
        this.channel = channel
        this.context = context
        LogUtils.getConfig().also {
            it.isLogSwitch = BuildConfig.DEBUG
            it.globalTag = "GameManager"
        }
    }

    fun startGame(gameParam: GameParam) {
        this.gameParam = gameParam
        initGameSdk()
    }

    fun getDefaultKeyboardData() {
        channel.invokeMethod("getDefaultKeyboardData", null)
    }

    fun getKeyboardData() {
        channel.invokeMethod("getKeyboardData", null)
    }

    fun getDefaultGamepadData() {
        channel.invokeMethod("getDefaultGamepadData", null)
    }

    fun getGamepadData() {
        channel.invokeMethod("getGamepadData", null)
    }

    fun updateKeyboardData(data: JsonObject) {
        data.addProperty("game_id", gameParam?.gameId)
        channel.invokeMethod("updateKeyboardData", data.toString())
    }

    private fun initGameSdk() {
        try {
//            Log.e("CloudGame", "init haiMaSDK:$gameParam")
            LogUtils.d("init haiMaSDK:${gson.toJson(gameParam)}")
            val config: Bundle = Bundle().also {
                it.putString(HmcpManager.ACCESS_KEY_ID, gameParam?.accessKeyId)
                it.putString(HmcpManager.CHANNEL_ID, "app_cloud_game")
            }
            Constants.IS_DEBUG = false
            Constants.IS_ERROR = false
            Constants.IS_INFO = false
//            Log.e("CloudGame", "init haiMaSDK:${gameParam?.accessKeyId}")
            LogUtils.d("init haiMaSDK:${gameParam?.accessKeyId}")
            HmcpManager.getInstance().init(config, context, object : OnInitCallBackListener {
                override fun success() {
                    LogUtils.d("haiMaSDK success:${HmcpManager.getInstance().sdkVersion}")
                    checkPlayingGame()
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
//            Log.e("CloudGame", "haiMaSDK fail:${e.message}", e)
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

    private fun checkPlayingGame() {
        LogUtils.d("checkPlayingGame->userId:${this.gameParam?.userId}, userToken:${this.gameParam?.userToken}")
        HmcpManager.getInstance().checkPlayingGame(UserInfo().also {
            it.userId = this.gameParam?.userId
            it.userToken = this.gameParam?.userToken
        }, object : OnGameIsAliveListener {
            override fun success(list: MutableList<CheckCloudServiceResult.ChannelInfo>?) {
                LogUtils.d("checkPlayingGame:$list")
                var cid: String? = null
                if (!list.isNullOrEmpty()) {
                    // 有未释放的游戏实例
                    val channelInfo = list[0]
                    if (channelInfo.pkgName.equals(gameParam?.gamePkName)) {
                        cid = channelInfo.cid
                    }
                }
                prepareGame(cid)
            }

            override fun fail(msg: String?) {
                LogUtils.d("checkPlayingGameFail->Msg:$msg")
                prepareGame()
            }
        })
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
                // 排队优先级
                it.putInt(HmcpVideoView.PRIORITY, gameParam?.priority ?: 0)
                // 游戏名称
                it.putString(HmcpVideoView.APP_NAME, gameParam?.gamePkName)
                // 渠道
                it.putString(HmcpVideoView.APP_CHANNEL, gameParam?.channelName)
                // token
                it.putString(HmcpVideoView.C_TOKEN, gameParam?.cToken)
//                it.putString(HmcpVideoView.EXTRA_ID, AppConstants.extraId)
                // 是否存档
//                it.putBoolean(HmcpVideoView.ARCHIVED, true)
                // 业务参数（调试版不传扣费数据）
//                if (!BuildConfig.DEBUG) {
                it.putString(
                    HmcpVideoView.PAY_PROTO_DATA,
                    GameUtils.getProtoData(
                        gson,
                        gameParam?.userId,
                        gameParam?.gameId,
                        gameParam?.priority ?: 0
                    )
                )
//                }
                // 码率
//                it.putInt(HmcpVideoView.INTERNET_SPEED, 300)
                // 清晰度挡位，会员默认超清，非会员默认流畅
                if (gameParam?.isVip() == false) {
                    it.putInt(HmcpVideoView.RESOLUTION_ID, 1)
                } else {
                    it.putInt(HmcpVideoView.RESOLUTION_ID, 4)
                }
                // 显示剩余时间
//                it.putBoolean(HmcpVideoView.IS_SHOW_TIME, true)
                if (!TextUtils.isEmpty(cid)) {
                    it.putString(HmcpVideoView.C_ID, cid)
                }
                // 背景色
//                it.putInt(HmcpVideoView.VERTICAL_BACKGROUND, Color.BLACK)
                // 分辨率宽高
//                it.putInt(HmcpVideoView.VIEW_RESOLUTION_WIDTH, 1920)
//                it.putInt(HmcpVideoView.VIEW_RESOLUTION_HEIGHT, 1080)
                // 流类型 0：表示RTMP 1：表示WEBRTC, 默认0
                it.putInt(HmcpVideoView.STREAM_TYPE, 0)
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
            // 通常是已进入普通队列，切换高速队列，释放普通队列实例，重新进入高速队列
            releaseGame(finish = "0", bundle)
        } else {
            gameView = HmcpVideoView(context)
            gameView?.setUserInfo(UserInfo().also {
                it.userId = gameParam?.userId
                it.userToken = gameParam?.userToken
            })
            // 上号助手
            gameParam?.accountInfo?.let { accountInfo ->
//            LogUtils.d("AccountInfo 1:${accountInfo.javaClass}")
                val result = gson.fromJson(gson.toJson(accountInfo), AccountInfo::class.java)
//            LogUtils.d("AccountInfo 2:${result}")
                gameView?.setExtraData(IntentExtraData().also {
                    it.setStringExtra(GameUtils.getStringData(result))
                })
            }
            gameView?.setConfigInfo("configInfo")
            gameView?.hmcpPlayerListener = this
            gameView?.virtualDeviceType = VirtualOperateType.NONE
            gameView?.play(bundle)
            // 默认静音启动，隐藏云端操作
            gameView?.setAudioMute(true)
            gameView?.virtualDeviceType = VirtualOperateType.NONE
        }
    }

    fun releaseGame(finish: String, bundle: Bundle?) {
        LogUtils.d("releaseGame:$finish")
        if (finish != "0") {
            // 非切换队列调用此方法，认定为退出游戏
            channel.invokeMethod("exitGame", mapOf(Pair("action", finish)))
        }
        isPlaying = false
        val cloudId = HmcpManager.getInstance().cloudId
        if (TextUtils.isEmpty(cloudId)) {
            LogUtils.d("undo releaseGame, cid is empty")
            gameView?.onDestroy()
            gameView = null
            if (finish == "0") {
                // 切换队列
                playGame(bundle)
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
                    // 游戏释放成功
                    LogUtils.d("releaseGame:$result")
                    gameView?.onDestroy()
                    gameView = null
                    if (finish == "0") {
                        // 切换队列
                        playGame(bundle)
                    }
                }

                override fun fail(error: String?) {
                    // 游戏释放失败
                    LogUtils.e("releaseGame:$error")
                    gameView?.onDestroy()
                    gameView = null
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

    override fun HmcpPlayerStatusCallback(statusData: String?) {
        LogUtils.d("playerStatusCallback:$statusData, cid:${HmcpManager.getInstance().cloudId}")
        statusData?.let {
            val data = JSONObject(it)
            when (val status = data.getInt(StatusCallbackUtil.STATUS)) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    gameView?.play()
                }
                // sdk反馈需选择是否进入排队，直接进入排队
                Constants.STATUS_WAIT_CHOOSE -> {
                    gameView?.entryQueue()
                }
                // 网络切换，尝试重连
                Constants.STATUS_TIPS_CHANGE_WIFI_TO_4G -> {
                    gameView?.reconnection()
                }
                // 实例进入排队，sdk反馈排队时间
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
                        LogUtils.e("queue info error:$dataStr")
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
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            context.startActivity(this)
                        }
                    } else {
                        LogUtils.e("The game feeds back the first frame again.")
                    }
                }

                Constants.STATUS_OPERATION_GAME_TIME_COUNT_DOWN -> {
                    val dataStr = data.getString(StatusCallbackUtil.DATA)
                    if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                        val resultData = gson.fromJson(dataStr, Map::class.java)
                        LogUtils.d("下线倒计时:${resultData["ahead"]}")
                    } else {
                        LogUtils.e("gameTimeCountDown error:$dataStr")
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

    fun setPCMouseMode(arguments: Any?) {
        if (arguments is Boolean) {
            gameView?.setPCMouseMode(arguments)
            EventBus.getDefault().post(PCMouseEvent(arguments))
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

    fun getGameParam(): GameParam? {
        return gameParam
    }

    fun openBuyPeakTime() {
        Intent().apply {
            putExtra("route", "/rechargeCenter")
            putExtra("arguments", Bundle().also {
                it.putString("type", "rechargeTime")
            })
            setClass(context, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    fun openBuyVip() {
        Intent().apply {
            putExtra("route", "/rechargeCenter")
            putExtra("arguments", Bundle().also {
                it.putString("type", "rechargeVip")
            })
            setClass(context, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    @JvmStatic
    fun openFlutterPage(route: String?, arguments: Bundle?) {
        val param = mutableMapOf<String, Any?>()
        param["route"] = route
        param["arguments"] = arguments.toString()
        channel.invokeMethod("openPage", param, object : MethodChannel.Result {
            override fun success(result: Any?) {
                LogUtils.d("openFlutterPage success:$result")
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
            }

            override fun notImplemented() {
            }
        })
    }

    fun exitGame(data: Map<*, *>) {
        channel.invokeMethod("exitGame", data)
    }

    fun openInteraction(cid: String?, open: Boolean) {
        channel.invokeMethod("openInteraction", mapOf(Pair("cid", cid), Pair("open", open)))
    }
}