package com.guard.udpframe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author skygge
 * @date 8/27/21.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：udp框架-数据监听
 */
public class UdpAnswerObserver {

    private static UdpAnswerObserver mInstance = null;

    private final List<OnDataAnalysisCallBack> mObservers = new ArrayList<>();

    public static UdpAnswerObserver getInstance() {
        if (mInstance == null){
            synchronized (UdpAnswerObserver.class) {
                if (mInstance == null) {
                    mInstance = new UdpAnswerObserver();
                }
            }
        }
        return mInstance;
    }

    public void registerObserver(OnDataAnalysisCallBack observer){
        if (observer == null) return;
        if (!mObservers.contains(observer)){
            mObservers.add(observer);
        }
    }

    public void unRegisterObserver(OnDataAnalysisCallBack observer){
        if (observer == null) return;
        mObservers.remove(observer);
    }

    public void notifyObservers(String str1, String str2) {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onReceiveData(str1, str2);
        }
    }

    public void notifyUdpError() {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onUdpError();
        }
    }

    public void notifyUdpAnswer(String str1, String str2) {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onUdpAnswer(str1, str2);
        }
    }

    public void notifyDeviceList(String str1, String str2) {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onGetDeviceList(str1, str2);
        }
    }

    public void notifyDeviceStatus(String deviceName, int cmd, String str1, String str2) {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onGetDeviceStatus(deviceName, cmd, str1, str2);
        }
    }

    public void notifyDeviceAlarm(String str1, String str2) {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onGetDeviceAlarm(str1, str2);
        }
    }

    public void notifyDeviceToken(String str1, String str2, String str3) {
        for (OnDataAnalysisCallBack callback:mObservers) {
            callback.onGetDeviceToken(str1, str2, str3);
        }
    }
}
