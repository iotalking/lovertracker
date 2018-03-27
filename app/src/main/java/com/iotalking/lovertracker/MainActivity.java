package com.iotalking.lovertracker;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.map.UiSettings;
import com.iotalking.lovertracker.service.WakeupService;

public class MainActivity extends AppCompatActivity {

    private TextureMapView mMapView;
    private BaiduMap mBaiduMap;
    private MyReceiver mReceiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        requestGPSPerssion();
        registerReceiver();
        startService();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void requestGPSPerssion(){
        this.requestPermissions(new String[]{Manifest.permission_group.LOCATION},6666);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void registerReceiver(){
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WakeupService.LOCATION_ACTION);
        filter.addAction(WakeupService.MOVE_TO_FRONT_ACTION);
        registerReceiver(mReceiver,filter);
    }
    void unregisterReceiver(){
        if(mReceiver != null){
            this.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
    void startService(){
        Intent i = new Intent();
        i.setClass(this,WakeupService.class);
        i.putExtra("taskId",getTaskId());
        startService(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mBaiduMap = null;
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WakeupService.LOCATION_ACTION)){
                BDLocation location = intent.getParcelableExtra("location");
                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);
                MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null);
            }
        }
    }
}
