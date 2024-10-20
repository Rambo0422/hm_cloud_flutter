package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.haima.hmcp.Constants
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.CheckCloudServiceResult
import com.haima.hmcp.beans.Control
import com.haima.hmcp.beans.ControlInfo
import com.haima.hmcp.beans.IntentExtraData
import com.haima.hmcp.beans.PlayNotification
import com.haima.hmcp.beans.SerializableMap
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.beans.UserInfo2
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.listeners.OnContronListener
import com.haima.hmcp.listeners.OnGameIsAliveListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.haima.hmcp.listeners.OnSaveGameCallBackListener
import com.haima.hmcp.listeners.OnUpdataGameUIDListener
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.dialog.AppCommonDialog
import com.sayx.hm_cloud.http.AppRepository
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.bean.HttpStatusConstants
import com.sayx.hm_cloud.model.AccountInfo
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.GameError
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.model.PCMouseEvent
import com.sayx.hm_cloud.model.SpecificArchive
import com.sayx.hm_cloud.model.TimeUpdateEvent
import com.sayx.hm_cloud.utils.GameUtils
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

@SuppressLint("StaticFieldLeak")
object GameManager : HmcpPlayerListener, OnContronListener {

    private lateinit var channel: MethodChannel

    val gson: Gson by lazy {
        GsonBuilder().disableHtmlEscaping()
            .create()
    }

    private var gameParam: GameParam? = null

    fun getGameParam(): GameParam? {
        return gameParam
    }

    var gameView: HmcpVideoView? = null

    // 此处绑定的是HMCloudPlugin挂载的activity
    private lateinit var activity: Activity

    var flutterActivity: AppFlutterActivity? = null

    lateinit var flutterEngine: FlutterEngine

    var lastControllerType = AppVirtualOperateType.NONE

    var isPlaying = false

    var isVideoShowed = false

    var openGame = false

    private var resume = false

    var inQueue = false

    private var needReattach = false

    private var needShowNotice = false

    var initState = false

    // 是否是派对吧
    var isPartyPlay = false
    var isPartyPlayOwner = false

    var disposable: Disposable? = null

    fun init(channel: MethodChannel, context: Activity) {
        this.channel = channel
        this.activity = context
        LogUtils.getConfig().also {
            it.isLogSwitch = BuildConfig.DEBUG
            it.globalTag = "GameManager"
        }
    }

