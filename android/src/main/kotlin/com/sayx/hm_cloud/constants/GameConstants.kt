package com.sayx.hm_cloud.constants

import com.blankj.utilcode.util.LogUtils
import com.haima.hmcp.beans.HMInputOpData
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.callback.OnKeyEventListener
import com.sayx.hm_cloud.callback.OnRockerOperationListener
import com.sayx.hm_cloud.model.Direction
import com.sayx.hm_cloud.model.KeyInfo

object GameConstants {

    const val settingsLeft = "settingsLeft"
    const val settingsTop = "settingsTop"

    // 是否展示游戏设置按钮引导层
    const val showGuide = "showGuide"
    // 是否开启振动
    const val vibrable = "vibrable"
    // 是否开启声音
    const val volumeSwitch = "volumeSwitch"
    // 鼠标灵敏度
    const val sensitivity = "sensitivity"
    // 按键透明度
    const val keyOpacity = "keyOpacity"

    // 操作键位配置类型
    const val gamepadConfig = 1
    const val keyboardConfig = 2

    // 默认操作方式
    const val keyboardControl = 1
    const val gamepadControl = 2

    const val mouseUp = 1
    const val mouseDown = -1
    const val mouseDefault = 0

    // 手柄十字摇杆8个方向
    // 中
    const val rockerCenter = 0

    // 上
    const val rockerUp = 1

    // 下
    const val rockerDown = 2

    // 左
    const val rockerLeft = 4

    // 右上
    const val rockerUpLeft = 5

    // 左下
    const val rockerDownLeft = 6

    // 右
    const val rockerRight = 8

    // 右上
    const val rockerUpRight = 9

    // 右下
    const val rockerDownRight = 10

    // 菜单
    const val gamepadMenuValue = 16

    // 设置
    const val gamepadSettingValue = 32

    const val gamepadButtonAValue = 4096
    const val gamepadButtonBValue = 8192
    const val gamepadButtonXValue = 16384
    const val gamepadButtonYValue = 32768

    const val gamepadButtonLSValue = 64
    const val gamepadButtonLBValue = 256
    const val gamepadButtonRSValue = 128
    const val gamepadButtonRBValue = 512

    const val gamepadButtonTValue = 255

    const val rockerOffsetMul = 32767
}

// 操作展示类型
enum class AppVirtualOperateType {
    // 空，不展示
    NONE,

    // 展示键鼠
    APP_KEYBOARD,

    // 展示手柄
    APP_STICK_XBOX,
}



