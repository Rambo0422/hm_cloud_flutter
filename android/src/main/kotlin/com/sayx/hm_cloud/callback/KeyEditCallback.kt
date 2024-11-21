package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface KeyEditCallback {

    fun onKeyDelete()

    fun onSaveKey(keyInfo: KeyInfo)

    fun onCombineKeyEdit(keyInfo: KeyInfo)
}