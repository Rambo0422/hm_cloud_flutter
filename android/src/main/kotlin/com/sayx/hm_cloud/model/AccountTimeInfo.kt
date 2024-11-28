package com.sayx.hm_cloud.model

import com.google.gson.annotations.SerializedName

data class AccountTimeInfo(
    val vip: Int,
    @SerializedName(value = "socre")
    val score: Int,
    @SerializedName(value = "_t")
    val totalTime: Int,
    @SerializedName(value = "_et")
    val limitTime: Int,
)