    fun initSDK(gameParam: GameParam) {
        this.gameParam = gameParam
        val config: Bundle = Bundle().also {
            it.putString(HmcpManager.ACCESS_KEY_ID, gameParam.accessKeyId)
            it.putString(HmcpManager.CHANNEL_ID, gameParam.channelName)
        }
        Constants.IS_DEBUG = false
        Constants.IS_ERROR = false
        Constants.IS_INFO = false

        channel.invokeMethod(
            "gameStatusStat", mapOf(
                Pair("type", "game_init"),
                Pair("page", "游戏初始化"),
                Pair("action", "游戏初始化"),
                Pair("arguments", gameParam.toString())
            )
        )
        HmcpManager.getInstance().releaseRequestManager()

        HmcpManager.getInstance().init(config, activity, object : OnInitCallBackListener {
            override fun success() {
                LogUtils.d("haiMaSDK success:${HmcpManager.getInstance().sdkVersion}")
                initState = true
            }

            override fun fail(msg: String?) {
                LogUtils.e("haiMaSDK fail:$msg")
                var errorCode = GameError.gameInitErrorCode
                var errorMsg = GameError.gameInitErrorMsg
                if (msg is String && !TextUtils.isEmpty(msg)) {
                    val resultData = gson.fromJson(msg, Map::class.java)
                    var errorCodeWithoutCid = ""
                    try {
                        errorCodeWithoutCid =
                            if (resultData["errorCodeWithoutCid"].toString() == "null") GameError.gameInitErrorCode else resultData["errorCodeWithoutCid"].toString()
                    } catch (e: Exception) {
                        LogUtils.e("${e.message}")
                    }
                    errorCode =
                        if (TextUtils.isEmpty(errorCodeWithoutCid)) GameError.gameInitErrorCode else errorCodeWithoutCid
                    try {
                        errorMsg =
                            if (resultData["errorMessage"].toString() == "null") resultData["errorMsg"].toString() else resultData["errorMessage"].toString()
                    } catch (e: Exception) {
                        LogUtils.e("${e.message}")
                    }
                }
                channel.invokeMethod(
                    "errorInfo",
                    mapOf(Pair("errorCode", errorCode), Pair("errorMsg", errorMsg))
                )
            }
        }, true)
    }

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun getUnReleaseGame(callback: MethodChannel.Result?) {
        if (!initState) {
            handler.postDelayed({
                getUnReleaseGame(callback)
            }, 3000L)
            return
        }
        channel.invokeMethod(
            "gameStatusStat", mapOf(
                Pair("type", "game_prepare"),
                Pair("page", "游戏检查"),
                Pair("action", "游戏检查"),
                Pair("arguments", gameParam?.toString())
            )
        )
        HmcpManager.getInstance().checkPlayingGame(UserInfo().also {
            it.userId = gameParam?.userId
            it.userToken = gameParam?.userToken
        }, gameParam?.accessKeyId, object : OnGameIsAliveListener {
            override fun success(list: MutableList<CheckCloudServiceResult.ChannelInfo>?) {
                LogUtils.d("checkPlayingGame:$list")
                val map = mutableMapOf<String, Any>(
                    Pair("isSucc", 1)
                )
                if (!list.isNullOrEmpty()) {
                    // 有未释放的游戏实例
                    val channelInfo = list[0]
                    LogUtils.d("checkPlayingGame->cid:${channelInfo.cid}, pkgName:${channelInfo.pkgName}, appChannel:${channelInfo.appChannel}")
                    map["data"] = arrayOf(
                        mapOf(
                            Pair("cid", channelInfo.cid),
                            Pair("pkgName", channelInfo.pkgName),
                            Pair("appChannel", channelInfo.appChannel),
                            Pair("gameName", "")
                        )
                    )
                    callback?.success(gson.fromJson(gson.toJson(map), Map::class.java))
                } else {
                    map["data"] = emptyList<Map<String, Any>>()
                    callback?.success(map)
                }
            }

            override fun fail(msg: String?) {
                LogUtils.d("checkPlayingGameFail->Msg:$msg")
                callback?.success(mapOf(Pair("isSucc", 0)))
            }
        })
    }

    fun getArchiveProgress(callback: MethodChannel.Result) {
        HmcpManager.getInstance()
            .getGameArchiveStatus(gameParam?.gamePkName, UserInfo().apply {
                userId = gameParam?.userId
                userToken = gameParam?.userToken
            }, gameParam?.accessKeyId, gameParam?.channelName, object : OnSaveGameCallBackListener {
                override fun success(result: Boolean) {
                    LogUtils.v("getArchiveProgress->success:$result")
                    callback.success(result)
                }

                override fun fail(msg: String?) {
                    LogUtils.v("getArchiveProgress->fail:$msg")
                    callback.success(false)
                }
            })
    }

    fun startGame(gameParam: GameParam) {
        needShowNotice = false
        this.gameParam = gameParam
        channel.invokeMethod(
            "gameStatusStat", mapOf(
                Pair("type", "game_start"),
                Pair("page", "游戏开始"),
                Pair("action", "检查游戏存档数据"),
                Pair("arguments", gameParam.toString())
            )
        )
        val params: HashMap<String, Any> = hashMapOf(
            "userId" to gameParam.userId,
            "gameId" to gameParam.gameId,
            "isNew" to 0,
            "bid" to gameParam.accessKeyId
        )
        AppRepository().requestArchiveData(
            params,
            object : Observer<HttpResponse<ArchiveData>> {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

                override fun onError(e: Throwable) {
                    channel.invokeMethod(
                        "errorInfo", mapOf(
                            "errorCode" to "${HttpStatusConstants.serviceException}",
                            "errorMsg" to "获取游戏存档数据发生错误"
                        )
                    )
                }

                override fun onComplete() {
                }

                override fun onNext(response: HttpResponse<ArchiveData>) {
                    response.data?.let {
                        prepareGame(it)
                    }
                }
            })
    }

