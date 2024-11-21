package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface HideListener {
    fun onHide(keyInfo: KeyInfo?)
}