package com.sayx.hm_cloud

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import com.antong.keyboard.sa.constants.HMInputOpData
import com.blankj.utilcode.util.LogUtils
import com.media.atkit.AnTongManager
import com.media.atkit.Constants
import com.media.atkit.beans.ChannelInfo
import com.media.atkit.beans.IntentExtraData
import com.media.atkit.beans.UserInfo
import com.media.atkit.listeners.AnTongPlayerListener
import com.media.atkit.listeners.OnGameIsAliveListener
import com.media.atkit.utils.StatusCallbackUtil
import com.media.atkit.widgets.AnTongVideoView
import com.sayx.hm_cloud.callback.OnKeyEventListener
import com.sayx.hm_cloud.callback.OnRockerOperationListener
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
import com.sayx.hm_cloud.constants.GameConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.calStickValue
import com.sayx.hm_cloud.constants.resetDirectionMap
import com.sayx.hm_cloud.constants.stickKeyMaps
import com.sayx.hm_cloud.model.AccountInfo
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.ArchiveInfo
import com.sayx.hm_cloud.model.Direction
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.utils.GameUtils
import org.json.JSONObject

object AnTongSDK {

    const val TYPE = "at_pc"
    const val CHANNEL_TYPE = "at"
    var anTongVideoView: AnTongVideoView? = null
    private var mRequestDeviceSuccess: RequestDeviceSuccess? = null
    private var ACCESS_KEY_ID = ""
    private var isInit = false
    private const val APP_CHANNEL = "szlk"

    fun initSdk(context: Context, gameParam: GameParam) {
        if (!isInit) {
            isInit = true
            Constants.IS_DEBUG = false
            Constants.IS_ERROR = false
            Constants.IS_INFO = false
            ACCESS_KEY_ID = gameParam.accessKeyId
            val channelName = gameParam.channelName
            AnTongManager.getInstance().init(context, channelName, ACCESS_KEY_ID)
        }
    }


    fun play(
        context: Context,
        gameParam: GameParam,
        archiveData: ArchiveData?,
        requestDeviceSuccess: RequestDeviceSuccess
    ) {
        this.mRequestDeviceSuccess = requestDeviceSuccess

        if (anTongVideoView == null) {
            anTongVideoView = AnTongVideoView(context)
        }
        anTongVideoView?.setHmcpPlayerListener(mAnTongPlayerListener)

        val userInfo = UserInfo()
        userInfo.userId = gameParam.userId
        userInfo.userToken = gameParam.userToken
        userInfo.flag = gameParam.priority
        anTongVideoView?.setUserInfo(userInfo)

        val bundle = Bundle()
        bundle.putInt(AnTongVideoView.PLAY_TIME, 99999)
        bundle.putInt(AnTongVideoView.VIEW_RESOLUTION_WIDTH, 1920)
        bundle.putInt(AnTongVideoView.VIEW_RESOLUTION_HEIGHT, 1080)
        bundle.putBoolean(AnTongVideoView.IS_ARCHIVE, true)

        bundle.putString(
            AnTongVideoView.PROTO_DATA,
            GameUtils.getProtoData(GameManager.gson, gameParam.userId, gameParam.gameId, gameParam.priority)
        )
        bundle.putBoolean(AnTongVideoView.AUTO_PLAY_AUDIO, true)
        if (gameParam.isVip()) {
            bundle.putInt(AnTongVideoView.RESOLUTION_ID, 1)
        } else {
            bundle.putInt(AnTongVideoView.RESOLUTION_ID, 4)
        }
        bundle.putString(AnTongVideoView.EXTRA_ID, "")
        bundle.putString(AnTongVideoView.PIN_CODE, "")
        bundle.putString(AnTongVideoView.PLAY_TOKEN, "")
        bundle.putString(AnTongVideoView.APP_CHANNEL, APP_CHANNEL)
        bundle.putBoolean(AnTongVideoView.IS_PORTRAIT, false)
        bundle.putString(AnTongVideoView.BUSINESS_GAME_ID, gameParam.gameId)
        bundle.putString(AnTongVideoView.SIGN, gameParam.cToken)
        bundle.putInt(AnTongVideoView.NO_INPUT_TIMEOUT, 10 * 60)
        gameParam.accountInfo?.let { accountInfo ->
//            LogUtils.d("AccountInfo 1:${accountInfo.javaClass}")
            val result = GameManager.gson.fromJson(GameManager.gson.toJson(accountInfo), AccountInfo::class.java)
//            LogUtils.d("AccountInfo 2:${result.json}")
            anTongVideoView?.setExtraData(IntentExtraData().also {
                it.setStringExtra(GameUtils.getStringData(result))
            })
        }
        if (archiveData?.custodian == "3a") {
            val archiveInfo = archiveData.list?.firstOrNull()
            val richDataBundle = richDataBundle(gameParam.gameId, archiveInfo)
            // 添加 richData，主要是附带的存档数据
            bundle.putBundle(AnTongVideoView.RICH_DATA, richDataBundle)
        }

        bundle.putString(AnTongVideoView.PKG_NAME, gameParam.gamePkName)
        anTongVideoView?.play(bundle)
    }

