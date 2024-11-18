package com.sayx.hm_cloud.dialog

import android.content.DialogInterface
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.sayx.hm_cloud.GameManager
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.adapter.PayInfoAdapter
import com.sayx.hm_cloud.model.PayInfoModel
import com.sayx.hm_cloud.model.PayOrderInfo
import com.sayx.hm_cloud.model.PayOrderStatus
import com.sayx.hm_cloud.mvp.pay.PayContract
import com.sayx.hm_cloud.mvp.pay.PayPresenter
import com.sayx.hm_cloud.utils.CircleTransform
import com.sayx.hm_cloud.utils.EncodingHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/**
 * 二维码支付弹窗
 */
class PayDialog : DialogFragment(), DialogInterface.OnKeyListener, PayContract.IPayView {

    private lateinit var tvName: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvOldPrice: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var ivQRcode: ImageView
    private lateinit var payInfoAdapter: PayInfoAdapter
    private val isProcessing = AtomicBoolean(false)
    private var lastProcessedTime = 0L
    private var payOderListener: PayOderListener? = null

    private lateinit var presenter: PayContract.IPayPresenter

    companion object {
        fun newInstance(): PayDialog {
            val payDialog = PayDialog()
            return payDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnKeyListener(this)
        val view = inflater.inflate(R.layout.dialog_pay, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        presenter = PayPresenter(this)
        // 向web端获取数据
        GameManager.requestPayData()
        tvName = view.findViewById(R.id.tv_name)
        tvTime = view.findViewById(R.id.tv_time)
        ivAvatar = view.findViewById(R.id.iv_avatar)
        ivQRcode = view.findViewById(R.id.iv_qrcode)
        tvPrice = view.findViewById(R.id.tv_price)
        tvOldPrice = view.findViewById(R.id.tv_old_price)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        initRecyclerView(recyclerView)
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        payInfoAdapter = PayInfoAdapter(mutableListOf())
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.bottom = AutoSizeUtils.dp2px(context, 7f)
            }
        })
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = payInfoAdapter

        recyclerView.setOnGenericMotionListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                // 获取摇杆的水平和垂直轴的偏移
                val deltaX = event.getAxisValue(MotionEvent.AXIS_X)
                val deltaY = event.getAxisValue(MotionEvent.AXIS_Y)
                if (abs(deltaY) > abs(deltaX)) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastProcessedTime > 150 && isProcessing.compareAndSet(
                            false,
                            true
                        )
                    ) {
                        lastProcessedTime = currentTime

                        // 如果垂直方向的移动更大，认为是上下移动
                        if (deltaY > 0) {
                            moveSelectionDown() // 向下移动
                        } else {
                            moveSelectionUp() // 向上移动
                        }

                        // 处理完成后，重置标志位
                        isProcessing.set(false)
                        return@setOnGenericMotionListener true
                    }
                }
            }
            return@setOnGenericMotionListener false
        }

        payInfoAdapter.setOnCreateOrderListener(object : PayInfoAdapter.CreateOrderListener {
            override fun createOrder(payInfo: PayInfoModel.PayInfo) {
                val orderInfo = orderInfoList.firstOrNull {
                    it.orderId == payInfo.id
                }
                if (orderInfo == null) {
                    GameManager.createOrder(payInfo.id)
                } else {
                    setQrCode(orderInfo)
                }

                tvOldPrice.text = "¥${payInfo.oldPrice}"
                tvPrice.text = "¥${payInfo.price}"
            }
        })
    }

    private fun moveSelectionUp() {
        payInfoAdapter.moveSelectionUp()
    }

    private fun moveSelectionDown() {
        payInfoAdapter.moveSelectionDown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        this.presenter.stopChecking()
        this.payOderListener = null
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        // 如果是B的话，则返回上一页
        if (event?.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
                dismiss()
            }
            return true
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        // 获取 Dialog 的窗口对象
        val window = dialog?.window
        if (window != null) {
            // 设置宽度和高度为全屏
            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT

            // 设置窗口为全屏显示，去除边距
            params.gravity = Gravity.CENTER
            window.attributes = params

            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        this.payOderListener?.onDismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPayOrderStatusEvent(event: PayOrderStatus) {
        if (event.status == 2) {
            // 代表充值成功
            dismiss()
            this.payOderListener?.onPaySuccess()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPayInfoModelEvent(event: PayInfoModel) {
        val userInfo = event.userInfo
        tvName.text = userInfo.nickName
        tvTime.text = "剩余时长: ${formatTime(userInfo.availableTime)}"
        if (userInfo.avatar.isNotEmpty()) {
            Picasso.get()
                .load(userInfo.avatar)
                .transform(CircleTransform())
                .into(ivAvatar)
        }
        payInfoAdapter.updateList(event.payInfo)

        // 创建订单
        event.payInfo.firstOrNull()?.let { payInfo ->
            val orderInfo = orderInfoList.firstOrNull {
                it.orderId == payInfo.id
            }
            if (orderInfo == null) {
                GameManager.createOrder(payInfo.id)
            }
            tvOldPrice.text = "¥${payInfo.oldPrice}"
            tvPrice.text = "¥${payInfo.price}"
        }
    }

    val orderInfoList = mutableListOf<PayOrderInfo>()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPayOrderInfo(event: PayOrderInfo) {
        val nullOrEmpty = orderInfoList.none {
            it.orderId == event.orderId
        }
        if (nullOrEmpty) {
            orderInfoList.add(event)
        }

        setQrCode(event)

        presenter.startCheckOrderStatus(event.orderNo)
    }

    private fun setQrCode(payOrderInfo: PayOrderInfo) {
        val currentBitmap = (ivQRcode.drawable as? BitmapDrawable)?.bitmap
        if (currentBitmap != null && !currentBitmap.isRecycled) {
            currentBitmap.recycle()
        }

        val qrCode = payOrderInfo.qrCode

        val generateQRCode = EncodingHandler.createQRCode(qrCode, 200)
        generateQRCode?.let { bitmap ->
            ivQRcode.setImageBitmap(bitmap)
        }
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
    }

    override fun checkOrderIsPay(orderNo: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            LogUtils.e("main: ${ThreadUtils.isMainThread()} orderNo: $orderNo")
            GameManager.checkOrderStatus(orderNo)
        }
    }

    fun setPayOderListener(payOderListener: PayOderListener) {
        this.payOderListener = payOderListener;
    }

    interface PayOderListener {
        fun onPaySuccess()

        fun onDismiss()
    }
}