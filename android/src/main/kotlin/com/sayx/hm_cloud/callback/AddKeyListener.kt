package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface AddKeyListener {
    fun onAddKey(keyInfo: KeyInfo)

    fun onUpdateKey()
}