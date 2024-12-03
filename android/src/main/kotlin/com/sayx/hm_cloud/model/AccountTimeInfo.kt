package com.sayx.hm_cloud.model

import com.google.gson.annotations.SerializedName

data class AccountTimeInfo(
    val vip: Long,
    @SerializedName(value = "socre")
    val score: Int,
    @SerializedName(value = "_t")
    val totalTime: Long,
    @SerializedName(value = "_et")
    val limitTime: Long,
)
