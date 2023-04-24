package com.shinbash.tpk.bean

class BackYBBean {
    // 商户号
    var mchNumber: String? = null

    // 支付订单号
    var payOrderId: String? = null

    // 支付金额
    var amount: String? = null

    // 货币代码
    var currency: String? = null

    // 支付方式
    var wayCode: String? = null

    // 订单状态
    var orderState: String? = null

    // 错误码
    var code: String? = null

    // 错误信息
    var message: String? = null

    // 通知时间
    var notifyTime: String? = null

    // 签名
    var sign: String? = null
}