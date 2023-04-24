package com.shinbash.tpk.bean

class OrderUpdateBean {
    /**
     * mchOrderNo : D789654126321
     * orderState : 0
     * userId : 20000019
     * wayCode : CSSC_FR
     */
    var mchOrderNo: String? = null
    var orderState: String? = null
    var userId: String? = null
    var wayCode: String? = null
    var cardId: String? = null
    var similar: String? = null
    override fun toString(): String {
        return "OrderUpdateBean(mchOrderNo=$mchOrderNo, orderState=$orderState, userId=$userId, wayCode=$wayCode, cardId=$cardId, similar=$similar)"
    }


}