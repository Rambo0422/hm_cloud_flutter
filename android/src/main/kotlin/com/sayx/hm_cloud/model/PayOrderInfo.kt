package com.sayx.hm_cloud.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PayOrderInfo(
    @SerializedName("orderId")
    val orderId: String = "",
    @SerializedName("qrCode")
    val qrCode: String = "",
    @SerializedName("orderNo")
    val orderNo: String = "",
)