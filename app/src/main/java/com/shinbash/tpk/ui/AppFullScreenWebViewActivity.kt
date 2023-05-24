package com.shinbash.tpk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.moufans.lib_base.R
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.databinding.BaseActivityWebviewFullscreenBinding
import com.moufans.lib_base.utils.StatusBarUtil
import com.shinbash.MyApplication
import com.shinbash.tpk.ui.fragment.AppWebViewFragment
import com.shinbash.tpk.utils.CheckCardCallbackV2Wrapper
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * 全屏网页跳转
 */
class AppFullScreenWebViewActivity : BaseActivity<BaseActivityWebviewFullscreenBinding>(), AppWebViewFragment.OnWebViewListener {
    private var webViewFragment: AppWebViewFragment? = null

    private val mStartScanActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        try {
            val intent = it.data
            if (intent != null) {
                val bundle = intent.extras
                val result = bundle?.getSerializable("data") as java.util.ArrayList<*>?
                val iterator = result!!.iterator()
                while (iterator.hasNext()) {
                    val hashMap = iterator.next() as HashMap<String, Any>
                    webViewFragment?.callJavascriptMethod("callbackScan('${hashMap["VALUE"]}')")
                }
            }
        }catch (e:java.lang.Exception){

        }


    }
    private var cardType = 0
    private var startScanCardJob: Job? = null
    private val mReadCardCallback: CheckCardCallbackV2 = object : CheckCardCallbackV2Wrapper() {
        @Throws(RemoteException::class)
        override fun findMagCard(bundle: Bundle) {
        }

        @Throws(RemoteException::class)
        override fun findICCardEx(info: Bundle) {
            val atr = info.getString("atr")
            handleCheckCardSuccess("findICCard, atr:$atr")
            cardType = info.getInt("cardType")
        }

        @Throws(RemoteException::class)
        override fun findRFCardEx(info: Bundle) {
            val uuid = info.getString("uuid")
            handleCheckCardSuccess("findRFCard, uuid:$uuid")
            cardType = info.getInt("cardType")
        }

        @Throws(RemoteException::class)
        override fun onErrorEx(info: Bundle) {
            val code = info.getInt("code")
            val msg = info.getString("message")
            handleCheckCardFailed(code, msg!!)
        }
    }

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.base_activity_webview_fullscreen
    }

    override fun addHeaderView() {

    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }

    override fun initView() {
//        FullscreenInputWorkaround.assistActivity(this, mDataBinding.mFullscreenWebViewFl)// 兼容软键盘弹起
        // 网址
        val webUrl = intent.getStringExtra(WEB_URL)
        // 设置内容，传递网址
        webViewFragment = AppWebViewFragment.newInstance(webUrl ?: "https://192.168.5.121:8000/")
//        webViewFragment = AppWebViewFragment.newInstance(webUrl ?: "https://36.110.167.44:82/#/user/login")
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mWebView, webViewFragment!!)// 将fragment设置到布局上
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun initListener() {
        // 设置webView的监听
        webViewFragment!!.onWebViewListener = this
    }

    override fun processingLogic() {
        mDataBinding.goActivity.setOnClickListener {
//            startActivity(Intent(this,InputWebActivity::class.java))
            checkCard()
        }
    }

    override fun onBackPressed() {
        webViewFragment!!.goBackUntilFinishActivity()
    }

    override fun onPageFinished(isCanGoBack: Boolean, title: String, url: String) {

    }

    /**
     * 跳转到扫码界面
     */
    fun jumpToScannerActivity() {
        XXPermissions.with(this).permission(Permission.CAMERA).request(object : OnPermissionCallback {

            override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                if (!allGranted) {
                    return
                }
                try {
                    val intent = Intent("com.summi.scan") //            intent.component = ComponentName("com.sunmi.sunmiqrcodescanner", "com.sunmi.sunmiqrcodescanner.activity.ScanActivity") //        intent.setClassName("com.sunmi.sunmiqrcodescanner", "com.sunmi.sunmiqrcodescanner.activity.ScanActivity")
                    intent.setClassName("com.sunmi.sunmiqrcodescanner", "com.sunmi.sunmiqrcodescanner.activity.ScanActivity");
                    intent.putExtra("CURRENT_PPI", 0X0003); //当前分辨率 M1和V1的最佳是800*480, PPI_1920_1080 = 0X0001;PPI_1280_720 = 0X0002;PPI_BEST = 0X0003;
                    intent.putExtra("PLAY_SOUND", true); // 扫描完成声音提示  默认true
                    intent.putExtra("PLAY_VIBRATE", false); // 扫描完成震动,默认false，目前M1硬件支持震动可用该配置，V1不支持
                    intent.putExtra("IDENTIFY_MORE_CODE", false); // 识别画面中多个二维码，默认false
                    intent.putExtra("IS_SHOW_SETTING", true); // 是否显示右上角设置按钮，默认true
                    intent.putExtra("IS_SHOW_ALBUM", true); // 是否显示从相册选择图片按钮，默认true
                    intent.putExtra("IDENTIFY_INVERSE", true); // 允许识读反色二维码，默认true
                    intent.putExtra("IS_EAN_8_ENABLE", true); //允许识读EAN-8码，默认true：允许
                    intent.putExtra("IS_UPC_E_ENABLE", true); //允许识读UPC-E码，默认true：允许
                    intent.putExtra("IS_ISBN_10_ENABLE", true); //允许识读ISBN-10 (from EAN-13)码，默认true：允许
                    intent.putExtra("IS_CODE_11_ENABLE", true); //允许识读CODE-11码，默认false：不允许
                    intent.putExtra("IS_UPC_A_ENABLE", true); //允许识读UPC-A码，默认true：允许
                    intent.putExtra("IS_EAN_13_ENABLE", true); //允许识读AN-13码，默认true：允许
                    intent.putExtra("IS_ISBN_13_ENABLE", true); //允许识读ISBN-13 (from EAN-13)码，默认true：允许
                    intent.putExtra("IS_INTERLEAVED_2_OF_5_ENABLE", true); //允许识读Interleaved 2 of 5码，默认false：不允许
                    intent.putExtra("IS_CODE_128_ENABLE", true); //允许识读ECode 128码，默认true：允许
                    intent.putExtra("IS_CODABAR_ENABLE", true); //允许识读Codabar码，默认true：允许
                    intent.putExtra("IS_CODE_39_ENABLE", true); //允许识读Code 39码，默认true：允许
                    intent.putExtra("IS_CODE_93_ENABLE", true); //允许识读Code 93码，默认true：允许
                    intent.putExtra("IS_DATABAR_ENABLE", true); //允许识读DataBar (RSS-14)码，默认true：允许
                    intent.putExtra("IS_DATABAR_EXP_ENABLE", true); //允许识读DataBar Expanded码，默认true：允许
                    intent.putExtra("IS_Micro_PDF417_ENABLE", true); //允许识读Micro PDF417码，默认true：允许
                    intent.putExtra("IS_MicroQR_ENABLE", true); //允许识读Micro QR Code码，默认true：允许
                    intent.putExtra("IS_Hanxin_ENABLE", true); //允许识读Hanxin Code码，默认true：允许
                    intent.putExtra("IS_OPEN_LIGHT", true); // 是否显示闪光灯，默认true
                    intent.putExtra("SCAN_MODE", false); // 是否是循环模式，默认false
                    intent.putExtra("IS_QR_CODE_ENABLE", true); // 允许识读QR码，默认true
                    intent.putExtra("IS_PDF417_ENABLE", true); // 允许识读PDF417码，默认true
                    intent.putExtra("IS_DATA_MATRIX_ENABLE", true); // 允许识读DataMatrix码，默认true
                    intent.putExtra("IS_AZTEC_ENABLE", true); // 允许识读AZTEC码，默认true
                    mStartScanActivityForResult.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@AppFullScreenWebViewActivity, "异常信息：${e.toString()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {

            }
        })


    }

    public fun startReadCard() {
        startScanCardJob?.cancel()
        startScanCardJob = lifecycleScope.launch {
            repeat(300) {
                delay(1000)
                runOnUiThread {
                    checkCard()
                }
            }

        }
    }

    private fun checkCard() {
        try {
            val ctrStr: String = "0"
            val activeCtr = ctrStr.toInt(16) //支持M1卡
            val allType = (AidlConstants.CardType.NFC.value or AidlConstants.CardType.IC.value or AidlConstants.CardType.MIFARE.value or AidlConstants.CardType.FELICA.value or AidlConstants.CardType.PSAM0.value); MyApplication.app.readCardOptV2.checkCardEx(allType, activeCtr, 0, mReadCardCallback, 60)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun handleCheckCardSuccess(msg: String) {
        startScanCardJob?.cancel()
        webViewFragment?.callJavascriptMethod("callbackReadCard('${msg.split(":")[1].toUpperCase()}')")

    }

    private fun handleCheckCardFailed(code: Int, msg: String) {
        Toast.makeText(this, "读卡失败，错误信息：$msg", Toast.LENGTH_SHORT).show()
    }


    companion object {

        private const val WEB_URL = "webUrl"

        /**
         * @param webUrl      加载的url地址
         */
        @JvmStatic
        fun newIntent(context: Context, webUrl: String): Intent {
            val intent = Intent(context, AppFullScreenWebViewActivity::class.java)
            intent.putExtra(WEB_URL, webUrl)
            return intent
        }
    }

}
