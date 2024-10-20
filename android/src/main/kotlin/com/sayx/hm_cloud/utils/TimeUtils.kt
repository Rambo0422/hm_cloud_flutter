package com.sayx.hm_cloud.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

    // 可玩时间
    // time 时间值秒
    fun getTimeString(time: Long): String {
        var seconds = time
        val hours = seconds / 3600
        seconds %= 3600
        val minutes = seconds / 60
        seconds %= 60
        return "${if (hours < 10) "0$hours" else "$hours"}:${if (minutes < 10) "0$minutes" else "$minutes"}:${if (seconds < 10) "0$seconds" else "$seconds"}"
    }

    fun getCountTime(time: Long): String {
        if (time <= 0) {
            return "00:00"
        }
        var seconds = time
        seconds %= 3600
        val minutes = seconds / 60
        seconds %= 60
        return "${if (minutes < 10) "0$minutes" else "$minutes"}:${if (seconds < 10) "0$seconds" else "$seconds"}"
    }

    fun isSameDay(time1: Any?, time2: Long): Boolean {
        if (time1 is Number) {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date1 = Date(time1.toLong())
            val date2 = Date(time2)
            return dateFormat.format(date1) == dateFormat.format(date2)
        }
        return false
    }
}