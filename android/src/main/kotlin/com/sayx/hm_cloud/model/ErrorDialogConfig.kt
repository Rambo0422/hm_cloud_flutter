package com.sayx.hm_cloud.model

import com.google.gson.annotations.SerializedName

class ErrorDialogConfig(
    val enable: Boolean = false,
    @SerializedName("list")
    val list: List<ErrorConfigInfo> = emptyList(),
)

class ErrorConfigInfo(
    val androidCode: String,
    val title: String,
    val subtitle: String,
)