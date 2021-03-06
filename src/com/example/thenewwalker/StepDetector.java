package com.example.thenewwalker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class StepDetector implements SensorEventListener {

	private static final float GRAVITY = 9.08665f;
	private static final float NOISE = 0.0001f;

//	private static final float ZSCALE = 0.8660f; // 用于消除Z轴的影响，检测手机3轴状态
	private static final float ZSCALE = 0.9397f;


	public static int CURRENT_STEP = 0;

	public static double ACCELRATE = 0;
	public static double XY = 0;
	public static double Z = 0;

	private ArrayList<Double> detectedDataOfOneStep = new ArrayList<Double>();
	private ArrayList<Double> filtedDataOfOneStep = new ArrayList<Double>();

	private ArrayList<Double> thredholdBackup = new ArrayList<Double>();
	private ArrayList<Double> biggestValue = new ArrayList<Double>();


	private static boolean gravityRead = false;
	private static boolean accelerationRead = false;
	private static boolean isInHand = false;

	private float xGravity;
	private float yGravity;
	private float zGravity;
	private float xAcceleration;
	private float yAcceleration;
	private float zAcceleration;

	public static double lastBigger;
	public static double lastSmaller;
	public static double zBigger;
	public static double zSmaller;

	public static Context mContext;


	private double vertical;

	public static double flashCount = 0;
	public static int steadyCount = 0;

	private static double pin;

	private float light;

	/**
	 * 传入上下文的构造函数
	 * 
	 * @param context
	 */
	public StepDetector(Context context) {
		super();
		mContext = context;
	}

	// 当传感器检测到的数值发生变化时就会调用这个方法
	public void onSensorChanged(SensorEvent event) {

		Sensor sensor = event.sensor;

		synchronized (this) {

			if (sensor.getType() == Sensor.TYPE_LIGHT) {
				light = event.values[0];
				if (light > 5) {
					pin = 6;
					isInHand = true;
				} else {
					pin = 3;
					isInHand = false;
				}
			}

			if (sensor.getType() == Sensor.TYPE_GRAVITY) {
				xGravity = event.values[0];
				yGravity = event.values[1];
				zGravity = event.values[2];
				gravityRead = true;
			}

			if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				xAcceleration = event.values[0];
				yAcceleration = event.values[1];
				zAcceleration = event.values[2];
				accelerationRead = true;
			}

			if (gravityRead && accelerationRead) {

				if (zGravity < GRAVITY * ZSCALE) {
					double lenA = Math.sqrt(xAcceleration * xAcceleration
							+ yAcceleration * yAcceleration + zAcceleration
							* zAcceleration);

					double lenG = Math.sqrt(xGravity * xGravity + yGravity
							* yGravity + zGravity * zGravity);
					double AG = xGravity * xAcceleration + yGravity
							* yAcceleration + zGravity * zAcceleration;
					double cosAG = AG / (lenA * lenG);

					vertical = -lenA * cosAG;

					int stepIncrease = 0;
					// 调用算法
					
					// isInHand = true;
					if (isInHand) {
						if (!currentInHand) {
							thHeight = 0.3;
							thLowest = -0.3;
							thWeightLo = 0;
							thWeightHi = 400;
							increase = 1;
							StepCountView.myGreen = Color.rgb(204, 51, 0);
						}
						stepIncrease = howManyStep(vertical);
					}
					else {
						if (currentInHand) {
							thHeight = 3;
							thLowest = -3;
							thWeightLo = 50;
							thWeightHi = 400;
							increase = 2;
							StepCountView.myGreen = Color.rgb(50, 200, 80);
						}
						stepIncrease = howManyStep(vertical);
						CURRENT_STEP += stepIncrease;
					}
					
					if (isInHand && stepIncrease > 0) {
						DealInterval(stepIncrease);
					}

				}
			}
		}
	}

	private static int isInHandCount = 0;
	private static boolean currentInHand = false;
	
	private static final long MININTERVAL = 300; // 两次计数之间的最小时间间隔
	private static long MAXINTERVAL = 4000; // 两次计数之间的最大时间间隔
	private static final long DETECTTOLERANCE = 1200;
	
	private static long start = 0; // 用于计算两次计步之间的时间间隔，消除噪点
	private static long end = 0;
