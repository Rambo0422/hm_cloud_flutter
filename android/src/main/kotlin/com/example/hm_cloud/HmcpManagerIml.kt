package com.example.hm_cloud

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.listeners.OnInitCallBackListener

object HmcpManagerIml {

    val TAG = this.javaClass.simpleName
    var isInit = false

    fun init(context: Context, creationParams: Map<String, Any>, onInitCallBackListener: OnInitCallBackListener) {
        if (!isInit) {
            val accessKeyId = creationParams["accessKeyId"].toString()
            val channelId = creationParams["channelId"].toString()

            val manager = HmcpManager.getInstance()
            val bundle = Bundle()
            bundle.putString(HmcpManager.ACCESS_KEY_ID, accessKeyId)
            bundle.putString(HmcpManager.CHANNEL_ID, channelId)
            manager.init(bundle, context, object : OnInitCallBackListener {
                override fun success() {
                    Log.e(TAG, "-------- init success --------")
                    isInit = true
                    onInitCallBackListener.success()
                }

                override fun fail(msg: String) {
                    Log.e(TAG, "-------- init fail--------$msg")
                    onInitCallBackListener.fail(msg)
                }
            })
        } else {
            onInitCallBackListener.success()
        }
    }
}