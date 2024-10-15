package com.sayx.hm_cloud.http.transformer

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.google.gson.JsonSyntaxException
import com.sayx.hm_cloud.http.bean.AppHttpException
import com.sayx.hm_cloud.http.bean.HttpResponse
import com.sayx.hm_cloud.http.bean.HttpStatusConstants
import io.reactivex.rxjava3.core.ObservableTransformer
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownServiceException
import javax.net.ssl.SSLHandshakeException

/**
 * HttpError
 */
object HttpError {

    fun <T> onError(): ObservableTransformer<HttpResponse<T>, HttpResponse<T>> {
        return ObservableTransformer {
            // http status onError
            it.onErrorReturn { throwable ->
                LogUtils.e(throwable)
                val httpResponse: HttpResponse<T> = HttpResponse(data = null)
                when (throwable) {
                    is UnknownServiceException -> {
                        httpResponse.responseCode = HttpStatusConstants.unknownServiceException
                        httpResponse.responseMessage = "未知服务错误"
                    }
                    is ConnectException -> {
                        httpResponse.responseCode = HttpStatusConstants.connectException
                        httpResponse.responseMessage = "连接错误"
                    }
                    is SocketTimeoutException -> {
                        httpResponse.responseCode = HttpStatusConstants.socketTimeoutException
                        httpResponse.responseMessage = "连接超时"
                    }
                    is SocketException -> {
                        httpResponse.responseCode = HttpStatusConstants.socketException
                        httpResponse.responseMessage = "连接失败"
                    }
                    is JsonSyntaxException -> {
                        httpResponse.responseCode = HttpStatusConstants.jsonSyntaxException
                        httpResponse.responseMessage = "数据解析错误"
                    }
                    is SSLHandshakeException -> {
                        httpResponse.responseCode = HttpStatusConstants.sslHandshakeException
                        httpResponse.responseMessage = "访问错误"
                    }
                    is HttpException -> {
                        httpResponse.responseCode = throwable.code()
                        httpResponse.responseMessage = throwable.message()
                    }
                    is AppHttpException -> {
                        httpResponse.responseCode = throwable.errorCode
                        httpResponse.responseMessage = throwable.errorMessage
                    }
                    else -> {
                        httpResponse.responseCode = HttpStatusConstants.unknownException
                        httpResponse.responseMessage = "未知错误"
                    }
                }
                httpResponse
            }
                .map { response ->
                    LogUtils.v("response:$response")
                    if (response.responseCode == 0) {
                        response
                    } else {
                        throw AppHttpException(response.responseCode, response.responseMessage)
                    }
                }
        }
    }
}