package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface AddKeyListener {
    fun onAddKey(keyInfo: KeyInfo)

    fun rouAddData(list : List<KeyInfo>?)

    fun rouRemoveData(list : List<KeyInfo>?)

    fun onKeyAdd(keyInfo: KeyInfo) {

    }

    fun onKeyRemove(keyInfo: KeyInfo) {

    }
}