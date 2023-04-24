package com.shinbash;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;

import com.moufans.lib_base.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;

import sunmi.paylib.SunmiPayKernel;

public class MyApplication extends Application {
    public static MyApplication app;
    public BasicOptV2 basicOptV2;           // 获取基础操作模块
    public ReadCardOptV2 readCardOptV2;     // 获取读卡模块
    private boolean connectPaySDK;//是否已连接PaySDK

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        String model = Build.BOARD;
        LogUtil.e("==============="+model);
        if ("P2".equals(model)){
            bindPaySDKService();
        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean isConnectPaySDK() {
        return connectPaySDK;
    }

    /**
     * bind PaySDK service
     */
    public void bindPaySDKService() {
        final SunmiPayKernel payKernel = SunmiPayKernel.getInstance();
        payKernel.initPaySDK(this, new SunmiPayKernel.ConnectCallback() {
            @Override
            public void onConnectPaySDK() {
                basicOptV2 = payKernel.mBasicOptV2;
                readCardOptV2 = payKernel.mReadCardOptV2;
                connectPaySDK = true;
                LogUtil.i("================onConnectPaySDK");
            }

            @Override
            public void onDisconnectPaySDK() {
                connectPaySDK = false;
                basicOptV2 = null;
                readCardOptV2 = null;
                LogUtil.i("================onDisconnectPaySDK");
            }
        });
    }

    /** bind printer service */

    /**
     * bind scanner service
     */
    public void bindScannerService() {
        Intent intent = new Intent();
        intent.setPackage("com.sunmi.scanner");
        intent.setAction("com.sunmi.scanner.IScanInterface");
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.i("================onServiceConnected"+name);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtil.i("================onServiceDisconnected"+name);
            }
        }, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
