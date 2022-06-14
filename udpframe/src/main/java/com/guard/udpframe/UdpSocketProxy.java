package com.guard.udpframe;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author skygge
 * @date 2022/5/21.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：udp框架-Socket
 */
public class UdpSocketProxy {

    private static final String TAG = "UdpSocketProxy";
    /**
     * 端口号
     */
    private static final int CLIENT_PORT = 1025;

    /**
     * 缓冲
     */
    private static final int BUFFER_LENGTH = 1024;

    private final byte[] mReceiveByte = new byte[BUFFER_LENGTH];

    private DatagramSocket mUdpClient;

    private DatagramPacket mReceivePacket;

    private Thread mClientThread;

    private boolean isThreadRunning = false;

    public UdpSocketProxy() {
    }

    /**
     * 开启UDP通信
     */
    public void startUDPSocket() {
        try {
            mUdpClient = new DatagramSocket(null);
            mUdpClient.setReuseAddress(true);
            mUdpClient.bind(new InetSocketAddress(CLIENT_PORT));
            if (mReceivePacket == null) {// 创建接受数据的 packet
                mReceivePacket = new DatagramPacket(mReceiveByte, BUFFER_LENGTH);
            }
            mClientThread = new Thread(this::receiveMessage);
            isThreadRunning = true;
            mClientThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(String hostIp, byte[] message) {
        if (hostIp == null) {
            return;
        }
        ExecutorService mThreadPool = Executors.newFixedThreadPool(7);// 根据CPU数目初始化线程池
        mThreadPool.execute(() -> {
            try {
                InetAddress address = InetAddress.getByName(hostIp);
                DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, CLIENT_PORT);
                mUdpClient.send(sendPacket);
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 接受到的消息
     */
    private void receiveMessage() {
        while (isThreadRunning) {
            try {
                if (mUdpClient != null) mUdpClient.receive(mReceivePacket);

                //无法接收UDP数据或者接收到的UDP数据为空
                if (mReceivePacket == null || mReceivePacket.getLength() == 0) continue;

                //获取UDP接收到数据
                String ipAddress = mReceivePacket.getAddress().getHostAddress();
                UdpReceiveProxy.getInstance().onGetReceiveData(ipAddress, mReceivePacket.getData());

                // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
                mReceivePacket.setLength(BUFFER_LENGTH);
            } catch (IOException | NullPointerException e) {
                //UDP数据包接收失败！stopUDPSocket
                stopUDPSocket();
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * 关闭UDP
     */
    public void stopUDPSocket() {
        isThreadRunning = false;
        mReceivePacket = null;
        if (mClientThread != null) {
            mClientThread.interrupt();
        }
        if (mUdpClient != null) {
            mUdpClient.close();
            mUdpClient = null;
        }
        Log.e(TAG, "UDP断开");
        UdpAnswerObserver.getInstance().notifyUdpError();
    }

    /**
     * 获取socket状态
     */
    public boolean isUdpSocketClose(){
        if (mUdpClient == null){
            return true;
        }
        return mUdpClient.isClosed();
    }
}