//	private static long lastDetect = 0;
	
	public static double avgInterval;
	private static double intervalSum = 0;
	private static int illegalCount = 0;
	private static int pendingSteps = 0;
	public static long interval;
	private static int countTooShort = 0;
	public static int inCommingInc = 0;
	
	private static final double TOLERANCE = 0.4; // 1.4

	static void DealInterval(int stepInc) {
		end = System.currentTimeMillis();
		
		if (start == 0)
			start = end;
//		if (lastDetect == 0)
//			lastDetect = end;
		interval = end - start;
		
		if (interval > MAXINTERVAL) { 	// 间隔太长
			StepCountView.isSteady = false;
			avgInterval = 0;
			pendingSteps = 0;
			intervalSum = 0;
			start = end;
			countTooShort = 0;
			inCommingInc = 0;
//			lastDetect = end;
		}
		if (StepCountView.isSteady == false && pendingSteps <= 7)
			inCommingInc++;
		if (interval >= MININTERVAL && interval <= MAXINTERVAL && StepCountView.isSteady == false) {
			pendingSteps += stepInc;
			intervalSum += interval;
			if (pendingSteps == 3 || pendingSteps == 5 || pendingSteps == 6) {
				if (inCommingInc > pendingSteps  + 3) {
					pendingSteps = 0;
					intervalSum = 0;
					flashCount = 0;
					inCommingInc = 0;
				}
			}
			flashCount = pendingSteps * 5;
			start = end;
//			lastDetect = end;
			if (pendingSteps >= 10) {
				StepCountView.isSteady = true;
				CURRENT_STEP += pendingSteps-1;
				avgInterval = intervalSum / pendingSteps;
				pendingSteps = 0;
				intervalSum = 0;
				flashCount = 0;

			}
		} else {
			// writer.write("10" + "," + "-10" + ",");
		}

//		if (interval >= MININTERVAL && interval <= MAXINTERVAL && StepCountView.isSteady
//				&& (interval <= avgInterval * (2 + TOLERANCE) 
//						&& (interval >= avgInterval * (2 - TOLERANCE)))) {
//			stepInc++;
//		}

		if (interval >= MININTERVAL && interval <= MAXINTERVAL && StepCountView.isSteady) {
			if ((interval <= avgInterval * (1 + TOLERANCE)
					&& (interval >= avgInterval * (1 - TOLERANCE)))) { // 稳定迈步
					CURRENT_STEP += stepInc;
					avgInterval = avgInterval * 0.8 + interval * 0.2;
					MAXINTERVAL = (long)avgInterval * 4;
					start = end;
					
					steadyCount = 1;
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							steadyCount = 0;
							cancel();
						}
					}, 50);
			}
			
			if ((interval >= avgInterval * (1 + TOLERANCE)) && StepCountView.isSteady) {
				start = end;
			}
		}
	}
	
	static double llStep;
	static double lStep;
	static double cStep;
	static double maxMax = 0;
	static double minMin = 0;
	static int maxInterval = 0;
	static int minInterval = 0;
	static boolean isMaxPin = false;
	static boolean isMinPin = false;
	static int countMax = 0;
	static int countMin = 0;
	static int countMaxMax = 0;
	static int countMinMin = 0;
	static boolean isPeak = true;
	static double average = 0;
	static boolean isMadMax = false;
	int peakMax = 0, peakMin = 0;
	double lastMaxPeak = 0, lastMinPeak = 0;
	boolean openForMad = true;
	int countMiddle = 0;
	int countPeak = 0;

	double thHeight = 0.6;
	double thLowest = -0.6;
	double thWeightLo = 0;
	double thWeightHi = 400;
	int increase = 1;

	static long cSystemMillisecond = 0;
	static long lSystemMillisecond = 0;

	public static int interval() {
		cSystemMillisecond = System.currentTimeMillis();
		int interval = (int) (cSystemMillisecond - lSystemMillisecond);
		lSystemMillisecond = cSystemMillisecond;
		return interval;
	}

	private int howManyStep(double inputStep) {

		llStep = lStep;
		lStep = cStep;
		cStep = inputStep;
		average = (llStep + lStep + cStep) / 3;
		int count = 0;

		if (average > maxMax || average < minMin) {
			if (average > maxMax) {
				if (isMadMax == false) { // isMadMax标记出是在波峰还是波谷
					isMadMax = true;
					lastMinPeak = minMin;
					if (lastMinPeak > -15) {
						peakMin++;
						thLowest = thLowest * 0.9 + 0.1 * (lastMinPeak / 2.5);
					}
				}
				maxMax = average;
				lastMinPeak = minMin;
				minMin = thLowest;
			} else if (average < minMin) {
				if (isMadMax == true) {
					isMadMax = false;
					lastMaxPeak = maxMax;
					if (lastMaxPeak < 15) {
						peakMax++;
						thHeight = thHeight * 0.9 + 0.1 * (lastMaxPeak / 2.5);
					}
				}
				minMin = average;
				lastMaxPeak = maxMax;
				maxMax = thHeight;
			}
		} else {
			// writer.write("10" + "," + "-10" + ",");
		}

		if (average <= thHeight && average >= thLowest) {
			// writer.write("-4,");
			countMiddle++;
			if (countMiddle > thWeightLo)
				openForMad = true;

			if (countMiddle > thWeightHi) {
				countMiddle = 0;
				openForMad = false;
				peakMin = 0;
				peakMax = 0;
			}
		} else {
			// writer.write("0," );
			countPeak++;
			if (countPeak > 30) {
				openForMad = false;
				countMiddle = 0;
			}
			countMiddle = 0;
		}
		// if (openForMad)
		// writer.write("-12,");
		// else
		// writer.write("0,");

		if (((peakMin >= 1 && peakMax >= 1)) && openForMad) {
			count += increase;
			// count++;
			peakMin = 0;
			peakMax = 0;
			// writer.write(((Double)average).toString()+",7");
			// writer.newLine();
			countMiddle = 0;
			openForMad = false;
		} else {
			// writer.write(((Double)average).toString()+",0");
			// writer.newLine();
		}

		// writer.flush();
		return count;
	}

	double cInStep, lInStep, llInStep, smallValue, lsmallValue, llsmallValue;
	int inMaxInterval = 0, inMinInterval = 0;

	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

}
