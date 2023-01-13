package com.example.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.listeners.OnInitCallBackListener
import com.orhanobut.logger.Logger

object HmcpManagerIml {

    fun init(context: Context, creationParams: Map<String, Any>, onInitCallBackListener: OnInitCallBackListener) {
        val accessKeyId = creationParams["accessKeyId"].toString()
        val channelId = creationParams["channelId"].toString()
        val videoViewType = kotlin.runCatching {
            creationParams["videoViewType"] as Int
        }.getOrElse { HmcpManager.RENDER_TEXTURE_VIEW }

        Logger.e("accessKeyId:$accessKeyId, channelId:$channelId, videoViewType:$videoViewType")

        val manager = HmcpManager.getInstance()
        val bundle = Bundle()
        bundle.putString(HmcpManager.ACCESS_KEY_ID, accessKeyId)
        bundle.putString(HmcpManager.CHANNEL_ID, channelId)

        val sdkVersion = manager.sdkVersion
        Logger.w("sdkVersion:$sdkVersion, cloudId:${manager.cloudId}")

        manager.videoViewType = videoViewType
        manager.init(bundle, context, object : OnInitCallBackListener {
            override fun success() {
                Logger.w("HmcpManager -------- init success --------")
                onInitCallBackListener.success()
            }

            override fun fail(msg: String) {
                Logger.w("HmcpManager -------- init fail --------: $msg")
                onInitCallBackListener.fail(msg)
            }
        })
    }
}