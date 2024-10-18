package com.sayx.hm_cloud

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.reflect.TypeToken
import com.sayx.hm_cloud.constants.AppVirtualOperateType
import com.sayx.hm_cloud.model.ControlInfo
import com.sayx.hm_cloud.model.ControllerChangeEvent
import com.sayx.hm_cloud.model.ControllerConfigEvent
import com.sayx.hm_cloud.model.ControllerEditEvent
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.ExitGameEvent
import com.sayx.hm_cloud.model.GameParam
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
        AutoSizeConfig.getInstance().onAdaptListener = object : onAdaptListener {
            override fun onAdaptBefore(target: Any?, activity: Activity?) {
                AutoSizeConfig.getInstance().screenWidth = ScreenUtils.getScreenWidth()
                AutoSizeConfig.getInstance().screenHeight = ScreenUtils.getScreenHeight()
                if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    AutoSizeConfig.getInstance().designWidthInDp = 812
                    AutoSizeConfig.getInstance().designHeightInDp = 375
                } else {
                    AutoSizeConfig.getInstance().designWidthInDp = 375
                    AutoSizeConfig.getInstance().designHeightInDp = 812
                }
            }

            override fun onAdaptAfter(target: Any?, activity: Activity?) {
            }
        }
        AutoSizeConfig.getInstance().isExcludeFontScale = true
    }

    override fun onMethodCall(call: MethodCall, callback: MethodChannel.Result) {
        val arguments = call.arguments
//        Log.e("CloudGame", "onMethodCall:${call.method}, param:$arguments")
        LogUtils.d("onMethodCall:${call.method}, param:$arguments")
        when (call.method) {
            "initSDK" -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameParam.formGson(arguments)
                    GameManager.initSDK(gameParam)
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
            // 切换操作方式
            "showController" -> {
                if (arguments is Int) {
                    when (arguments) {
                        0 -> {
                            GameManager.lastControllerType = AppVirtualOperateType.NONE
                        }

                        1 -> {
                            GameManager.lastControllerType = AppVirtualOperateType.APP_KEYBOARD
                        }

                        2 -> {
                            GameManager.lastControllerType = AppVirtualOperateType.APP_STICK_XBOX
                        }
                    }
                    EventBus.getDefault().post(ControllerChangeEvent(arguments))
                }
            }
            // 监听到设备接入，设置为电脑模式
            "setPCMouseMode" -> {
                GameManager.setPCMouseMode(arguments)
            }
            // 释放游戏
            "releaseGame" -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameParam.formGson(arguments)
                    GameManager.releaseGame(gameParam, callback)
                }
            }

            // 操作方式数据
            "setControllerData" -> {
                val data = GameManager.gson.fromJson(
                    GameManager.gson.toJson(arguments),
                    Map::class.java
                )
                val controllerInfo = ControllerInfo.fromData(data)
                LogUtils.v("setControllerData:$controllerInfo")
                EventBus.getDefault().post(ControllerConfigEvent(controllerInfo))
            }

            // 操作方式编辑成功
            "controllerEditSuccess" -> {
                ToastUtils.showShort("已保存")
                val data =
                    GameManager.gson.fromJson(arguments as String, Map::class.java)
                val controllerInfo = ControllerInfo.fromData(data)
                EventBus.getDefault()
                    .post(ControllerEditEvent(controllerInfo.type))
            }

            // 操作方式编辑失败
            "controllerEditFail" -> {
                ToastUtils.showShort("编辑保存失败")
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
                Log.d("flutter", "arguments: $arguments")
                if (arguments is Map<*, *>) {
                    GameManager.initHmcpSdk(arguments)
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
