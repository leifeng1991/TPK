package com.shinbash.tpk.ext

import com.moufans.lib_base.request.net.HttpLogInterceptor
import com.moufans.lib_base.request.net.RetrofitFactory
import com.shinbash.tpk.api.AppApi

val RetrofitFactory.Companion.appInstance
    get() = getInstance("http://54.94.67.56/mercadocredito/",  HttpLogInterceptor())

val appApi by lazy { RetrofitFactory.appInstance.create(AppApi::class.java) }