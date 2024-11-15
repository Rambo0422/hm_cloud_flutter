package com.sayx.hm_cloud.http.interceptor

import com.blankj.utilcode.util.LogUtils
import com.sayx.hm_cloud.utils.TimeUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale

class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val date = response.headers["date"]
        date?.let {
            val millis = TimeUtils.string2Millis(it, SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH))
            TimeUtils.timeInterval = System.currentTimeMillis() - millis
        }
        return response
    }
}