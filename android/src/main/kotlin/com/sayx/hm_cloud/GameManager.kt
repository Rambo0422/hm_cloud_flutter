package com.sayx.hm_cloud

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.haima.hmcp.Constants
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.CheckCloudServiceResult
import com.haima.hmcp.beans.Control
import com.haima.hmcp.beans.ControlInfo
import com.haima.hmcp.beans.IntentExtraData
import com.haima.hmcp.beans.SerializableMap
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.beans.UserInfo2
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.listeners.OnContronListener
import com.haima.hmcp.listeners.OnGameIsAliveListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.haima.hmcp.listeners.OnSaveGameCallBackListener
import com.haima.hmcp.listeners.OnUpdataGameUIDListener
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import com.haima.hmcp.widgets.beans.VirtualOperateType
import com.media.atkit.AnTongManager
import com.sayx.hm_cloud.BuildConfig.DEBUG
import com.sayx.hm_cloud.callback.KeyboardListCallback
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.dialog.AppCommonDialog
import com.sayx.hm_cloud.http.HttpManager
import com.sayx.hm_cloud.http.bean.AppHttpException
import com.sayx.hm_cloud.http.bean.BaseObserver
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.repository.AppRepository
import com.sayx.hm_cloud.http.repository.GameRepository
import com.sayx.hm_cloud.http.repository.UserRepository
import com.sayx.hm_cloud.imp.HmcpPlayerListenerImp
import com.sayx.hm_cloud.model.AccountInfo
import com.sayx.hm_cloud.model.AccountTimeInfo
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.ControllerConfigEvent
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.ErrorDialogConfig
import com.sayx.hm_cloud.model.GameError
import com.sayx.hm_cloud.model.GameErrorEvent
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.model.KeyboardList
import com.sayx.hm_cloud.model.SpecificArchive
import com.sayx.hm_cloud.model.TimeUpdateEvent
import com.sayx.hm_cloud.model.UserRechargeStatusEvent
import com.sayx.hm_cloud.utils.GameUtils
import com.sayx.hm_cloud.utils.TimeUtils
import com.sayx.hm_cloud.widget.HMGameView
import com.sayx.hm_cloud.widget.KeyboardListView
import com.sayx.hm_cloud.widget.TouchEventDispatcher
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

@SuppressLint("StaticFieldLeak")
object GameManager : HmcpPlayerListenerImp(), OnContronListener {

    private lateinit var channel: MethodChannel

    val gson: Gson by lazy {
        GsonBuilder().disableHtmlEscaping()
            .create()
    }

    private var gameParam: GameParam? = null

    fun getGameParam(): GameParam? {
        return gameParam
    }

    var gameView: HMGameView? = null

    // 此处绑定的是HMCloudPlugin挂载的activity
    private lateinit var activity: Activity

    var flutterActivity: AppFlutterActivity? = null
        set(value) {
            field = value
        }

    val gamepadList: MutableList<ControllerInfo> by lazy {
        mutableListOf()
    }

    val keyboardList: MutableList<ControllerInfo> by lazy {
        mutableListOf()
    }

    lateinit var flutterEngine: FlutterEngine

    var lastControllerType = AppVirtualOperateType.NONE

    // 游戏是否启动
    var isPlaying = false

    // 游戏页面是否已经打开
    var isVideoShowed = false

    // 是否需要打开游戏页面
    var openGame = false

    // App是否在前台
    private var resume = false

    private var needReattach = false

    var initState = false

    // 是否是派对吧
    var isPartyPlay = false
    var isPartyPlayOwner = false

    var hasPremission = false

    // 是否是第一次获取手柄或者键盘数据
    private var isFirstGetControllerInfo = true

    var disposable: Disposable? = null

    // 后台配置的错误弹窗信息
    private var dialogConfig: ErrorDialogConfig? = null

    fun init(channel: MethodChannel, context: Activity) {
        this.channel = channel
        this.activity = context
        LogUtils.getConfig().also {
            it.isLogSwitch = DEBUG
            it.globalTag = "GameManager"
        }
    }