abstract class OnRockerOperationListenerImp : OnRockerOperationListener {
    // 摇杆移动
    override fun onRockerMove(keyInfo: KeyInfo, moveX: Float, moveY: Float) {
        val pointX: Int = (moveX * GameConstants.rockerOffsetMul).toInt()
        val pointY: Int = (moveY * GameConstants.rockerOffsetMul).toInt()
        val inputOp = HMInputOpData()
        if (keyInfo.type == KeyType.ROCKER_LEFT) {
            val oneInputOpData = HMInputOpData.HMOneInputOPData()
            oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbLx
            oneInputOpData.value = pointX
            inputOp.opListArray.add(oneInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
            val secondInputOpData = HMInputOpData.HMOneInputOPData()
            secondInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbLy
            secondInputOpData.value = pointY
            inputOp.opListArray.add(secondInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
        } else if (keyInfo.type == KeyType.ROCKER_RIGHT) {
            val oneInputOpData = HMInputOpData.HMOneInputOPData()
            oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbRx
            oneInputOpData.value = pointX
            inputOp.opListArray.add(oneInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
            val secondInputOpData = HMInputOpData.HMOneInputOPData()
            secondInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputThumbRy
            secondInputOpData.value = pointY
            inputOp.opListArray.add(secondInputOpData)
//            LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
        }
        GameManager.gameView?.sendCustomKeycode(inputOp)
//        val result = GameManager.gameView?.sendCustomKeycode(inputOp)
//        LogUtils.d("key:${keyInfo.type}, result:$result")
    }

    // 字母摇杆：4个方向 WASD
    // 箭头摇杆：4个放向 上左下右
    // 十字摇杆：8个放向 上左下右，左上，左下，右上，右下
    override fun onRockerDirection(keyInfo: KeyInfo, direction: Direction?) {
        val inputOp = HMInputOpData()
        when (direction) {
            // 左
            Direction.DIRECTION_LEFT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 左 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        // 左 按
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerLeft)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 上
            Direction.DIRECTION_UP -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 上 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 上 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerUp)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 右
            Direction.DIRECTION_RIGHT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerRight)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 下
            Direction.DIRECTION_DOWN -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 放
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerDown)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 左上
            Direction.DIRECTION_UP_LEFT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 左 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 左 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerUpLeft)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 右上
            Direction.DIRECTION_UP_RIGHT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp = getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 下 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 左 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 右 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 上 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 左 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 下 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerUpRight)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 下左
            Direction.DIRECTION_DOWN_LEFT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 右 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 左 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 右 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerDownLeft)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 下右
            Direction.DIRECTION_DOWN_RIGHT -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 右 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 上 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 左 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        // 下 按
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        // 右 按
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        // 左 放
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        // 上 放
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerDownRight)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }
            // 中
            Direction.DIRECTION_CENTER -> {
                when (keyInfo.type) {
                    KeyType.ROCKER_LETTER -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyW.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyA.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyS.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyKeyD.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    KeyType.ROCKER_ARROW -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        oneInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkUp.value)
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                        val secondInputOpData = HMInputOpData.HMOneInputOPData()
                        secondInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        secondInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkLeft.value)
                        inputOp.opListArray.add(secondInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${secondInputOpData.inputOp}, value:${secondInputOpData.value}")
                        val thirdInputOpData = HMInputOpData.HMOneInputOPData()
                        thirdInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        thirdInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkRight.value)
                        inputOp.opListArray.add(thirdInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${thirdInputOpData.inputOp}, value:${thirdInputOpData.value}")
                        val fourInputOpData = HMInputOpData.HMOneInputOPData()
                        fourInputOpData.inputState = HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        fourInputOpData.inputOp =
                            getInputOp(HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpKeyVkDown.value)
                        inputOp.opListArray.add(fourInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${fourInputOpData.inputOp}, value:${fourInputOpData.value}")
                    }

                    else -> {
                        val oneInputOpData = HMInputOpData.HMOneInputOPData()
                        oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                        resetDirectionMap(GameConstants.rockerCenter)
                        oneInputOpData.value = calStickValue()
                        inputOp.opListArray.add(oneInputOpData)
//                        LogUtils.d("key:${keyInfo.type}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}")
                    }
                }
            }

            else -> {}
        }
        GameManager.gameView?.sendCustomKeycode(inputOp)
//        val result = GameManager.gameView?.sendCustomKeycode(inputOp)
//        LogUtils.d("key:${keyInfo.type}, result:$result")
    }
}

abstract class OnKeyEventListenerImp : OnKeyEventListener {
    // 按钮按压
    override fun onButtonPress(keyInfo: KeyInfo, press: Boolean) {
        when (keyInfo.type) {
            // RS/LS, X,Y,A,B,setting,menu
            KeyType.GAMEPAD_ELLIPTIC, KeyType.GAMEPAD_ROUND_MEDIUM, KeyType.GAMEPAD_ROUND_SMALL -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                stickKeyMaps[keyInfo.inputOp] = press
                oneInputOpData.value = calStickValue()
                inputOp.opListArray.add(oneInputOpData)
                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // LT/RT, LB/RB
            KeyType.GAMEPAD_SQUARE -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                // LT/RT -> Trigger, LB/RB -> 1024
                oneInputOpData.inputOp =
                    if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                        keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                    )
                        getInputOp(keyInfo.inputOp) else
                        HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                // LT/RT -> 255, LB/RB -> inputOp
                oneInputOpData.value =
                    if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                        keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                    ) {
                        if (press) GameConstants.gamepadButtonTValue else 0
                    } else {
                        stickKeyMaps[keyInfo.inputOp] = press
                        calStickValue()
                    }

