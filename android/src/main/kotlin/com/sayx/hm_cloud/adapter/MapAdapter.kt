package com.sayx.hm_cloud.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.callback.MapClickListener
import com.sayx.hm_cloud.constants.maps

class MapAdapter(private val itemClickListener: MapClickListener) : RecyclerView.Adapter<MapHolder>() {

    var selectIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_map, parent, false)
        return MapHolder(itemView)
    }

    override fun getItemCount(): Int {
        return maps.size
    }

    override fun onBindViewHolder(holder: MapHolder, position: Int) {
        val drawable = maps[holder.absoluteAdapterPosition].second
        holder.ivMap.setImageResource(drawable)
        holder.ivMap.isSelected = selectIndex == holder.absoluteAdapterPosition
        holder.itemView.setOnClickListener {
            notifyItemChanged(selectIndex)
            selectIndex = position
            notifyItemChanged(selectIndex)
            itemClickListener.onClick(holder.absoluteAdapterPosition)
        }
    }
}

class MapHolder(itemView: View) : ViewHolder(itemView) {
    val ivMap: ImageView = itemView.findViewById(R.id.iv_map)
}