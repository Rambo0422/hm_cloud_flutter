package com.sayx.hm_cloud.model

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
    @Expose(serialize = false) var zoomChange: Boolean = false,
    @Expose(serialize = false) var opacityChange: Boolean = false,
    @Expose(serialize = false) var textChange: Boolean = false,
    @Expose(serialize = false) var listChange: Boolean = false
) : Serializable {

    fun getKeyWidth(): Int {
        return ceil(width * (zoom / 100f * 2f)).toInt()
    }

    fun getKeyHeight(): Int {
        return ceil(height * (zoom / 100f * 2f)).toInt()
    }

    fun changeZoom(zoom: Int) {
        this.zoom = zoom
        zoomChange = true
    }

    fun changeOpacity(opacity: Int) {
        this.opacity = opacity
        opacityChange = true
    }

    fun changePosition(left: Int, top: Int) {
        this.left = left
        this.top = top
    }

    fun changeText(text: String?) {
        this.text = text
        textChange = true
    }

    fun changeList(composeArr: List<KeyInfo>) {
        this.composeArr = composeArr
        listChange = true
    }

    fun updateRouList(rouArr: List<KeyInfo>) {
        this.rouArr = rouArr
        listChange = true
    }

    fun updateChange(boolean: Boolean) {
        textChange = boolean
        zoomChange = boolean
        opacityChange = boolean
        listChange = boolean
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
            this.composeArr,
            this.rouArr,
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
        return map
    }
}
