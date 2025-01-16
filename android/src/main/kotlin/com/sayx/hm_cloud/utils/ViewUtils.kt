package com.sayx.hm_cloud.utils

import android.view.View
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.model.ControllerInfo
import com.sayx.hm_cloud.model.KeyInfo
import kotlin.math.pow
import kotlin.math.sqrt

object ViewUtils {

    fun getViewLeft(view: View): Float {
        return view.x
    }

    fun getViewTop(view: View): Float {
        return view.y
    }

    fun getViewRight(view: View): Float {
        return view.x + view.width
    }

    fun getViewBottom(view: View): Float {
        return view.y + view.height
    }

    fun getViewCenterX(view: View): Float {
        return view.x + view.width / 2f
    }

    fun getViewCenterY(view: View): Float {
        return view.y + view.height / 2f
    }

    fun getViewDistance(targetView: View, resultView: View): Float {
        val targetCenterX = targetView.x + targetView.width / 2
        val targetCenterY = targetView.y + targetView.height / 2
        val viewCenterX = resultView.x + resultView.width / 2
        val viewCenterY = resultView.y + resultView.height / 2
        return sqrt(
            (viewCenterX - targetCenterX).toDouble().pow(2.0) + (viewCenterY - targetCenterY).toDouble().pow(2.0)
        ).toFloat()
    }

    fun translateViewSize(keyInfo: KeyInfo) {
        when (keyInfo.type) {
            KeyType.ROCKER_RIGHT, KeyType.ROCKER_LEFT, KeyType.ROCKER_LETTER,
            KeyType.ROCKER_ARROW, KeyType.KEY_ROULETTE, KeyType.GAMEPAD_ROULETTE -> {
                if (keyInfo.width != 144 || keyInfo.height != 144) {
                    keyInfo.width = 144
                    keyInfo.height = 144
                    keyInfo.zoom = 50
                }
            }

            KeyType.GAMEPAD_SQUARE -> {
                if (keyInfo.width != 80 || keyInfo.height != 60) {
                    keyInfo.width = 80
                    keyInfo.height = 60
                    keyInfo.zoom = 50
                }
            }

            KeyType.GAMEPAD_ROUND_SMALL -> {
                if (keyInfo.width != 40 || keyInfo.height != 40) {
                    keyInfo.width = 40
                    keyInfo.height = 40
                    keyInfo.zoom = 50
                }
            }

            KeyType.GAMEPAD_ELLIPTIC -> {
                if (keyInfo.width != 64 || keyInfo.height != 40) {
                    keyInfo.width = 64
                    keyInfo.height = 40
                    keyInfo.zoom = 50
                }
            }

            KeyType.ROCKER_CROSS -> {
                if (keyInfo.width != 128 || keyInfo.height != 128) {
                    keyInfo.width = 128
                    keyInfo.height = 128
                    keyInfo.zoom = 50
                }
            }

            KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT,
            KeyType.KEYBOARD_MOUSE_MIDDLE, KeyType.KEYBOARD_MOUSE_UP, KeyType.GAMEPAD_COMBINE,
            KeyType.KEYBOARD_MOUSE_DOWN, KeyType.KEY_SHOOT, KeyType.KEY_COMBINE -> {
                if (keyInfo.width != 64 || keyInfo.height != 64) {
                    keyInfo.width = 64
                    keyInfo.height = 64
                    keyInfo.zoom = 50
                }
            }

            KeyType.KEYBOARD_KEY, KeyType.GAMEPAD_ROUND_MEDIUM -> {
                if (keyInfo.width != 48 || keyInfo.height != 48) {
                    keyInfo.width = 48
                    keyInfo.height = 48
                    keyInfo.zoom = 50
                }
            }

            KeyType.KEY_CONTAINER -> {
                if (keyInfo.width != 22 || keyInfo.height != 48) {
                    keyInfo.width = 22
                    keyInfo.height = 48
                    keyInfo.zoom = 50
                }
            }
        }
    }

    fun translateViewSize(controllerInfo: ControllerInfo?) {
        controllerInfo?.keyboard?.forEach { keyInfo ->
            translateViewSize(keyInfo)
        }
    }
}