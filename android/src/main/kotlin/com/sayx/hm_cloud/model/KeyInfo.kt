package com.sayx.hm_cloud.model

import com.sayx.hm_cloud.GameManager
import java.io.Serializable
import java.util.UUID
import kotlin.math.ceil

class KeyInfo(
    var id: UUID?,
    var left: Int,
    var top: Int,
    val width: Int,
    var zoom: Int,
    var text: String?,
    var type: String,
    var opacity: Int,
    var click: Int,
    val inputOp: Int,
    val height: Int,
    var composeArr: List<KeyInfo>? = null,
    var zoomChange: Boolean = false,
    var opacityChange: Boolean = false,
    var textChange: Boolean = false,
    var listChange: Boolean = false
) : Serializable {

    init {
        id = UUID.randomUUID()
    }

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
            this.type,
            this.opacity,
            this.click,
            this.inputOp,
            this.height,
            this.composeArr
        )
    }

    override fun toString(): String {
        return GameManager.gson.toJson(this)
    }
}
