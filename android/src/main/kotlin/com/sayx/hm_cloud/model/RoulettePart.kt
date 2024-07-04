package com.sayx.hm_cloud.model

import com.google.gson.Gson

data class RoulettePart(
    val keyInfo: KeyInfo,
    val startAngle: Float,
    val angle: Float,
    var selected: Boolean = false
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
