package com.sayx.hm_cloud.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PartyPlayWantPlay(
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("cid")
    val cid: String = "",
    @SerializedName("nickName")
    val nickName: String = "",
    @SerializedName("uid")
    val uid: String = ""
)