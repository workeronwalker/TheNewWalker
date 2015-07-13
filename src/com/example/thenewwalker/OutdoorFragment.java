package com.example.thenewwalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.thenewwalker.OutdoorDataManager.LocPoint;

public class OutdoorFragment extends Fragment implements Observer{

	public static final String DIRECTION_CHANGED = "518002";
	public static final String LOCATION_CHANGED = "518003";
	private TextView outdoorRadius, outdoorHi, outdoorLo;
	
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	
	private LocationMode mCurrentMode = null;
	private BitmapDescriptor mCurrentMarker;
	
	private boolean newlyCreated = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.fragment_outdoor, container,
				false);
		getActivity().setTitle("户外模式");
		// mView.setId(1111);

		Log.i("OutdoorFragment", "onCreate");
		newlyCreated = true;

		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i("OutdoorFragment", "onStart");
		if (newlyCreated == true) {
			newlyCreated = false;
			View fragmentView = this.getView();
			FrameLayout mFramLayout = (FrameLayout) fragmentView.findViewById(R.id.frameLayout_outdoor);
			outdoorRadius = (TextView) fragmentView.findViewById(R.id._outdoor_radius);
			outdoorLo = (TextView) fragmentView.findViewById(R.id._outdoor_lo);
			outdoorHi = (TextView) fragmentView.findViewById(R.id._outdoor_hi);
			
			mMapView = getBDmapView();
			// mMapView.setZ(-1);
			mFramLayout.addView(mMapView);
			
			mBaiduMap = mMapView.getMap();
			mCurrentMode = LocationMode.FOLLOWING;
			mCurrentMarker = null;
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, mCurrentMarker));
			mBaiduMap.setMyLocationEnabled(true);
		}
		
		OutdoorService.mOutdoorDataManager.deleteObservers();
		OutdoorService.mOutdoorDataManager.addObserver(this);
		
		if (!OutdoorDataManager.points.isEmpty()) {	// 有记录的点阵
			drawHistroy();
		}
	}
	@Override
	public void onResume() {  
        super.onResume();
        Log.i("OutdoorFragment", "onResume");
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();  
    }

	
	private void drawHistroy() {
		if (OutdoorDataManager.points.size() <= 2)
			return;
		for (int i = 1; i < OutdoorDataManager.points.size(); i++) {
			if (OutdoorDataManager.points.get(i-1).isSuccessive == true)
				drawLine(OutdoorDataManager.points.get(i-1), OutdoorDataManager.points.get(i));
				
		}
	}

	public MapView getBDmapView() {
		Log.i("Outdoor", "Setting up BDmapView");

		// 初始化地图
		BaiduMapOptions mapOptions = new BaiduMapOptions();
		mapOptions.scaleControlEnabled(false); // 隐藏比例尺控件
		mapOptions.zoomControlsEnabled(false); // 隐藏缩放按钮
		mapOptions.mapStatus(new MapStatus.Builder().zoom(18).build());
		MapView mMapView = new MapView(getActivity(), mapOptions);
		mMapView.setLayoutParams(new ViewGroup.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mMapView.setClickable(true);

		return mMapView;
	}

	@Override
	public void update(Observable observable, Object data) {
		// Log.i("OutdoorFragment", data.toString() + " Received");
		
		if (data.toString() == "direction") {
			mBaiduMap.setMyLocationData(OutdoorDataManager.locData);
		}
		if (data.toString() == "location") {
			if (OutdoorDataManager.points.size() <= 2)
				return;
			mBaiduMap.animateMapStatus(OutdoorDataManager.cMapStatus);
			mBaiduMap.setMyLocationData(OutdoorDataManager.locData);
			
			int i = OutdoorDataManager.points.size() -1;
			drawLine(OutdoorDataManager.points.get(i-1), OutdoorDataManager.points.get(i));
		}
	}
	
	private void drawLine(LocPoint lPoint, LocPoint cPoint) {
		List<LatLng> lcPoints = new ArrayList<LatLng>();

		lcPoints.add(new LatLng(lPoint.latitute, lPoint.longitude));
		lcPoints.add(new LatLng(cPoint.latitute, cPoint.longitude));
			
		OverlayOptions ooPoly = new PolylineOptions()
			.color(cPoint.color)
			.width(10)
			.points(lcPoints);
		mBaiduMap.addOverlay(ooPoly);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (OutdoorService.mOutdoorDataManager.countObservers() > 0)
			OutdoorService.mOutdoorDataManager.deleteObservers();
		mMapView.onPause();
	}
	
	@Override  
    public void onDestroy() {  
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();  
    } 
}