package com.sayx.hm_cloud.model

data class ArchiveData(
    val code: Int,
    val custodian: String,
    val list: List<ArchiveInfo>?,
    val target: String
)