package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface OnKeyEventListener {

    fun onButtonPress(keyInfo: KeyInfo, press: Boolean)
}