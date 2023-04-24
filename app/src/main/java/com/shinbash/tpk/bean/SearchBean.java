package com.shinbash.tpk.bean;

public class SearchBean {

    /**
     * code : 200
     * data : {"status":"REAL","result":{"key":"99","similar":0.5481019},"environment":true,"seetaRect":null,"register":null,"peopleNum":1}
     * msg :
     */

    private int code;
    /**
     * status : REAL
     * result : {"key":"99","similar":0.5481019}
     * environment : true
     * seetaRect : null
     * register : null
     * peopleNum : 1
     */

    private DataBean data;
    private String msg;

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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean {
        private String status;
        /**
         * key : 99
         * similar : 0.5481019
         */

        private ResultBean result;
        private boolean environment;
        private Object seetaRect;
        private Object register;
        private int peopleNum;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public ResultBean getResult() {
            return result;
        }

        public void setResult(ResultBean result) {
            this.result = result;
        }

        public boolean isEnvironment() {
            return environment;
        }

        public void setEnvironment(boolean environment) {
            this.environment = environment;
        }

        public Object getSeetaRect() {
            return seetaRect;
        }

        public void setSeetaRect(Object seetaRect) {
            this.seetaRect = seetaRect;
        }

        public Object getRegister() {
            return register;
        }

        public void setRegister(Object register) {
            this.register = register;
        }

        public int getPeopleNum() {
            return peopleNum;
        }

        public void setPeopleNum(int peopleNum) {
            this.peopleNum = peopleNum;
        }

        public static class ResultBean {
            private String key;
            private double similar;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public double getSimilar() {
                return similar;
            }

            public void setSimilar(double similar) {
                this.similar = similar;
            }

            @Override
            public String toString() {
                return "ResultBean{" +
                        "key='" + key + '\'' +
                        ", similar=" + similar +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "status='" + status + '\'' +
                    ", result=" + result +
                    ", environment=" + environment +
                    ", seetaRect=" + seetaRect +
                    ", register=" + register +
                    ", peopleNum=" + peopleNum +
                    '}';
        }


    }

    @Override
    public String toString() {
        return "SearchBean{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }
}
