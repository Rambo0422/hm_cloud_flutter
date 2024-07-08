package com.sayx.hm_cloud.model

enum class CallBackMode {
    // 有移动就立刻回调
    CALL_BACK_MODE_MOVE,

    // 只有状态变化的时候才回调
    CALL_BACK_MODE_STATE_CHANGE
}