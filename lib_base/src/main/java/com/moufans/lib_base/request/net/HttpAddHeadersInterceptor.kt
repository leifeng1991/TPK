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
class HttpAddHeadersInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().apply {
            // 主板
            addHeader("Device-Board", Build.BOARD)
            addHeader("tenant-id", "1")
        }.build()
        return chain.proceed(request)
    }


}