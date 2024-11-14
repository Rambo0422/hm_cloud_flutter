package com.sayx.hm_cloud.http.interceptor

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val date = response.headers["date"]
        val millis = TimeUtils.string2Millis(date, "EEE, dd MMM yyyy HH:mm:ss 'GMT'")
        LogUtils.d("intercept:$date -> $millis")
        return response
    }
}