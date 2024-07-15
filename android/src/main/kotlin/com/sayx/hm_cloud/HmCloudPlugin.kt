package com.sayx.hm_cloud

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.model.ControllerChangeEvent
import com.sayx.hm_cloud.model.ControllerConfigEvent
import com.sayx.hm_cloud.model.ControllerEditEvent
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.GameParam
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import org.greenrobot.eventbus.EventBus

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
            // 游戏启动
            GameViewConstants.startCloudGame -> {
                if (arguments is Map<*, *>) {
                    val gameParam = GameManager.gson.fromJson(
                        GameManager.gson.toJson(arguments),
                        GameParam::class.java
                    )
                    GameManager.startGame(gameParam)
                }
            }
            // 切换操作方式
            "showController" -> {
                if (arguments is Int) {
                    EventBus.getDefault().post(ControllerChangeEvent(arguments))
                }
            }
            // 监听到设备接入，设置为电脑模式
            "setPCMouseMode" -> {
                GameManager.setPCMouseMode(arguments)
            }

            // 操作方式数据
            "setControllerData" -> {
                val data = GameManager.gson.fromJson(
                    GameManager.gson.toJson(arguments),
                    ControllerInfo::class.java
                )
                EventBus.getDefault().post(ControllerConfigEvent(data))
            }

            // 操作方式编辑成功
            "controllerEditSuccess" -> {
                ToastUtils.showShort("已保存")
                val data = GameManager.gson.fromJson(arguments as String, ControllerInfo::class.java)
                EventBus.getDefault()
                    .post(ControllerEditEvent(data.type))
            }

            // 操作方式编辑失败
            "controllerEditFail" -> {
                ToastUtils.showShort("编辑保存失败")
            }

            GameViewConstants.getPinCode -> {
                GameManager.getPinCode()
            }

            GameViewConstants.queryControlUsers -> {
                GameManager.queryControlUsers()
            }

            GameViewConstants.controlPlay -> {
                Log.d("flutter","arguments: $arguments")
                if (arguments is Map<*, *>) {
                    GameManager.initHmcpSdk(arguments)
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

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        GameManager.init(channel, binding.activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivity() {

    }
}
