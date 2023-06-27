package com.moufans.lib_base.request.net

import android.os.Build
import androidx.annotation.RequiresApi
import com.moufans.lib_base.utils.DeviceUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by leifeng on 2020/9/14.
 * 功能描述：请求头拦截器
 */
open class HttpAddHeadersInterceptor : Interceptor {
    
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().apply {
            // 设备类型 Android
            addHeader("Device-Type", "android")
            // 设置唯一id
            addHeader("Device-Id", DeviceUtils.getDeviceId())
            // 主板
            addHeader("Device-Board", Build.BOARD)
            addHeader("tenant-id ", "1")
            setHeader(this)
            addHeader("Content-Type", "application/json;charset=utf-8")
        }.build()
        return chain.proceed(request)
    }
    
    open fun setHeader(request: Request.Builder){
    
    }
    
}