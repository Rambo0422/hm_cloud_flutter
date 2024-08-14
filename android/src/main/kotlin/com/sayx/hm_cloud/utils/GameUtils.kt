package com.sayx.hm_cloud.utils

import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.view.InputDevice
import android.view.InputEvent
import com.google.gson.Gson
import com.sayx.hm_cloud.model.AccountInfo

object GameUtils {

    // 是否是外接带游戏杆的手柄
    fun isGamePadController(inputDevice: InputDevice): Boolean {
        // 虚拟设备
        if (inputDevice.isVirtual) {
            return false
        }
        // 非外接
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !inputDevice.isExternal) {
            return false
        }
        return (inputDevice.sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD &&
                (inputDevice.sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }

    // 是否是外接字母键盘
    fun isKeyBoardController(inputDevice: InputDevice): Boolean {
        // 虚拟设备
        if (inputDevice.isVirtual) {
            return false
        }
        // 非外接
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !inputDevice.isExternal) {
            return false
        }
        return (inputDevice.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD &&
                inputDevice.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
    }

    // 是否是外接鼠标控制器
    fun isMouseController(inputDevice: InputDevice): Boolean {
        // 虚拟设备
        if (inputDevice.isVirtual) {
            return false
        }
        // 非外接
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !inputDevice.isExternal) {
            return false
        }
        return (inputDevice.sources and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE &&
                (inputDevice.sources and (InputDevice.SOURCE_KEYBOARD or InputDevice.SOURCE_JOYSTICK)) == 0
    }

    // 是否是外接手柄操作
    fun isGamePadEvent(motionEvent: InputEvent): Boolean {
        return (motionEvent.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (motionEvent.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
    }

    // 是否是外接键盘操作
    fun isKeyBoardEvent(motionEvent: InputEvent): Boolean {
        return (motionEvent.source and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
                && motionEvent.source == InputDevice.KEYBOARD_TYPE_ALPHABETIC
    }

    // 是否是外鼠标操作
    fun isMouseEvent(event: InputEvent): Boolean {
        return (event.source and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE
    }

    // 配置数据处理
    fun getProtoData(gson: Gson, userId: String?, gameId: String?, priority: Int): String {
        val map = mutableMapOf<String, Any?>()
        val type = if (priority > 46) 2 else 1
        map["uid"] = userId
        map["gameId"] = gameId
        map["type"] = type
        val json = gson.toJson(map)
        val encode = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
        LogUtils.logD("getProtoData-->json:$json,\n$encode")
        return encode
    }

    fun getStringData(accountInfo: AccountInfo): Map<String, String> {
        val params = if (!TextUtils.isEmpty(accountInfo.account) && !TextUtils.isEmpty(accountInfo.password)) {
            "--platform=${accountInfo.platform} --userid=${accountInfo.userId} --gameid=${accountInfo.gameId} --account=${accountInfo.account} --password=${accountInfo.password} --platform_game_id=${accountInfo.platformGameId}"
        } else if (!TextUtils.isEmpty(accountInfo.key) && !TextUtils.isEmpty(accountInfo.token)) {
            "--platform=${accountInfo.platform} --userid=${accountInfo.userId} --gameid=${accountInfo.gameId} --token=${accountInfo.token} --key=${accountInfo.key} --platform_game_id=${accountInfo.platformGameId} --mode=1"
        } else {
            "--platform=${accountInfo.platform} --userid=${accountInfo.userId} --gameid=${accountInfo.gameId} --platform_game_id=${accountInfo.platformGameId}"
        }
        return hashMapOf("StartAppParams" to params)
    }
}