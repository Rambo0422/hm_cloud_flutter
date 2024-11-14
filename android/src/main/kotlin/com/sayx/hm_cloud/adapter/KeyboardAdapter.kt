package com.sayx.hm_cloud.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.KeyboardClickListener
import com.sayx.hm_cloud.model.ControllerInfo

class KeyboardAdapter : RecyclerView.Adapter<ItemViewHolder>() {

    var itemList: List<ControllerInfo>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var keyboardClickListener : KeyboardClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_keyboard, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        itemList?.get(position)?.let { info->
            holder.btnDelete.setOnClickListener {
                keyboardClickListener?.onDeleteClick(info, position)
            }
            holder.btnEdit.setOnClickListener {
                keyboardClickListener?.onEditClick(info, position)
            }
            holder.btnUse.setOnClickListener {
                keyboardClickListener?.onUseClick(info, position)
            }
        }
        holder.layoutEmpty.setOnClickListener {
            keyboardClickListener?.onAddClick(position)
        }
    }
}

class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
    val layoutKeyboardInfo: ConstraintLayout = itemView.findViewById(R.id.layout_keyboard_info)
    val layoutEmpty: FrameLayout = itemView.findViewById(R.id.layout_empty)
    val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
    val tvName: TextView = itemView.findViewById(R.id.tv_name)
    val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
    val tvSharerName: TextView = itemView.findViewById(R.id.tv_sharer_name)
    val btnDelete: ImageView = itemView.findViewById(R.id.btn_delete)
    val btnUse: TextView = itemView.findViewById(R.id.btn_use)
    val ivSign: ImageView = itemView.findViewById(R.id.iv_sign)
    val btnEdit: TextView = itemView.findViewById(R.id.btn_edit)
}