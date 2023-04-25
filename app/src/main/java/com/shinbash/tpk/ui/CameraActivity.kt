package com.shinbash.tpk.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.AI.FaceVerify.baseImage.BaseImageCallBack
import com.AI.FaceVerify.baseImage.BaseImageDispose
import com.google.gson.Gson
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.moufans.lib_base.ext.rawReqExecute
import com.moufans.lib_base.utils.LogUtil
import com.shinbash.tpk.R
import com.shinbash.tpk.bean.OrderUpdateBean
import com.shinbash.tpk.ext.appApi
import com.shinbash.tpk.ui.MainActivity.Companion.INTENT_PAY_ORDER_NO
import com.shinbash.tpk.ui.MainActivity.Companion.ORDER_STATE
import com.shinbash.tpk.ui.MainActivity.Companion.TIP_MSG
import com.shinbash.tpk.utils.CameraHelper
import com.shinbash.tpk.view.FaceView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream


class CameraActivity : AppCompatActivity() {
    // 人脸圆框
    private var faceView: FaceView? = null

    // 摄像头工具类
    private var mCameraHelper: CameraHelper? = null

    // 订单号，从上个界面传进来的
    private val mchOrderNo by lazy {
        intent.getStringExtra("mchOrderNo")
    }

    // true:预览流正在进行人脸识别，不能有其他的的预览流进入，false：可以允许预览流进行人脸识别
    private var isPrameFace = false

    // 第二种方案控制参数
    private var mIsSecond = false

    // 第二种方案 控制人脸识别请求次数
    private var mRequestFaceVeryTimes = 5

