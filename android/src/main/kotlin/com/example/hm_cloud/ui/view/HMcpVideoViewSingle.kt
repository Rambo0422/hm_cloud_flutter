package com.example.hm_cloud.ui.view

/**
 * 海马云的单例View
 */
class HMcpVideoViewSingle {

    // 单例
    companion object {
        val instance: HMcpVideoViewSingle by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HMcpVideoViewSingle()
        }
    }

}