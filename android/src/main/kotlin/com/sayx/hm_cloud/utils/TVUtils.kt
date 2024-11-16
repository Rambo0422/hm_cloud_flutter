package com.sayx.hm_cloud.utils

import android.content.Context
import android.content.Intent

object TVUtils {
    /**
     * 写这个的目的是，有些手柄点击home键是不会自动跳转的，所以这里单独进行处理跳转
     */
    fun toTVHome(context: Context) {
        val intent = Intent()
        intent.setClassName(context.packageName, "com.sayx.sagame.MainActivity")
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // 添加标志以在新任务中启动应用
        context.startActivity(intent)
    }
}