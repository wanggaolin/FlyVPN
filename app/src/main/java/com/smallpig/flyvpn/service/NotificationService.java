package com.smallpig.flyvpn.service;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.*;
import android.text.format.Formatter;
import android.widget.RemoteViews;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.tools.MySqlController;
import com.smallpig.flyvpn.tools.TrafficInfo;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private TrafficInfo trafficInfo;
    private String notificationId = "channelId";
    private String notificationName = "channelName";
    private Handler mHander;
    private Timer mTimer;

    private long rechargeFlow;

    private final int UPDATE_FREQUENCY = 60;
    private int times = 1;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.activity_notification);

        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        final String username = sp.getString("username", "");
        try {
            rechargeFlow = MySqlController.getInstance().getFlow(username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mHander = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    long speed = (long)msg.obj;
                    rechargeFlow -= speed;
                    remoteViews.setTextViewText(R.id.notification_textview_rechargeflow, "剩余流量：" + Formatter.formatFileSize(getApplicationContext(), rechargeFlow));
                    remoteViews.setTextViewText(R.id.notification_textview_totalspeed, "当前速度：" + Formatter.formatFileSize(getApplicationContext(), speed));
                    notificationManager.notify(1, notification);
                }
            }
        };

        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (times == UPDATE_FREQUENCY) {
                        try {
                            MySqlController.getInstance().setFlow(username, rechargeFlow);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        times++;
                    }
                }
            }, 60000, 60000);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.shadowsocks)
                .setContentTitle("FlyVPN代理已启动")
                .setContent(getRemoteViews())
                .setSound(null)
                .setOngoing(false)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        notification = builder.build();
        startForeground(1, notification);

        try {
            trafficInfo = new TrafficInfo(this, mHander);
            trafficInfo.startCalculateNetSpeed();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        trafficInfo.stopCalculateNetSpeed();
        stopForeground(true);
        mTimer.cancel();
        mTimer = null;
        super.onDestroy();
    }

    RemoteViews getRemoteViews(){
        remoteViews.setTextViewText(R.id.notification_textview_rechargeflow,"剩余流量：0");
        remoteViews.setTextViewText(R.id.notification_textview_totalspeed,"当前速度：0");
        return remoteViews;
    }
}
