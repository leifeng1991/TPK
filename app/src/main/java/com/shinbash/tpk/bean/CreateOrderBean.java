package com.shinbash.tpk.bean;

public class CreateOrderBean {


    /**
     * msg : SUCCESS
     * code : 0
     * data : {"payCode":"P2023042116202175196"}
     */

    private String msg;
    private int code;

    @Override
    public String toString() {
        return "CreateOrderBean{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                '}';
    }
}
