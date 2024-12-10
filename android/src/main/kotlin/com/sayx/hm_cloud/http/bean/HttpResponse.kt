package com.sayx.hm_cloud.http.bean

import com.google.gson.annotations.SerializedName

data class HttpResponse<T>(

    var exceptionName : String? = null,

    @SerializedName("message")
    var responseMessage: String? = null,

    @SerializedName("code")
    var responseCode: Int? = -1,

    @SerializedName("data", alternate = ["id"])
    val data: T?,
) {
    override fun toString(): String {
        return "HttpResponse(responseMessage=$responseMessage, responseCode=$responseCode, data=$data, exceptionName=$exceptionName)"
    }
}