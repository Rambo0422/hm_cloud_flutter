package com.sayx.hm_cloud.callback

import com.haima.hmcp.beans.ResolutionInfo
import com.haima.hmcp.beans.VideoDelayInfo
import com.sayx.hm_cloud.constants.AppVirtualOperateType

interface GameSettingChangeListener {

    fun onAddAvailableTime()

    fun onDebugCodeClick()

    fun onControlMethodChange(operateType: AppVirtualOperateType)

    fun onLiveInteractionChange(status: Boolean)

    fun onImageQualityChange(resolution: ResolutionInfo)

    fun onLightChange(light: Int)

    fun onVoiceChange(volume: Int)

    fun onExitGame()

    fun onCustomSettings()

    fun onShowVipDialog()

    fun onHideLayout()

    fun onPlayTimeLack(lack: Boolean)

    fun updateNetSignal(icon: Int)

    fun onShowPlayParty()

    fun onDelayChange(delayInfo: VideoDelayInfo?)
}