package com.smallpig.flyvpn.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class TrafficInfo {

    private static final int UNSUPPORTED = -1;
    private static final String LOG_TAG = "test";

    private static TrafficInfo instance;

    static int uid;
    private long preRxBytes = 0;
    private Timer mTimer = null;
    private Context mContext;
    private Handler mHandler;

    /**
     * 更新频率（每几秒更新一次,至少1秒）
     */
    private final int UPDATE_FREQUENCY = 1;
    private int times = 1;

    public TrafficInfo(Context mContext, Handler mHandler)throws PackageManager.NameNotFoundException {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.uid = getUid();
    }

    public static TrafficInfo getInstance(Context mContext, Handler mHandler) throws PackageManager.NameNotFoundException {
        if (instance == null) {
            instance = new TrafficInfo(mContext, mHandler);
        }
        return instance;
    }

    /**
     * 获取下载流量 某个应用的网络流量数据保存在系统的/proc/uid_stat/$UID/tcp_rcv | tcp_snd文件中
     */
    public long getRcvTraffic() throws IOException {
        return TrafficStats.getUidRxBytes(uid);
    }

    /**
     * 获取上传流量
     */
    public long getSndTraffic() throws IOException {
        return TrafficStats.getUidTxBytes(uid);
    }

    /**
     * 获取当前下载流量总和
     */
    public static long getNetworkRxBytes() {
        return TrafficStats.getTotalRxBytes();
    }

    /**
     * 获取当前上传流量总和
     */
    public static long getNetworkTxBytes() {
        return TrafficStats.getTotalTxBytes();
    }

    /**
     * 获取当前网速，小数点保留一位
     */
    public double getNetSpeed() {
        long curRxBytes = getNetworkRxBytes();
        if (preRxBytes == 0)
            preRxBytes = curRxBytes;
        long bytes = curRxBytes - preRxBytes;
        preRxBytes = curRxBytes;
        //int kb = (int) Math.floor(bytes / 1024 + 0.5);
        double kb = (double) bytes / (double) 1024;
        BigDecimal bd = new BigDecimal(kb);
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 开启流量监控
     */
    public void startCalculateNetSpeed() {
        preRxBytes = getNetworkRxBytes();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (times == UPDATE_FREQUENCY) {
                        Message msg = new Message();
                        msg.what = 1;
                        //msg.arg1 = getNetSpeed();
                        //msg.obj = getNetSpeed();
                        Bundle data = new Bundle();
                        try {
                            data.putLong("upload", getSndTraffic());
                            data.putLong("download", getRcvTraffic());
                            data.putDouble("speed", getNetSpeed());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                        times = 1;
                    } else {
                        times++;
                    }
                }
            }, 1000, 1000); // 每秒更新一次
        }
    }

    public void stopCalculateNetSpeed() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 获取当前应用uid
     */
    public int getUid() throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        @SuppressLint("WrongConstant") ApplicationInfo ai = pm.getApplicationInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        return ai.uid;
    }
}
