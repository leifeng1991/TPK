package com.shinbash.tpk.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.rawReqExecute
import com.moufans.lib_base.ext.setOnClickListener2
import com.moufans.lib_base.utils.LogUtil
import com.shinbash.tpk.R
import com.shinbash.tpk.bean.BackYBBean
import com.shinbash.tpk.bean.OrderCreateBean
import com.shinbash.tpk.bean.OrderUpdateBean
import com.shinbash.tpk.bean.YBBean
import com.shinbash.tpk.databinding.ActivityMainBinding
import com.shinbash.tpk.ext.appApi
import com.shinbash.tpk.utils.DataConvert
import com.sunmi.idcardservice.IDCardServiceAidl
import com.sunmi.idcardservice.MiFareCardAidl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.util.concurrent.Executors

class MainActivity : BaseActivity<ActivityMainBinding>() {
    // 银豹传进来数据bean
    private var mYBBean: YBBean? = null

    // 返回给银豹数据bean
    private val mBackYBBean: BackYBBean by lazy {
        BackYBBean()
    }

    private var mRepeatJob: Job? = null

    // 跳转到人脸识别页面
    private val mStartScanActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intent = it.data
        if (intent != null) {
            // 订单状态 0：失败 1：成功
            val orderState = intent.getIntExtra(ORDER_STATE, 0)
            // 为空时记账成功 其他根据msg进行提示
            val tipMsg = intent.getStringExtra(TIP_MSG)
            if (orderState == 1) {
                mBackYBBean.orderState = "2"
                mBackYBBean.wayCode = "CSSC_FR"
                mBackYBBean.payOrderId = intent.getStringExtra(INTENT_PAY_ORDER_NO)
            } else {
                mBackYBBean.orderState = "3"
            }
            bookkeepingStateLayout(orderState, 2, tipMsg ?: "")
        }
    }

    // K2和T2扫码内容
    private val sb = java.lang.StringBuilder()
    private val myHandler = Handler()
    private var mIdCardService: IDCardServiceAidl? = null
    private var mMifareService: MiFareCardAidl? = null
    private val mService = Executors.newCachedThreadPool()
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "service connect.")
            try {
                mIdCardService = IDCardServiceAidl.Stub.asInterface(service)
                Log.d(TAG, "id card connect.")
                if (mIdCardService != null) {
                    mMifareService = mIdCardService?.miFareCardService
                    Log.d(TAG, "get mifare card.")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "service disconnect.")
            mMifareService = null
            mIdCardService = null
        }
    }

    // true：正在更新记账状态 false:无操作
    private var orderUpdateRequesting = false

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun setStatusBar() {

    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        setHeaderViewVisibleByWebView(false)
        LogUtil.e(TAG, "=========BOARD========${Build.BOARD}")
        if (Build.BOARD == "P2") {
            startActivity(AppFullScreenWebViewActivity.newIntent(this, "https://124.70.4.91:82/"))
            finish()
        } else {
            val intentJson = intent.getStringExtra(INTENT_EXT_PARAM)
            if (TextUtils.isEmpty(intentJson)) {
                setResult("1")
                return
            }

            try {
                mYBBean = Gson().fromJson(intentJson, YBBean::class.java)
                mBackYBBean.mchNumber = mYBBean?.mchNumber
                mBackYBBean.amount = mYBBean?.amount
                mBackYBBean.currency = mYBBean?.currency
                mBackYBBean.sign = mYBBean?.sign
            } catch (e: Exception) {
                setResult("2")
                return
            }

            mDataBinding.apply {
                // 只有K2设备有船卡识别功能 其他设备不显示船卡识别按钮
                mCardPayRtv.visibility = if (Build.BOARD == "K2") View.VISIBLE else View.GONE
                mOrderCardSpace.visibility = if (Build.BOARD == "K2") View.VISIBLE else View.GONE
                // 设置记账金额
                mPriceTv.text = mYBBean?.amount
                // 设置记账订单号
                mOrderNumberTv.text = "订单号：${mYBBean?.mchOrderNo}"
                // 默认扫码记账
                mPaymentCodeRtv.isSelected = true
            }

            // 船卡识别检测
            mRepeatJob = lifecycleScope.launch {
                repeat(Int.MAX_VALUE) {
                    delay(1000)
                    onGetMifareUid()
                }
            }
            // 创建订单
            orderCreate()
        }
    }

    override fun initListener() {
        mDataBinding.apply {
            // 扫码支付
            mPaymentCodeRtv.setOnClickListener2 {
                mPaymentCodeRtv.isSelected = true
                mCardPayRtv.isSelected = false
                mFacePayRtv.isSelected = false
                mWaitTipIv.setImageResource(R.mipmap.ic_wait_scan_code)
                mWaitTipTv.text = "请出示二维码，在扫码口扫码"
            }
            // 船卡支付
            mCardPayRtv.setOnClickListener2 {
                mPaymentCodeRtv.isSelected = false
                mCardPayRtv.isSelected = true
                mFacePayRtv.isSelected = false
                mWaitTipIv.setImageResource(R.mipmap.ic_wait_card)
                mWaitTipTv.text = "请出示船卡，在刷卡处刷卡"
            }
            // 人脸支付
            mDataBinding.mFacePayRtv.setOnClickListener2 {
                mPaymentCodeRtv.isSelected = false
                mCardPayRtv.isSelected = false
                mFacePayRtv.isSelected = true
            }
            // 立即支付
            mDataBinding.mPayRTv.setOnClickListener2 {
                if (mDataBinding.mPaymentCodeRtv.isSelected) {
                    mDataBinding.mWaitLayout.visibility = View.VISIBLE
                }
                if (mDataBinding.mCardPayRtv.isSelected) {
                    mDataBinding.mWaitLayout.visibility = View.VISIBLE
                }
                if (mDataBinding.mFacePayRtv.isSelected) {
                    mStartScanActivityForResult.launch(Intent(this@MainActivity, CameraActivity::class.java).apply {
                        putExtra("mchOrderNo", mYBBean?.mchOrderNo ?: "")
                    })
                }

            }
            // 支付等待页面关闭
            mDataBinding.mWaitLayout.setOnClickListener2 {
                mDataBinding.mWaitLayout.visibility = View.GONE
            }
        }
        // 返回
        mDataBinding.mBackRTv.setOnClickListener2 {
            setResult("4")
        }
        mDataBinding.mStateRTv.setOnClickListener2 {
            // 失败，进行相应操作
            // 隐藏状态界面
            mDataBinding.mOrderStateLl.visibility = View.GONE
            when (mDataBinding.mStateRTv.text.toString()) {
                // 扫码
                "重新扫码" -> {
                    mDataBinding.mWaitLayout.visibility = View.VISIBLE
                    mDataBinding.mWaitTipIv.setImageResource(R.mipmap.ic_wait_scan_code)
                    mDataBinding.mWaitTipTv.text = "请出示二维码，在扫码口扫码"
                }
                // 船卡
                "重新识卡" -> {
                    mDataBinding.mWaitLayout.visibility = View.VISIBLE
                    mDataBinding.mWaitTipIv.setImageResource(R.mipmap.ic_wait_card)
                    mDataBinding.mWaitTipTv.text = "请出示船卡，在刷卡处刷卡"
                }
                // 人脸识别
                "重新识别" -> {
                    mStartScanActivityForResult.launch(Intent(this@MainActivity, CameraActivity::class.java).apply {
                        putExtra(INTENT_MCH_ORDER_NO, mYBBean?.mchOrderNo)
                    })
                }

                else -> {}
            }
        }
    }

    override fun processingLogic() {
    }


    override fun onResume() {
        super.onResume()
        if (Build.BOARD == "K2") {
            bindIDCardService()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.BOARD == "K2") {
            unbindIDCardService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRepeatJob?.cancel()
    }


    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (mDataBinding.mPaymentCodeRtv.isSelected && mDataBinding.mWaitLayout.visibility == View.VISIBLE) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    val unicodeChar = event.unicodeChar
                    sb.append(unicodeChar.toChar())
                    if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        return super.dispatchKeyEvent(event)
                    }
                    if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        return super.dispatchKeyEvent(event)
                    }
                    if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                        return super.dispatchKeyEvent(event)
                    }
                    if (event.keyCode == KeyEvent.KEYCODE_MENU) {
                        return super.dispatchKeyEvent(event)
                    }
                    if (event.keyCode == KeyEvent.KEYCODE_HOME) {
                        return super.dispatchKeyEvent(event)
                    }
                    if (event.keyCode == KeyEvent.KEYCODE_POWER) {
                        return super.dispatchKeyEvent(event)
                    }
                    myHandler.postDelayed({
                        if (sb.isNotEmpty()) {
                            runOnUiThread {
                                val content = sb.toString().split("\n")
                                if (content.isNotEmpty()) {
                                    orderUpdate(0, content[0])
                                } else {
                                    orderUpdate(0, sb.toString())
                                }

                            }
                            sb.setLength(0)
                        }
                    }, 300)
                    return true
                }

                else -> {

                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun bindIDCardService() {
        val intent = Intent()
        intent.setPackage("com.sunmi.idcardservice")
        intent.action = "com.sunmi.idcard"
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    private fun unbindIDCardService() {
        unbindService(mConnection)
    }

    /**
     * 识别船卡
     */
    private fun onGetMifareUid() {
        if (Build.BOARD != "K2") return
        if (mDataBinding.mCardPayRtv.isSelected && mDataBinding.mWaitLayout.visibility == View.VISIBLE) {
            newHandlerWork {
                try {
                    val snr = ByteArray(10)
                    val uid = ByteArray(4)
                    System.arraycopy(snr, 0, uid, 0, uid.size)
                    runOnUiThread {
                        if (snr[0].toInt() > 0) {
                            orderUpdate(1, DataConvert.bytesToHexString(uid))
                            try {
                                mMifareService?.beep(5)
                            } catch (e: RemoteException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun newHandlerWork(runnable: Runnable) {
        if (mMifareService == null) return
        mService.execute(runnable)
    }

    /**
     * 创建记账单
     */
    private fun orderCreate() {
        lifecycleScope.launch {
            val orderCreateBean = OrderCreateBean()
            orderCreateBean.mchNumber = mYBBean?.mchNumber
            orderCreateBean.appId = mYBBean?.appId
            orderCreateBean.mchOrderNo = mYBBean?.mchOrderNo
            orderCreateBean.amount = mYBBean?.amount
            orderCreateBean.currency = mYBBean?.currency
            orderCreateBean.airRangeId = "1"
            orderCreateBean.storeId = "123"
            val extParam = mYBBean?.extParam
            if (!TextUtils.isEmpty(extParam)) {
                try {
                    val goodList = Gson().fromJson<ArrayList<OrderCreateBean.GoodsInfosBean>>(extParam, object : TypeToken<ArrayList<OrderCreateBean.GoodsInfosBean>>() {}.type)
                    orderCreateBean.goodsInfos = goodList
                } catch (e: Exception) {
                    LogUtil.e(TAG, "======orderCreate======gson格式化异常========${e}")
                }
            }
            val json = Gson().toJson(orderCreateBean)
            rawReqExecute({ appApi.orderCreate(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)) }, onSuccess = {
                LogUtil.e(TAG, "===orderCreate===成功=====${it}")
            }, onFailure = {
                LogUtil.e(TAG, "===orderCreate====失败===$it")
                setResult("3==${it.message}")
            })
        }
    }


    /**
     * 修改记账单
     */
    private fun orderUpdate(type: Int, userId: String) {
        if (orderUpdateRequesting) return
        orderUpdateRequesting = true
        lifecycleScope.launch {
            val orderUpdateBean = OrderUpdateBean()
            // 商户订单号
            orderUpdateBean.mchOrderNo = mYBBean?.mchOrderNo
            // 订单状态
            orderUpdateBean.orderState = "0"
            if (type == 1) {
                // 船卡
                orderUpdateBean.cardId = userId
            } else {
                // 人脸/扫码
                orderUpdateBean.userId = userId
            }

            // 支付方式 条码支付:CSSC_BAR  人脸支付:CSSC_FR  船卡支付:CSSC_CARD
            orderUpdateBean.wayCode = if (type == 0) "CSSC_BAR" else "CSSC_CARD"

            val json = Gson().toJson(orderUpdateBean)
            rawReqExecute({ appApi.orderUpdate(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)) }, onSuccess = {
                LogUtil.e(TAG, "===orderUpdate========${it}")
                mBackYBBean.orderState = if (it.code == 0) "2" else "3"
                mBackYBBean.payOrderId = if (it.code == 0 && it.data != null) it.data.payCode else ""
                mBackYBBean.wayCode = if (type == 0) "CSSC_BAR" else "CSSC_CARD"
                bookkeepingStateLayout(if (it.code == 0) 1 else 0, type, if (it.code == 0) "记账成功！" else "记账失败！")
                orderUpdateRequesting = false
            }, onFailure = {
                LogUtil.e(TAG, "===orderUpdate========${it.toString()}")
                mBackYBBean.orderState = "3"
                bookkeepingStateLayout(0, type, "记账失败！")
                orderUpdateRequesting = false
            })
        }
    }

    /**
     * 设置记账状态
     * @param type       0:记账失败 1：记账成功
     * @param buttonType 0:二维码记账 1：船卡记账 2：人脸记账
     * @param msg 提示信息
     */
    private fun bookkeepingStateLayout(type: Int, buttonType: Int = 0, msg: String) {
        mDataBinding.apply {
            // 显示状态界面
            mOrderStateLl.visibility = View.VISIBLE
            // 隐藏等待界面
            mWaitLayout.visibility = View.GONE
            // 1:记账成功 0：记账失败
            val isSuccess = type == 1
            // 设置记账状态icon
            mStateIv.setImageResource(if (isSuccess) R.mipmap.ic_success else R.mipmap.ic_failed)
            // 设置记账成功或失败提示语
            mStateTv.text = if (isSuccess && TextUtils.isEmpty(msg)) "记账成功！" else msg
            // 成功不显示该按钮 失败显示该按钮
            mStateRTv.visibility = if (isSuccess) View.GONE else View.VISIBLE
            // 设置返回建边框颜色和字体颜色
            mBackRTv.helper.apply {
                borderColorNormal = getColor(if (isSuccess) R.color.back_two else R.color.back_one)
                textColorNormal = getColor(if (isSuccess) R.color.back_two else R.color.back_one)
            }

            // 重新进行操作
            mStateRTv.apply {
                text = when (buttonType) {
                    1 -> {
                        "重新识卡"
                    }

                    2 -> {
                        "重新识别"
                    }

                    else -> {
                        "重新扫码"
                    }
                }

            }
        }

    }

    /**
     * 返回银豹app数据
     */
    private fun setResult(state: String = "0") {
        LogUtil.e(TAG, "===============setResult=============")
        if (TextUtils.isEmpty(mBackYBBean.orderState)) {
//            mBackYBBean.orderState = "4"
            mBackYBBean.orderState = state
        }
        mBackYBBean.code = "0"
        mBackYBBean.message = "message"
        mBackYBBean.notifyTime = "${System.currentTimeMillis()}"
        val contentJson = Gson().toJson(mBackYBBean)
        setResult(RESULT_OK, Intent().apply {
            putExtras(Bundle().apply {
                putExtra("extParam", contentJson)
            })
        })
        finish()

    }


    companion object {
        private val TAG = MainActivity::class.simpleName
        const val ORDER_STATE = "orderState"
        const val TIP_MSG = "tipMsg"
        const val INTENT_EXT_PARAM = "extParam"
        const val INTENT_MCH_ORDER_NO = "mchOrderNo"
        const val INTENT_PAY_ORDER_NO = "payOrderNo"
    }
}