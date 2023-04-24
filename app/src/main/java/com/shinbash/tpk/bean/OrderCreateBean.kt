package com.shinbash.tpk.bean

class OrderCreateBean {
    /**
     * mchNumber : M1668394731
     * appId : 456
     * mchOrderNo : D789654126321
     * amount : 100
     * currency : CNY
     * airRangeId : 1
     * storeId : 123
     * goodsInfos : [{"goodsName":"测试商品1","count":1,"price":100}]
     */
    var mchNumber: String? = null
    var appId: String? = null
    var mchOrderNo: String? = null
    var amount: String? = null
    var currency: String? = null
    var airRangeId: String? = null
    var storeId: String? = null

    /**
     * goodsName : 测试商品1
     * count : 1
     * price : 100
     */
    var goodsInfos: List<GoodsInfosBean>? = null

    class GoodsInfosBean {
        var goodsName: String? = null
        var count: String? = null
        var price: String? = null
        override fun toString(): String {
            return "GoodsInfosBean(goodsName=$goodsName, count=$count, price=$price)"
        }

    }

    override fun toString(): String {
        return "OrderCreateBean(mchNumber=$mchNumber, appId=$appId, mchOrderNo=$mchOrderNo, amount=$amount, currency=$currency, airRangeId=$airRangeId, storeId=$storeId, goodsInfos=$goodsInfos)"
    }


}