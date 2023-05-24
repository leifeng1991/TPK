package com.shinbash.tpk.bean

class BackYBBean {
    // 商户订单号
    var mchOrderNo: String? = null

    // 支付订单号
    var payOrderId: String? = null

    // 支付金额
    var amount: String? = null

    // 货币代码
    var currency: String? = null

    // 支付方式 条码支付 CSSC_BAR  人脸支付 CSSC_FR  船卡支付 CSSC_CARD
    var wayCode: String? = null

    // 订单状态 0-订单生成 1-支付中 2-支付成功 3-支付失败 4-已撤销 5-已退款 6-订单关闭
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