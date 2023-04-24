package com.sunmi.idcardservice;

import com.sunmi.idcardservice.CardCallback;
import com.sunmi.idcardservice.IDCardInfo;
import com.sunmi.idcardservice.MiFareCardAidl;

interface IDCardServiceAidl {

     IDCardInfo readCard();

     void readCardAuto(in CardCallback callback);

     void cancelAutoReading();

     MiFareCardAidl getMiFareCardService();
}
