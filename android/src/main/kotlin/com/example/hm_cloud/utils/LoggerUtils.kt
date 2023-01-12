package com.example.hm_cloud.utils

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.LogcatLogStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object LoggerUtils {
    fun init() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .methodCount(0)
            .methodOffset(7)
            .logStrategy(LogcatLogStrategy())
            .tag("guozewen")
            .build()


        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return true
            }
        })
    }
}