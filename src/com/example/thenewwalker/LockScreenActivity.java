package com.example.thenewwalker;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;

public class LockScreenActivity extends Activity {

	/** Called when the activity is first created. */
	KeyguardManager.KeyguardLock k1;
	boolean inDragMode;
	int selectedImageViewX;
	int selectedImageViewY;
	int windowWidth;

	float touch_x;
	float touch_y;
	// ImageView droid, phone, home;
	// int phone_x,phone_y;

	int[] droidpos;
	private static Thread thread;

	private LayoutParams layoutParams;
	View mView;

	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub

		super.onAttachedToWindow();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("LockScreenActivity", "onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_FULLSCREEN);

		windowWidth = getWindowManager().getDefaultDisplay().getWidth();


//		StepCountView.XiuGeKanZheLi = true;
		mView = new StepCountView(this, true);


		setContentView(mView);

		mView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					touch_x = event.getRawX();
					touch_y = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					if (getDistance(touch_x, touch_y, event.getRawX(),
							event.getRawY()) > windowWidth / 2) {

						v.setVisibility(View.GONE);
						finish();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (getDistance(touch_x, touch_y, event.getRawX(),
							event.getRawY()) > windowWidth / 2) {

						v.setVisibility(View.GONE);
						finish();
					}
					break;
				}
				return true;
			}
		});
	}

	double getDistance(float Ax, float Ay, float Bx, float By) {
		return (double) Math.sqrt(((Ax - Bx) * (Ax - Bx) + (Ay - By)
				* (Ay - By)));
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i("LockScreenActivity", "onStart");

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				StepCountFragment.total_step = StepDetector.CURRENT_STEP;

				mView.invalidate();
			}
		};

		thread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(30);
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

	class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				System.out.println("call Activity off hook");
				finish();

				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		// Don't allow back to dismiss.
		return;
	}

	// only used in lockdown mode
	@Override
	protected void onPause() {
		super.onPause();

		// Don't hang around.
		// finish();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Don't hang around.
		// finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (keyCode == KeyEvent.KEYCODE_POWER)
				|| (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
				|| (keyCode == KeyEvent.KEYCODE_CAMERA)) {
			// this is where I can do my stuff
			return true; // because I handled the event
		}
		if ((keyCode == KeyEvent.KEYCODE_HOME)) {

			return true;
		}

		return false;

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER
				|| (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
			// Intent i = new Intent(this, NewActivity.class);
			// startActivity(i);
			return false;
		}
		if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

			System.out.println("alokkkkkkkkkkkkkkkkk");
			return true;
		}
		return false;
	}

	/*
	 * public void unloack(){
	 * 
	 * finish();
	 * 
	 * }
	 */
	public void onDestroy() {
		// k1.reenableKeyguard();
		Log.i("LockScreen", "onDestory");
		super.onDestroy();
	}

}