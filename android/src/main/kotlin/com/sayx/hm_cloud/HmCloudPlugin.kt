package com.sayx.hm_cloud

import android.app.Activity
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.reflect.TypeToken
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.ErrorDialogConfig
import com.sayx.hm_cloud.model.ExitGameEvent
import com.sayx.hm_cloud.model.GameParam
import com.sayx.hm_cloud.model.MessageEvent
import com.sayx.hm_cloud.model.PartyPlayWantPlay
import com.sayx.hm_cloud.model.PlayPartyRoomInfo
import com.sayx.hm_cloud.model.PlayPartyRoomInfoEvent
import com.sayx.hm_cloud.model.PlayPartyRoomSoundAndMicrophoneStateEvent
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray

class HmCloudPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel =
            MethodChannel(flutterPluginBinding.binaryMessenger, GameViewConstants.methodChannelName)
        channel.setMethodCallHandler(this)


        AutoSizeConfig.getInstance()
            .setScreenWidth(ScreenUtils.getScreenWidth())
            .setScreenHeight(ScreenUtils.getScreenHeight())
            .setDesignWidthInDp(812)
            .setDesignHeightInDp(375)
            .setLog(false)
            .setExcludeFontScale(true)
            .setOnAdaptListener(object : onAdaptListener {
                override fun onAdaptBefore(target: Any?, activity: Activity?) {
                    AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getScreenWidth())
                    AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getScreenHeight())

                }

                override fun onAdaptAfter(target: Any?, activity: Activity?) {
                }
            })
    }

    override fun onMethodCall(call: MethodCall, callback: MethodChannel.Result) {
        val arguments = call.arguments
//        Log.e("CloudGame", "onMethodCall:${call.method}, param:$arguments")
        LogUtils.d("onMethodCall:${call.method}, param:$arguments")
        when (call.method) {
            "initSDK" -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameParam.formGson(arguments)
                    GameManager.initSDK(gameParam, callback)
                }
            }
            "getUnReleaseGame" -> {
                GameManager.getUnReleaseGame(callback)
            }
            "getArchiveProgress" -> {
                GameManager.getArchiveProgress(callback)
            }
            // 游戏启动
            GameViewConstants.startCloudGame -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameParam.formGson(arguments)
                    GameManager.startGame(gameParam)
                }
            }
            "openGamePage" -> {
                GameManager.openGamePage()
            }
            // 释放游戏
            "releaseGame" -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameParam.formGson(arguments)
                    GameManager.releaseGame(gameParam, callback)
                }
            }

            "updatePlayInfo" -> {
                if (arguments is Map<*, *>) {
                    GameManager.updatePlayInfo(arguments)
                }
            }

            "showToast" -> {
                ToastUtils.showShort(arguments as String)
            }

            GameViewConstants.getPinCode -> {
                GameManager.getPinCode()
            }

            GameViewConstants.queryControlUsers -> {
                GameManager.queryControlUsers()
            }

            GameViewConstants.controlPlay -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameParam.formGson(arguments)
                    GameManager.initHmcpSdk(gameParam)
                }
            }

            "distributeControl" -> {
                kotlin.runCatching {
                    JSONArray(arguments.toString())
                }.onSuccess { jsonArray ->
                    GameManager.distributeControlPermit(jsonArray)
                }
            }

            "test" -> {
                val intent = Intent().apply {
                    setClass(activity, GameActivity::class.java)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            }

            "playPartyInfo" -> {
                if (arguments is Map<*, *>) {
                    val controlInfoJson = arguments["controlInfos"].toString()
                    val gson = GameManager.gson
                    val controlInfosType = object : TypeToken<List<ControlInfo>>() {}.type
                    val controlInfos: List<ControlInfo> =
                        gson.fromJson(controlInfoJson, controlInfosType)
                    val roomInfoJson = arguments["roomInfo"].toString()
                    val roomInfo = gson.fromJson(
                        roomInfoJson,
                        PlayPartyRoomInfo::class.java
                    )
                    EventBus.getDefault().post(PlayPartyRoomInfoEvent(controlInfos, roomInfo))
                }
            }

            "requestWantPlayPermission" -> {
                if (arguments is Map<*, *>) {
                    // 转成对象
                    val gson = GameManager.gson
                    val wantPlayInfo = gson.fromJson(
                        gson.toJson(arguments),
                        PartyPlayWantPlay::class.java
                    )
                    EventBus.getDefault().post(wantPlayInfo)
                }
            }

            "buySuccess"-> {
                GameManager.updateGamePlayableTime()
            }

            "closePage" -> {
                GameManager.flutterActivity?.finish()
            }

            "exitQueue" -> {
                GameManager.exitQueue()
            }

            "cancelGame" -> {
                GameManager.cancelGame()
            }

            "shareFail" -> {
                EventBus.getDefault().post(MessageEvent("shareFail", arguments as String))
            }

            "exitGame" -> {
                EventBus.getDefault().post(ExitGameEvent())
            }

            "updatePlayPartySoundAndMicrophoneState" -> {
                if (arguments is Map<*, *>) {
                    val soundState = arguments["sound"] as Boolean
                    val microphoneState = arguments["microphone"] as Boolean
                    val event = PlayPartyRoomSoundAndMicrophoneStateEvent(soundState, microphoneState)
                    EventBus.getDefault().post(event)
                }
            }

            "error_dialog_config" -> {
                // 配置错误弹框
                val dialogConfig = GameManager.gson.fromJson(
                    arguments.toString(),
                    ErrorDialogConfig::class.java
                )
                GameManager.setErrorDialogConfig(dialogConfig)
            }

            "updateUserRechargeStatus" -> {
                if (arguments is Map<*, *>) {
                    GameManager.updateUserRechargeStatus(arguments)
                }
            }

            else -> {
                callback.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private lateinit var activity: Activity

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        GameManager.init(channel, binding.activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivity() {

    }
}