    fun initSDK(gameParam: GameParam, callback: MethodChannel.Result) {
        this.gameParam = gameParam
        // 键盘数据重置
        gamepadList.clear()
        keyboardList.clear()
        // touch传递绑定清空
        TouchEventDispatcher.removeView()
        // 网络请求请求头加入token数据
        HttpManager.addHttpHeader("token", gameParam.userToken)

        // 初始化安通 SDK
        if (isAnTong()) {
            AnTongSDK.initSdk(activity, gameParam)
            initState = true
            callback.success(true)
            return
        }

        val config: Bundle = Bundle().also {
            it.putString(HmcpManager.ACCESS_KEY_ID, gameParam.accessKeyId)
            it.putString(HmcpManager.CHANNEL_ID, gameParam.channelName)
        }
        Constants.IS_DEBUG = false
        Constants.IS_ERROR = false
        Constants.IS_INFO = false

        HmcpManager.getInstance().releaseRequestManager()

        HmcpManager.getInstance().init(config, activity, object : OnInitCallBackListener {
            override fun success() {
                LogUtils.d("haiMaSDK success:${HmcpManager.getInstance().sdkVersion}")
                initState = true
                callback.success(true)
            }

            override fun fail(msg: String?) {
                LogUtils.e("haiMaSDK fail:$msg")
                callback.success(false)
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
                    mapOf(
                        Pair("errorCode", errorCode),
                        Pair("errorMsg", errorMsg),
                    )
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

        // 判断是否是安通
        if (isAnTong()) {
            // TODO: 这里暂时先不做处理!!! 延后再处理
//            val userId = this.gameParam?.userId ?: ""
//            AnTongSDK.checkPlayingGame(userId)
//            val map = mutableMapOf<String, Any>(
//                Pair("isSucc", 1)
//            )
//            map["data"] = emptyList<Map<String, Any>>()
//            callback?.success(map)
            val map = mutableMapOf<String, Any>(
                Pair("isSucc", 1)
            )
            map["data"] = emptyList<Map<String, Any>>()
            callback?.success(map)
            return
        }

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
        // 安通没有这部分逻辑，所以直接跳过
        // 判断是否是安通
        if (isAnTong()) {
            callback.success(true)
            return
        }

        HmcpManager.getInstance()
            .getGameArchiveStatus(gameParam?.gamePkName, UserInfo().apply {
                userId = gameParam?.userId
                userToken = gameParam?.userToken
            }, gameParam?.accessKeyId, gameParam?.channelName, object : OnSaveGameCallBackListener {
                override fun success(result: Boolean) {
                    LogUtils.d("getArchiveProgress->success:$result")
                    callback.success(result)
                }

                override fun fail(msg: String?) {
                    LogUtils.d("getArchiveProgress->fail:$msg")
                    callback.success(false)
                }
            })
    }

    var requestCount = 0

    fun startGame(gameParam: GameParam) {
        lastControllerType == AppVirtualOperateType.NONE
        this.gameParam = gameParam

        this.isPartyPlay = gameParam.isPartyGame
        this.isPartyPlayOwner = true
        hasPremission = true
        isFirstGetControllerInfo = true
        getArchiveData(gameParam)
    }

    private fun getArchiveData(gameParam: GameParam) {
        val params: HashMap<String, Any> = hashMapOf(
            "userId" to gameParam.userId,
            "gameId" to gameParam.gameId,
            "isNew" to 0,
            "bid" to gameParam.accessKeyId,
            "clientType" to "Android",
            "channel" to getChannel(),
            "version" to getAppVersion()
        )
        AppRepository.requestArchiveData(
            params,
            object : BaseObserver<HttpResponse<ArchiveData>>() {
                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

                override fun onError(e: Throwable) {
//                    LogUtils.e("requestArchiveData:${e.message}")
                    if (requestCount == 0) {
                        requestCount += 1
                        getArchiveData(gameParam)
                        return
                    }
                    requestCount = 0
                    if (e is AppHttpException) {
                        channel.invokeMethod(
                            "errorInfo", mapOf(
                                "errorCode" to "${e.errorCode}",
                                "errorMsg" to e.errorMessage
                            )
                        )
                    }
                }

                override fun onNext(response: HttpResponse<ArchiveData>) {
                    requestCount = 0
                    prepareGame(response.data)
                }
            })
    }

    private fun getAppVersion(): String {
        val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "${packageInfo.versionName}-${packageInfo.longVersionCode}"
        } else {
            "${packageInfo.versionName}-${packageInfo.versionCode}"
        }
    }

    private fun getChannel(): String {
        return try {
            val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.packageManager.getApplicationInfo(
                    activity.packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                activity.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
            }
            applicationInfo.metaData.getString("CHANNEL_NAME") ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * 准备进入游戏队列
     */
    private fun prepareGame(archiveData: ArchiveData?) {
//        LogUtils.d("priority:${gameParam?.priority}")
        // 进入安通页面
        if (isAnTong()) {
            AnTongSDK.play(
                activity,
                gameParam!!,
                archiveData,
                object : RequestDeviceSuccess {
                    override fun onQueueStatus(time: Int, rank: Int) {
                        activity.runOnUiThread {
                            channel.invokeMethod(
                                "queueInfo",
                                mapOf(
                                    Pair("queueTime", time),
                                    Pair("rank", rank),
                                )
                            )
                        }
                    }

                    override fun onRequestDeviceSuccess() {
                        if (!isVideoShowed) {
                            isVideoShowed = true
                            // 跳转activity
                            activity.runOnUiThread {
                                channel.invokeMethod(GameViewConstants.firstFrameArrival, null)
                            }
                            isPlaying = true
                        }
                    }
                })
            return
        }

        try {
            val bundle = Bundle().also {
                // 横屏
                it.putSerializable(HmcpVideoView.ORIENTATION, ScreenOrientation.LANDSCAPE)
                // 可玩时间
//                val playTime: Long = gameParam?.playTime ?: 0L
//                val playTime: Long = 20 * 1000L
                it.putInt(HmcpVideoView.PLAY_TIME, 99999999)
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
                // cid
                if (gameParam?.cid?.isNotEmpty() == true) {
                    it.putString(HmcpVideoView.C_ID, gameParam?.cid)
                }
                // 业务参数
                it.putString(
                    HmcpVideoView.PAY_PROTO_DATA,
                    GameUtils.getProtoData(
                        gson,
                        gameParam?.userId,
                        gameParam?.gameId,
                        gameParam?.priority ?: 1,
                        "android",
                        "hmy",
                        AppUtils.getAppVersionName(),
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
                if (archiveData?.custodian == "3a") {
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
                    Pair("errorMsg", GameError.gameConfigErrorMsg),
                )
            )
        }
    }

    fun openGamePage() {
        openGame = true

        processEvent("gamePageShow")
        if (isAnTong()) {
            AtGameActivity.startActivityForResult(activity)
        } else {
            Intent().apply {
                setClass(activity, GameActivity::class.java)
                activity.startActivityForResult(this, 200)
            }
        }
    }

    private fun playGame(bundle: Bundle?) {
//        LogUtils.d("playGame:$gameView")
        if (gameView != null) {
            // 通常是已进入普通队列，切换高速队列，释放普通队列实例，重新进入高速队列
            if (!isPlaying) {
                releaseGame(finish = "0", bundle)
            }
        } else {
            gameView = HMGameView(activity)
            gameView?.setUserInfo(UserInfo().also {
                it.userId = gameParam?.userId
                it.userToken = gameParam?.userToken
            })
//            LogUtils.d("playGame:${gameParam?.accountInfo}")
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
            invokeMethod("hm_start", mapOf())
            gameView?.play(bundle)
        }
    }

    fun onActivityResumed(activity: Activity) {
        resume = true
        if (openGame) {
            if (isAnTong()) {
                AtGameActivity.startActivityForResult(activity)
            } else {
                Intent().apply {
                    setClass(GameManager.activity, GameActivity::class.java)
                    GameManager.activity.startActivityForResult(this, 200)
                }
            }
        }
    }

    fun onActivityPaused(activity: Activity) {
        resume = false
    }

    override fun HmcpPlayerStatusCallback(statusData: String?) {
        LogUtils.d("PlayerStatusCallback:$statusData")
        statusData?.let {
            val data = JSONObject(it)
            val status = data.getInt(StatusCallbackUtil.STATUS)
            when (status) {
                // 游戏准备完成，可以启动游戏
                Constants.STATUS_PLAY_INTERNAL -> {
                    gameView?.play()
                }
                // sdk反馈需选择是否进入排队，直接进入排队
                Constants.STATUS_WAIT_CHOOSE -> {
                    if (resume) {
                        gameView?.entryQueue()
                        processEvent("开始排队")
                    } else {

                    }
                }

                Constants.STATUS_START_PLAY -> {
                    isPlaying = true
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
                // 游戏首帧画面到达，可展示游戏画面
                Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                    if (!isVideoShowed) {
                        isVideoShowed = true
                        gameView?.setAudioMute(true)
                        gameView?.virtualDeviceType = VirtualOperateType.NONE
                        channel.invokeMethod(
                            GameViewConstants.firstFrameArrival, mapOf(
                                Pair("cid", HmcpManager.getInstance().cloudId)
                            )
                        )
                        isPlaying = true
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
                // 401，异端登录
                Constants.STATUS_INVALID_CONN_IN_MULTI_CONN -> {
                    EventBus.getDefault().post(GameErrorEvent("$status", ""))
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

    private fun statusOperationStateChangeReason(data: JSONObject, status: Int) {
        // 各类游戏中断状态下，获取errorCode,errorMsg展示
        val dataStr = data.getString(StatusCallbackUtil.DATA)
//        LogUtils.d("errorInfo:$dataStr")
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
            mapOf(
                Pair("errorCode", errorCode),
                Pair("errorMsg", errorMsg)
            )
        )
    }

    /// 游戏云游直播开关
    fun openInteraction(cid: String?, open: Boolean) {
        channel.invokeMethod("openInteraction", mapOf(Pair("cid", cid), Pair("open", open)))
    }

    /**
     * 获取虚拟手柄数据
     *
     * 首次进入，默认操作方式为手柄触发
     */
    fun initGamepadData() {
        LogUtils.d("initGamepadData")
        if (gamepadList.isNotEmpty()) {
            val info = gamepadList.find { data -> data.use == 1 } ?: gamepadList[0]
            EventBus.getDefault().post(ControllerConfigEvent(info))
            return
        }
        if (gameParam?.isVip() == true) {
            getDefaultGamepadData(false, null)
        } else {
            getDefaultGamepadData(true, null)
        }
    }

    /**
     * 获取虚拟键盘数据
     *
     * 首次进入，默认操作方式为键盘触发
     */
    fun initKeyboardData() {
        LogUtils.d("initKeyboardData")
        if (keyboardList.isNotEmpty()) {
            val info = keyboardList.find { data -> data.use == 1 } ?: keyboardList[0]
            EventBus.getDefault().post(ControllerConfigEvent(info))
            return
        }
        if (gameParam?.isVip() == true) {
            getDefaultKeyboardData(false, null)
        } else {
            // 非vip用户，直接使用默认配置
            getDefaultKeyboardData(true, null)
        }
    }

    // 获取默认虚拟手柄数据
    private fun getDefaultGamepadData(defaultUse: Boolean, callback: KeyboardListCallback? = null) {
        LogUtils.d("getDefaultGamepadData:$defaultUse")
        GameRepository.requestDefaultGamepad(gameParam?.gameId, object : BaseObserver<HttpResponse<ControllerInfo>>() {

            override fun onNext(response: HttpResponse<ControllerInfo>) {
                super.onNext(response)
                response.data?.let {
                    if (gamepadList.isEmpty()) {
                        it.isOfficial = true
                        gamepadList.add(0, it)
                        if (callback != null) {
                            it.use = 1
                            callback.onGamepadList(gamepadList)
                            getUserGamepadData(callback)
                        } else if (defaultUse || it.use == 1) {
                            it.use = if (defaultUse) 1 else it.use
                            EventBus.getDefault().post(ControllerConfigEvent(it))
                        } else {
                            getUserGamepadData(null)
                        }
                    }
                }

                if (isFirstGetControllerInfo) {
                    isFirstGetControllerInfo = false
                    processEvent("加载按键成功")
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                val controllerInfo = GameRepository.readDefaultGamepadFromAsset(activity.assets)
                if (controllerInfo != null)  {
                    if (gamepadList.isEmpty()) {
                        controllerInfo.gameId = gameParam?.gameId ?: ""
                        controllerInfo.userId = gameParam?.userId ?: ""
                        gamepadList.add(0, controllerInfo)
                        if (callback != null) {
                            controllerInfo.use = 1
                            callback.onGamepadList(gamepadList)
                            getUserGamepadData(callback)
                        } else if (defaultUse || controllerInfo.use == 1) {
                            controllerInfo.use = if (defaultUse) 1 else controllerInfo.use
                            EventBus.getDefault().post(ControllerConfigEvent(controllerInfo))
                        } else {
                            getUserGamepadData(null)
                        }
                    }
                } else {
                    LogUtils.e("读取assets手柄配置失败")
                }

                if (isFirstGetControllerInfo) {
                    isFirstGetControllerInfo = false
                    processEvent("加载按键成功")
                }
            }
        })
    }

    // 获取默认虚拟键盘数据
    private fun getDefaultKeyboardData(defaultUse: Boolean, callback: KeyboardListCallback? = null) {
        LogUtils.d("getDefaultKeyboardData:$defaultUse")
        GameRepository.requestDefaultKeyboard(gameParam?.gameId, object : BaseObserver<HttpResponse<ControllerInfo>>() {
            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                processEvent("getKeyboard")
            }

            override fun onNext(response: HttpResponse<ControllerInfo>) {
                super.onNext(response)
                response.data?.let {
                    if (keyboardList.isEmpty()) {
                        it.isOfficial = true
                        keyboardList.add(0, it)
                        if (callback != null) {
                            it.use = 1
                            callback.onKeyboardList(keyboardList)
                            getUserKeyboardData(callback)
                        } else if (defaultUse || it.use == 1) {
                            it.use = if (defaultUse) 1 else it.use
                            EventBus.getDefault().post(ControllerConfigEvent(it))
                        } else {
                            getUserKeyboardData(null)
                        }
                    }
                }

                if (isFirstGetControllerInfo) {
                    isFirstGetControllerInfo = false
                    processEvent("加载按键成功")
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                processEvent("keyboardFail")
            }
        })
    }

    fun getUserGamepadData(callback: KeyboardListCallback? = null) {
        LogUtils.d("getUserGamepadData:${callback == null}")
        GameRepository.requestUserGamepadData(gameParam?.gameId, object : BaseObserver<HttpResponse<KeyboardList>>() {
            override fun onNext(response: HttpResponse<KeyboardList>) {
                response.data?.let {
                    gamepadList.addAll(it.datas)
                    if (callback != null) {
                        val info = it.datas.find { data -> data.use == 1 }
                        if (info != null) {
                            if (gameParam?.isVip() == true) {
                                gamepadList[0].use = 0
                            } else {
                                info.use = 0
                            }
                        }
                        callback.onGamepadList(gamepadList)
                    } else {
                        val info = it.datas.find { data -> data.use == 1 } ?: gamepadList[0]
                        EventBus.getDefault().post(ControllerConfigEvent(info))
                    }
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                if (callback != null) {
                    if (e is AppHttpException && e.errorCode != -1) {
                        ToastUtils.showLong("获取用户手柄配置数据失败")
                    }
                } else {
                    EventBus.getDefault().post(ControllerConfigEvent(gamepadList[0]))
                }
            }
        })
    }

    fun getUserKeyboardData(callback: KeyboardListCallback? = null) {
        LogUtils.d("getUserKeyboardData:${callback == null}")
        GameRepository.requestUserKeyboardData(gameParam?.gameId, object : BaseObserver<HttpResponse<KeyboardList>>() {
            override fun onNext(response: HttpResponse<KeyboardList>) {
                response.data?.let {
                    keyboardList.addAll(it.datas)
                    if (callback != null) {
                        val info = it.datas.find { data -> data.use == 1 }
                        if (info != null) {
                            if (gameParam?.isVip() == true) {
                                keyboardList[0].use = 0
                            } else {
                                info.use = 0
                            }
                        }
                        callback.onKeyboardList(keyboardList)
                    } else {
                        val info = it.datas.find { data -> data.use == 1 }?: keyboardList[0]
                        EventBus.getDefault().post(ControllerConfigEvent(info))
                    }
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                if (callback != null) {
                    if (e is AppHttpException && e.errorCode != -1) {
                        ToastUtils.showLong("获取用户键鼠配置数据失败")
                    }
                } else  {
                    EventBus.getDefault().post(ControllerConfigEvent(keyboardList[0]))
                }
            }
        })
    }

    fun getAllGamepad(callback: KeyboardListCallback) {
        LogUtils.d("getAllGamepad")
        if (gamepadList.isEmpty()) {
            getDefaultGamepadData(false, callback)
        } else {
            if (gamepadList[0].isOfficial == true && gamepadList.size == 1) {
                gamepadList[0].use = 1
                callback.onGamepadList(gamepadList)
                getUserGamepadData(callback)
            } else {
                callback.onGamepadList(gamepadList)
            }
        }
    }

    fun getAllKeyboard(callback: KeyboardListCallback) {
        LogUtils.d("getAllKeyboard")
        if (keyboardList.isEmpty()) {
            getDefaultKeyboardData(false, callback)
        } else {
            if (keyboardList[0].isOfficial == true && keyboardList.size == 1) {
                // 只获取到了默认数据
                keyboardList[0].use = 1
                callback.onKeyboardList(keyboardList)
                getUserKeyboardData(callback)
            } else {
                callback.onKeyboardList(keyboardList)
            }
        }
    }

    fun addKeyboardConfig(keyboardInfo: ControllerInfo) {
        LogUtils.d("addKeyboardConfig:$keyboardInfo")
        GameRepository.requestAddKeyboard(keyboardInfo, object : BaseObserver<HttpResponse<String>>() {
            override fun onNext(response: HttpResponse<String>) {
                response.data?.let {
                    keyboardInfo.id = it
                    // 添加成功
                    if (keyboardInfo.type == GameConstants.gamepadConfig) {
                        gamepadList.add(keyboardInfo)
                        KeyboardListView.updateGamePad(gamepadList, "addSuccess")
                    } else if (keyboardInfo.type == GameConstants.keyboardConfig) {
                        keyboardList.add(keyboardInfo)
                        KeyboardListView.updateKeyboard(keyboardList, "addSuccess")
                    }
                }
            }
        })
    }

    fun deleteKeyboardConfig(keyboardInfo: ControllerInfo) {
        LogUtils.d("deleteKeyboardConfig")
        GameRepository.requestDeleteKeyboard(keyboardInfo.id, object : BaseObserver<HttpResponse<Any>>() {
            override fun onNext(response: HttpResponse<Any>) {
                if (keyboardInfo.type == GameConstants.gamepadConfig) {
                    val info = gamepadList.find { info -> info.id == keyboardInfo.id }
                    info?.let {
                        gamepadList.remove(info)
                        if (info.use == 1) {
                            gamepadList[0].use = 1
                            EventBus.getDefault().post(ControllerConfigEvent(gamepadList[0]))
                        }
                    }
                    KeyboardListView.updateGamePad(gamepadList, "deleteSuccess")
                } else if (keyboardInfo.type == GameConstants.keyboardConfig) {
                    val info = keyboardList.find { info -> info.id == keyboardInfo.id }
                    info?.let {
                        keyboardList.remove(info)
                        if (info.use == 1) {
                            keyboardList[0].use = 1
                            EventBus.getDefault().post(ControllerConfigEvent(keyboardList[0]))
                        }
                    }
                    KeyboardListView.updateKeyboard(keyboardList, "deleteSuccess")
                }
            }
        })
    }

    fun useKeyboardData(keyboardInfo: ControllerInfo) {
        LogUtils.d("useKeyboardData:$keyboardInfo")
        keyboardInfo.use = 1
        updateKeyboardData(keyboardInfo, object : BaseObserver<HttpResponse<Any>>() {
            override fun onNext(response: HttpResponse<Any>) {
                if (keyboardInfo.type == GameConstants.gamepadConfig) {
                    val index = gamepadList.indexOfFirst { info -> info.id == keyboardInfo.id }
                    if (index != -1) {
                        gamepadList[index] = keyboardInfo
                    }
                    val info = gamepadList.find { info -> info.id != keyboardInfo.id && info.use == 1 }
                    info?.use = 0
                    KeyboardListView.updateGamePad(gamepadList, "useSuccess")
                } else if (keyboardInfo.type == GameConstants.keyboardConfig) {
                    val index = keyboardList.indexOfFirst { info -> info.id == keyboardInfo.id }
                    if (index != -1) {
                        keyboardList[index] = keyboardInfo
                    }
                    val info = keyboardList.find { info -> info.id != keyboardInfo.id && info.use == 1 }
                    info?.use = 0
                    KeyboardListView.updateKeyboard(keyboardList, "useSuccess")
                }
            }
        })
    }

    fun updateKeyboardConfig(keyboardInfo: ControllerInfo) {
        LogUtils.d("updateKeyboardConfig")
        updateKeyboardData(keyboardInfo, object : BaseObserver<HttpResponse<Any>>() {
            override fun onNext(response: HttpResponse<Any>) {
                if (keyboardInfo.type == GameConstants.gamepadConfig) {
                    val index = gamepadList.indexOfFirst { info -> info.id == keyboardInfo.id }
                    if (index != -1) {
                        gamepadList[index] = keyboardInfo
                    }
                    KeyboardListView.updateGamePad(gamepadList, "updateSuccess")
                } else if (keyboardInfo.type == GameConstants.keyboardConfig) {
                    val index = keyboardList.indexOfFirst { info -> info.id == keyboardInfo.id }
                    if (index != -1) {
                        keyboardList[index] = keyboardInfo
                    }
                    KeyboardListView.updateKeyboard(keyboardList, "updateSuccess")
                }
            }
        })
    }

    // 更新虚拟操作数据
    private fun updateKeyboardData(keyboardInfo: ControllerInfo, observer: Observer<HttpResponse<Any>>) {
        GameRepository.requestUpdateKeyboard(keyboardInfo, observer)
    }

    override fun onSceneChanged(sceneMessage: String?) {
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

    override fun onPlayerError(errorCode: String?, errorMsg: String?) {
        if (errorMsg != "网络请求超时") {
            channel.invokeMethod(
                "errorInfo",
                mapOf(
                    Pair("errorCode", errorCode),
                    Pair("errorMsg", errorMsg),
                )
            )
        } else {
            channel.invokeMethod(
                "errorInfo",
                mapOf(
                    Pair(
                        "errorCode", errorCode?.replace("[", "")
                            ?.replace("]", "")
                            ?.replace("网络请求超时", "")
                            ?.split("-")
                            ?.get(0)
                    ),
                    Pair("errorMsg", errorMsg),
                )
            )
        }
    }

    fun openBuyPeakTime() {
        Intent().apply {
            putExtra("route", "/rechargeCenter")
            putExtra("arguments", Bundle().also {
                it.putString("type", "rechargeTime")
                it.putString("from", "游戏页面")
            })
            setClass(activity, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(this)
        }
    }

    fun openFirstCharge() {
        Intent().apply {
            putExtra("route", "/webview")
            putExtra("arguments", Bundle().also {
                it.putString("url", "https://play.3ayx.net/pages/webView/firstCharge?landscape=true")
                it.putString("gameId", "${gameParam?.gameId}")
                it.putString("from", "游戏页面")
            })
            setClass(activity, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(this)
        }
    }

    fun openLimitActivity() {
        Intent().apply {
            putExtra("route", "/webview")
            putExtra("arguments", Bundle().also {
                it.putString("url", activityUrl?.replace("activity", "webview"))
                it.putString("gameId", "${gameParam?.gameId}")
                it.putString("from", "游戏页面")
            })
            setClass(activity, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(this)
        }
    }

    fun openLimitDiscount() {
        Intent().apply {
            putExtra("route", "/rechargeCenter")
            putExtra("arguments", Bundle().also {
                it.putString("type", "rechargeTime")
                it.putString("from", "游戏页面")
                it.putBoolean("discount", true)
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
                it.putString("from", "游戏页面")
            })
            setClass(activity, AppFlutterActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(this)
        }
    }

    fun statGameTime(time: Long) {
        channel.invokeMethod("statGameTime", mapOf(Pair("time", time)))
    }

    fun invokeMethod(
        method: String,
        arg: Map<String, Any?>? = null
    ) {
        activity.runOnUiThread {
            channel.invokeMethod(method, arg)
        }
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
        if (isAnTong()) {
            EventBus.getDefault().post(TimeUpdateEvent(gameParam!!))
            return
        }
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
                    gameParam?.priority ?: 1,
                    "android",
                    "hmy",
                    AppUtils.getAppVersionName()
                )
            )
            putString(HmcpVideoView.C_TOKEN, gameParam?.cToken)
        }
        LogUtils.d("updatePlayTime:$bundle")
        gameView?.updateGameUID(bundle, object : OnUpdataGameUIDListener {
            override fun success(result: Boolean) {
                LogUtils.d("updateGameUID->success:$result")
                if (result) {
                    EventBus.getDefault().post(TimeUpdateEvent(gameParam!!))
                }
            }

            override fun fail(result: String?) {
                LogUtils.d("updateGameUID->fail:$result")
            }
        })
    }

    fun exitQueue() {
        isVideoShowed = false
        openGame = false
        if (isAnTong()) {
            AnTongSDK.leaveQueue()
        } else {
            releaseGame("-1")
        }
    }

    fun cancelGame() {
        if (isAnTong()) {
            AnTongSDK.stopGame()
        }
        releaseGame(finish = "1")
    }

    fun exitGame() {
        isVideoShowed = false
        openGame = false
        channel.invokeMethod("exitGame", mapOf(Pair("action", "0")))
    }

    fun releaseGame(gameParam: GameParam, callback: MethodChannel.Result) {
        if (isAnTong()) {
            callback.success(true)
            return
        }
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
        val isAnTong = isAnTong()
        // 安通没有 cid，安通使用 userId
        val cloudId = if (isAnTong) {
            this.gameParam?.userId
        } else {
            HmcpManager.getInstance().cloudId
        }
        if (finish != "0") {
            // 非切换队列调用此方法，认定为退出游戏
            channel.invokeMethod(
                "exitGame",
                mapOf(Pair("action", finish), Pair("needReattach", needReattach))
            )
            needReattach = false
        }
        isVideoShowed = false
        if (TextUtils.isEmpty(cloudId)) {
            LogUtils.d("undo releaseGame, cid is empty")
            gameView?.release()
            gameView?.onDestroy()
            gameView = null
            isPlaying = false
            if (finish == "0" && !isAnTong) {
                // 切换队列
                playGame(bundle)
            }
            return
        }

        if (isAnTong || finish == "401") {
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
                    isVideoShowed = false
                    channel.invokeMethod(
                        "errorInfo",
                        mapOf(
                            Pair("errorCode", GameError.gameReleaseErrorCode),
                            Pair("errorMsg", GameError.gameReleaseErrorMsg),
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
        if (isAnTong()) {
            AnTongSDK.getPinCode()
        } else {
            gameView?.getPinCode(this)
        }
    }

    /**
     * 查询当前房间内的⽤户
     */
    fun queryControlUsers() {
        if (isAnTong()) {
            AnTongSDK.queryControlUsers()
        } else {
            gameView?.queryControlPermitUsers(this)
        }
    }

    /**
     * 派对吧情况下才会用的到
     * 主要提供给游客，因为游客初始之前是并没有初始化的
     */
    fun initHmcpSdk(gameParam: GameParam) {
        this.gameParam = gameParam
        isPartyPlay = true
        isPartyPlayOwner = false
        hasPremission = false

        val accessKeyId = gameParam.accessKeyId
        val channelName = gameParam.channelName
        val cid = gameParam.cid
        val pinCode = gameParam.pinCode
        val userId = gameParam.userId
        val userToken = gameParam.userToken

        HttpManager.addHttpHeader("token", gameParam.userToken)

        if (isAnTong()) {
            if (!initState) {
                AnTongSDK.initSdk(activity, gameParam)
            }
            initState = true

            AnTongSDK.controlPlay(activity, gameParam, cid, pinCode)
            return
        }

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
        gameView = HMGameView(activity)
        gameView?.setUserInfo(userInfo)

        gameView?.hmcpPlayerListener = this
        gameView?.virtualDeviceType = VirtualOperateType.NONE
    }

    fun sendCurrentCid(cloudId: String) {
        val cidArr = JSONObject().apply {
            put("index", gameParam?.roomIndex ?: -1)
            put("uid", gameParam?.userId)
            put("cid", cloudId)
        }
        channel.invokeMethod("cidArr", cidArr.toString())
    }

    /**
     * 设置派对吧每个用户的操作权限
     */
    fun distributeControlPermit(arguments: JSONArray) {
        // 判断是否是安通还是海马
        if (isAnTong()) {
            AnTongSDK.distributeControlPermit(arguments)
        } else {
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

    fun isAnTong(): Boolean {
        val channel = gameParam?.channel ?: ""
        if (channel.isEmpty()) {
            // 未配置channel，根据游戏类型判断
            return gameParam?.gameType == AnTongSDK.TYPE
        } else {
            // 配置了channel，根据channel判断
            return channel == AnTongSDK.CHANNEL_TYPE
        }
    }

    fun setErrorDialogConfig(dialogConfig: ErrorDialogConfig?) {
        this.dialogConfig = dialogConfig
    }

    fun getErrorDialogConfig(): ErrorDialogConfig? {
        return this.dialogConfig
    }

    fun getUserRechargeStatus() {
        channel.invokeMethod("getUserRechargeStatus", null)
    }

    private var activityUrl: String? = ""

    fun updateUserRechargeStatus(arguments: Map<*, *>) {
        val type = arguments["type"]
        activityUrl = arguments["jumpUrl"] as String?
        if (type is String) {
            EventBus.getDefault().post(UserRechargeStatusEvent(type))
        }
    }

    // 向 flutter 端发送埋点需求
    fun processEvent(processStr: String) {
        val paramsMap = hashMapOf("processStr" to processStr)
        if (ThreadUtils.isMainThread()) {
            channel.invokeMethod("processEvent", paramsMap)
        } else {
            activity.runOnUiThread {
                channel.invokeMethod("processEvent", paramsMap)
            }
        }
    }

    fun updateGameTime() {
        val buyout = gameParam?.buyout ?: 0L
        if (buyout > TimeUtils.currentTime()) {
            // 游戏买断，且买断未到期
            return
        }
        val peakChannel = gameParam?.isPeakChannel ?: false
        val isPeakTime = TimeUtils.isPeakTime()
        if (!isPeakTime && !peakChannel) {
            // 非高峰通道，且非高峰时段
            return
        }
        // 未买断，用了高峰通道或当前处于高峰时段，检查可用时长，为0提示下线
        UserRepository.getUserTimeInfo(object : BaseObserver<HttpResponse<AccountTimeInfo>>() {
            override fun onNext(response: HttpResponse<AccountTimeInfo>) {
                super.onNext(response)
                response.data?.let {
                    val totalTime = it.totalTime
                    // 无时长
                    if (totalTime <= 0) {
                        if (isAnTong()) {
                            EventBus.getDefault().post(GameErrorEvent("2111114", ""))
                        } else {
                            EventBus.getDefault().post(GameErrorEvent("42", ""))
                        }
                    }
                }
            }
        })
    }

    fun onHttpError(code: Int?, url: String, errorType: String?) {
        if (code == -1) {
            return
        }
        activity.runOnUiThread {
            channel.invokeMethod(
                "http_error", mapOf(
                    "errorCode" to code,
                    "requestUrl" to url,
                    "errorType" to errorType,
                )
            )
        }
    }

    /**
     * 向 flutter 端发送 pinCode 事件
     */
    fun invokePinCodeResult(pinCode: String, cid: String) {
        val map = hashMapOf<String, String>()
        map["pinCode"] = pinCode
        map["cid"] = cid
        activity.runOnUiThread {
            channel.invokeMethod("pinCodeResult", map)
        }
    }

    fun invokeControlDistribute(controlInfos: String) {
        activity.runOnUiThread {
            channel.invokeMethod("controlDistribute", controlInfos)
        }
    }

    fun invokeControlQuery(controlInfos: String) {
        activity.runOnUiThread {
            channel.invokeMethod("controlInfos", controlInfos)
        }
    }

    fun anTongFirstFrameArrival() {
        if (!isVideoShowed) {
            isVideoShowed = true
            val playToken = AnTongManager.getInstance().playToken ?: ""
            channel.invokeMethod(
                GameViewConstants.firstFrameArrival, mapOf(
                    Pair("cid", playToken)
                )
            )
            isPlaying = true
            openGame = true
            AtGameActivity.startActivityForResult(activity)
        }
    }
}