    private fun richDataBundle(gameId: String, archiveData: ArchiveInfo?): Bundle {
        val richDataBundle = Bundle()
        val specificArchiveBundle = Bundle()
        specificArchiveBundle.putString("gameId", gameId)
        specificArchiveBundle.putBoolean("uploadArchive", true)
        specificArchiveBundle.putBoolean("thirdParty", archiveData != null)

        if (archiveData != null) {
            kotlin.runCatching {
                archiveData.cid.toInt()
            }.onSuccess { cid ->
                specificArchiveBundle.putInt("cid", cid)
            }.onFailure {
                specificArchiveBundle.putInt("cid", 0)
            }
            specificArchiveBundle.putString("downloadUrl", archiveData.downLoadUrl)
            specificArchiveBundle.putString("md5", archiveData.fileMD5)
            specificArchiveBundle.putString("format", archiveData.format)
            richDataBundle.putBundle("specificArchive", specificArchiveBundle)
        }
        return richDataBundle
    }

    fun stopGame() {
        anTongVideoView?.stopGame()
    }

    fun leaveQueue() {
        val leaveQueue = anTongVideoView?.leaveQueue() ?: true
        if (leaveQueue) {
            onDestroy()
        }
    }

    fun onDestroy() {
        anTongVideoView?.onDestroy()
        val parentViewGroup = anTongVideoView?.parent as? ViewGroup
        if (parentViewGroup != null && anTongVideoView != null) {
            parentViewGroup.removeView(anTongVideoView)
        }
        anTongVideoView = null
    }

    fun checkPlayingGame(userId: String) {
        AnTongManager.getInstance().checkPlayingGame(userId, object : OnGameIsAliveListener {
            override fun success(channelInfo: ChannelInfo) {

            }

            override fun fail(msg: String?) {
            }
        })
    }

    private val mAnTongPlayerListener = object : AnTongPlayerListener {
        override fun antongPlayerStatusCallback(callback: String?) {
            callback?.let {
                val jsonObject = JSONObject(it)
                val status = jsonObject.getInt(StatusCallbackUtil.STATUS)
                when (status) {
                    Constants.STATUS_FIRST_FRAME_ARRIVAL -> {
                        anTongVideoView?.setHmcpPlayerListener(null)
                        // 跳转远程页面
                        mRequestDeviceSuccess?.onRequestDeviceSuccess()
                    }
                    Constants.STATUS_OPERATION_INTERVAL_TIME -> {
                        val dataStr = jsonObject.getString(StatusCallbackUtil.DATA)
                        if (dataStr is String && !TextUtils.isEmpty(dataStr)) {
                            val resultData = GameManager.gson.fromJson(dataStr, Map::class.java)
                            mRequestDeviceSuccess?.onQueueTime((resultData["avg_time"] as Number?)?.toInt() ?: 300)
                        } else {
                            LogUtils.e("queue info error:$dataStr")
                        }
                    }
                    Constants.STATUS_APP_ID_ERROR,
                    Constants.STATUS_NOT_FOND_GAME,
                    Constants.STATUS_SIGN_FAILED,
                    Constants.STATUS_STOP_PLAY,
                    Constants.STATUS_CONN_FAILED -> {
                        uploadErrorCode(status)
                        val errorMessage =
                            jsonObject.optString(StatusCallbackUtil.DATA, "服务器异常")
                        mRequestDeviceSuccess?.onRequestDeviceFailed(status, errorMessage)
                    }

                    else -> {
                        uploadErrorCode(status)
                    }
                }
            } ?: return
        }
    }

