package com.sayx.hm_cloud

object GameViewConstants {

    const val methodChannelName = "hm_cloud_controller"

    const val viewType = "plugins.flutter.io/hm_cloud_view"

    /// 云游戏SDK初始化成功
    const val initSDKSuccess = "initSDKSuccess"

    /// 开始云游戏
    const val startCloudGame = "startCloudGame"

    /// 结束云游戏
    const val stopGame = "stopGame"

    /// 发送按键事件
    const val sendCustomKey = "sendCustomKey"

    /// callback-首帧到达
    const val firstFrameArrival = "firstFrameArrival"

    /// 设置鼠标模式
    const val setMouseMode = "setMouseMode"

    /// 设置鼠标灵敏度
    const val setMouseSensitivity = "setMouseSensitivity"

    /// 展示输入法
    const val showInput = "showInput"

    /// 云游互动开关
    const val switchInteraction = "switchInteraction"

    /// 静音
    const val setMute = "setMute"

    /// 画质
    const val setQuality = "setQuality"

    const val getPinCode = "getPinCode"

    const val queryControlUsers = "queryControlUsers"

    const val controlPlay = "controlPlay"
}