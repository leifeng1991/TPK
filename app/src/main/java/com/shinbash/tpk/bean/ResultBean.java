package com.shinbash.tpk.bean;

public class ResultBean {


    /**
     * msg : SUCCESS
     * code : 0
     * data : {"payCode":"P2023042116202175196"}
     */

    private String msg;
    private int code;
    /**
     * payCode : P2023042116202175196
     */

    private DataBean data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String payCode;

        public String getPayCode() {
            return payCode;
        }

        public void setPayCode(String payCode) {
            this.payCode = payCode;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "payCode='" + payCode + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}
