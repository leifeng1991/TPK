package com.sunmi.idcardservice;

import com.sunmi.idcardservice.IDCardInfo;

interface CardCallback {

    void getCardData(inout IDCardInfo info,int code);
}
