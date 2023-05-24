package com.shinbash.tpk.api

import com.shinbash.tpk.bean.CreateOrderBean
import com.shinbash.tpk.bean.ResultBean
import com.shinbash.tpk.bean.SearchBean
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST


interface AppApi {

    /**
     * 上传图片
     */
    @POST("http://36.110.167.44:8001/seetaface6/seetaface/seach")
    suspend fun uploadDamo(@Body multipartBody: MultipartBody): SearchBean


    /**
     * 创建订单
     */
    @POST("http://36.110.167.44:9217/api/order/create")
    suspend fun orderCreate(@Body requestBody: RequestBody): CreateOrderBean


    /**
     * 修改订单
     */
    @POST("http://36.110.167.44:9217/api/order/update")
    suspend fun orderUpdate(@Body requestBody: RequestBody): ResultBean
}

