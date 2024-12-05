package com.sayx.hm_cloud.model

import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.internal.LinkedTreeMap
import java.io.Serializable
import java.util.UUID
import kotlin.math.ceil

class KeyInfo(
    @Expose(serialize = false) var id: UUID?,
    @Expose(serialize = true) var left: Int,
    @Expose(serialize = true) var top: Int,
    @Expose(serialize = true) val width: Int,
    @Expose(serialize = true) var zoom: Int,
    @Expose(serialize = true) var text: String?,
    @Expose(serialize = true) var remark: Int?,
    @Expose(serialize = true) var type: String,
    @Expose(serialize = true) var opacity: Int,
    @Expose(serialize = true) var click: Int,
    @Expose(serialize = true) val inputOp: Int,
    @Expose(serialize = true) val height: Int,
    @Expose(serialize = true) var map: String? = "",
    @Expose(serialize = true) var composeArr: List<KeyInfo>? = null,
    @Expose(serialize = true) var rouArr: List<KeyInfo>? = null,
    @Expose(serialize = true) var containerArr: List<KeyInfo>? = null,
) : Serializable {

    init {
        id = if (id == null) UUID.randomUUID() else id
    }

    fun getKeyWidth(): Int {
        return ceil(width * (zoom / 100f * 2f)).toInt()
    }

    fun getKeyHeight(): Int {
        return ceil(height * (zoom / 100f * 2f)).toInt()
    }

    fun changePosition(left: Int, top: Int) {
        this.left = left
        this.top = top
    }

    fun copy(): KeyInfo {
        return KeyInfo(
            this.id,
            this.left,
            this.top,
            this.width,
            this.zoom,
            this.text,
            this.remark,
            this.type,
            this.opacity,
            this.click,
            this.inputOp,
            this.height,
            this.map,
            this.composeArr?.map { item -> item.copy() }?.toList(),
            this.rouArr?.map { item -> item.copy() }?.toList(),
            this.containerArr?.map { item -> item.copy() }?.toList(),
        )
    }

    override fun toString(): String {
        val gson = GsonBuilder()
            // 仅序列化有 @Expose 标记的字段
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        return "${this.id}:${gson.toJson(this)}"
    }

    fun toMap(): Map<*, *> {
        val map = mutableMapOf<String, Any?>(
            "top" to top,
            "left" to left,
            "width" to width,
            "height" to height,
            "type" to type,
            "zoom" to zoom,
            "opacity" to opacity,
            "click" to click,
            "inputOp" to inputOp,
            "text" to text,
            "remark" to remark,
            "map" to map,
        )
        composeArr?.let {
            map["composeArr"] = it.map { keyInfo -> keyInfo.toMap() }.toList()
        }
        rouArr?.let {
            map["rouArr"] = it.map { keyInfo -> keyInfo.toMap() }.toList()
        }
        containerArr?.let {
            map["containerArr"] = it.map { keyInfo -> keyInfo.toMap() }.toList()
        }
        return map
    }

    fun copyFrom(keyInfo: KeyInfo) {
        this.text = keyInfo.text
        this.zoom = keyInfo.zoom
        this.opacity = keyInfo.opacity
        this.composeArr = keyInfo.composeArr
        this.rouArr = keyInfo.rouArr
        this.containerArr = keyInfo.containerArr
        this.click = keyInfo.click
        this.map = keyInfo.map
    }
}
