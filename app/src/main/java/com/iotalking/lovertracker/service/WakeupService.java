package com.iotalking.lovertracker.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.iotalking.lovertracker.R;
import com.iotalking.lovertracker.receiver.WakeupReceiver;

/**
 * Created by funny on 2018/3/20.
 */

public class WakeupService extends Service {
    public static final String START_ACTION = "LT_Start";
    public static final String LOCATION_ACTION = "LT_location";
    public static final String MOVE_TO_FRONT_ACTION = "LT_MoveToFront";
    public static final String WAKEUP_ALARM_ACTION = "LT_Wakeup";
    public static final String WAKEUP_ALARM_STOP_ACTION = "LT_WakeupAlarmStop";
    public static final String START_TRACE_ACTION = "LT_StartTrace";
    public static final String UPDATE_ADDRESS_ACTION = "LT_UpdateAddress";
    public static final String RESTART_GPS_ACTION = "LT_RestartGPS";
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "WakeupService";

    private LBSTraceClient mTraceClient = null;
    private Trace mTrace = null;
    private boolean mTracking;
    private LocationClient mLocationClient;
    private static BDLocation mLastLocation = null;
    private BDAbstractLocationListener mGPSListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLastLocation = bdLocation;
            Intent i = new Intent(LOCATION_ACTION);
            i.putExtra("location",bdLocation);
            sendBroadcast(i);
            i = new Intent(UPDATE_ADDRESS_ACTION);
            i.setClass(getApplicationContext(),WakeupService.class);
            i.putExtra("address",bdLocation.getAddrStr());
            startService(i);
        }
    };
    private int mTaskId = -1;
    private WakeupReceiver mReceiver;

    public static BDLocation getLastLocation(){
        return mLastLocation;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver();
        initLocation();
        setupTrace();
        startTrace();
    }
    final static int TIMEOUT = 5*1000;
    void SetupPingAlarm(){
        Intent intent = new Intent(WAKEUP_ALARM_ACTION);

        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);

        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()*TIMEOUT,pi);
        }if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            am.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+TIMEOUT,pi);
        }else{
            am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),TIMEOUT,pi);
        }

    }
    void StopAlarm(){
        Intent intent = new Intent(WAKEUP_ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_NO_CREATE);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.hasExtra("taskId")){
            mTaskId = intent.getIntExtra("taskId",-1);
            setupNotification(mTaskId,null);
        }

        if(intent != null ){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(START_ACTION)){
                    Log.i(TAG,"started");
                }else if(action.equals(WAKEUP_ALARM_ACTION)){
                    SetupPingAlarm();
                }else if(action.equals(WAKEUP_ALARM_STOP_ACTION)){
                    StopAlarm();
                }else if(action.equals(START_TRACE_ACTION)){
                    stopTrace();
                    startTrace();
                }else if(action.equals(RESTART_GPS_ACTION)){
                    if(mLocationClient != null){
                        mLocationClient.stop();
                    }
                    initLocation();

                    mLocationClient.start();
                }else if(action.equals(UPDATE_ADDRESS_ACTION)){
                    if(intent.hasExtra("address")){
                        updateAddress(intent.getStringExtra("address"));
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void registerReceiver(){
        if(mReceiver == null){
            mReceiver = new WakeupReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(mReceiver,filter);
        }
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
        int packInterval = 10;
        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);
    }
    // 初始化轨迹服务监听器
    OnTraceListener mTraceListener = new OnTraceListener() {
        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        // 开启服务回调
        @Override
        public void onStartTraceCallback(int status, String message) {

        }
        // 停止服务回调
        @Override
        public void onStopTraceCallback(int status, String message) {

        }
        // 开启采集回调
        @Override
        public void onStartGatherCallback(int status, String message) {

        }
        // 停止采集回调
        @Override
        public void onStopGatherCallback(int status, String message) {}
        // 推送回调
        @Override
        public void onPushCallback(byte messageNo, PushMessage message) {

        }

        @Override
        public void onInitBOSCallback(int i, String s) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTrace();
        if(mLocationClient!=null){
            mLocationClient.stop();;
            mLocationClient = null;
        }
    }

    private void startTrace() {
        if(mTracking == false){
            mTraceClient.startTrace(mTrace,mTraceListener);
            mTraceClient.startGather(mTraceListener);
            mTracking = true;
        }
    }
    private void stopTrace(){
        if(mTracking == true){
            mTraceClient.stopTrace(mTrace,null);
            mTraceClient.stopGather(null);
            mTracking = false;
        }
    }
    private String genDeviceId() {
        return "test";
    }

    void setupNotification(int taskId,String address){
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
        builder.setContentTitle("爱心定位");
        if(address == null || address.length() == 0){
            builder.setContentText("定位中...");
        }else{
            builder.setContentText(address);
        }
        Intent i = new Intent(MOVE_TO_FRONT_ACTION);
        i.putExtra("taskId",taskId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,NOTIFICATION_ID,i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        //创建通知
        Notification notification = builder.build();
        this.startForeground(NOTIFICATION_ID,notification);
    }

    void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span=1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        //  option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mGPSListener);
        mLocationClient.start();
    }

    private void updateAddress(String addr){
        if(mTaskId > 0 && addr!= null && addr.length() > 0 ){
            setupNotification(mTaskId,addr);
        }
    }
}
