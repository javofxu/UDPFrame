package com.guard.udpframe;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * @author skygge
 * @date 5/24/22.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：解析UDP数据
 */
public class UdpReceiveProxy {

    private static final String TAG = "UdpReceiveDataUtil";

    private static final String CMD_CODE = "CMD_CODE";

    private static final String ALARM_MSG = "alarmMessage";

    private static final String DATA_STR1 = "data_str1";

    private static final String DATA_STR2 = "data_str2";

    private static final String CMD_ACTION = "action";

    private static final String CMD_DEVICE_ID = "devID";

    private static final String CMD_MSG = "msg";

    private static final String CMD_DEVICE = "device_id";

    private static final String CMD_TOKEN = "token";

    private static final String CMD_ACK = "ack";

    private static final String CMD_ACK_OK = "ok";

    private static UdpReceiveProxy mInstance = null;

    public static UdpReceiveProxy getInstance() {
        if (mInstance == null){
            synchronized (UdpReceiveProxy.class) {
                if (mInstance == null) {
                    mInstance = new UdpReceiveProxy();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取到UDP接收到的数据
     * @param ip 地址
     * @param data 接收到的数据
     */
    public void onGetReceiveData(String ip, byte[] data) {
        String message = new String(data);
        Log.i(TAG, "onGetReceiveData: " + message);
        String json;
        if (message.contains("{")) { //没有加密
            json = getReceiveMessage(message);
        } else { //加密数据，先解密
            json = getEncryptedData(data);
        }
        receiveData(json, ip);
    }

    /**
     * 解析UDP数据
     */
    private void receiveData(String message, String ipAddress){
        String deviceName;
        try {
            Log.i(TAG, "receiveData: " + message);
            JSONObject jsonObject = new JSONObject(message);
            if(jsonObject.has(CMD_ACTION) && jsonObject.has(CMD_MSG)) {
                deviceName = jsonObject.getString(CMD_DEVICE_ID);
                JSONObject msg = jsonObject.getJSONObject(CMD_MSG);

                //报警推送信息
                if (msg.has(ALARM_MSG)) {
                    String alarmMessage = msg.getString(ALARM_MSG);
                    UdpAnswerObserver.getInstance().notifyDeviceAlarm(deviceName, alarmMessage);
                } else {
                    //上报信息
                    int command = msg.has(CMD_CODE) ? msg.getInt(CMD_CODE) : -1;
                    if(0 == command) {//广播扫描到的网关列表
                        UdpAnswerObserver.getInstance().notifyDeviceList(ipAddress, deviceName);
                    }else { //其他设备状态上报
                        String str1 = msg.getString(DATA_STR1);
                        String str2 = msg.getString(DATA_STR2);
                        UdpAnswerObserver.getInstance().notifyDeviceStatus(deviceName, command, str1, str2);
                    }
                }
            }else if(jsonObject.has(CMD_DEVICE) && jsonObject.has(CMD_TOKEN)) {//热点AP配网回传Token
                deviceName = jsonObject.getString(CMD_DEVICE);
                String token = jsonObject.getString(CMD_TOKEN);
                UdpAnswerObserver.getInstance().notifyDeviceToken(deviceName, token, ipAddress);
            }else if(jsonObject.has(CMD_DEVICE) && jsonObject.has(CMD_ACK)) { //网关接收到应答回复OK
                deviceName = jsonObject.getString(CMD_DEVICE);
                if (CMD_ACK_OK.equals(jsonObject.getString(CMD_ACK))) UdpAnswerObserver.getInstance().notifyUdpAnswer(ipAddress, deviceName);
            }else {
                deviceName = "NULL";
            }
            UdpControlProxy.getInstance().onSendAnswer(ipAddress, deviceName);
        } catch (JSONException e) {
            //数据异常
            Log.e(TAG, "receiveData: json格式错误");
        }
    }

    /**
     * 解析UDP数据(已加密数据）
     * @param receiveData UDP收到的数据
     * @return json
     */
    private String getEncryptedData(byte[] receiveData){
        String decrypt = getAllDecryption(receiveData);
        String msg = getStringFromAscii2(decrypt);
        return getReceiveMessage(msg);
    }

    /**
     * UDP解密
     */
    private String getAllDecryption(byte[] input){
        try {
            int random = input[0];
            StringBuilder buffer = new StringBuilder();
            for(int i=1;i<input.length;i++){
                int  a1 = input[i];
                int a = random ^ 0x23 ^ a1;
                String v = a<16?("0"+Integer.toHexString(a)):(Integer.toHexString(a));
                buffer.append(v);
            }
            return buffer.toString();
        }catch (Exception e){
            return "";
        }
    }

    /**
     * ascii码转字符串
     */
    private String getStringFromAscii2(String input){
        try {
            byte[]a = hexStr2Bytes(input);
            return new String(a, StandardCharsets.UTF_8);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 十六进制字符串转换成bytes
     */
    private byte[] hexStr2Bytes(String src) {
        int m, n;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        try {
            for (int i = 0; i < l; i++) {
                m = i * 2 + 1;
                n = m + 1;
                ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    private byte uniteBytes(String src0, String src1) {
        byte b0 = Byte.decode("0x" + src0);
        b0 = (byte) (b0 << 4);
        byte b1 = Byte.decode("0x" + src1);
        return (byte) (b0 | b1);
    }

    /**
     * 优化UDP数据 删除****无用数据，得到有用的JSON体
     * @param msg UDP收到的数据
     * @return json
     */
    public static String getReceiveMessage(String msg){
        Log.d(TAG, "getReceiveMessage: " + msg);
        if (!TextUtils.isEmpty(msg)) {
            String json;
            if (msg.contains("}}")){
                json = msg.substring(0, msg.indexOf("}}") + 2);
            }else if (msg.contains("}")){
                json = msg.substring(0, msg.indexOf("}") + 1);
            }else {
                json = msg;
            }
            return json;
        }
        return null;
    }
}
