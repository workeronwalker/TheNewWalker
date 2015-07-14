package com.example.thenewwalker;


import java.util.ArrayList;

import com.example.thenewwalker.OutdoorDataManager.LocPoint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

public class SettingFragment extends Fragment {
	
	public TextView nameView, dateView, heightView, weightView, goalView;
	private String nameString, dateString, heightString, weightString, goalString, checkedString;
	private Button changeBtn, cleanBtn;
	private Switch lockSwitch;
	public static double stride = 0.7, weight = 55;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.fragment_setting, container, false);
		getActivity().setTitle("个人设置");
		changeBtn = (Button)mView.findViewById(R.id.change);
		nameView = (TextView)mView.findViewById(R.id.name);
		dateView = (TextView)mView.findViewById(R.id.date);
		heightView = (TextView)mView.findViewById(R.id.height);
		weightView = (TextView)mView.findViewById(R.id.weight);
		goalView = (TextView)mView.findViewById(R.id.goal);
		cleanBtn = (Button)mView.findViewById(R.id.cleanning);
		lockSwitch = (Switch)mView.findViewById(R.id.switching);
		
		SharedPreferences reader = getActivity().getSharedPreferences("userProfile", 0);
		nameString = reader.getString("name", "");
		dateString = reader.getString("date", "");
		heightString = reader.getString("height", "");
		weightString = reader.getString("weight", "");
		goalString = reader.getString("goal", "");
		checkedString = reader.getString("isChecked", "");
		String temp = reader.getString("stride", "");
		if (temp.isEmpty())
			stride = 0.7;
		else
			stride = Double.parseDouble(temp);
		
		if (weightString.isEmpty())
			weight = 55;
		else
			weight = Double.parseDouble(weightString);
		
		if (checkedString.equals("YES")) {
			getActivity().startService(new Intent(getActivity(), LockScreenService.class));
			lockSwitch.setChecked(true);
		}
		
		SettingChangeFragment.delegate = this;
		nameView.setText(nameString);
		dateView.setText(dateString);
		heightView.setText(heightString);
		weightView.setText(weightString);
		goalView.setText(goalString);
		
		changeBtn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Fragment mFragment = new SettingChangeFragment();
				FragmentManager mFragmentManager = getFragmentManager();
				mFragmentManager.beginTransaction().replace(R.id.main_container, mFragment).commit();
				RadioGroup main_tab_RadioGroup = (RadioGroup) getActivity().findViewById(R.id.main_tab_RadioGroup);
				main_tab_RadioGroup.setVisibility(View.GONE);
			}
		});
		
		cleanBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				OutdoorDataManager.clearOutdoorData();
				OutdoorDataManager.points = new ArrayList<LocPoint>();
				OutdoorDataManager.pointCounts = 0;
			}
			
		});
		
		lockSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton switchBtn, boolean isChecked) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor editor = getActivity().getSharedPreferences("userProfile", 0).edit();
				if (isChecked) {
	                editor.putString("isChecked", "YES");
					getActivity().startService(new Intent(getActivity(), LockScreenService.class));
				} else {  
					editor.putString("isChecked", "NO");
					getActivity().stopService(new Intent(getActivity(), LockScreenService.class));
                }
				editor.commit();
			}
			
		});
		
		return mView;
		
    }
	
	public void updateData()
	{
		SharedPreferences reader = getActivity().getSharedPreferences("userProfile", 0);
		nameString = reader.getString("name", "");
		dateString = reader.getString("date", "");
		heightString = reader.getString("height", "");
		weightString = reader.getString("weight", "");
		goalString = reader.getString("goal", "");
		checkedString = reader.getString("isChecked", "");
		String temp = reader.getString("stride", "");
		if (temp.isEmpty())
			stride = 0.7;
		else
			stride = Double.parseDouble(temp);
		
		if (checkedString.equals("YES"))
			lockSwitch.setChecked(true);
		nameView.setText(nameString);
		dateView.setText(dateString);
		heightView.setText(heightString);
		weightView.setText(weightString);
		goalView.setText(goalString);
		        
	}
	
}
