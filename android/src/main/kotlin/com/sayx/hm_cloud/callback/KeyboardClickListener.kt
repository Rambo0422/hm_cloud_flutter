package com.sayx.hm_cloud.callback

import com.sayx.hm_cloud.model.ControllerInfo

interface KeyboardClickListener {

    /**
     * 点击添加
     */
    fun onAddClick(position: Int)

    /**
     * 点击编辑
     */
    fun onEditClick(info: ControllerInfo, position: Int)

    /**
     * 点击删除
     */
    fun onDeleteClick(info: ControllerInfo, position: Int)

    /**
     * 点击使用
     */
    fun onUseClick(info: ControllerInfo, position: Int)
}