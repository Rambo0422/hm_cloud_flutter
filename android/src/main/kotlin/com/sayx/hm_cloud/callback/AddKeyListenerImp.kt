package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

abstract class AddKeyListenerImp : AddKeyListener {
    override fun onAddKey(keyInfo: KeyInfo) {}

    override fun rouAddData(list: List<KeyInfo>?) {}

    override fun rouRemoveData(list: List<KeyInfo>?) {}
}