    fun uploadErrorCode(errorCode: Int) {
        GameManager.gameEsStat(
            "game_error",
            "安通报错码",
            "show",
            mapOf("errorCode" to "$errorCode").toString(),
        )
        GameManager.gameStat("安通报错码", "show", mapOf(
            "errorcode_at" to "$errorCode"
        ))
    }
}

open class OnRockerOperationListenerImp : OnRockerOperationListener {
    override fun onRockerMove(keyInfo: KeyInfo, moveX: Float, moveY: Float) {
        val pointX: Int = (moveX * GameConstants.rockerOffsetMul).toInt()
        val pointY: Int = (moveY * GameConstants.rockerOffsetMul).toInt()
        val inputOp = HMInputOpData()
        if (keyInfo.type == KeyType.ROCKER_LEFT) {
            val oneInputOpData = HMInputOpData.HMOneInputOPData()
            oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbLx
            oneInputOpData.value = pointX
            inputOp.opListArray.add(oneInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
            val secondInputOpData = HMInputOpData.HMOneInputOPData()
            secondInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbLy
            secondInputOpData.value = pointY
            inputOp.opListArray.add(secondInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
        } else if (keyInfo.type == KeyType.ROCKER_RIGHT) {
            val oneInputOpData = HMInputOpData.HMOneInputOPData()
            oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbRx
            oneInputOpData.value = pointX
            inputOp.opListArray.add(oneInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
            val secondInputOpData = HMInputOpData.HMOneInputOPData()
            secondInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbRy
            secondInputOpData.value = pointY
            inputOp.opListArray.add(secondInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
        }
        AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
    }

    override fun onRockerDirection(keyInfo: KeyInfo, direction: Direction?) {
        val inputOp = HMInputOpData()
        when (direction) {
            // 左
            Direction.DIRECTION_LEFT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 左 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        // 左 按
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerLeft)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 上
            Direction.DIRECTION_UP -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 上 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 上 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerUp)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 右
            Direction.DIRECTION_RIGHT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerRight)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 下
            Direction.DIRECTION_DOWN -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerDown)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 左上
            Direction.DIRECTION_UP_LEFT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 左 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 左 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerUpLeft)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 右上
            Direction.DIRECTION_UP_RIGHT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp = getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 左 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 左 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerUpRight)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 下左
            Direction.DIRECTION_DOWN_LEFT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerDownLeft)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 下右
            Direction.DIRECTION_DOWN_RIGHT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 右 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 左 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 右 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 左 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerDownRight)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 中
            Direction.DIRECTION_CENTER -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerCenter)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }

            else -> {}
        }
        AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
    }
}

open class OnKeyEventListenerImp : OnKeyEventListener {
    override fun onButtonPress(keyInfo: KeyInfo, press: Boolean) {
        when (keyInfo.type) {
            // RS/LS, X,Y,A,B,setting,menu
            KeyType.GAMEPAD_ELLIPTIC, KeyType.GAMEPAD_ROUND_MEDIUM, KeyType.GAMEPAD_ROUND_SMALL -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                stickKeyMaps[keyInfo.inputOp] = press
                oneInputOpData.value = calStickValue()
                inputOp.opListArray.add(oneInputOpData)
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // LT/RT, LB/RB
            KeyType.GAMEPAD_SQUARE -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                // LT/RT -> Trigger, LB/RB -> 1024
                oneInputOpData.inputOp =
                    if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                        keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                    )
                        getInputOp(keyInfo.inputOp) else
                        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                // LT/RT -> 255, LB/RB -> inputOp
                oneInputOpData.value =
                    if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                        keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                    ) {
                        if (press) GameConstants.gamepadButtonTValue else 0
                    } else {
                        stickKeyMaps[keyInfo.inputOp] = press
                        calStickValue()
                    }