    /**
     * 准备进入游戏队列
     */
    private fun prepareGame(archiveData: ArchiveData) {
        LogUtils.d("priority:${gameParam?.priority}")
        channel.invokeMethod(
            "gameStatusStat", mapOf(
                Pair("type", "game_prepare"),
                Pair("page", "游戏准备"),
                Pair("action", "游戏准备"),
                Pair("arguments", gameParam?.toString())
            )
        )
        try {
            val bundle = Bundle().also {
                // 横屏
                it.putSerializable(HmcpVideoView.ORIENTATION, ScreenOrientation.LANDSCAPE)
                // 可玩时间
                val playTime: Long = gameParam?.playTime ?: 0L
//                val playTime: Long = 20 * 1000L
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
                // 是否使用存档
                it.putBoolean(HmcpVideoView.ARCHIVED, true)
                // 业务参数
                it.putString(
                    HmcpVideoView.PAY_PROTO_DATA,
                    GameUtils.getProtoData(
                        gson,
                        gameParam?.userId,
                        gameParam?.gameId,
                        gameParam?.priority ?: 1
                    )
                )
                // 码率
//                it.putInt(HmcpVideoView.INTERNET_SPEED, 300)
                // 清晰度挡位，会员默认超清，非会员默认流畅
                if (gameParam?.isVip() == false) {
                    it.putInt(HmcpVideoView.RESOLUTION_ID, 4)
                } else {
                    it.putInt(HmcpVideoView.RESOLUTION_ID, 1)
                }
                // 显示剩余时间
//                it.putBoolean(HmcpVideoView.IS_SHOW_TIME, true)
                // 背景色
//                it.putInt(HmcpVideoView.VERTICAL_BACKGROUND, Color.BLACK)
                // 分辨率宽高
//                it.putInt(HmcpVideoView.VIEW_RESOLUTION_WIDTH, 1920)
//                it.putInt(HmcpVideoView.VIEW_RESOLUTION_HEIGHT, 1080)
                // 流类型 0：表示RTMP 1：表示WEBRTC, 默认0
                // 建议使用rtc：1
                // 建议使用rtc：1
                // 建议使用rtc：1
                it.putInt(HmcpVideoView.STREAM_TYPE, 1)
                // rtmp解码类型 0：软解码 1：硬解码, 默认1
//                it.putInt(HmcpVideoView.DECODE_TYPE, 1)
                // 输入法类型 0：表示云端键盘 1：表示本地键盘
//                it.putInt(HmcpVideoView.IME_TYPE, 1)
                // 存档上传
                if (archiveData.custodian == "3a") {
                    val specificArchive = SpecificArchive()
                    specificArchive.uploadArchive = true
                    specificArchive.gameId = gameParam?.gameId ?: ""
                    if (!archiveData.list.isNullOrEmpty()) {
                        val archiveInfo = archiveData.list.first()
                        specificArchive.isThirdParty = true
                        specificArchive.downloadUrl = archiveInfo.downLoadUrl
                        specificArchive.md5 = archiveInfo.fileMD5
                        // 注意这里的cid，是 long 类型
                        specificArchive.cid = archiveInfo.cid.toLong()
                    } else {
                        specificArchive.isThirdParty = false
                    }
                    val hashMap = HashMap<String, Serializable>()
                    hashMap["specificArchive"] = specificArchive
                    val data = SerializableMap(hashMap)
                    it.putSerializable(HmcpVideoView.TRANSMISSION_DATA_TO_SAAS, data)
                }
            }
            LogUtils.d("prepareGame->bundle:$bundle")
            if (initState) {
                playGame(bundle)
            } else {
                handler.postDelayed({
                    playGame(bundle)
                }, 3000L)
            }
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
        channel.invokeMethod(
            "gameStatusStat", mapOf(
                Pair("type", "game_play"),
                Pair("page", "游戏开始"),
                Pair("action", "游戏开始"),
                Pair("arguments", gameParam?.toString())
            )
        )
        if (gameView != null) {
            // 通常是已进入普通队列，切换高速队列，释放普通队列实例，重新进入高速队列
            if (!isPlaying) {
                releaseGame(finish = "0", bundle)
            }
        } else {
            gameView = HmcpVideoView(activity)
            gameView?.setUserInfo(UserInfo().also {
                it.userId = gameParam?.userId
                it.userToken = gameParam?.userToken
            })
            LogUtils.d("playGame:${gameParam?.accountInfo}")
            // 上号助手
            gameParam?.accountInfo?.let { accountInfo ->
//            LogUtils.d("AccountInfo 1:${accountInfo.javaClass}")
                val result = gson.fromJson(gson.toJson(accountInfo), AccountInfo::class.java)
//            LogUtils.d("AccountInfo 2:${result.json}")
                gameView?.setExtraData(IntentExtraData().also {
                    it.setStringExtra(GameUtils.getStringData(result))
                })
            }
            gameView?.setConfigInfo("configInfo")
            // 状态监听
            gameView?.hmcpPlayerListener = this
            gameView?.virtualDeviceType = VirtualOperateType.NONE
            gameView?.play(bundle)
            // 默认静音启动，隐藏虚拟操作按钮
//            gameView?.setAudioMute(true)
            gameView?.virtualDeviceType = VirtualOperateType.NONE
        }
    }

    fun onActivityResumed(activity: Activity) {
        resume = true
        if (openGame) {
            Intent().apply {
                setClass(GameManager.activity, GameActivity::class.java)
                GameManager.activity.startActivityForResult(this, 200)
            }
        }
        if (activity is GameActivity && needShowNotice) {
            needShowNotice = false
            AppCommonDialog.Builder(activity)
                .setTitle("温馨提示")
                .setSubTitle("游戏过程中请勿切换应用或刷新页面，会导致无法运行游戏", Color.parseColor("#FF555A69"))
                .setRightButton("知道了") {
                    AppCommonDialog.hideDialog(activity)
                }
                .build()
                .show()
        }
    }

    fun onActivityPaused(activity: Activity) {
        resume = false
        if (activity is GameActivity) {
            needShowNotice = true
        }
    }

    override fun HmcpPlayerStatusCallback(statusData: String?) {
        LogUtils.d("playerStatusCallback:$statusData, cid:${HmcpManager.getInstance().cloudId}")
        statusData?.let {
            val data = JSONObject(it)
            val status = data.getInt(StatusCallbackUtil.STATUS)
            channel.invokeMethod(
                "gameStatusStat", mapOf(
                    Pair("type", "game_sdk_status"),
                    Pair("page", "$status"),
                    Pair("action", data.getString(StatusCallbackUtil.DATA)),
                    Pair("arguments", gameParam?.toString())
                )
            )
            when (status) {
                // 游戏准备完成，可以启动游戏
                Constants.STATUS_PLAY_INTERNAL -> {
                    gameView?.play()
                }
                // sdk反馈需选择是否进入排队，直接进入排队
                Constants.STATUS_WAIT_CHOOSE -> {
                    if (resume) {
                        gameView?.entryQueue()
                    } else {

                    }
                }

                Constants.STATUS_START_PLAY -> {
                    isPlaying = true
                    inQueue = false
                }
                // 网络切换，尝试重连
                Constants.STATUS_TIPS_CHANGE_WIFI_TO_4G -> {
                    gameView?.reconnection()
                }
                // 实例进入排队，sdk反馈排队时间
                Constants.STATUS_OPERATION_INTERVAL_TIME -> {
                    inQueue = true
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
                // 游戏首帧画面到达，可展示游戏画面
                Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                    if (!isVideoShowed) {
                        isVideoShowed = true
                        gameView?.virtualDeviceType = VirtualOperateType.NONE
                        channel.invokeMethod(
                            GameViewConstants.firstFrameArrival, mapOf(
                                Pair("cid", HmcpManager.getInstance().cloudId)
                            )
                        )
                        openGame = true
                        // 打开新的页面展示游戏画面
                        Intent().apply {
                            setClass(activity, GameActivity::class.java)
                            activity.startActivityForResult(this, 200)
                        }
                    } else {
                        LogUtils.e("The game feeds back the first frame again.")
                    }
                }
                // sdk回调下线提醒
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
                    if (!isPartyPlay || (isPartyPlay && isPartyPlayOwner)) {
                        statusOperationStateChangeReason(data, status)
                    } else {
                        LogUtils.d("这里只有一种情况，当前是派对吧，但是是游客")
                    }
                }

                else -> {}
            }
        }
    }

