package com.example.thenewwalker;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.mapapi.SDKInitializer;

public class MainActivity extends FragmentActivity implements OnCheckedChangeListener
{
	public ViewPager main_viewPager ;
	private RadioGroup main_tab_RadioGroup ;
	private RadioButton	step_count, statistic, outdoor, setting;
	public ArrayList<Fragment> fragmentList ;
	private CharSequence mTitle;
	
	public OutdoorFragment mOutdoorFragment;
		
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.startService(new Intent(this, OutdoorService.class));
		setContentView(R.layout.activity_main);
		SDKInitializer.initialize(getApplicationContext());
		InitView();
		InitViewPager();
		mTitle = getTitle();
	}
	
	public void InitView()
	{
		main_tab_RadioGroup = (RadioGroup) findViewById(R.id.main_tab_RadioGroup) ;
		
		step_count = (RadioButton) findViewById(R.id.radio_chats) ;
		statistic = (RadioButton) findViewById(R.id.radio_contacts) ;
		outdoor = (RadioButton) findViewById(R.id.radio_discover) ;
		setting = (RadioButton) findViewById(R.id.radio_me) ;
		
		main_tab_RadioGroup.setOnCheckedChangeListener(this);
	}
	
	public void InitViewPager()
	{
		main_viewPager = (ViewPager) findViewById(R.id.main_ViewPager);
		
		fragmentList = new ArrayList<Fragment>() ;
		
		Fragment stepCountFragment = new StepCountFragment() ;
		Fragment stepStatisticFragment = new StepStatisticFragment();
		Fragment outdoorFragment = new OutdoorFragment();
		Fragment settingFragment = new SettingFragment();
		
		fragmentList.add(stepCountFragment);
		fragmentList.add(stepStatisticFragment);
		fragmentList.add(outdoorFragment);
		fragmentList.add(settingFragment);
		
		main_viewPager.setAdapter(new MyAdapter(getSupportFragmentManager(), fragmentList));
		main_viewPager.setCurrentItem(0);
		main_viewPager.setOnPageChangeListener(new MyListner());
	}
	
	public class MyAdapter extends FragmentPagerAdapter
	{
		ArrayList<Fragment> list ;
		public MyAdapter(FragmentManager fm , ArrayList<Fragment> list)
		{
			super(fm);
			this.list = list ;
		}
		
		@Override
		public Fragment getItem(int arg0) {
			return list.get(arg0);
		}
		
		@Override
		public int getCount() {
			return list.size();
		}
	}

	public class MyListner implements OnPageChangeListener
	{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		@Override
		public void onPageSelected(int arg0) {
			int current = main_viewPager.getCurrentItem() ;
			switch(current)
			{
			case 0:
				main_tab_RadioGroup.check(R.id.radio_chats);
				break;
			case 1:
				main_tab_RadioGroup.check(R.id.radio_contacts);
				break;
			case 2:
				main_tab_RadioGroup.check(R.id.radio_discover);
				break;
			case 3:
				main_tab_RadioGroup.check(R.id.radio_me);
				break;
			}
		}
		
	}
	
	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int CheckedId) 
	{
		int current=0;
		switch(CheckedId)
		{
		case R.id.radio_chats:
			current = 0 ;
			break ;
		case R.id.radio_contacts:
			current = 1 ;
			break;
		case R.id.radio_discover:
			current = 2 ;
			break;
		case R.id.radio_me:
			current = 3 ;
			break ;
		}
		if(main_viewPager.getCurrentItem() != current)
		{
			main_viewPager.setCurrentItem(current);
		}
	}
	
}