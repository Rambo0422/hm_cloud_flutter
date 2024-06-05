package com.sayx.hm_cloud

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import com.google.gson.Gson
import com.haima.hmcp.Constants
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.CheckCloudServiceResult
import com.haima.hmcp.beans.Control
import com.haima.hmcp.beans.ControlInfo
import com.haima.hmcp.beans.HMInputOpData
import com.haima.hmcp.beans.PlayNotification
import com.haima.hmcp.beans.UserInfo
import com.haima.hmcp.beans.UserInfo2
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.enums.ScreenOrientation
import com.haima.hmcp.enums.TouchMode
import com.haima.hmcp.listeners.HmcpPlayerListener
import com.haima.hmcp.listeners.OnContronListener
import com.haima.hmcp.listeners.OnGameIsAliveListener
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.haima.hmcp.listeners.OnLivingListener
import com.haima.hmcp.listeners.OnSaveGameCallBackListener
import com.haima.hmcp.utils.StatusCallbackUtil
import com.haima.hmcp.widgets.HmcpVideoView
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.utils.GameUtils
import com.sayx.hm_cloud.utils.LogUtils
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import org.json.JSONObject

class GameView(private val context: Context, viewId: Int, messenger: BinaryMessenger, args: Any?) :
    PlatformView, MethodChannel.MethodCallHandler,
    HmcpPlayerListener {

    private val channel: MethodChannel =
        MethodChannel(messenger, GameViewConstants.methodChannelName)

    private var layout: FrameLayout? = null

    private var gameView: HmcpVideoView? = null

    private var gameParam: GameParam? = null

    private var cloudId = ""

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val runnable: Runnable by lazy {
        Runnable {
            val latencyInfo = gameView?.clockDiffVideoLatencyInfo
            val delay = latencyInfo?.netDelay ?: 450
            val netDelay = if (delay > 450) 450 else delay
            val lostRate = latencyInfo?.packetsLostRate?.toDouble() ?: 0.0
            channel.invokeMethod(
                "delayInfo",
                mapOf(
                    Pair("pingpongCostTime", netDelay),
                    Pair("packetLostPercent", lostRate)
                )
            )
            handler.postDelayed(runnable, 1000L)
            LogUtils.logV("delayInfo:netDelay=${netDelay}, lostRate=${lostRate}");
        }
    }

    init {
        channel.setMethodCallHandler(this)
        LogUtils.logD("init->context:$context, viewId=$viewId, argType=${args?.javaClass?.simpleName}, args=$args")
    }

    override fun getView(): View {
        return initGameView()
    }

    private fun initGameView(): View {
        if (layout == null) {
            layout = FrameLayout(context)
            layout?.setBackgroundColor(Color.parseColor("#FF111111"))
        }
        return layout!!
    }

    override fun onMethodCall(call: MethodCall, callback: MethodChannel.Result) {
        val arguments = call.arguments
//        Log.e("CloudGame", "onMethodCall:${call.method}, param:$arguments")
        LogUtils.logD("onMethodCall:${call.method}, param:$arguments")
        when (call.method) {
            GameViewConstants.startCloudGame -> {
                if (arguments is Map<*, *>) {
                    val gson = Gson()
                    gameParam = gson.fromJson(gson.toJson(arguments), GameParam::class.java)
                }
//                Log.e("CloudGame", "gameParam:$gameParam")
                LogUtils.logD("gameParam:$gameParam")
                if (gameView != null) {
                    // 已初始化过
                    prepareGame()
                } else {
                    // 未初始化过
                    initHaiMaSdk()
                }
            }

            GameViewConstants.stopGame -> {
                dispose()
            }

            GameViewConstants.sendCustomKey -> {
                sendCustomKey(arguments)
            }

            GameViewConstants.showInput -> {
                gameView?.switchKeyboard(true)
            }

            GameViewConstants.setMouseMode -> {
                if (arguments is Map<*, *>) {
                    updateMouseMode(arguments["mode"] as Int)
                }
            }

            GameViewConstants.switchInteraction -> {
                if (arguments is Map<*, *>) {
                    updateInteraction(arguments["interaction"] as Boolean)
                }
            }

            GameViewConstants.setMute -> {
                if (arguments is Map<*, *>) {
                    gameView?.setAudioMute(arguments["mute"] as Boolean)
                }
            }

            GameViewConstants.setQuality -> {
                if (arguments is Map<*, *>) {
                    if (arguments["quality"] as Int == 1) {
                        // 标清
                        val resolution = gameView?.resolutionList?.last()
                        LogUtils.logV("标清:$resolution")
                        gameView?.onSwitchResolution(0, resolution, 0)
                    } else {
                        // 蓝光
                        val resolution = gameView?.resolutionList?.first()
                        LogUtils.logV("蓝光:$resolution");
                        gameView?.onSwitchResolution(0, resolution, 0)
                    }
                }
            }

            GameViewConstants.setMouseSensitivity -> {
                if (arguments is Map<*, *>) {
                    gameView?.mouseSensitivity =
                        ((arguments["sensitivity"] as Double) * 2).toFloat()
                }
            }

            GameViewConstants.getPinCode -> {
                getPinCode()
            }

            GameViewConstants.queryControlUsers -> {
                queryControlUsers()
            }

            GameViewConstants.contronPlay -> {
                contronPlay()
            }

            else -> {
                callback.notImplemented()
            }
        }
    }

    private fun updateMouseMode(mode: Int) {
        when (mode) {
            0 -> {
                gameView?.setTouchMode(TouchMode.TOUCH_MODE_NONE)
            }

            1 -> {
                gameView?.setTouchMode(TouchMode.TOUCH_MODE_MOUSE)
            }

            2 -> {
                gameView?.setTouchMode(TouchMode.TOUCH_MODE_SCREEN)
            }

            3 -> {
                gameView?.setTouchMode(TouchMode.TOUCH_MODE_SCREEN_SLIDE)
            }
        }
    }

    private fun updateInteraction(arguments: Boolean) {
        cloudId = HmcpManager.getInstance().cloudId
        if (arguments) {
            // 开启直播
            val liveUrl = "rtmp://push-cg.3ayx.net/live/$cloudId"
            gameView?.startLiving(cloudId, liveUrl, object : OnLivingListener {
                override fun start(success: Boolean, msg: String?) {
                    LogUtils.logD("startLiving:$success, $msg, url:$liveUrl")
                }

                override fun stop(success: Boolean, msg: String?) {
                    LogUtils.logD("startLiving:$success, $msg")
                }
            })
        } else {
            // 停止直播
            gameView?.stopLiving(cloudId, object : OnLivingListener {
                override fun start(success: Boolean, msg: String?) {
                    LogUtils.logD("stopLiving:$success, $msg")
                }

                override fun stop(success: Boolean, msg: String?) {
                    LogUtils.logD("stopLiving:$success, $msg")
                }
            })
        }
    }

    /**
     * 获取授权码
     */
    private fun getPinCode() {
        LogUtils.logD("getPinCode gameView: $gameView")
        gameView?.getPinCode(object : OnContronListener {
            override fun pinCodeResult(
                success: Boolean,
                cid: String,
                pinCode: String,
                msg: String
            ) {
                LogUtils.logD("getPinCode pinCodeResult success :$success, cid: $cid pinCode: $pinCode msg: $msg")
                val METHOD_PIN_CODE = "pinCodeResult"
                val mutableMapOf = mutableMapOf<String, Any>()
                mutableMapOf["success"] = success
                if (success) {
                    mutableMapOf["cid"] = cid
                    mutableMapOf["pin_code"] = pinCode
                    mutableMapOf["msg"] = msg
                }
                channel.invokeMethod(METHOD_PIN_CODE, mutableMapOf)
            }

            /**
             * @param success 获取控制权成功
             */
            override fun contronResult(success: Boolean, msg: String) {
                LogUtils.logD("getPinCode contronResult success :$success,  msg: $msg")

                val METHOD_CONTRON_RESULT = "contronResult"
                val mutableMapOf = mutableMapOf<String, Any>()
                mutableMapOf["success"] = success
                mutableMapOf["msg"] = msg
                channel.invokeMethod(METHOD_CONTRON_RESULT, mutableMapOf)
            }

            /**
             * 失去控制权
             */
            override fun contronLost() {
                LogUtils.logD("getPinCode contronLost")
                val METHOD_CONTRON_LOST = "contronLost"
                channel.invokeMethod(METHOD_CONTRON_LOST, null)
            }

            /**
             * 操作权限分配结果(仅x86使⽤)
             */
            override fun controlDistribute(
                success: Boolean,
                controlInfo: MutableList<ControlInfo>,
                msg: String
            ) {
                LogUtils.logD("getPinCode controlDistribute")
                val METHOD_CONTROL_DISTRIBUTE = "controlDistribute"
                channel.invokeMethod(METHOD_CONTROL_DISTRIBUTE, null)
            }

            /**
             * //此API触发qurey回调
             * //查询操作权回调(仅x86使⽤)
             */
            override fun controlQuery(
                success: Boolean,
                controlInfo: MutableList<ControlInfo>,
                msg: String
            ) {
                LogUtils.logD("getPinCode controlQuery")
            }
        })
    }

    /**
     * 查询当前房间内的⽤户
     */
    private fun queryControlUsers() {
        LogUtils.logD("queryControlUsers")
        gameView?.queryControlPermitUsers(object : OnContronListener {
            /**
             * @param success 获取授权码的状态
             */
            override fun pinCodeResult(
                success: Boolean,
                cid: String,
                pinCode: String,
                msg: String
            ) {
                LogUtils.logD("queryControlUsers pinCodeResult")
            }

            /**
             * @param success 获取控制权的状态
             */
            override fun contronResult(success: Boolean, msg: String) {
                LogUtils.logD("queryControlUsers contronResult")
            }

            /**
             * 失去控制权(arm需主动结束当前播放并且在Activity#onDestroy⽅法中不调⽤
             * HmcpVideoView#onDestroy⽅法,x86⽆需结束)
             */
            override fun contronLost() {
                LogUtils.logD("queryControlUsers contronLost")
            }

            /**
             * 操作权限分配结果(仅x86使⽤)
             */
            override fun controlDistribute(
                success: Boolean,
                controlInfo: MutableList<ControlInfo>?,
                msg: String
            ) {
                LogUtils.logD("queryControlUsers controlDistribute")
            }

            /**
             * 此API触发qurey回调
             * 查询操作权回调(仅x86使⽤)
             */
            override fun controlQuery(
                success: Boolean,
                controlInfos: MutableList<ControlInfo>,
                msg: String
            ) {
                LogUtils.logD("queryControlUsers controlQuery success: $success")

                val METHOD_CONTROL_QUERY = "controlQuery"
                val mutableMapOf = mutableMapOf<String, Any>()
                mutableMapOf["success"] = success
                if (success) {
                    val gson = Gson()
                    val jsonList = gson.toJsonTree(controlInfos).asJsonArray.toString()
                    LogUtils.logD("queryControlUsers controlQuery jsonList: $jsonList")

                    mutableMapOf["control_info_list"] = jsonList
                }
                mutableMapOf["msg"] = msg

                channel.invokeMethod(METHOD_CONTROL_QUERY, mutableMapOf)
            }
        })
    }

    private fun contronPlay() {
        // 选择流类型。 0：表示RTMP  1：表示WEBRTC
        val streamType = 0

        // 获取控制权参数对象
        val control = Control()
        control.cid = ""
        control.pinCode = ""
        control.accessKeyID = ""
        control.isIPV6 = false
        control.orientation = ScreenOrientation.LANDSCAPE

        gameView?.contronPlay(streamType, control, object : OnContronListener {
            override fun pinCodeResult(
                success: Boolean,
                cid: String,
                pinCode: String,
                msg: String
            ) {
                LogUtils.logD("contronPlay pinCodeResult success: $success cid: $cid pinCode: $pinCode msg: $msg")
            }

            override fun contronResult(success: Boolean, msg: String) {
                LogUtils.logD("contronPlay contronResult success: $success  msg: $msg")
            }

            // 失去控制权
            override fun contronLost() {
                LogUtils.logD("contronPlay contronLost")
            }

            /**
             * 操作权限分配结果(仅x86使⽤)
             */
            override fun controlDistribute(
                success: Boolean,
                controlInfo: MutableList<ControlInfo>,
                msg: String
            ) {
                LogUtils.logD("controlDistribute success msg: $msg")
                for (controlInfo1 in controlInfo) {
                    LogUtils.logD("controlDistribute controlInfo1: $controlInfo1")
                }
            }

            override fun controlQuery(p0: Boolean, p1: MutableList<ControlInfo>?, p2: String?) {
            }
        })
    }

    private fun distributeControlPermit(controlInfos: List<ControlInfo>) {
        gameView?.distributeControlPermit(controlInfos, object : OnContronListener {
            /**
             * @param success 获取授权码状态
             */
            override fun pinCodeResult(
                success: Boolean,
                cid: String,
                pinCode: String,
                msg: String
            ) {

            }

            /**
             *  @suppress 获取控制权状态
             */
            override fun contronResult(success: Boolean, msg: String) {
            }

            /**
             * 失去控制权(arm需主动结束当前播放并且在Activity#onDestroy⽅法中不调⽤
             * HmcpVideoView#onDestroy⽅法,x86⽆需结束)
             */
            override fun contronLost() {
            }

            override fun controlDistribute(
                success: Boolean,
                controlInfo: MutableList<ControlInfo>,
                msg: String
            ) {
            }

            /**
             * 查询操作权回调(仅x86触发)
             */
            override fun controlQuery(
                success: Boolean,
                controlInfo: MutableList<ControlInfo>,
                msg: String
            ) {
            }
        })

    }

    /// 初始化sdk
    private fun initHaiMaSdk() {
        try {
//            Log.e("CloudGame", "init haiMaSDK:$gameParam")
            LogUtils.logD("init haiMaSDK:$gameParam")
            gameParam?.channel = gameParam?.channel ?: "android"
            val config: Bundle = Bundle().also {
                it.putString(HmcpManager.ACCESS_KEY_ID, gameParam?.accessKeyId)
                it.putString(HmcpManager.CHANNEL_ID, gameParam?.channel)
            }
//                Constants.IS_DEBUG = BuildConfig.DEBUG
//                Constants.IS_ERROR = BuildConfig.DEBUG
//                Constants.IS_INFO = BuildConfig.DEBUG
//            Log.e("CloudGame", "init haiMaSDK:${gameParam?.accessKeyId}")
            LogUtils.logD("init haiMaSDK:${gameParam?.accessKeyId}")
            HmcpManager.getInstance().init(config, context, object : OnInitCallBackListener {
                override fun success() {
                    LogUtils.logD("haiMaSDK success:${HmcpManager.getInstance().sdkVersion}")
                    checkPlayingGame()
                }

                override fun fail(msg: String?) {
                    LogUtils.logE("haiMaSDK fail:$msg")
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
            LogUtils.logE("haiMaSDK fail:${e.message}")
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
     * 检查是否有未释放的游戏实例
     */
    private fun checkPlayingGame() {
        LogUtils.logD("checkPlayingGame->userId:${this.gameParam?.userId}, userToken:${this.gameParam?.userToken}")
        HmcpManager.getInstance().checkPlayingGame(UserInfo().also {
            it.userId = this.gameParam?.userId
            it.userToken = this.gameParam?.userToken
        }, object : OnGameIsAliveListener {
            override fun success(list: MutableList<CheckCloudServiceResult.ChannelInfo>?) {
                LogUtils.logD("checkPlayingGame:$list")
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
                LogUtils.logD("checkPlayingGameFail->Msg:$msg")
                prepareGame()
            }
        })
    }

    /**
     * 准备进入游戏队列
     */
    private fun prepareGame(cid: String? = null) {
        LogUtils.logD("prepareGame:${cid}, priority:${gameParam?.priority}")
        try {
            val bundle = Bundle().also {
                // 横屏
                it.putSerializable(HmcpVideoView.ORIENTATION, ScreenOrientation.LANDSCAPE)
                // 可玩时间
                it.putInt(
                    HmcpVideoView.PLAY_TIME,
                    if ((gameParam?.playTime
                            ?: 0) > Int.MAX_VALUE
                    ) Int.MAX_VALUE else (gameParam?.playTime?.toInt() ?: 0)
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
                        Gson(),
                        gameParam?.userId,
                        gameParam?.gameId,
                        gameParam?.priority ?: 0
                    )
                )
//                }
                // 码率
//                it.putInt(HmcpVideoView.INTERNET_SPEED, 300)
                // 清晰度挡位，会员默认超清，非会员默认流畅
                if (gameParam?.isVip == false) {
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
            LogUtils.logD("prepareGame->bundle:$bundle")
            playGame(bundle)
        } catch (e: Exception) {
            LogUtils.logE("game error:${e.message}", throwable = Throwable())
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

    private fun playGame(bundle: Bundle) {
        LogUtils.logD("playGame:$gameView")
        if (gameView != null) {
            val frameLayout = view as FrameLayout
            frameLayout.removeView(gameView)
            gameView?.onDestroy()
            gameView = HmcpVideoView(context)
            frameLayout.addView(
                gameView,
                0,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
        } else {
            val frameLayout = view as FrameLayout
            gameView = HmcpVideoView(context)
            frameLayout.addView(
                gameView,
                0,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
        }
        gameView?.setUserInfo(UserInfo().also {
            it.userId = gameParam?.userId
            it.userToken = gameParam?.userToken
        })
        // 上号助手
//        gameParam?.accountInfo?.let { accountInfo ->
//            val result = Gson().fromJson(accountInfo, AccountInfo::class.java)
//            gameView?.setExtraData(IntentExtraData().also {
//                it.setStringExtra(GameUtils.getStringData(result))
//            })
//        }
        gameView?.hmcpPlayerListener = this
        gameView?.play(bundle)
    }

    override fun HmcpPlayerStatusCallback(statusData: String?) {
        LogUtils.logD("playerStatusCallback:$statusData, cid:${HmcpManager.getInstance().cloudId}")
        statusData?.let {
            val data = JSONObject(it)
            val status = data.getInt(StatusCallbackUtil.STATUS)
            when (status) {
                Constants.STATUS_PLAY_INTERNAL -> {
                    gameView?.play()
                }

                Constants.STATUS_STOP_PLAY -> {
//                    gameView?.startPlay()
                }

                Constants.STATUS_WAIT_CHOOSE -> {
                    gameView?.entryQueue()
                }

                Constants.STATUS_OPERATION_INTERVAL_TIME -> {
                    val dataStr = data.getString(StatusCallbackUtil.DATA)
                    cloudId = HmcpManager.getInstance().cloudId
                    if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                        val resultData = Gson().fromJson(dataStr, Map::class.java)
                        channel.invokeMethod(
                            "queueInfo",
                            mapOf(
                                Pair("queueTime", resultData["time"])
                            )
                        )
                    } else {
                        LogUtils.logE("queue info error:$dataStr");
                    }
                }

                Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                    gameView?.setAudioMute(gameParam?.mute ?: true)
                    updateInteraction(!(gameParam?.isVip ?: false))
                    channel.invokeMethod(GameViewConstants.firstFrameArrival, null)
                    handler.postDelayed(runnable, 1000L);
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
                    LogUtils.logD("errorInfo:$dataStr")
                    var errorCode = ""
                    if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                        val resultData = Gson().fromJson(dataStr, Map::class.java)
                        var errorCodeWithoutCid = ""
                        try {
                            errorCodeWithoutCid =
                                if (resultData["errorCodeWithoutCid"].toString() == "null") "$status" else resultData["errorCodeWithoutCid"].toString()
                        } catch (e: Exception) {
                            LogUtils.logE("${e.message}")
                        }
                        errorCode =
                            if (TextUtils.isEmpty(errorCodeWithoutCid)) "$status" else errorCodeWithoutCid
                    }
                    channel.invokeMethod(
                        "errorInfo", mapOf(
                            Pair("errorCode", errorCode),
                            Pair("cid", cloudId),
                        )
                    )
                }

                else -> {}
            }
        }
    }

    private fun sendCustomKey(arguments: Any?) {
        LogUtils.logD("sendCustomKey:$arguments")
        if (arguments is Map<*, *>) {
            val inputOp = HMInputOpData()
            val oneInputOpData = HMInputOpData.HMOneInputOPData()
            val inputOpValue = arguments["inputOp"]
            if (inputOpValue is Int) {
                oneInputOpData.inputOp = getInputOp(inputOpValue)
            }
            val inputOpValueData = arguments["value"]
            if (inputOpValueData is Int) {
                oneInputOpData.value = inputOpValueData
            }
            val inputOpState = arguments["inputState"]
            if (inputOpState is Int) {
                oneInputOpData.inputState = getInputOpState(inputOpState)
            }
            inputOp.opListArray.add(oneInputOpData)
            val result = gameView?.sendCustomKeycode(inputOp)
            LogUtils.logD("sendCustomKey:$result, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, inputState:${oneInputOpData.inputState}")
        }
    }

    private fun getInputOpState(state: Int): HMInputOpData.HMOneInputOPData_InputState? {
        return HMInputOpData.HMOneInputOPData_InputState.entries.findLast { inputState -> inputState.sate == state }
    }

    private fun getInputOp(value: Int): HMInputOpData.HMOneInputOPData_InputOP? {
        return HMInputOpData.HMOneInputOPData_InputOP.entries.findLast { inputOp -> inputOp.value == value }
    }

    override fun onCloudDeviceStatus(status: String?) {
        LogUtils.logD("onCloudDeviceStatus:$status")
    }

    override fun onInterceptIntent(intentData: String?) {
        LogUtils.logD("onInterceptIntent:$intentData")
    }

    override fun onCloudPlayerKeyboardStatusChanged(keyboardStatus: CloudPlayerKeyboardStatus?) {
        LogUtils.logD("onCloudPlayerKeyboardStatusChanged:${keyboardStatus?.name}")
    }

    override fun onError(errorType: ErrorType?, errorMsg: String?) {
        LogUtils.logE("onError-> errorType:$errorType, errorMsg:$errorMsg")
    }

    override fun onSuccess() {
        LogUtils.logD("onSuccess")
    }

    override fun onExitQueue() {
        LogUtils.logD("onExitQueue")
    }

    override fun onMessage(msg: String?) {
        LogUtils.logD("onMessage:$msg")
    }

    override fun onSceneChanged(sceneMessage: String?) {
        LogUtils.logD("onSceneChanged:$sceneMessage")
    }

    override fun onNetworkChanged(networkState: NetWorkState?) {
        LogUtils.logD("onNetworkChanged:$networkState")
    }

    override fun onPlayStatus(status: Int, value: Long, data: String?) {
        LogUtils.logD("onPlayStatus->status:$status, value:$value, data:$data")
    }

    override fun onPlayerError(errorCode: String?, errorMsg: String?) {
        LogUtils.logE("onPlayerError->errorCode:$errorCode, errorMsg:$errorMsg")
    }

    override fun onInputMessage(msg: String?) {
        LogUtils.logD("onInputMessage:$msg")
    }

    override fun onInputDevice(device: Int, operationType: Int) {
        LogUtils.logD("onInputDevice-> device:$device, operationType:$operationType")
    }

    override fun onPermissionNotGranted(msg: String?) {
        LogUtils.logE("onPermissionNotGranted:$msg")
    }

    override fun onMiscResponse(msg: String?) {
        LogUtils.logD("onInputMessage:$msg")
    }

    override fun onAccProxyConnectStateChange(connectState: Int) {
        super.onAccProxyConnectStateChange(connectState)
        LogUtils.logD("onAccProxyConnectStateChange:$connectState")
    }

    override fun onPlayNotification(playNotification: PlayNotification?) {
        super.onPlayNotification(playNotification)
        LogUtils.logD("onPlayNotification")
    }

    override fun onSwitchConnectionCallback(statusCode: Int, networkType: Int) {
        super.onSwitchConnectionCallback(statusCode, networkType)
        LogUtils.logD("onSwitchConnectionCallback:$statusCode, $networkType")
    }

    override fun dispose() {
        LogUtils.logD("dispose gameView")
        releaseGame()
        handler.removeCallbacksAndMessages(null)
    }

    private fun releaseGame() {
        gameView?.onStop()
        val cloudId = HmcpManager.getInstance().cloudId
        if (TextUtils.isEmpty(cloudId)) {
            LogUtils.logE("undo releaseGame, cid is empty")
            return
        }
        val cToken = gameParam?.cToken
        HmcpManager.getInstance().setReleaseCid(
            gameParam?.gamePkName, cloudId, cToken, gameParam?.channelName,
            UserInfo2().also {
                it.userId = gameParam?.userId
                it.userToken = gameParam?.userToken
            },
            object : OnSaveGameCallBackListener {
                override fun success(result: Boolean) {
                    gameView?.release()
                    LogUtils.logD("releaseGame:$result, cid:$cloudId")
                }

                override fun fail(error: String?) {
                    gameView?.release()
                    LogUtils.logD("releaseGame:$error")
                }
            }
        )
    }
}