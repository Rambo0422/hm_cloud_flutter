package com.example.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.listeners.OnInitCallBackListener

object HmcpManagerIml {

    val TAG = "guozewen"

    fun init(context: Context, creationParams: Map<String, Any>, onInitCallBackListener: OnInitCallBackListener) {
        val accessKeyId = creationParams["accessKeyId"].toString()
        val channelId = creationParams["channelId"].toString()
        val videoViewType = kotlin.runCatching {
            creationParams["videoViewType"] as Int
        }.getOrElse { HmcpManager.RENDER_TEXTURE_VIEW }

        Log.e(TAG, "$TAG accessKeyId: $accessKeyId")
        Log.e(TAG, "$TAG channelId: $channelId")

        val manager = HmcpManager.getInstance()
        val bundle = Bundle()
        bundle.putString(HmcpManager.ACCESS_KEY_ID, accessKeyId)
        bundle.putString(HmcpManager.CHANNEL_ID, channelId)

        val sdkVersion = manager.sdkVersion
        Log.e(TAG, "$TAG sdkVersion: $sdkVersion")
        Log.e(TAG, "$TAG cloudId: ${manager.cloudId}")

        manager.videoViewType = videoViewType
        manager.init(bundle, context, object : OnInitCallBackListener {
            override fun success() {
                Log.e(TAG, "$TAG -------- init success --------")
                onInitCallBackListener.success()
            }

            override fun fail(msg: String) {
                Log.e(TAG, "$TAG -------- init fail--------$msg")
                onInitCallBackListener.fail(msg)
            }
        })
    }
}