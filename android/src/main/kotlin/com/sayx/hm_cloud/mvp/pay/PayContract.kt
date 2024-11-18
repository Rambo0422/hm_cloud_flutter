package com.sayx.hm_cloud.mvp.pay


class PayContract {
    interface IPayPresenter {
        fun startCheckOrderStatus(orderNo: String)

        fun stopChecking()
    }

    interface IPayView {
        fun checkOrderIsPay(orderNo: String)
    }
}
