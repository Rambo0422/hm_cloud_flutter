package com.sayx.hm_cloud.mvp.pay

import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger

class PayPresenter(val payView: PayContract.IPayView) : PayContract.IPayPresenter {

    // 开始倒计时5s请求订单是否接口
    private var timer: Timer? = null // 定时器
    private val MAX_ATTEMPTS: Int = 10 // 最大尝试次数
    private val INTERVAL: Long = 5000L // 每隔 5 秒执行一次

    override fun stopChecking() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    override fun startCheckOrderStatus() {
        stopChecking()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // 遍历四个条目
                val payInfoList = payView.getPayInfoList()
                for (payInfo in payInfoList) {
                    val orderNo = payInfo.orderNo
                    val price = payInfo.price
                    if (!TextUtils.isEmpty(orderNo)) {
                        payView.checkOrderIsPay(orderNo, price)
                    }
                }
            }
        }, 1000, INTERVAL)
    }
}