    fun statusOperationStateChangeReason(data: JSONObject, status: Int) {
        // 各类游戏中断状态下，获取errorCode,errorMsg展示
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
//        channel.invokeMethod(
//            "reportError",
//            mapOf(Pair("cid", HmcpManager.getInstance().cloudId), Pair("errorInfo", dataStr))
//        )
        channel.invokeMethod(
            "errorInfo",
            mapOf(Pair("errorCode", errorCode), Pair("errorMsg", errorMsg))
        )
    }

    /// 游戏云游直播开关
    fun openInteraction(cid: String?, open: Boolean) {
        channel.invokeMethod("openInteraction", mapOf(Pair("cid", cid), Pair("open", open)))
    }

    // 获取默认虚拟键盘数据
    fun getDefaultKeyboardData() {
        channel.invokeMethod("getDefaultKeyboardData", null)
    }

    // 获取虚拟键盘数据
    fun getKeyboardData() {
        channel.invokeMethod("getKeyboardData", null)
    }

    // 获取默认虚拟手柄数据
    fun getDefaultGamepadData() {
        channel.invokeMethod("getDefaultGamepadData", null)
    }

    // 获取默认手柄数据
    fun getGamepadData() {
        channel.invokeMethod("getGamepadData", null)
    }

    // 更新虚拟操作数据
    fun updateKeyboardData(data: JsonObject) {
        data.addProperty("game_id", gameParam?.gameId)
        channel.invokeMethod("updateKeyboardData", data.toString())
    }

