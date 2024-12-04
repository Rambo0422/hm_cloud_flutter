package com.sayx.hm_cloud.constants

import com.haima.hmcp.beans.HMInputOpData
import com.sayx.hm_cloud.R

var controllerStatus: ControllerStatus = ControllerStatus.Normal

// 操作按键状态
enum class ControllerStatus {
    // 正常状态，可执行操作
    Normal,

    // 编辑状态，可点击进行编辑按键大小，透明度，触发效果，名称，可移动换位置
    Edit,

    // 组合状态，点击添加组合按键
    Combine,

    // 轮盘状态，点击添加轮盘按键
    Roulette
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

    // 收纳功能键
    const val KEY_CONTAINER = "kb-container"

    // 射击功能键
    const val KEY_SHOOT = "kb-shoot"
}

val maps : List<Pair<String, Int>> by lazy {
    listOf(
        "map1" to R.drawable.map1,
        "map2" to R.drawable.map2,
        "map3" to R.drawable.map3,
        "map4" to R.drawable.map4,
        "map5" to R.drawable.map5,
        "map6" to R.drawable.map6,
        "map7" to R.drawable.map7,
        "map8" to R.drawable.map8,
        "map9" to R.drawable.map9,
        "map10" to R.drawable.map10,
        "map11" to R.drawable.map11,
        "map12" to R.drawable.map12,
        "map13" to R.drawable.map13,
        "map14" to R.drawable.map14,
        "map15" to R.drawable.map15,
        "map16" to R.drawable.map16,
        "map17" to R.drawable.map17,
        "map18" to R.drawable.map18,
        "map19" to R.drawable.map19,
        "map20" to R.drawable.map20,
        "map21" to R.drawable.map21,
        "map22" to R.drawable.map22,
        "map23" to R.drawable.map23,
        "map24" to R.drawable.map24,
        "map25" to R.drawable.map25,
        "map26" to R.drawable.map26,
        "map27" to R.drawable.map27,
        "map28" to R.drawable.map28,
        "map29" to R.drawable.map29,
        "map30" to R.drawable.map30,
        "map31" to R.drawable.map31,
        "map32" to R.drawable.map32,
        "map33" to R.drawable.map33,
        "map34" to R.drawable.map34,
        "map35" to R.drawable.map35,
        "map36" to R.drawable.map36,
        "map37" to R.drawable.map37,
        "map38" to R.drawable.map38,
        "map39" to R.drawable.map39,
        "map40" to R.drawable.map40,
        "map41" to R.drawable.map41,
        "map42" to R.drawable.map42,
        "map43" to R.drawable.map43,
        "map44" to R.drawable.map44,
        "map45" to R.drawable.map45,
        "map46" to R.drawable.map46,
        "map47" to R.drawable.map47,
        "map48" to R.drawable.map48,
        "map49" to R.drawable.map49,
        "map50" to R.drawable.map50,
        "map51" to R.drawable.map51,
        "map52" to R.drawable.map52,
        "map53" to R.drawable.map53,
        "map54" to R.drawable.map54,
        "map55" to R.drawable.map55,
        "map56" to R.drawable.map56,
        "map57" to R.drawable.map57,
        "map58" to R.drawable.map58,
        "map59" to R.drawable.map59,
        "map60" to R.drawable.map60,
        "map61" to R.drawable.map61,
        "map62" to R.drawable.map62,
        "map63" to R.drawable.map63,
        "map64" to R.drawable.map64,
        "map65" to R.drawable.map65,
        "map66" to R.drawable.map66,
        "map67" to R.drawable.map67,
        "map68" to R.drawable.map68,
        "map69" to R.drawable.map69,
        "map70" to R.drawable.map70,
        "map71" to R.drawable.map71,
        "map72" to R.drawable.map72,
        "map73" to R.drawable.map73,
        "map74" to R.drawable.map74,
        "map75" to R.drawable.map75,
        "map76" to R.drawable.map76,
        "map77" to R.drawable.map77,
        "map78" to R.drawable.map78,
        "map79" to R.drawable.map79,
        "map80" to R.drawable.map80,
        "map81" to R.drawable.map81,
        "map82" to R.drawable.map82,
        "map83" to R.drawable.map83,
        "map84" to R.drawable.map84,
        "map85" to R.drawable.map85,
        "map86" to R.drawable.map86,
        "map87" to R.drawable.map87,
        "map88" to R.drawable.map88,
        "map89" to R.drawable.map89,
        "map90" to R.drawable.map90,
        "map91" to R.drawable.map91,
        "map92" to R.drawable.map92,
        "map93" to R.drawable.map93,
        "map94" to R.drawable.map94,
        "map95" to R.drawable.map95,
    )
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