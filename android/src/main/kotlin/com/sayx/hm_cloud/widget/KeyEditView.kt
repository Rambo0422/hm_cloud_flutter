package com.sayx.hm_cloud.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.sayx.hm_cloud.R
import com.sayx.hm_cloud.adapter.MapAdapter
import com.sayx.hm_cloud.callback.KeyEditCallback
import com.sayx.hm_cloud.callback.MapClickListener
import com.sayx.hm_cloud.callback.TextWatcherImp
import com.sayx.hm_cloud.constants.KeyConstants
import com.sayx.hm_cloud.constants.KeyType
import com.sayx.hm_cloud.constants.maps
import com.sayx.hm_cloud.databinding.ViewKeyEditBinding
import com.sayx.hm_cloud.model.KeyInfo
import com.sayx.hm_cloud.utils.AppSizeUtils

class KeyEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    lateinit var mKeyInfo: KeyInfo

    private var dataBinding: ViewKeyEditBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_key_edit, this, true)

    var callback: KeyEditCallback? = null

    private var previewView: View? = null

    init {
        initView()
    }

    private fun initView() {
        dataBinding.btnExitEdit.setOnClickListener {
            visibility = GONE
        }
        dataBinding.btnDeleteKey.setOnClickListener {
            visibility = GONE
            callback?.onKeyDelete()
        }
        dataBinding.btnSaveEdit.setOnClickListener {
            val text = dataBinding.etKeyName.text ?: ""
            if (text.length > 4) {
                ToastUtils.showLong("按键名称建议为1～4个字符")
                return@setOnClickListener
            }
            mKeyInfo.text = text.toString()
            visibility = GONE
            callback?.onSaveKey(mKeyInfo, dataBinding.etKeyName.windowToken)
        }
        dataBinding.etKeyName.addTextChangedListener(object : TextWatcherImp() {
            @SuppressLint("DefaultLocale")
            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                mKeyInfo.text = s?.toString() ?: ""
                s?.toString()?.let {
                    dataBinding.tvCount.text = String.format("%d/4", it.length)
                }
                updateView()
            }
        })
        dataBinding.btnEdit.setOnClickListener {
            visibility = GONE
            callback?.onCombineKeyEdit(mKeyInfo)
        }
        dataBinding.tabSetting.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                dataBinding.tabMap.isSelected = false
                dataBinding.layoutKeyParam.visibility = VISIBLE
                dataBinding.rvMaps.visibility = INVISIBLE
            }
        }
        dataBinding.tabMap.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                dataBinding.tabSetting.isSelected = false
                dataBinding.layoutKeyParam.visibility = INVISIBLE
                dataBinding.rvMaps.visibility = VISIBLE
            }
        }
        dataBinding.btnClick.setOnClickListener {
            if (!it.isSelected) {
                mKeyInfo.click = 0
                it.isSelected = true
                dataBinding.btnPress.isSelected = false
            }
        }
        dataBinding.btnPress.setOnClickListener {
            if (!it.isSelected) {
                mKeyInfo.click = 1
                it.isSelected = true
                dataBinding.btnClick.isSelected = false
            }
        }
        dataBinding.btnAddKeySize.setOnClickListener {
            if (mKeyInfo.zoom < 100) {
                mKeyInfo.zoom += 10
                dataBinding.tvKeySize.text = String.format("%s", "${mKeyInfo.zoom}%")
                updateView()
            }
        }
        dataBinding.btnReduceKeySize.setOnClickListener {
            if (mKeyInfo.zoom > 40) {
                mKeyInfo.zoom -= 10
                dataBinding.tvKeySize.text = String.format("%s", "${mKeyInfo.zoom}%")
                updateView()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun setKeyInfo(keyInfo: KeyInfo) {
        mKeyInfo = keyInfo.copy()
        dataBinding.tvKeySize.text = String.format("%s", "${keyInfo.zoom}%")
        dataBinding.btnClick.isSelected = keyInfo.click == 0
        dataBinding.btnPress.isSelected = keyInfo.click != 0
        dataBinding.tabSetting.isSelected = true
        dataBinding.tabMap.isSelected = false
        dataBinding.layoutKeyParam.visibility = VISIBLE
        dataBinding.rvMaps.visibility = INVISIBLE
        keyInfo.text?.let {
            val text = if (it.length <= 10) {
                it
            } else {
                it.substring(0, 10)
            }
            dataBinding.etKeyName.setText(text)
            dataBinding.etKeyName.setSelection(text.length)
            dataBinding.tvCount.text = String.format("%d/4", text.length)
        }
        val nameable =
            keyInfo.type == KeyType.KEYBOARD_KEY || keyInfo.type == KeyType.GAMEPAD_SQUARE ||
                    keyInfo.type == KeyType.GAMEPAD_ROUND_MEDIUM || keyInfo.type == KeyType.GAMEPAD_ROUND_SMALL ||
                    keyInfo.type == KeyType.KEY_COMBINE || keyInfo.type == KeyType.GAMEPAD_COMBINE ||
                    keyInfo.type == KeyType.KEY_ROULETTE || keyInfo.type == KeyType.GAMEPAD_ROULETTE
        val clickable =
            keyInfo.type == KeyType.KEYBOARD_MOUSE_LEFT || keyInfo.type == KeyType.KEYBOARD_MOUSE_RIGHT ||
                    keyInfo.type == KeyType.KEYBOARD_MOUSE_UP || keyInfo.type == KeyType.KEYBOARD_MOUSE_DOWN ||
                    keyInfo.type == KeyType.KEYBOARD_MOUSE_MIDDLE || keyInfo.type == KeyType.KEYBOARD_KEY
        val editable =
            keyInfo.type == KeyType.KEY_COMBINE || keyInfo.type == KeyType.GAMEPAD_COMBINE ||
                    keyInfo.type == KeyType.KEY_ROULETTE || keyInfo.type == KeyType.GAMEPAD_ROULETTE ||
                    keyInfo.type == KeyType.KEY_CONTAINER
        val mapAble = keyInfo.type == KeyType.KEYBOARD_KEY || keyInfo.type == KeyType.KEY_COMBINE
        dataBinding.layoutKeyName.visibility = if (nameable) VISIBLE else GONE
        dataBinding.layoutKeyInteract.visibility = if (clickable) VISIBLE else GONE
        dataBinding.btnEdit.visibility = if (editable) VISIBLE else INVISIBLE
        dataBinding.tvInfo.visibility = if (editable) VISIBLE else INVISIBLE
        dataBinding.tabMap.visibility = if (mapAble) VISIBLE else INVISIBLE
        if (mapAble) {
            dataBinding.rvMaps.layoutManager = GridLayoutManager(context, 5)
            dataBinding.rvMaps.adapter = MapAdapter(object : MapClickListener {
                override fun onClick(position: Int) {
                    mKeyInfo.map = maps[position].first
                    updateView()
                }
            }).apply {
                selectIndex = maps.indexOfFirst { item -> item.first == mKeyInfo.map }
            }
        }
        // 移除上个添加的View
        when (val view = dataBinding.layoutPreview[0]) {
            is KeyView, is RockerView, is CombineKeyView, is RouletteKeyView, is ContainerKeyView, is ShotKeyView -> {
                dataBinding.layoutPreview.removeView(view)
            }
        }
        previewKeyView()
    }

    private fun previewKeyView() {
        when (mKeyInfo.type) {
            KeyType.GAMEPAD_SQUARE, KeyType.GAMEPAD_ELLIPTIC, KeyType.GAMEPAD_ROUND_MEDIUM,
            KeyType.GAMEPAD_ROUND_SMALL, KeyType.KEYBOARD_KEY, KeyType.KEYBOARD_MOUSE_UP, KeyType.KEYBOARD_MOUSE_DOWN,
            KeyType.KEYBOARD_MOUSE_LEFT, KeyType.KEYBOARD_MOUSE_RIGHT, KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                previewView = addKeyButton()
            }

            KeyType.ROCKER_RIGHT, KeyType.ROCKER_LEFT, KeyType.ROCKER_LETTER, KeyType.ROCKER_ARROW -> {
                previewView = addRocker()
            }

            KeyType.ROCKER_CROSS -> {
                previewView = addCrossRocker()
            }

            KeyType.GAMEPAD_COMBINE -> {
                dataBinding.tvInfo.text = mKeyInfo.composeArr?.map { info -> info.text }?.toList()?.joinToString(" + ")
                previewView = addCombineKey()
            }

            KeyType.KEY_COMBINE -> {
                dataBinding.tvInfo.text = mKeyInfo.composeArr?.map { info -> getRouletteKeyText(info) }?.toList()?.joinToString(" + ")
                previewView = addCombineKey()
            }

            KeyType.KEY_ROULETTE -> {
                dataBinding.tvInfo.text = ""
                previewView = addRouletteKey()
            }

            KeyType.KEY_CONTAINER -> {
                dataBinding.tvInfo.text = mKeyInfo.containerArr?.map { info -> getRouletteKeyText(info) }?.toList()?.joinToString(" + ")
                previewView = addContainerKey()
            }

            KeyType.KEY_SHOOT -> {
                previewView = addShotKey()
            }

            else -> {
                LogUtils.e("previewKeyView:${mKeyInfo.type}")
            }
        }
    }

    private fun getRouletteKeyText(info: KeyInfo): String {
        return when (info.type) {
            KeyType.KEYBOARD_MOUSE_LEFT -> {
                "左击"
            }

            KeyType.KEYBOARD_MOUSE_RIGHT -> {
                "右击"
            }

            KeyType.KEYBOARD_MOUSE_MIDDLE -> {
                "中键"
            }

            KeyType.KEYBOARD_MOUSE_UP -> {
                "上滚"
            }

            KeyType.KEYBOARD_MOUSE_DOWN -> {
                "下滚"
            }

            else -> {
                KeyConstants.keyControl[info.inputOp]
                    ?: KeyConstants.keyNumber[info.inputOp] ?: ""
            }
        }
    }

    private fun updateView() {
        previewView?.let {
            when (it) {
                is KeyView -> {
                    it.setKeyInfo(mKeyInfo)
                }

                is RockerView -> {
                    it.setKeyInfo(mKeyInfo)
                }

                is CombineKeyView -> {
                    it.setKeyInfo(mKeyInfo)
                }

                is RouletteKeyView -> {
                    it.setKeyInfo(mKeyInfo)
                }

                is ContainerKeyView -> {
                    it.setKeyInfo(mKeyInfo)
                }

                is ShotKeyView -> {
                    it.setKeyInfo(mKeyInfo)
                }
            }
            val layoutParams = FrameLayout.LayoutParams(
                AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
                AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
            )
            layoutParams.gravity = Gravity.CENTER
            previewView?.layoutParams = layoutParams
        }
    }

    private fun addKeyButton(): View {
        val keyView = KeyView(context)
        keyView.needDrawShadow = false
        keyView.setKeyInfo(mKeyInfo)
        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(keyView, 0, layoutParams)
        return keyView
    }

    private fun addRocker(): View {
        val rockerView = RockerView(context)
        rockerView.needDrawShadow = false
        when (mKeyInfo.type) {
            KeyType.ROCKER_RIGHT -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_bg
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_r
                    )
                )
            }

            KeyType.ROCKER_LEFT -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_bg
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_l
                    )
                )
            }

            KeyType.ROCKER_LETTER -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_letter_pad
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_default
                    )
                )
            }

            KeyType.ROCKER_ARROW -> {
                rockerView.setArrowBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_arrow
                    )
                )
                rockerView.setBackgroundBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_arrow_pad
                    )
                )
                rockerView.setRockerBitmap(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.img_rocker_default
                    )
                )
            }
        }

        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(rockerView, 0, layoutParams)
        return rockerView
    }

    private fun addCrossRocker(): View {
        val rockerView = RockerView(context)
        rockerView.needDrawShadow = false
        rockerView.setBackgroundBitmap(
            ContextCompat.getDrawable(
                context,
                R.drawable.img_rocker_cross_default
            )
        )

        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(rockerView, 0, layoutParams)
        return rockerView
    }

    private fun addCombineKey(): View {
        val keyView = CombineKeyView(context)
        keyView.needDrawShadow = false
        keyView.setKeyInfo(mKeyInfo)
        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(keyView, 0, layoutParams)
        return keyView
    }

    private fun addRouletteKey(): View {
        val keyView = RouletteKeyView(context)
        keyView.needDrawShadow = false
        keyView.setKeyInfo(mKeyInfo)
        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(keyView, 0, layoutParams)
        return keyView
    }

    private fun addContainerKey(): View {
        val keyView = ContainerKeyView(context)
        keyView.needDrawShadow = false
        keyView.setKeyInfo(mKeyInfo)
        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(keyView, 0, layoutParams)
        return keyView
    }

    private fun addShotKey(): View {
        val keyView = ShotKeyView(context)
        keyView.needDrawShadow = false
        keyView.setKeyInfo(mKeyInfo)
        val layoutParams = FrameLayout.LayoutParams(
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyWidth()),
            AppSizeUtils.convertViewSize(mKeyInfo.getKeyHeight())
        )
        layoutParams.gravity = Gravity.CENTER
        dataBinding.layoutPreview.addView(keyView, 0, layoutParams)
        return keyView
    }
}