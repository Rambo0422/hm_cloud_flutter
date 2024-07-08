package com.sayx.hm_cloud.model

import java.io.Serializable
import java.util.UUID

class KeyInfo(
    var id: UUID?,
    var top: Int,
    var left: Int,
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

    fun getWidth(): Float {
        return width * (zoom / 100f * 2.25f)
    }

    fun getHeight(): Float {
        return height * (zoom / 100f * 2.25f)
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
            this.top,
            this.left,
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
}
