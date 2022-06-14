package com.guard.udpframe;

/**
 * @author skygge
 * @date 2022/5/21.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：UDP数据接收及状态接口
 */
public class OnDataAnalysisCallBack implements OnUdpReceiveCallBack{


    public void onGetDeviceList(String ip, String deviceName) {

    }

    public void onGetDeviceStatus(String deviceName, int cmd, String str1, String str2) {

    }

    public void onGetDeviceAlarm(String deviceName, String alarmMsg) {

    }

    public void onGetDeviceToken(String ip, String deviceName, String token) {

    }

    public void onUdpAnswer(String ip, String deviceName) {

    }

    @Override
    public void onReceiveData(String json, String ip) {

    }

    @Override
    public void onUdpError() {

    }
}
