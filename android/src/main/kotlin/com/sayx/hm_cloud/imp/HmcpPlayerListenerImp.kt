package com.sayx.hm_cloud.imp

import com.blankj.utilcode.util.LogUtils
import com.haima.hmcp.HmcpManager
import com.haima.hmcp.beans.PlayNotification
import com.haima.hmcp.enums.CloudPlayerKeyboardStatus
import com.haima.hmcp.enums.ErrorType
import com.haima.hmcp.enums.NetWorkState
import com.haima.hmcp.listeners.HmcpPlayerListener

open class HmcpPlayerListenerImp : HmcpPlayerListener {
    override fun onCloudDeviceStatus(status: String?) {
        LogUtils.d("onCloudDeviceStatus:$status")
    }

    override fun onInterceptIntent(intentData: String?) {
        LogUtils.d("onInterceptIntent:$intentData")
    }

    override fun onCloudPlayerKeyboardStatusChanged(keyboardStatus: CloudPlayerKeyboardStatus?) {
        LogUtils.d("onCloudPlayerKeyboardStatusChanged:${keyboardStatus?.name}")
    }

    override fun onError(errorType: ErrorType?, errorMsg: String?) {
        LogUtils.e("onError-> errorType:$errorType, errorMsg:$errorMsg")
    }

    override fun onSuccess() {
        LogUtils.d("onSuccess")
    }

    override fun onExitQueue() {
        LogUtils.d("onExitQueue")
    }

    override fun onMessage(msg: String?) {
        LogUtils.d("onMessage:$msg")
    }

    override fun onSceneChanged(sceneMessage: String?) {
        LogUtils.d("onSceneChanged:$sceneMessage, cid:${HmcpManager.getInstance().cloudId}")
    }

    override fun onNetworkChanged(networkState: NetWorkState?) {
        LogUtils.d("onNetworkChanged:$networkState")
    }

    override fun onPlayStatus(status: Int, value: Long, data: String?) {
        LogUtils.d("onPlayStatus->status:$status, value:$value, data:$data")
    }

    override fun HmcpPlayerStatusCallback(statusData: String?) {
        LogUtils.d("playerStatusCallback:$statusData, cid:${HmcpManager.getInstance().cloudId}")
    }

    override fun onPlayerError(errorCode: String?, errorMsg: String?) {
        LogUtils.e("onPlayerError->errorCode:$errorCode, errorMsg:$errorMsg")
    }

    override fun onInputMessage(msg: String?) {
        LogUtils.d("onInputMessage:$msg")
    }

    override fun onInputDevice(device: Int, operationType: Int) {
        LogUtils.d("onInputDevice-> device:$device, operationType:$operationType")
    }

    override fun onPermissionNotGranted(msg: String?) {
        LogUtils.e("onPermissionNotGranted:$msg")
    }

    override fun onMiscResponse(msg: String?) {
        LogUtils.d("onInputMessage:$msg")
    }

    override fun onAccProxyConnectStateChange(connectState: Int) {
        super.onAccProxyConnectStateChange(connectState)
        LogUtils.d("onAccProxyConnectStateChange:$connectState")
    }

    override fun onPlayNotification(playNotification: PlayNotification?) {
        super.onPlayNotification(playNotification)
        LogUtils.d("onPlayNotification")
    }

    override fun onSwitchConnectionCallback(statusCode: Int, networkType: Int) {
        super.onSwitchConnectionCallback(statusCode, networkType)
        LogUtils.d("onSwitchConnectionCallback:$statusCode, $networkType")
    }
}