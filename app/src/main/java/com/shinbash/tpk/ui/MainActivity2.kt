package com.shinbash.tpk.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.gyf.immersionbar.ktx.hideStatusBar
import com.moufans.lib_base.base.activity.BaseActivity
import com.shinbash.tpk.R
import com.shinbash.tpk.bean.BackYBBean
import com.shinbash.tpk.bean.GoodsBean
import com.shinbash.tpk.bean.OrderCreateBean
import com.shinbash.tpk.bean.YBBean
import com.shinbash.tpk.databinding.ActivityMain2Binding

class MainActivity2 : BaseActivity<ActivityMain2Binding>() {

    // 跳转到人脸识别页面
    private val mStartScanActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intent = it.data
        if (intent != null) {
            val bundle = intent.extras
            if (bundle != null) {
                val contentJson = bundle.getString(MainActivity.INTENT_EXT_PARAM)
                mDataBinding.mBackContentTextView.text = contentJson
            }
        }
    }


    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_main2
    }

    override fun setStatusBar() {

    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        setHeaderViewVisibleByWebView(false)
        refreshJsonData()


    }

    override fun initListener() {
        mDataBinding.mBackButton.setOnClickListener {
            mStartScanActivityForResult.launch(Intent(this, MainActivity::class.java).apply {
//                setClassName("com.shinbash.tpk", "com.shinbash.tpk.ui.MainActivity")
                putExtra(MainActivity.INTENT_EXT_PARAM, mDataBinding.mContentTextView.text.toString())
            })
        }
        mDataBinding.mUpdateOrderNoButton.setOnClickListener {
            refreshJsonData()
        }
    }

    override fun processingLogic() {
        if (Build.BOARD == "P2") {
            startActivity(AppFullScreenWebViewActivity.newIntent(this, "https://124.70.4.91:82/"))
            finish()
        }
    }

    private fun refreshJsonData() {
        val mYBBean = YBBean()
        mYBBean.mchNumber = "M1673407452"
        mYBBean.appId = "123456"
        mYBBean.mchOrderNo = "D${System.currentTimeMillis()}"
        mYBBean.amount = "0.01"
        mYBBean.currency = "CNY"
        mYBBean.expiredTime = "${System.currentTimeMillis() + 24 * 60 * 60 * 1000}"
        mYBBean.reqTime = "${System.currentTimeMillis()}"
        mYBBean.sign = "${System.currentTimeMillis()}"

        val goodsList = ArrayList<GoodsBean>()
        val goodsBean = GoodsBean()
        goodsBean.barcode = "111000011100"
        goodsBean.name = "商品名称"
        goodsBean.qty = "10"
        goodsBean.price = "0.01"
        goodsBean.totalAmount = "0.01"
        goodsList.add(goodsBean)
        mYBBean.extParam = Gson().toJson(goodsList)


        mDataBinding.mContentTextView.text = Gson().toJson(mYBBean)
    }

    companion object {
        private val TAG = MainActivity2::class.simpleName
        const val INTENT_EXT_PARAM = "extParam"
    }
}