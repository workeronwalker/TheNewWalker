package com.example.thenewwalker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StepCountFragment extends Fragment {
		
	public static int total_step = 0;
	public static int user_goal = 0;

	private static View mView;
	
	private static Thread thread, fThread;
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        total_step = StepDetector.CURRENT_STEP;
	        
	        mView.invalidate();

	    }
	 
	};
	
	@SuppressLint("HandlerLeak")
	Handler flashHandler = new Handler() {
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);	 
	    	if ((int)StepDetector.flashCount % 50 != 0)
	    		StepDetector.flashCount-=0.5;
	    	
	        super.handleMessage(msg);	        

	        mView.invalidate();

	    }
	 
	};
	
	private void mThread() {
	    if (thread == null) {
	 
	        thread = new Thread(new Runnable() {
	           public void run() {
	                while (true) {
	                    try {
	                        Thread.sleep(200);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                    if (StepServices.flag) {
	                        Message msg = new Message();
	                        handler.sendMessage(msg);
	                    }
	                }
	            }
	        });
	        thread.start();
	    }
	}
	
	private void flashThread() {
	    if (fThread == null) {
	 
	    	fThread = new Thread(new Runnable() {
	           public void run() {
	                while (true) {
	                	StepCountView.XiuGeKanZheLi = false;
	                    try {
	                        Thread.sleep(20);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                    Message msg = new Message();
	                    flashHandler.sendMessage(msg);
	                }
	            }
	        });
	        fThread.start();
	    }
	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		// StepCountView.XiuGeKanZheLi = false;
		mView = new StepCountView(getActivity(), false);
		
		getActivity().setTitle("计步器");
		init();
		mView.invalidate();
		
        return mView;
    }
	
	
	private void init() {
		Intent intent = new Intent(getActivity(), StepServices.class);
		getActivity().startService(intent);
		
		SharedPreferences reader = getActivity().getSharedPreferences("userProfile", 0);
		String goalString = reader.getString("goal", "0");
		if (!goalString.isEmpty())
			user_goal = Integer.parseInt(goalString);
		else
			user_goal = 0;
        mThread();
        flashThread();

	}

}