    // 人脸识别类，以及会掉
    private val baseImageDispose by lazy {
        BaseImageDispose(baseContext, object : BaseImageCallBack() {
            override fun onCompleted(bitmap: Bitmap) {
                Log.d(TAG, "==onCompleted=============识别到人脸")
                val os = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os)
                val bytes = os.toByteArray()
                faceRecognizer(bytes, bitmap, null)
                // 识别到人脸进行拍照
//                mCameraHelper?.takePic()
            }

            override fun onProcessTips(actionCode: Int) {
                runOnUiThread {
                    val tipText = when (actionCode) {
                        NO_FACE -> "未检测到人脸"
                        MANY_FACE -> "多张人脸出现"
                        SMALL_FACE -> "靠近一点"
                        AlIGN_FAILED -> "图像校准失败"
                        else -> {
                            ""
                        }
                    }
                    Log.d("1NN1", "==onProcessTips====$tipText")
                }

                isPrameFace = false
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_camera)
        } else {
            setContentView(R.layout.activity_camera_land)
        }

        XXPermissions.with(this).permission(Permission.CAMERA).request(object : OnPermissionCallback {

            override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                if (!allGranted) {
                    return
                }
                initView()
            }

            override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {

            }
        })

    }

    private fun initView() {
        faceView = findViewById(R.id.face_view)
        faceView?.visibility = View.VISIBLE
        val takeImageView = findViewById<ImageView>(R.id.takeImageView)
        takeImageView?.visibility = View.GONE
        val cameras = Camera.getNumberOfCameras()
        if (cameras <= 0) {
            finish()
            return
        }
        mCameraHelper = CameraHelper(this, findViewById(R.id.surfaceView))
        mCameraHelper!!.addCallBack(object : CameraHelper.CallBack {
            override fun onFaceDetect(faces: ArrayList<RectF>) {

            }

            override fun onTakePic(data: ByteArray?) {
                try {
                    LogUtil.i(TAG, "=====onTakePic======${data.contentToString()}")
                    data?.let {
                        dealWithByteArray(it)
                    }

                } catch (e: Exception) {
                    LogUtil.e(TAG, "=====onTakePic======${e}")
                }


            }

            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                if (data != null && !isPrameFace && camera != null) {
                    isPrameFace = true
                    val startTime = System.currentTimeMillis()
                    try {
                        // 获取Camera预览尺寸
                        val size = camera.parameters.previewSize
                        // 将帧数据转为bitmap
                        val image = YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
                        val stream = ByteArrayOutputStream()
                        image.compressToJpeg(Rect(0, 0, size.width, size.height), 100, stream)
                        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
                        val m = Matrix()
                        // 台式机不进行旋转
                        if (Build.BOARD == "K2") {
                            if (bmp.width > bmp.height) {
                                // 纠正图像的旋转角度问题
                                m.setRotate(90f)
                                LogUtil.i(TAG, "=================通过旋转纠正图片=====${bmp.width}${bmp.height}")
                            }
                        }
                        val bm = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
                        if (!mIsSecond) {
                            baseImageDispose.dispose(bm)
                        } else {
                            val os = ByteArrayOutputStream()
                            bm.compress(Bitmap.CompressFormat.JPEG, 80, os)
                            val bytes = os.toByteArray()
                            faceRecognizer(bytes, bmp, bm)
                        }
                        LogUtil.i(TAG, "=================耗时=====${System.currentTimeMillis() - startTime}")
                    } catch (ex: java.lang.Exception) {
                        isPrameFace = false
                        LogUtil.e(TAG, "========Exception=========耗时=====${System.currentTimeMillis() - startTime}")
                    }

                }

            }
        })

        // 进行15秒倒计时，倒计时结束认为人脸识别超时
        lifecycleScope.launch {
            repeat(15) {
                delay(1000)
                if (it == 14) {
                    setActivityResult(0, "人脸识别超时")
                }
            }
        }
    }

    override fun onDestroy() { //释放资源
        faceView?.destroyView()
        mCameraHelper?.releaseCamera()
        super.onDestroy()
    }

    private fun dealWithByteArray(data: ByteArray) {
        val rawBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        if (rawBitmap.width > rawBitmap.height) {
            val matrix = Matrix()
            // 台式机不进行旋转
            if (Build.BOARD == "K2") {
                matrix.postRotate(90f)
            }
            val rotatedBitMap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            val os = ByteArrayOutputStream()
            rotatedBitMap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            val bytes = os.toByteArray()
            faceRecognizer(bytes, rawBitmap, rotatedBitMap)
            LogUtil.e(TAG, "==============rotatedBitMap===============${rotatedBitMap.width}===============${rotatedBitMap.height}")
//            val takeImageView = findViewById<ImageView>(R.id.takeImageView)
//            takeImageView.visibility = View.VISIBLE
//            Glide.with(this).load(bytes).into(takeImageView)
        } else {
            LogUtil.e(TAG, "==============rawBitmap===============${rawBitmap.width}===============${rawBitmap.height}")
            faceRecognizer(data, rawBitmap, null)
//            val takeImageView = findViewById<ImageView>(R.id.takeImageView)
//            takeImageView.visibility = View.VISIBLE
//            Glide.with(this).load(data).into(takeImageView)
        }


    }

    /**
     * 人脸比对
     */
    private fun faceRecognizer(data: ByteArray, rawBitmap: Bitmap?, rotatedBitMap: Bitmap?) {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val fileBody: RequestBody = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), data)
        builder.addFormDataPart("faceImage", fileName, fileBody)
        lifecycleScope.launch {
            rawReqExecute({ appApi.uploadDamo(builder.build()) }, onSuccess = {
                LogUtil.e(TAG, "=========onSuccess===========${it.toString()}")
                rotatedBitMap?.recycle()
                rawBitmap?.recycle()
                mRequestFaceVeryTimes--
                if (it.code == 200 && it.data != null && it.data.result != null && !TextUtils.isEmpty(it.data.result.key)) { // 成功
                    // 更新订单
                    orderUpdate(it.data.result.key, "${it.data.result.similar}")
                } else { // 失败
                    // 直接返回上一页
                    if (mIsSecond) {
                        if (mRequestFaceVeryTimes == 0) {
                            setActivityResult(0, "人脸识别失败")
                        } else {
                            isPrameFace = false
                        }
                    } else {
                        setActivityResult(0, "人脸识别失败")
                    }
                }

            }, onFailure = {
                LogUtil.e(TAG, "=========onFailure===========$it")
                rotatedBitMap?.recycle()
                rawBitmap?.recycle()
                mRequestFaceVeryTimes--
                // 直接返回上一页
                if (mIsSecond) {
                    if (mRequestFaceVeryTimes == 0) {
                        setActivityResult(0, "人脸识别失败")
                    } else {
                        isPrameFace = false
                    }
                } else {
                    setActivityResult(0, "人脸识别失败")
                }

            })
        }

    }


    /**
     * 修改记账单
     */
    private fun orderUpdate(userId: String, similar: String) {
        lifecycleScope.launch {
            val orderUpdateBean = OrderUpdateBean() // 商户订单号
            orderUpdateBean.mchOrderNo = mchOrderNo
            orderUpdateBean.orderState = "0"
            orderUpdateBean.userId = userId
            orderUpdateBean.similar = similar
            // 支付方式 条码支付:CSSC_BAR  人脸支付:CSSC_FR  船卡支付:CSSC_CARD
            orderUpdateBean.wayCode = "CSSC_FR"
            val json = Gson().toJson(orderUpdateBean)
            rawReqExecute({ appApi.orderUpdate(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)) }, onSuccess = {
                when (it.code) {
                    0 -> {
                        setActivityResult(1, msg = "记账成功！", payOrderNo = it.data.payCode)
                    }

                    6001 -> {
                        setActivityResult(0, it.msg)
                    }

                    else -> {
                        setActivityResult(0, "记账失败！")
                    }
                }
            }, onFailure = {
                setActivityResult(0, "记账失败！")
            })
        }
    }


    /**
     * @param orderState 记账状态 1 记账成功 0 记账失败
     * @param errorType  错误类型 0 记账失败 1人脸识别超时 2后台返回错误信息提示
     * @param msg        提示信息
     * @param payOrderNo 记账成功订单号
     */
    private fun setActivityResult(orderState: Int = 0, msg: String = "", payOrderNo: String = "") {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ORDER_STATE, orderState)
            putExtra(TIP_MSG, msg)
            putExtra(INTENT_PAY_ORDER_NO, payOrderNo)
        })
        finish()
    }

    companion object {

        private val TAG = CameraActivity::class.simpleName
    }


}