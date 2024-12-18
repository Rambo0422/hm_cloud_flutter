package com.sayx.hm_cloud.http

import com.sayx.hm_cloud.BuildConfig
import com.sayx.hm_cloud.http.interceptor.RequestInterceptor
import com.sayx.hm_cloud.http.interceptor.ResponseInterceptor
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * HttpManager
 */
object HttpManager {

    private val retrofitBuilder = Retrofit.Builder()

    private val requestInterceptor = RequestInterceptor()
    private val responseInterceptor = ResponseInterceptor()

    init {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(requestInterceptor)
            .addInterceptor(responseInterceptor)

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        retrofitBuilder.baseUrl("https://api-cgfc.3ayx.net")
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()
    }

    fun <H> createApi(serviceClass: Class<H>): H {
        return retrofitBuilder.build().create(serviceClass) as H
    }

    fun updateBaseUrl(url: String) {
        retrofitBuilder.baseUrl(url)
    }

    fun updateBaseUrl(url: URL) {
        retrofitBuilder.baseUrl(url)
    }

    fun updateBaseUrl(url: HttpUrl) {
        retrofitBuilder.baseUrl(url)
    }

    fun getBaseUrl(): HttpUrl = retrofitBuilder.build().baseUrl()

    fun setCommonParam(commonParam: HashMap<String, Any>) {
        requestInterceptor.commonParam.clear()
        requestInterceptor.commonParam.putAll(commonParam)
    }

    fun addCommonParam(key: String, value: Any) {
        requestInterceptor.commonParam[key] = value
    }

    fun setHttpHeader(httpHeader: HashMap<String, Any>) {
        requestInterceptor.httpHeader.clear()
        requestInterceptor.httpHeader.putAll(httpHeader)
    }

    fun addHttpHeader(key: String, value: Any) {
        requestInterceptor.httpHeader[key] = value
    }

    fun removeHttpHeader(key: String) {
        requestInterceptor.httpHeader.remove(key)
    }

    fun getHttpHeader(): HashMap<String, Any> {
        return requestInterceptor.httpHeader
    }

    fun getCommonParam(): HashMap<String, Any> {
        return requestInterceptor.commonParam
    }

    fun removeCommonParam(key: String) {
        requestInterceptor.commonParam.remove(key)
    }
}