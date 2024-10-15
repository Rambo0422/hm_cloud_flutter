package com.sayx.hm_cloud.model

data class ArchiveInfo(
    val cid: String,
    val createdTime: Long,
    val downLoadUrl: String,
    val fileMD5: String,
    val format: String,
    val source: String
)