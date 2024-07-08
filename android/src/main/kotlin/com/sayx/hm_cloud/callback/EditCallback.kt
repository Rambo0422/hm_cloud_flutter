package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface EditCallback {
    fun onExitEdit()

    fun onSaveEdit()

    fun onAddKey()

    fun onAddCombineKey()

    fun onAddRouletteKey()

    fun onRestoreDefault()

    fun onAddKeySize()

    fun onReduceKeySize()

    fun onAddKeyOpacity()

    fun onReduceKeyOpacity()

    fun onTextChange()

    fun onDeleteKey()

    fun onEditCombine(keyInfo: KeyInfo)
}