                inputOp.opListArray.add(oneInputOpData)
                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // 键盘按键，鼠标左中右键
            KeyType.KEYBOARD_KEY, KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE, KeyType.KEY_SHOOT -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputState = if (press) HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown else
                    HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                oneInputOpData.inputOp = getInputOp(keyInfo.inputOp)
                inputOp.opListArray.add(oneInputOpData)
                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.inputState}, result:$result")
            }
            // 鼠标滑轮向上短触发
            KeyType.KEYBOARD_MOUSE_UP -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputOp = getInputOp(keyInfo.inputOp)
                oneInputOpData.value = if (press) GameConstants.mouseUp else GameConstants.mouseDefault
                inputOp.opListArray.add(oneInputOpData)
                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // 鼠标滑轮向下
            KeyType.KEYBOARD_MOUSE_DOWN -> {
                val inputOp = HMInputOpData()
                val oneInputOpData = HMInputOpData.HMOneInputOPData()
                oneInputOpData.inputOp = getInputOp(keyInfo.inputOp)
                oneInputOpData.value = if (press) GameConstants.mouseDown else GameConstants.mouseDefault
                inputOp.opListArray.add(oneInputOpData)
                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
                LogUtils.d("key:${keyInfo.text}, inputOp:${oneInputOpData.inputOp}, value:${oneInputOpData.value}, result:$result")
            }
            // 组合键(键鼠)
            KeyType.KEY_COMBINE -> {
                val inputOp = HMInputOpData()
                val text = StringBuilder()
                keyInfo.composeArr?.let {
                    it.forEachIndexed { index, keyInfo ->
                        val inputOpData = HMInputOpData.HMOneInputOPData()
                        inputOpData.inputState = if (press) HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateDown else
                            HMInputOpData.HMOneInputOPData_InputState.HMOneInputOPData_InputState_OpStateUp
                        inputOpData.inputOp = getInputOp(keyInfo.inputOp)
                        inputOp.opListArray.add(inputOpData)
                        text.append("$keyInfo")
                        if (index != it.size - 1) {
                            text.append(", ")
                        }
                    }
                }
                GameManager.gameView?.sendCustomKeycode(inputOp)
//                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
//                LogUtils.d("key:${keyInfo.text.json}, inputOpList:[$text.json], result:$result")
            }
            // 组合键(手柄)
            KeyType.GAMEPAD_COMBINE -> {
                val inputOp = HMInputOpData()
                val text = StringBuilder()
                keyInfo.composeArr?.let {
                    it.forEachIndexed { index, keyInfo ->
                        val inputOpData = HMInputOpData.HMOneInputOPData()
                        if (keyInfo.type == KeyType.GAMEPAD_SQUARE) {
                            inputOpData.inputOp =
                                if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                                    keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                                )
                                    getInputOp(keyInfo.inputOp) else
                                    HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                            // LT/RT -> 255, LB/RB -> inputOp
                            inputOpData.value =
                                if (keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputRightTrigger.value ||
                                    keyInfo.inputOp == HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputLeftTrigger.value
                                ) {
                                    if (press) GameConstants.gamepadButtonTValue else 0
                                } else {
                                    stickKeyMaps[keyInfo.inputOp] = press
                                    calStickValue()
                                }
                        } else {
                            inputOpData.inputOp = HMInputOpData.HMOneInputOPData_InputOP.HMOneInputOPData_InputOP_OpXinputButtons
                            stickKeyMaps[keyInfo.inputOp] = press
                            inputOpData.value = calStickValue()
                        }
                        inputOp.opListArray.add(inputOpData)
                        text.append("$keyInfo")
                        if (index != it.size - 1) {
                            text.append(", ")
                        }
                    }
                }
                GameManager.gameView?.sendCustomKeycode(inputOp)
//                val result = GameManager.gameView?.sendCustomKeycode(inputOp)
//                LogUtils.d("key:${keyInfo.text.json}, inputOpList:[$text.json], result:$result")
            }

            else -> {
                LogUtils.d("key:${keyInfo.type}, press:${press}")
            }
        }
    }
}

private fun getInputOp(value: Int): HMInputOpData.HMOneInputOPData_InputOP? {
    return HMInputOpData.HMOneInputOPData_InputOP.entries.findLast { inputOp -> inputOp.value == value }
}