                inputOp.opListArray.add(oneInputOpData)
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // 键盘按键，鼠标左中右键
            KeyType.KEYBOARD_KEY, KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputState = if (press) HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown else
                    HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                oneInputOpData.inputOp = getInputOp(keyInfo.inputOp)
                inputOp.opListArray.add(oneInputOpData)
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.inputState}")
            }
            // 鼠标滑轮向上短触发
            KeyType.KEYBOARD_MOUSE_UP -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputOp = getInputOp(keyInfo.inputOp)
                oneInputOpData.value = if (press) GameConstants.mouseUp else GameConstants.mouseDefault
                inputOp.opListArray.add(oneInputOpData)
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // 鼠标滑轮向下
            KeyType.KEYBOARD_MOUSE_DOWN -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputOp = getInputOp(keyInfo.inputOp)
                oneInputOpData.value = if (press) GameConstants.mouseDown else GameConstants.mouseDefault
                inputOp.opListArray.add(oneInputOpData)
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // 组合键(键鼠)
            KeyType.KEY_COMBINE -> {
                val inputOp = HMInputOpData()
                val text = StringBuilder()
                keyInfo.composeArr?.let {
                    it.forEachIndexed { index, keyInfo ->
                        val inputOpData = HMInputOpData.HMOneInputOPData()
                        inputOpData.inputState = if (press) HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown else
                            HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        inputOpData.inputOp = getInputOp(keyInfo.inputOp)
                        inputOp.opListArray.add(inputOpData)
                        text.append("$keyInfo")
                        if (index != it.size - 1) {
                            text.append(", ")
                        }
                    }
                }
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                LogUtils.d("key:${keyInfo.text.json}, inputOpList:[$text.json], result:$result")
            }
            // 组合键(手柄)
            KeyType.GAMEPAD_COMBINE -> {
                val inputOp = HMInputOpData()
                val text = StringBuilder()
                keyInfo.composeArr?.let {
                    it.forEachIndexed { index, keyInfo ->
                        val inputOpData = HMInputOpData.HMOneInputOPData()
                        if (keyInfo.type == KeyType.GAMEPAD_SQUARE) {
                            inputOpData.inputOp =
                                if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                                    keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                                )
                                    getInputOp(keyInfo.inputOp) else
                                    HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                            // LT/RT -> 255, LB/RB -> inputOp
                            inputOpData.value =
                                if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                                    keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                                ) {
                                    if (press) GameConstants.gamepadButtonTValue else 0
                                } else {
                                    stickKeyMaps[keyInfo.inputOp] = press
                                    calStickValue()
                                }
                        } else {
                            inputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                            stickKeyMaps[keyInfo.inputOp] = press
                            inputOpData.value = calStickValue()
                        }
                        inputOp.opListArray.add(inputOpData)
                        text.append("$keyInfo")
                        if (index != it.size - 1) {
                            text.append(", ")
                        }
                    }
                }
                AnTongSDK.anTongVideoView?.cmdToCloud(inputOp)
//                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
//                LogUtils.d("key:${keyInfo.text.json}, inputOpList:[$text.json], result:$result")
            }

            else -> {
                LogUtils.d("key:${keyInfo.type}, press:${press}")
            }
        }
    }
}

private fun getInputOp(value: Int): HMInputOpData.HMOneInputOPData_InputOP? {
    return HMInputOpData.HMOneInputOPData_InputOP.entries.findLast { inputOp -> inputOp.value == value }
}