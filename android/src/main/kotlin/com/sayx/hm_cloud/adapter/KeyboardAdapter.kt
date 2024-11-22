package com.sayx.hm_cloud.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sayx.hm_cloud.GameManager
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
        if (position < (itemList?.size ?: 0)) {
            val info = itemList!![position]
            holder.layoutKeyboardInfo.visibility = View.VISIBLE
            holder.layoutEmpty.visibility = View.INVISIBLE
            holder.ivIcon.setImageResource(if (info.type == 1) R.drawable.icon_gamepad_item else R.drawable.icon_keyboard_item)
            holder.tvName.text = getItemName(info.name, info.type)
            if (info.isOfficial == true) {
                holder.ivAvatar.setImageResource(R.drawable.icon_official)
                holder.tvSharerName.text = holder.tvSharerName.context.getString(R.string.official_share)
                holder.ivSign.visibility = View.INVISIBLE
                holder.btnEdit.visibility = View.INVISIBLE
                holder.btnDelete.visibility = View.INVISIBLE
            } else {
                // 展示用户头像
                showUserAvatar(holder.ivAvatar)
                // 展示用户名称
                showUserName(holder.tvSharerName)
                holder.ivSign.visibility = View.VISIBLE
                holder.btnEdit.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.VISIBLE
            }
            holder.btnUse.text = holder.btnUse.context.getString(if (info.use == 1) R.string.using else R.string.use)
            holder.btnUse.isSelected = info.use == 1
            holder.layoutKeyboardInfo.isSelected = info.use == 1
            holder.btnEdit.isSelected = false
            holder.btnDelete.setOnClickListener {
                keyboardClickListener?.onDeleteClick(info, position)
            }
            holder.btnEdit.setOnClickListener {
                keyboardClickListener?.onEditClick(info, position)
            }
            holder.btnUse.setOnClickListener {
                keyboardClickListener?.onUseClick(info, position)
            }
        } else {
            holder.layoutKeyboardInfo.visibility = View.INVISIBLE
            holder.layoutEmpty.visibility = View.VISIBLE
            holder.layoutEmpty.setOnClickListener {
                keyboardClickListener?.onAddClick(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showUserName(tvSharerName: TextView) {
        val userName = GameManager.getGameParam()?.userName
        val userMobile = GameManager.getGameParam()?.userMobile
        if (TextUtils.isEmpty(userName)) {
            tvSharerName.text = "${showMobile(userMobile)}分享"
        } else {
            tvSharerName.text = "${userName}分享"
        }
    }

    private fun showMobile(userMobile: String?): String {
        if (TextUtils.isEmpty(userMobile)) {
            return "用户"
        } else {
            return userMobile!!.replaceRange(3, 7, "****")
        }
    }

    private fun showUserAvatar(ivAvatar: ImageView) {
        val userAvatar = GameManager.getGameParam()?.userAvatar
        if (TextUtils.isEmpty(userAvatar)) {
            ivAvatar.setImageResource(R.drawable.img_default_avatar)
        } else {
            Glide.with(ivAvatar.context)
                .load(userAvatar)
                .error(R.drawable.img_default_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar)
        }
    }

    private fun getItemName(name: String?, type: Int): CharSequence? {
        if (TextUtils.isEmpty(name)) {
            return StringBuilder(GameManager.getGameParam()?.gameName ?: "").append(if (type == 1) "手柄按键" else "键鼠按键")
        } else {
            return name
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