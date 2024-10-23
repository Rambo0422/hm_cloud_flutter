package com.sayx.hm_cloud

import android.app.Activity
import android.content.res.Configuration
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.sayx.hm_cloud.model.GameParam
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener

class HmCloudPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        LogUtils.getConfig().also {
            it.isLogSwitch = BuildConfig.DEBUG
            it.globalTag = "GameManager"
        }
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
        LogUtils.d("onMethodCall->callMethod:${call.method}, param:$arguments")
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

            "checkUnReleaseGame" -> {
                GameManager.checkPlayingGame(callback, arguments.toString())
            }

            "releaseGame" -> {
                GameManager.releaseGame("1", null)
            }

            "releaseOldGame" -> {
                GameManager.releaseOldGame(callback, arguments.toString())
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
