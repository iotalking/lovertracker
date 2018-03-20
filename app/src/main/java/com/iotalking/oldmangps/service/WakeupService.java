package com.iotalking.oldmangps.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.iotalking.oldmangps.R;

/**
 * Created by funny on 2018/3/20.
 */

public class WakeupService extends Service {
    private LBSTraceClient mTraceClient = null;
    private Trace mTrace = null;
    private boolean mTracking;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupTrace();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupNotification(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    void setupTrace(){
        // 轨迹服务ID
        long serviceId = 161814;
        // 设备标识
        String entityName = genDeviceId();
        // 是否需要对象存储服务，默认为：false，关闭对象存储服务。注：鹰眼 Android SDK v3.0以上版本支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
        boolean isNeedObjectStorage = false;
        // 初始化轨迹服务
        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage);
        // 初始化轨迹服务客户端
        mTraceClient = new LBSTraceClient(getApplicationContext());

        // 定位周期(单位:秒)
        int gatherInterval = 10;
        // 打包回传周期(单位:秒)
        int packInterval = 60;
        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);


        startTrace();
    }
    // 初始化轨迹服务监听器
    OnTraceListener mTraceListener = new OnTraceListener() {
        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        // 开启服务回调
        @Override
        public void onStartTraceCallback(int status, String message) {}
        // 停止服务回调
        @Override
        public void onStopTraceCallback(int status, String message) {}
        // 开启采集回调
        @Override
        public void onStartGatherCallback(int status, String message) {}
        // 停止采集回调
        @Override
        public void onStopGatherCallback(int status, String message) {}
        // 推送回调
        @Override
        public void onPushCallback(byte messageNo, PushMessage message) {}

        @Override
        public void onInitBOSCallback(int i, String s) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTrace();
    }

    private void startTrace() {
        if(mTracking == false){
            mTraceClient.startTrace(mTrace,mTraceListener);
            mTracking = true;
        }
    }
    private void stopTrace(){
        if(mTracking == true){
            mTraceClient.stopTrace(mTrace,mTraceListener);
            mTracking = false;
        }
    }
    private String genDeviceId() {
        return "test";
    }

    void setupNotification(Intent intent){
        if(intent != null && intent.hasExtra("taskId")){
            //使用兼容版本
            NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
            //设置状态栏的通知图标
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            //设置通知栏横条的图标
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_foreground));
            //禁止用户点击删除按钮删除
            builder.setAutoCancel(false);
            //禁止滑动删除
            builder.setOngoing(true);
            //右上角的时间显示
            builder.setShowWhen(true);
            //设置通知栏的标题内容
            builder.setContentTitle("正在定位中");
            builder.setContentText(this.getString(R.string.app_name));
            Intent i = new Intent("OldManGPSMoveToFront");
            i.putExtra("taskId",intent.getIntExtra("taskId",0));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            //创建通知
            Notification notification = builder.build();
            this.startForeground(1,notification);
        }
    }
}
