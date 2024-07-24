package com.sayx.hm_cloud.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ControlInfo(
    @SerializedName("cid")
    val cid: String = "",
    @SerializedName("position")
    val position: Int = 0,
    @SerializedName("uid")
    val uid: String = ""
)