package com.sayx.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import com.media.atkit.AnTongManager
import com.media.atkit.Constants
import com.media.atkit.beans.ChannelInfo
import com.media.atkit.beans.UserInfo
import com.media.atkit.listeners.AnTongPlayerListener
import com.media.atkit.listeners.OnGameIsAliveListener
import com.media.atkit.utils.StatusCallbackUtil
import com.media.atkit.widgets.AnTongVideoView
import com.sayx.hm_cloud.callback.RequestDeviceSuccess
import com.sayx.hm_cloud.model.ArchiveData
import com.sayx.hm_cloud.model.ArchiveInfo
import com.sayx.hm_cloud.model.GameParam
import org.json.JSONObject

object AnTongSDK {

    const val TYPE = "at_pc"
    var anTongVideoView: AnTongVideoView? = null
    private var mRequestDeviceSuccess: RequestDeviceSuccess? = null
    private var ACCESS_KEY_ID = ""
    private var isInit = false
    private const val APP_CHANNEL = "szlk"

    fun initSdk(context: Context, gameParam: GameParam) {
        if (!isInit) {
            isInit = true
            Constants.IS_DEBUG = BuildConfig.DEBUG
            Constants.IS_ERROR = BuildConfig.DEBUG
            Constants.IS_INFO = BuildConfig.DEBUG
            Constants.IS_TV = true
            ACCESS_KEY_ID = gameParam.accessKeyId
            val channelName = gameParam.channelName
            AnTongManager.getInstance().init(context, channelName, ACCESS_KEY_ID)
        }
    }


    fun play(
        context: Context,
        userId: String,
        userToken: String,
        gameId: String,
        anTongPackageName: String,
        sign: String,
        archiveData: ArchiveData,
        requestDeviceSuccess: RequestDeviceSuccess
    ) {
        this.mRequestDeviceSuccess = requestDeviceSuccess

        if (anTongVideoView == null) {
            anTongVideoView = AnTongVideoView(context)
        }
        anTongVideoView?.setHmcpPlayerListener(mAnTongPlayerListener)

        val flag = 48
        val userInfo = UserInfo()
        userInfo.userId = userId
        userInfo.userToken = userToken
        userInfo.flag = flag
        anTongVideoView?.setUserInfo(userInfo)

        val bundle = Bundle()
        bundle.putInt(AnTongVideoView.PLAY_TIME, 99999)
        bundle.putInt(AnTongVideoView.VIEW_RESOLUTION_WIDTH, 1920)
        bundle.putInt(AnTongVideoView.VIEW_RESOLUTION_HEIGHT, 1080)
        bundle.putBoolean(AnTongVideoView.IS_ARCHIVE, false)

        val protoData = getProtoData(userId, gameId)
        bundle.putString(AnTongVideoView.PROTO_DATA, protoData)
        bundle.putBoolean(AnTongVideoView.AUTO_PLAY_AUDIO, true)
        bundle.putInt(AnTongVideoView.RESOLUTION_ID, 1)
        bundle.putString(AnTongVideoView.EXTRA_ID, "")
        bundle.putString(AnTongVideoView.PIN_CODE, "")
        bundle.putString(AnTongVideoView.PLAY_TOKEN, "")
        bundle.putString(AnTongVideoView.APP_CHANNEL, APP_CHANNEL)
        bundle.putBoolean(AnTongVideoView.IS_PORTRAIT, false)
        bundle.putString(AnTongVideoView.BUSINESS_GAME_ID, gameId)
        bundle.putString(AnTongVideoView.SIGN, sign)
        bundle.putInt(AnTongVideoView.NO_INPUT_TIMEOUT, 5 * 60)
        bundle.putString(AnTongVideoView.PKG_NAME, anTongPackageName)

        if (archiveData.custodian == "3a") {
            val archiveInfo = archiveData.list?.firstOrNull()
            val richDataBundle = richDataBundle(gameId, archiveInfo)
            // 添加 richData，主要是附带的存档数据
            bundle.putBundle(AnTongVideoView.RICH_DATA, richDataBundle)
        }

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

    private fun getProtoData(userId: String, gameId: String): String {
        val protoJSONObject = JSONObject()
        protoJSONObject.put("uid", userId)
        protoJSONObject.put("gameId", gameId)
        protoJSONObject.put("type", 2)
        val toString = protoJSONObject.toString()
        val byteArray = toString.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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

                        // 首帧出现，修改码率
                        anTongVideoView?.onSwitchResolution(1)
                    }

                    Constants.STATUS_APP_ID_ERROR,
                    Constants.STATUS_NOT_FOND_GAME,
                    Constants.STATUS_SIGN_FAILED,
                    Constants.STATUS_201003 -> {
                        val errorMessage =
                            jsonObject.optString(StatusCallbackUtil.DATA, "服务器异常")
                        mRequestDeviceSuccess?.onRequestDeviceFailed(errorMessage)
                    }

                    else -> {}
                }
            } ?: return
        }

        override fun onPlayerError(errorCode: String?, errorInfo: String?) {
        }
    }
}