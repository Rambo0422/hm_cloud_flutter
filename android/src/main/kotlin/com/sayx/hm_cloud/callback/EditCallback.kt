package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.KeyInfo

interface EditCallback {
    fun onExitEdit()

    fun onSaveEdit()

    fun onAddKey()

    fun onAddCombineKey()

    fun onAddRouletteKey()

    fun onAddContainerKey()

    fun onRestoreDefault()

    fun onDeleteKey()

    fun onEditName()
}