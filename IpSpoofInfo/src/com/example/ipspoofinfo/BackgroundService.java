package com.example.ipspoofinfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class BackgroundService extends Service {
	
	private static int interval = 300000;
	
	private static boolean running = false;
	
	private static Timer timer;
	
	public static final String INFO_FILE = "ipspoofinfo.txt";
	public static final String SEPARATOR = ";";
	public static final String LOG_FILE = "ipspoofinfo.log";

	private static MobileInfo mobileInfo; 
	private static WakeLock wakeLock;
	
	private PowerManager pm;
	
	private static Handler msgClient;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		timer = new Timer();
		mobileInfo = new MobileInfo(this);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "backgroundservice");
	}
	
	public static void start() {
		setRunning(true);
		wakeLock.acquire();
		reset();		
		
	}
	
	public static void stop() {
		setRunning(false);;
		wakeLock.release();
		timer.cancel();
		
	}
	
	public static int getInterval() {
		return interval;
	}

	public static void setInterval(int interval) {
		BackgroundService.interval = interval;
		notifyIntervalUpdate();
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		BackgroundService.running = running;
		notifyStatusUpdate();
	}
	
	public static String getPhoneIp() {
		return MobileInfo.getLocalIpAddress();
	}
	
	public static void registerMsgClient(Handler client) {
		msgClient = client;
		
		notifyStatusUpdate();
		notifyIntervalUpdate();
		notifyPhoneIpUpdate();
	}
	
	public static void deregisterMsgClient(Handler client) {
		if (msgClient == client) {
			msgClient = null;
		}
		
		/*Message msg = new Message();
		msg.obj = "Lookup Time: " + result;
		if (client != null) {
			client.sendMessage(msg);
		}*/
	}
	
	private static void notifyStatusUpdate() {		
		if (msgClient != null) {
			Message msg = new Message();
			msg.what = MainActivity.MSG_STATUS_UPDATE;
			msgClient.sendMessage(msg);			
		}		
	}
	
	private static void notifyIntervalUpdate() {
		if (msgClient != null) {
			Message msg = new Message();
			msg.what = MainActivity.MSG_INTERVAL_UPDATE;
			msgClient.sendMessage(msg);			
		}		
	}
	
	private static void notifyPhoneIpUpdate() {
		if (msgClient != null) {
			Message msg = new Message();
			msg.what = MainActivity.MSG_PHONEIP_UPDATE;
			msgClient.sendMessage(msg);			
		}		
	} 

	public static void reset() {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new UpdateTask(), 0, interval);
	}
	
	public static void runOnce() {
		if (timer == null) {
			timer = new Timer();
		}
		timer.schedule(new UpdateTask(), 0);		
	}
	
	private static class UpdateTask extends TimerTask {

		@Override
		public void run() {			
			StringBuffer sb = new StringBuffer();
			sb.append(System.currentTimeMillis());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getOperatorName());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(Build.MODEL);
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getNetworkTech());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getNetworkType());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getCellId());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getGeoLat());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getGeoLong());
			//sb.append(mobileInfo.getGeoLocation());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getLocalIpAddress());			
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getPhoneNum());
			sb.append(BackgroundService.SEPARATOR);
			sb.append(mobileInfo.getTraceRoute());
			String info = sb.toString();
			
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + INFO_FILE);
			try {
				PrintWriter writer = new PrintWriter(file);
				writer.println(info);
				Log.d("ipspoofinfo", "Update: " + info);
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("ipspoofinfo", e.toString());
			}
			
			File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LOG_FILE);
			
			try {
				FileOutputStream stream = new FileOutputStream(logFile, true);
				PrintWriter writer = new PrintWriter(stream);
				writer.println(info);
				//Log.d("ipspoofinfo", "Update: " + info);
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//Log.d("ipspoofinfo", e.toString());
			}
			
			notifyPhoneIpUpdate();
			
		}
		
	}

}
