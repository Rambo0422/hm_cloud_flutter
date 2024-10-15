package com.sayx.hm_cloud.model

class GameError {
    companion object {
        const val gameInitErrorCode = "1000001"
        const val gameInitErrorMsg = "游戏初始化失败"
        const val gameParamErrorCode = "1000002"
        const val gameParamErrorMsg = "游戏运行参数错误"
        const val gameConfigErrorCode = "1000003"
        const val gameConfigErrorMsg = "游戏配置参数错误"
        const val gameReleaseErrorCode = "1000004"
        const val gameReleaseErrorMsg = "游戏释放失败"
    }
}