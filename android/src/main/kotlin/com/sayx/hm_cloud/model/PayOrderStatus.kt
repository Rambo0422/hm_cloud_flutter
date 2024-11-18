package com.sayx.hm_cloud.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PayOrderStatus(
    @SerializedName("orderNo")
    val orderNo: String = "",
    @SerializedName("orderStatus")
    val status: Int = 0,
)