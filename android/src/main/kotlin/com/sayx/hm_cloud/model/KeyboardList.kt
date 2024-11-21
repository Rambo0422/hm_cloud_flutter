package com.sayx.hm_cloud.model

data class KeyboardList(
    val page: Int,
    val size: Int,
    val total: Int,
    val datas: List<ControllerInfo>,
)
