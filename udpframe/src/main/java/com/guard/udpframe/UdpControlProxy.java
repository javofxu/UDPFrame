package com.guard.udpframe;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * @author skygge
 * @date 2020/8/21.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：udp框架-发送数据
 */
public class UdpControlProxy {

    private static UdpControlProxy mInstance = null;

    private UdpSocketProxy mUdpSocket;

    public static UdpControlProxy getInstance() {
        if (mInstance == null){
            synchronized (UdpControlProxy.class) {
                if (mInstance == null) {
                    mInstance = new UdpControlProxy();
                }
            }
        }
        return mInstance;
    }

    /**
     * 开启并搜索局域网内设备
     * 通过广播搜索内网在线设备
     * ByBroadcast
     */
    public void onSearchDeviceUdp(Application app) {
        onOpenUdpSocket();
        String ipAddress = getBroadcastAddress(app);
        if (ipAddress == null) return;
        String message = "{\"action\":\"IOT_KEY?\",\"devID\":\"NULL\"}";
        onSendMessage(ipAddress, message);
    }

    /**
     * 激活设备DUP状态
     */
    public void onActivationUdp(String ipAddress, String deviceName) {
        String message = "{\"action\":\"IOT_KEY?\",\"devID\":" + deviceName + "}";
        onSendMessage(ipAddress, message);
    }

    /**
     * UDP应答
     */
    public void onSendAnswer(String ip, String deviceName) {
        String message = getAnswerOk(deviceName);
        onSendMessage(ip, message);
    }

    /**
     * 通过UDP发送数据
     */
    public void onSendMessage(String ip, String message) {
        onOpenUdpSocket();
        try {
            mUdpSocket.sendMessage(ip, message.getBytes());
        }catch (Exception e) {
            onCloseUdpSocket();
        }
    }

    /**
     * 开启UDP
     */
    public void onOpenUdpSocket() {
        if (isUdpSocketClose()) {
            mUdpSocket = new UdpSocketProxy();
            mUdpSocket.startUDPSocket();
        }
    }

    /**
     * 关闭UDP
     */
    public void onCloseUdpSocket() {
        if (mUdpSocket!=null) {
            mUdpSocket.stopUDPSocket();
            mUdpSocket = null;
        }
    }

    /**
     * 获取socket状态
     */
    public boolean isUdpSocketClose() {
        if (mUdpSocket == null){
            return true;
        }
        return mUdpSocket.isUdpSocketClose();
    }

    /**
     * 获取当前ip地址
     * 获取当前网络广播地址
     */
    private String getBroadcastAddress(Application application) {
        try {
            WifiManager mManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
            if (mManager == null) return null;
            else {
                WifiInfo wifiInfo = mManager.getConnectionInfo();
                if (wifiInfo == null) return null;
                else {
                    String localAddress = int2ip(wifiInfo.getIpAddress());
                    return localAddress.substring(0, localAddress.lastIndexOf(".") + 1) + 255;
                }
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     */
    private static String int2ip(int ipInt) {
        return (ipInt & 0xFF) + "." +
                ((ipInt >> 8) & 0xFF) + "." +
                ((ipInt >> 16) & 0xFF) + "." +
                ((ipInt >> 24) & 0xFF);
    }

    /**
     * udp应答
     * 251
     */
    private String getAnswerOk(String deviceName) {
        return "{ " +
                "    \"action\": \"APP_ACK\",  " +
                "    \"devID\": \""+ deviceName +"\",  " +
                "    \"msg\": { " +
                "    \"msg_ID\": "+ 0 +",  " +
                "    \"CMD_CODE\": "+ 11 +",  " +
                "    \"rev_str1\": \""+ 11 +"\"," +
                "    \"rev_str2\": \"OK\"," +
                "    \"rev_str3\": \"\"" +
                "    } " +
                "}";

    }
}
