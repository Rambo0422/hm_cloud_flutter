package com.sayx.hm_cloud.callback

import android.os.IBinder
import com.sayx.hm_cloud.model.KeyInfo

interface KeyEditCallback {

    fun onKeyDelete()

    fun onSaveKey(keyInfo: KeyInfo, windowToken: IBinder)

    fun onCombineKeyEdit(keyInfo: KeyInfo)

    fun onViewHide()
}