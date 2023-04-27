package com.shinbash.tpk.bean;

public class GoodsBean {


    /**
     * barcode : 123456
     * name : 测试商品
     * price : 8.5
     * qty : 2
     * totalAmount : 17
     */

    private String barcode;
    private String name;
    private String price;
    private String qty;
    private String totalAmount;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
