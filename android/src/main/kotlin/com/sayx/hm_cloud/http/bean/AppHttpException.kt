package com.sayx.hm_cloud.http.bean

import com.google.gson.annotations.SerializedName

/**
 * AppHttpException
 */
data class AppHttpException(
    @SerializedName("code")
    val errorCode: Int?,
    @SerializedName(value = "message",alternate = ["msg"])
    var errorMessage: String?
) : Throwable()