    // 根据设备连接，修改鼠标模式
    fun setPCMouseMode(arguments: Any?) {
        if (arguments is Boolean) {
            gameView?.setPCMouseMode(arguments)
            EventBus.getDefault().post(PCMouseEvent(arguments))
        }
    }

    fun getGameData() {
        channel.invokeMethod("getGameData", null)
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
        LogUtils.d("onSceneChanged:$sceneMessage, cid:${HmcpManager.getInstance().cloudId}")
        sceneMessage?.let {
            val data = gson.fromJson(it, Map::class.java)
            val sceneId = data["sceneId"]
            when (sceneId) {
                "play" -> {
                    isPlaying = true
                }
            }
        }
    }

    override fun onNetworkChanged(networkState: NetWorkState?) {
        LogUtils.d("onNetworkChanged:$networkState")
    }

    override fun onPlayStatus(status: Int, value: Long, data: String?) {
        LogUtils.d("onPlayStatus->status:$status, value:$value, data:$data")
    }

    override fun onPlayerError(errorCode: String?, errorMsg: String?) {
        LogUtils.e("onPlayerError->errorCode:$errorCode, errorMsg:$errorMsg")
        channel.invokeMethod(
            "errorInfo",
            mapOf(
                Pair("errorCode", errorCode),
                Pair("errorMsg", errorMsg),
            )
        )

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

    fun openBuyPeakTime() {
        Intent().apply {
            putExtra("route", "/rechargeCenter")
            putExtra("arguments", Bundle().also {
                it.putString("type", "rechargeTime")
                it.putString("from", "native")
            })
            setClass(activity, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(this)
        }
    }

    fun openBuyVip() {
        Intent().apply {
            putExtra("route", "/rechargeCenter")
            putExtra("arguments", Bundle().also {
                it.putString("type", "rechargeVip")
                it.putString("from", "native")
            })
            setClass(activity, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(this)
        }
    }

    fun statGameTime(time: Long) {
        channel.invokeMethod("statGameTime", mapOf(Pair("time", time)))
    }

    fun gameStat(page: String, action: String, arg: Map<String, Any>? = null, type: String = "event") {
        channel.invokeMethod("gameStat", mapOf(Pair("page", page), Pair("action", action), Pair("arguments", arg), Pair("type", type)))
    }

    fun statGamePlay() {
        channel.invokeMethod("statGamePlay", null)
    }

    fun openFlutterPage(route: String?, arguments: Map<String, Any>?) {
        val param = mutableMapOf<String, Any?>()
        param["route"] = route
        param["arguments"] = arguments
        needReattach = true
        channel.invokeMethod("openPage", param)
    }

    fun updateGamePlayableTime() {
        channel.invokeMethod("updateTime", null)
    }

    fun updatePlayInfo(arguments: Map<*, *>) {
        val playTime = (arguments["playTime"] as Number?)?.toLong() ?: 0L
        val peakTime = GameParam.getTimeValue(arguments["peakTime"])
        val vipExpiredTime = GameParam.getTimeValue(arguments["vipExpiredTime"])
        gameParam?.playTime = playTime
        gameParam?.peakTime = peakTime
        gameParam?.vipExpiredTime = vipExpiredTime
        val bundle = Bundle().apply {
            putLong(HmcpVideoView.PLAY_TIME, playTime)
            putString(HmcpVideoView.USER_ID, gameParam?.userId)
            putString(HmcpVideoView.TIPS_MSG, "");
            putString(
                HmcpVideoView.PAY_PROTO_DATA,
                GameUtils.getProtoData(
                    gson,
                    gameParam?.userId,
                    gameParam?.gameId,
                    gameParam?.priority ?: 1
                )
            )
            putString(HmcpVideoView.C_TOKEN, gameParam?.cToken)
        }
        LogUtils.d("updatePlayTime:$bundle")
        gameView?.updateGameUID(bundle, object : OnUpdataGameUIDListener {
            override fun success(result: Boolean) {
                LogUtils.v("updateGameUID->success:$result")
                if (result) {
                    EventBus.getDefault().post(TimeUpdateEvent(gameParam!!))
                }
            }

            override fun fail(result: String?) {
                LogUtils.v("updateGameUID->fail:$result")
            }
        })
    }

    fun exitQueue() {
        releaseGame("-1")
    }

    fun exitGame() {
        channel.invokeMethod("exitGame", mapOf(Pair("action", "0")))
    }

    fun releaseGame(gameParam: GameParam, callback: MethodChannel.Result) {
        HmcpManager.getInstance().setReleaseCid(
            gameParam.gamePkName, gameParam.cid, gameParam.cToken, gameParam.channelName,
            UserInfo2().also {
                it.userId = gameParam.userId
                it.userToken = gameParam.userToken
            },
            gameParam.accessKeyId,
            object : OnSaveGameCallBackListener {
                override fun success(result: Boolean) {
                    // 游戏释放成功
                    LogUtils.d("releaseGame:$result")
                    callback.success(result)
                }

                override fun fail(error: String?) {
                    // 游戏释放失败
                    LogUtils.e("releaseGame:$error")
                    callback.success(false)
                }
            }
        )
    }

    /// 游戏释放
    fun releaseGame(finish: String, bundle: Bundle? = null) {
        LogUtils.d("releaseGame:$finish")
        disposable?.dispose()
        val cloudId = HmcpManager.getInstance().cloudId
        channel.invokeMethod(
            "gameStatusStat", mapOf(
                Pair("type", "game_release"),
                Pair("page", "游戏释放"),
                Pair("action", "游戏释放:$finish, $cloudId"),
                Pair("arguments", gameParam?.toString())
            )
        )
        if (finish != "0") {
            // 非切换队列调用此方法，认定为退出游戏
            channel.invokeMethod(
                "exitGame",
                mapOf(Pair("action", finish), Pair("needReattach", needReattach))
            )
            needReattach = false
        }
        if (TextUtils.isEmpty(cloudId)) {
            LogUtils.d("undo releaseGame, cid is empty")
            gameView?.release()
            gameView?.onDestroy()
            gameView = null
            isPlaying = false
            inQueue = false
            isVideoShowed = false
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
            gameParam?.accessKeyId,
            object : OnSaveGameCallBackListener {
                override fun success(result: Boolean) {
                    // 游戏释放成功
                    LogUtils.d("releaseGame:$result")
                    gameView?.release()
                    gameView?.onDestroy()
                    gameView = null
                    isPlaying = false
                    inQueue = false
                    isVideoShowed = false
                    if (finish == "0") {
                        // 切换队列
                        playGame(bundle)
                    }
                }

                override fun fail(error: String?) {
                    // 游戏释放失败
                    LogUtils.e("releaseGame:$error")
                    gameView?.release()
                    gameView?.onDestroy()
                    gameView = null
                    isPlaying = false
                    inQueue = false
                    isVideoShowed = false
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

    fun releasePlayPartyGame() {
        gameView?.release()
        gameView?.onDestroy()
        gameView = null
        isPlaying = false
        inQueue = false
        isVideoShowed = false
    }

    override fun pinCodeResult(success: Boolean, cid: String?, pinCode: String?, msg: String?) {
        if (success && !TextUtils.isEmpty(pinCode) && !TextUtils.isEmpty(cid)) {
            val map = hashMapOf<String, String>()
            map["pinCode"] = pinCode ?: ""
            map["cid"] = cid ?: ""
            channel.invokeMethod("pinCodeResult", map)
        }
    }

    override fun contronResult(success: Boolean, msg: String?) {
        LogUtils.d("contronResult success: $success msg: $msg")
//        channel.invokeMethod("contronResult", msg)
    }

    override fun contronLost() {
//        LogUtils.d("contronLost")
//        channel.invokeMethod("contronLost", null)
    }

    override fun controlDistribute(
        success: Boolean,
        controlInfo: MutableList<ControlInfo>?,
        msg: String?
    ) {
        val controlInfos = controlInfo ?: emptyList()
        channel.invokeMethod("controlDistribute", gson.toJson(controlInfos))
    }

    override fun controlQuery(
        success: Boolean,
        controlInfos: MutableList<ControlInfo>?,
        msg: String?
    ) {
        if (success) {
            val jsonArray = JSONArray()
            controlInfos?.forEach { controlInfo ->
                val jsonObject = JSONObject()
                jsonObject.put("position", controlInfo.position)
                jsonObject.put("cid", controlInfo.cid.toString())
                jsonArray.put(jsonObject)
            }
            channel.invokeMethod("controlInfos", jsonArray.toString())
        }
    }

    /**
     * 获取授权码
     */
    fun getPinCode() {
        gameView?.getPinCode(this)
    }

    /**
     * 查询当前房间内的⽤户
     */
    fun queryControlUsers() {
        gameView?.queryControlPermitUsers(this)
    }
    var roomIndex = -1
    var userId = ""

    /**
     * 派对吧情况下才会用的到
     * 主要提供给游客，因为游客初始之前是并没有初始化的
     */
    fun initHmcpSdk(arguments: Map<*, *>) {
        isPartyPlay = true
        isPartyPlayOwner = false

        val accessKeyId = arguments["accessKeyId"]?.toString() ?: ""
        val channelName = arguments["channelName"]?.toString() ?: ""
        val cid = arguments["cid"]?.toString() ?: ""
        val pinCode = arguments["pinCode"]?.toString() ?: ""
        val userId = arguments["userId"]?.toString() ?: ""
        val userToken = arguments["userToken"]?.toString() ?: ""
        val roomIndex = (arguments["roomIndex"] as? Int) ?: -1

        this.userId = userId
        this.roomIndex = roomIndex

        if (initState) {
            controlPlay(cid, pinCode, accessKeyId, userId, userToken)
        } else {
            val config = Bundle().apply {
                putString(HmcpManager.ACCESS_KEY_ID, accessKeyId)
                putString(HmcpManager.CHANNEL_ID, channelName)
            }
            HmcpManager.getInstance().init(config, activity, object : OnInitCallBackListener {
                override fun success() {
                    initState = true
                    controlPlay(cid, pinCode, accessKeyId, userId, userToken)
                }

                override fun fail(msg: String?) {
                    initState = false
                }
            }, true)
        }
    }

    fun controlPlay(
        cid: String,
        pinCode: String,
        accessKeyID: String,
        userId: String,
        userToken: String
    ) {
        val userInfo = UserInfo()
        userInfo.userId = userId
        userInfo.userToken = userToken
        initHmcpView(userInfo)

        // 选择流类型。 0：表示RTMP  1：表示WEBRTC
        val streamType = 1

        // 获取控制权参数对象
        val control = Control()
        control.cid = cid
        control.pinCode = pinCode
        control.accessKeyID = accessKeyID
        control.isIPV6 = false
        control.orientation = ScreenOrientation.LANDSCAPE

        gameView?.contronPlay(streamType, control, this)
    }

    private fun initHmcpView(userInfo: UserInfo) {
        gameView = HmcpVideoView(activity)
        gameView?.setUserInfo(userInfo)

        gameView?.hmcpPlayerListener = this
        gameView?.virtualDeviceType = VirtualOperateType.NONE
        // 默认静音启动，隐藏云端操作
//        gameView?.setAudioMute(true)
    }

    fun sendCurrentCid() {
        val cloudId = HmcpManager.getInstance().cloudId
        val cidArr = JSONObject().apply {
            put("index", roomIndex)
            put("uid", userId)
            put("cid", cloudId)
        }
        channel.invokeMethod("cidArr", cidArr.toString())
    }

    /**
     * 设置派对吧每个用户的操作权限
     */
    fun distributeControlPermit(arguments: JSONArray) {
        val list = arrayListOf<ControlInfo>()
        for (i in 0 until arguments.length()) {
            val jsonObject = arguments.getJSONObject(i)
            val controlInfo = ControlInfo().apply {
                cid = jsonObject.getString("cid").toLong()
                position = jsonObject.getInt("position")
            }
            list.add(controlInfo)
        }

        if (list.isNotEmpty()) {
            gameView?.distributeControlPermit(list, this)
        }
    }

    fun wantPlay(uid: String) {
        channel.invokeMethod("wantPlay", uid)
    }

    fun closeUserPlay(uid: String) {
        channel.invokeMethod("closeUserPlay", uid)
    }

    fun letPlay(uid: String) {
        channel.invokeMethod("letPlay", uid)
    }

    fun updatePlayPartyRoomInfo() {
        channel.invokeMethod("updatePlayPartyRoomInfo", null)
    }

    fun changePositionStatus(position: Int, isLock: Boolean) {
        val changePositionStatusData = JSONObject().apply {
            put("position", position)
            put("isLock", isLock)
        }.toString()
        channel.invokeMethod("changePositionStatus", changePositionStatusData)
    }

    fun setPlayPartySoundAndMicrophone(arguments: String) {
        channel.invokeMethod("playPartySoundAndMicrophone", arguments)
    }

    fun kickOutUser(arguments: String) {
        channel.invokeMethod("kickOutUser", arguments)
    }
}