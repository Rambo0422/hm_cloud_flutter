package com.sayx.hm_cloud.mvp.pay

import com.sayx.hm_cloud.model.PayInfoModel


class PayContract {
    interface IPayPresenter {
        fun startCheckOrderStatus()

        fun stopChecking()
    }

    interface IPayView {
        fun checkOrderIsPay(orderNo: String, price: Number)

        fun getPayInfoList(): MutableList<PayInfoModel.PayInfo>
    }
}
