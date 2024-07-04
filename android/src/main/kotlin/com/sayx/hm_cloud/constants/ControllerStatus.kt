package com.sayx.hm_cloud.constants

import com.haima.hmcp.beans.HMInputOpData

var controllerStatus: ControllerStatus = ControllerStatus.Normal

// 操作按键状态
enum class ControllerStatus {
    // 正常状态，可执行操作
    Normal,

    // 编辑状态，可点击进行编辑按键大小，透明度，触发效果，名称，可移动换位置
    Edit,

    // 组合状态，点击添加组合/轮盘
    Combine
}

object KeyType {
    // 手柄右摇杆
    const val ROCKER_RIGHT = "xbox-rock-rt"

    // 手柄LT,LB,RT,RB按钮
    const val GAMEPAD_SQUARE = "xbox-square"

    // 手柄LS,RS按钮
    const val GAMEPAD_ROUND_SMALL = "xbox-round-small"

    // 手柄A，B，X，Y按钮
    const val GAMEPAD_ROUND_MEDIUM = "xbox-round-medium"

    // 手柄选择，菜单按钮
    const val GAMEPAD_ELLIPTIC = "xbox-elliptic"

    // 手柄左摇杆
    const val ROCKER_LEFT = "xbox-rock-lt"

    // 手柄十字方向盘
    const val ROCKER_CROSS = "xbox-cross"

    // 键盘鼠标左键
    const val KEYBOARD_MOUSE_LEFT = "kb-mouse-lt"

    // 键盘鼠标右键
    const val KEYBOARD_MOUSE_RIGHT = "kb-mouse-rt"

    // 键盘鼠标中键
    const val KEYBOARD_MOUSE_MIDDLE = "kb-mouse-md"

    // 键盘鼠标上滑
    const val KEYBOARD_MOUSE_UP = "kb-mouse-up"

    // 键盘鼠标下滑
    const val KEYBOARD_MOUSE_DOWN = "kb-mouse-down"

    // 键盘字母摇杆
    const val ROCKER_LETTER = "kb-rock-letter"

    // 键盘箭头摇杆
    const val ROCKER_ARROW = "kb-rock-arrow"

    // 键盘按键
    const val KEYBOARD_KEY = "kb-round"

    // 手柄组合键
    const val GAMEPAD_COMBINE = "xbox-combination"

    // 手柄轮盘键
    const val GAMEPAD_ROULETTE = "xbox-roulette"

    // 键盘组合键
    const val KEY_COMBINE = "kb-combination"

    // 键盘轮盘键
    const val KEY_ROULETTE = "kb-roulette"
}

object KeyConstants {
    val keyControl: HashMap<Int, String> = hashMapOf(
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkBack.value to "Back",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkTab.value to "Tab",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkReturn.value to "Enter",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkShift.value to "Shift",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkControl.value to "Ctrl",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkMenu.value to "Alt",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkCapital.value to "Caps",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkEscape.value to "Esc",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkSpace.value to "Space",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkPrior.value to "PgUp",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNext.value to "PaDn",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkEnd.value to "End",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkHome.value to "Home",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value to "←",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value to "↑",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value to "→",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value to "↓",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkInsert.value to "Ins",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDelete.value to "Del",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey0.value to "0",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey1.value to "1",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey2.value to "2",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey3.value to "3",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey4.value to "4",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey5.value to "5",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey6.value to "6",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey7.value to "7",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey8.value to "8",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKey9.value to "9",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value to "A",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyB.value to "B",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyC.value to "C",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value to "D",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyE.value to "E",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyF.value to "F",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyG.value to "G",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyH.value to "H",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyI.value to "I",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyJ.value to "J",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyK.value to "K",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyL.value to "L",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyM.value to "M",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyN.value to "N",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyO.value to "O",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyP.value to "P",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyQ.value to "Q",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyR.value to "R",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value to "S",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyT.value to "T",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyU.value to "U",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyV.value to "V",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value to "W",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyX.value to "X",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyY.value to "Y",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyZ.value to "Z",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF1.value to "F1",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF2.value to "F2",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF3.value to "F3",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF4.value to "F4",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF5.value to "F5",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF6.value to "F6",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF7.value to "F7",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF8.value to "F8",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF9.value to "F9",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF10.value to "F10",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF11.value to "F11",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkF12.value to "F12",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem1.value to ";",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOemPlus.value to "=",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOemComma.value to ",",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOemMinus.value to "-",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOemPeriod.value to ".",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem2.value to "/",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem3.value to "`",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem4.value to "[",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem5.value to "\\",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem6.value to "]",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOem7.value to "‘"
    )

    val keyNumber: HashMap<Int, String> = hashMapOf(
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad0.value to "0",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad1.value to "1",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad2.value to "2",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad3.value to "3",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad4.value to "4",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad5.value to "5",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad6.value to "6",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad7.value to "7",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad8.value to "8",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkNumpad9.value to "9",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDivide.value to "/",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkMultiply.value to "*",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkSubtract.value to "-",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkAdd.value to "+",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkSeparator.value to "Enter",
        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkOemPeriod.value to "."
    )
}

val stickKeyMaps: HashMap<Int, Boolean> by lazy {
    HashMap<Int, Boolean>().also {
        // 方向
        it[GameConstants.rockerUp] = false
        it[GameConstants.rockerDown] = false
        it[GameConstants.rockerLeft] = false
        it[GameConstants.rockerUpLeft] = false
        it[GameConstants.rockerDownLeft] = false
        it[GameConstants.rockerRight] = false
        it[GameConstants.rockerUpRight] = false
        it[GameConstants.rockerDownRight] = false

        // 按钮(除LT/RT，LT/RT不支持连按)
        it[GameConstants.gamepadMenuValue] = false
        it[GameConstants.gamepadSettingValue] = false

        it[GameConstants.gamepadButtonAValue] = false
        it[GameConstants.gamepadButtonBValue] = false
        it[GameConstants.gamepadButtonXValue] = false
        it[GameConstants.gamepadButtonYValue] = false

        it[GameConstants.gamepadButtonLSValue] = false
        it[GameConstants.gamepadButtonLBValue] = false
        it[GameConstants.gamepadButtonRSValue] = false
        it[GameConstants.gamepadButtonRBValue] = false
    }
}



fun resetDirectionMap(value: Int) {
    stickKeyMaps[GameConstants.rockerUp] = value == GameConstants.rockerUp
    stickKeyMaps[GameConstants.rockerDown] = value == GameConstants.rockerDown
    stickKeyMaps[GameConstants.rockerLeft] = value == GameConstants.rockerLeft
    stickKeyMaps[GameConstants.rockerUpLeft] = value == GameConstants.rockerUpLeft
    stickKeyMaps[GameConstants.rockerDownLeft] = value == GameConstants.rockerDownLeft
    stickKeyMaps[GameConstants.rockerRight] = value == GameConstants.rockerRight
    stickKeyMaps[GameConstants.rockerUpRight] = value == GameConstants.rockerUpRight
    stickKeyMaps[GameConstants.rockerDownRight] = value == GameConstants.rockerDownRight
}

fun calStickValue(): Int {
    var value = 0
    stickKeyMaps.entries.forEach { entry ->
        if (entry.value) {
            value = value or entry.key
        }
    }
    return value
}