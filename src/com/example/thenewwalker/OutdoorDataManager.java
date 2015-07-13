package com.example.thenewwalker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.FrameLayout;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class OutdoorDataManager extends Observable{
	// 百度地图相关
	public static SharedPreferences.Editor editor = null;

	// BDm Location
	private com.baidu.location.LocationClientOption.LocationMode tempMode = com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy;

	public static boolean isFirstLoc = true;
	public static List<LocPoint> points = new ArrayList<LocPoint>();
	public static int pointCounts = 0;
	
	static double hi = 0;
	static double lo = 99999;
	public static double runTime;
	public static double runSpeed;
	public static float direction;
	public static MyLocationData locData;
	
	public static float locDirection = 0;
	public static double distance;
	public static boolean isOutdoor;
	
	public static MapStatusUpdate cMapStatus;
	
	Context mContext = null;
	FrameLayout container;
	
	boolean showTime;

	public OutdoorDataManager(final Context context) { // Application acquired
		Log.i("Outdoor", "Setting up outdoor.class");
		editor = context.getSharedPreferences("outDoor", 0).edit();
		getHistoryInformation(context);
	}

	public void getHistoryInformation(final Context context) {
		
		SharedPreferences reader = context.getSharedPreferences("outDoor", 0);
		int i = 0;
		int errorCount = 0;
		while (true) {
			LocPoint locPoi = new LocPoint(
					(double)reader.getFloat("latitude" + i, -1), 
					(double)reader.getFloat("longitude" + i, -1), 
					reader.getBoolean("isSuccessive" + i, false), 
					reader.getInt("color" + i, -1));
			Log.i("OutdoorDataManager", "history " + i + ": " + Integer.toHexString(locPoi.color));
			
			if (locPoi.latitute == -1 || locPoi.longitude == -1 || locPoi.color == -1) {
				errorCount++;
				if (errorCount >= 20)
					return;
			}
			else {
				points.add(locPoi);
			}
			i++;
		}
	}
	
	public void NotifyUI(String cmd) {
		super.setChanged();
		super.notifyObservers(cmd);
	}

	public static int getSpeedColor(double time, LatLng point1, LatLng point2) {
		try {
			double distance = DistanceUtil.getDistance(point1, point2);
			OutdoorDataManager.distance += distance;
			Log.i("Outdoor", "distance: " + OutdoorDataManager.distance);
			double speed = (distance / time) * 0.8 + runSpeed * 0.2;
			runSpeed = speed;
			if (speed > hi) {
				hi = speed;
				// outdoorHi.setText("" + hi);
			}
			if (speed < lo) {
				lo = speed;
				// outdoorLo.setText("" + lo);
			}
			
			int rColor = 0xAAFFFF00;
			double slow, sMid, fMid, fast;
			// 0xAAFF0000
			slow = 1.5;
			// (0xAAFF0000+(int)(65280 / (sMid - slow) * (speed-slow)))
			// ret - ret%256
			sMid = 4.5;
			// 0xAAFFFF00
			fMid = 5.5;
			// (0xAAFFFF00-(int)(16711680 / (fast - fMid) * (speed-fMid)));
			// ret - ret%256
			fast = 8.5;
			// 0xAA00FF00
			
			if (speed <= slow)	{		// [0, 0.5]
				rColor = 0xAAFF0000;
				System.out.println(rColor);
			}
			else if (speed >= fast) {	// [7.5, ~]
				rColor = 0xAA00FF00;
				System.out.println(rColor);
			}
			else if (speed < sMid) { 	// [0.5, 3.5] 21760 * 3
				int ret = (0xAAFF0000+(int)(65280 / (sMid - slow) * (speed-slow)));
				rColor = ret - ret%256;
				System.out.println(rColor);
			}
			else if (speed <= fMid && speed >= sMid) {
				int ret = 0xAAFFFF00;
				rColor = ret;
				System.out.println(rColor);
			}
			else if (speed > fMid) { 	// [4.5, 7.5] FFFF 00FF
				int ret = (0xAAFFFF00-(int)(16711680 / (fast - fMid) * (speed-fMid)));
				rColor = ret - ret%65536 - 256;
				System.out.println(rColor);
			}
			return rColor;
		}
		catch (Exception e) {
			return 0xAAFFFF00;
		}
	}

	public static class LocPoint {
		public double latitute;
		public double longitude;
		public boolean isSuccessive;
		// public double time;
		public int color;
		LocPoint(double lat, double lon, boolean isOut, int clr) {
			this.latitute = lat;
			this.longitude = lon;
			this.isSuccessive = isOut;
			// this.time = t;
			this.color = clr;
		}
	}
	
	public static void saveOutdoorData(OutdoorDataManager.LocPoint locPoi) {
		if (editor == null)
			return;
		int i = OutdoorDataManager.points.size();
        editor.putInt("tempData" + i, StepDetector.CURRENT_STEP);
        editor.putFloat("latitude" + i, (float)locPoi.latitute);
        editor.putFloat("longitude" + i, (float)locPoi.longitude);
        editor.putBoolean("isSuccessive" + i, locPoi.isSuccessive);
        editor.putInt("color" + i, locPoi.color);
        editor.commit();
	}
	
	public static void clearOutdoorData() {
		File file = new File("/data/data/com.example.walker"
	            + "/shared_prefs", "outDoor.xml");  
		if (file.exists()) {
			Log.i("HealthStaticsFragment", "Delete success");
			OutdoorDataManager.points.clear();
			file.delete();
		}
		
		else
			Log.i("HealthStaticsFragment", "Delete failed");
	}
}