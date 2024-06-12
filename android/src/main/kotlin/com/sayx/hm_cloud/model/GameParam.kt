package com.sayx.hm_cloud.model

import com.google.gson.annotations.SerializedName

data class GameParam(
    @SerializedName("accessKeyId")
    val accessKeyId: String,
    @SerializedName("accountInfo")
    val accountInfo: AccountInfo,
    @SerializedName("cToken")
    val cToken: String,
    @SerializedName("channelName")
    val channelName: String,
    @SerializedName("gameId")
    val gameId: String,
    @SerializedName("gamePkName")
    val gamePkName: String,
    @SerializedName("isVip")
    val isVip: Boolean,
    @SerializedName("mouseMode")
    val mouseMode: Int,
    @SerializedName("mute")
    val mute: Boolean,
    @SerializedName("playTime")
    val playTime: Int,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("channel")
    var channel: String,
    @SerializedName("userToken")
    val userToken: String
)