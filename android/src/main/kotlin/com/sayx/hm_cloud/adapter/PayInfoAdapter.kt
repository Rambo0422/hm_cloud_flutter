package com.sayx.hm_cloud.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.model.PayInfoModel

class PayInfoAdapter(private val dataSet: MutableList<PayInfoModel.PayInfo>) :
    RecyclerView.Adapter<PayInfoAdapter.ViewHolder>() {

    private var selectedPosition = 0
    private var mCreateOrderListener: CreateOrderListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.tv_title)
        val layoutContent: View = view.findViewById(R.id.layout_content)
        val tvDesc: TextView = view.findViewById(R.id.tv_desc)
        val tvPrice1: TextView = view.findViewById(R.id.tv_price1)
        val tvPrice2: TextView = view.findViewById(R.id.tv_price2)
        val ivArrow: ImageView = view.findViewById(R.id.iv_arrow)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_pay_info, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val payInfo = dataSet[position]
        viewHolder.titleView.text = payInfo.name
        viewHolder.tvDesc.text = payInfo.subName
        viewHolder.tvPrice1.text = payInfo.price.toString()
        viewHolder.tvPrice2.apply {
            text = "原价:${payInfo.oldPrice}"
            paint.apply {
                isAntiAlias = true
                flags = Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        if (selectedPosition == position) {
            viewHolder.layoutContent.setBackgroundResource(R.drawable.shape_pay_info_select)
            viewHolder.ivArrow.visibility = View.VISIBLE
        } else {
            viewHolder.layoutContent.setBackgroundResource(R.drawable.shape_pay_info_default)
            viewHolder.ivArrow.visibility = View.INVISIBLE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        for (payload in payloads) {
            if (payload == 1) {
                holder.layoutContent.setBackgroundResource(R.drawable.shape_pay_info_default)
                holder.ivArrow.visibility = View.INVISIBLE
            } else if (payload == 2) {
                holder.layoutContent.setBackgroundResource(R.drawable.shape_pay_info_select)
                holder.ivArrow.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = dataSet.size

    fun updateList(payInfo: List<PayInfoModel.PayInfo>) {
        dataSet.clear()
        dataSet.addAll(payInfo)
        notifyDataSetChanged()
    }

    fun moveSelectionUp() {
        if (selectedPosition > 0) {
            notifyItemChanged(selectedPosition, 1)
            selectedPosition--
            notifyItemChanged(selectedPosition, 2)
            mCreateOrderListener?.createOrder(dataSet[selectedPosition])
        }
    }

    fun moveSelectionDown() {
        if (selectedPosition < getItemCount() - 1) {
            notifyItemChanged(selectedPosition, 1)
            selectedPosition++
            notifyItemChanged(selectedPosition, 2)
            mCreateOrderListener?.createOrder(dataSet[selectedPosition])
        }
    }

    interface CreateOrderListener {
        fun createOrder(orderInfo: PayInfoModel.PayInfo)
    }

    fun setOnCreateOrderListener(m: CreateOrderListener) {
        this.mCreateOrderListener = m
    }

    fun data(): MutableList<PayInfoModel.PayInfo> {
        return dataSet
    }
}