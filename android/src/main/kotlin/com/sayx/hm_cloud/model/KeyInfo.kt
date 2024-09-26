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

    companion object {
        fun fromData(data: Any): KeyInfo {
            val left = if (data is LinkedTreeMap<*, *> && data["left"] is Number) {
                (data["left"] as Number).toInt()
            } else {
                0
            }
            val top = if (data is LinkedTreeMap<*, *> && data["top"] is Number) {
                (data["top"] as Number).toInt()
            } else {
                0
            }
            val width = if (data is LinkedTreeMap<*, *> && data["width"] is Number) {
                (data["width"] as Number).toInt()
            } else {
                0
            }
            val zoom = if (data is LinkedTreeMap<*, *> && data["zoom"] is Number) {
                (data["zoom"] as Number).toInt()
            } else {
                0
            }
            val text = if (data is LinkedTreeMap<*, *> && data["text"] is String) {
                data["text"]
            } else {
                ""
            }
            val remark = if (data is LinkedTreeMap<*, *> && data["remark"] is Number) {
                (data["remark"] as Number).toInt()
            } else {
                0
            }
            val type = if (data is LinkedTreeMap<*, *> && data["type"] is String) {
                data["type"]
            } else {
                0
            }
            val opacity = if (data is LinkedTreeMap<*, *> && data["opacity"] is Number) {
                (data["opacity"] as Number).toInt()
            } else {
                0
            }
            val click = if (data is LinkedTreeMap<*, *> && data["click"] is Number) {
                (data["click"] as Number).toInt()
            } else {
                0
            }
            val inputOp = if (data is LinkedTreeMap<*, *> && data["inputOp"] is Number) {
                (data["inputOp"] as Number).toInt()
            } else {
                0
            }
            val height = if (data is LinkedTreeMap<*, *> && data["height"] is Number) {
                (data["height"] as Number).toInt()
            } else {
                0
            }
            val composeArr = if (data is LinkedTreeMap<*, *> && data["composeArr"] is ArrayList<*>) {
                (data["composeArr"] as ArrayList<*>).map { item ->
                    fromData(item)
                }.toList()
            } else {
                listOf()
            }
            val rouArr = if (data is Map<*, *> && data["rouArr"] is ArrayList<*>) {
                (data["rouArr"] as ArrayList<*>).map { item ->
                    fromData(item)
                }.toList()
            } else {
                listOf()
            }
            return KeyInfo(
                UUID.randomUUID(),
                left,
                top,
                width,
                zoom,
                text as String,
                remark,
                type as String,
                opacity,
                click,
                inputOp,
                height,
                composeArr,
                rouArr
            )
        }
    }
}
