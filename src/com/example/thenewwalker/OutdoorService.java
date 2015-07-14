package com.example.thenewwalker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.FrameLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
//import com.baidu.location.LocationClientOption.LocationMode;

public class OutdoorService extends Service{
	// 百度地图相关
	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	
	private SharedPreferences.Editor editor;

	// private MyLocationData locData;
	
	

	// BDm Location
	private com.baidu.location.LocationClientOption.LocationMode tempMode = com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy;

	private boolean isFirstLoc = true;
	
	// private int pointCounts = 0;
	
	// private double hi = 0;
	// private double lo = 99999;
	
	public static OutdoorDataManager mOutdoorDataManager;
	
	Context mContext = null;
	FrameLayout container;
	
	private double runTime = 0;
	private double cRunSpeed = 0;
	private boolean showTime;

	public OutdoorService() { // getApplicationContext() needed
		Log.i("Outdoor", "Setting up outdoor.class");
		
		// editor = getApplicationContext().getSharedPreferences("outDoor", 0).edit();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("OutdoorService", "onCreate()");
		
		setUpBDmapClient();
		setUpSensor();
		
		mOutdoorDataManager = new OutdoorDataManager(getApplication());
		mOutdoorDataManager.getHistoryInformation(getApplicationContext());
	}


	public void setUpBDmapClient() {
		// 声明LocationClient类
		Context context = this;
		mLocationClient = new LocationClient(context);
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(1000);
		option.setNeedDeviceDirect(true);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		// setUpSensor(context); // 设置方向传感器。
	}

	public void setUpSensor() {
		Context context = this;
		SensorManager sm = (SensorManager) context
				.getSystemService(context.SENSOR_SERVICE);
		sm.registerListener(
				new SensorEventListener() {
					// 用于传感器监听中，设置灵敏程度
					int mIncrement = 1;
					@Override
					public void onSensorChanged(SensorEvent event) {
						// 方向传感器
						if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
							// x表示手机指向的方位，0表示北,90表示东，180表示南，270表示西
							float x = event.values[SensorManager.DATA_X];
							mIncrement++;
							if (mIncrement >= 7) {
								OutdoorDataManager.locDirection = x;
								if (!isFirstLoc) {
									// 修改定位图标方向
									 OutdoorDataManager.locData = new MyLocationData.Builder()
										.accuracy(OutdoorDataManager.locData.accuracy)
										.direction(x)
										.latitude(OutdoorDataManager.locData.latitude)
										.longitude(OutdoorDataManager.locData.longitude)
										.build();
									mOutdoorDataManager.NotifyUI("direction");
								}
								mIncrement = 1;
							}
						}
					}

					@Override
					public void onAccuracyChanged(Sensor sensor, int accuracy) {
						// TODO Auto-generated method stub

					}

				}, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	/*
	private void NotifyUI(String cmd) {
		Outdoor.NotifyUI();
		super.notifyObservers(cmd);
	}*/

	private class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			runTime++; // 每次收到请求，说明时间度过了一秒
			if (location == null)
				return;
			// Log.i("Outdoor", "data sent");
			
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				OutdoorDataManager.cMapStatus = MapStatusUpdateFactory.newLatLng(ll);
			}
			OutdoorDataManager.locData = new MyLocationData.Builder()
				.accuracy(location.getRadius()).direction(OutdoorDataManager.locDirection)
				.latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();

			if (location.getRadius() > 10) {
				if (OutdoorDataManager.isOutdoor == true) {
					OutdoorDataManager.isOutdoor = 	false;
					mOutdoorDataManager.NotifyUI("isOutdoor");
				}
			}
			else if (!isFirstLoc && location.getRadius() <= 10) {
				if (OutdoorDataManager.isOutdoor == false) {
					OutdoorDataManager.isOutdoor = 	true;
					mOutdoorDataManager.NotifyUI("isOutdoor");
				}
				
				
				// 精度超过9才加入点阵。
				int cColor;
				if (OutdoorDataManager.points.size() <= 1)
					cColor = -1;
				else
					cColor = OutdoorDataManager.getSpeedColor(runTime, 
						new LatLng(OutdoorDataManager.points.get(OutdoorDataManager.points.size() -2).latitute, 
								OutdoorDataManager.points.get(OutdoorDataManager.points.size() -2).longitude),
						new LatLng(location.getLatitude(), location.getLongitude()));
				
				
				boolean isSuccessive = true;
				if (runTime > 10)
					isSuccessive = false;
				
		
				OutdoorDataManager.LocPoint locPoi = new OutdoorDataManager.LocPoint(location.getLatitude(), location
						.getLongitude(), isSuccessive, cColor);
				
				OutdoorDataManager.saveOutdoorData(locPoi);
			
				Log.i("Outdoor", "Loc.color" + locPoi.color /* +  " Loc.time" + locPoi.time */
						+ " radius" + location.getRadius());
				
				OutdoorDataManager.points.add(locPoi);
				
				if (OutdoorDataManager.points.size() >= 2)
					Log.i("OutdoorService", "location Emit");
					mOutdoorDataManager.NotifyUI("location");
				
				runTime = 0; // 重新计时。
				
			}
		}

		public void onREceivePoi(BDLocation poiLocation) {

		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}