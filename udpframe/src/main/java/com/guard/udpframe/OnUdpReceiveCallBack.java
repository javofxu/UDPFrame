package com.guard.udpframe;

/**
 * @author skygge
 * @date 2022/5/21.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：UDP数据接收及状态接口
 */
public interface OnUdpReceiveCallBack {

    void onReceiveData(String json, String ip);

    void onUdpError();
}
