package edu.buffalo.cse.phonelab.statusmonitor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Util;

public class StatusMonitorDump extends Service {
	private final String DUMP_DIR = Environment.getExternalStorageDirectory() + "/" + Util.DUMP_DIR + "/";
	private final IBinder mBinder = new DumpBinder();
	private Timer timer = new Timer();
	boolean isFailed = false;
	int transferedFileCounter = 0;
	private static String Timestamp = null;

	public void onCreate() {
		super.onCreate();
		Log.i(Util.STATUS_TAG + getClass().getSimpleName(), "Start Dump Service");
		start();
	}

	public void start() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Log.i(Util.STATUS_TAG + getClass().getSimpleName(), "Service Running");
				// Check for LogCat Process
				submit();

			}
		}, 0, 1000 * 60 * 10);
	}

	private void submit() {
		try {
			createDumpDir();
			Timestamp = String.valueOf(System.currentTimeMillis());
			String dumpsys = "dumpsys ";
			String dir = " >> " + DUMP_DIR + Timestamp + ".dump";
			// String[] dumpServices ={"battery","batteryinfo","cpuinfo","location","meminfo","netstats","wifi","usagestats"};
			Process p = Runtime.getRuntime().exec("/system/bin/sh -");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			for (String service : StatusMonitor.dumpServices) {
				os.writeBytes("echo " + service + ": " + dir + "\n");
				os.writeBytes(dumpsys + service + dir + "\n");
			}
			Log.i(Util.STATUS_TAG, "Dump Successful");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create Dump Directory if it doesn't exist
	 * 
	 */
	private void createDumpDir() {
		File f = new File(DUMP_DIR);
		if (f.exists() && f.isDirectory()) {
			return;
		} else {
			f.mkdirs();
			Log.i(Util.STATUS_TAG + getClass().getSimpleName(), "Changing Access and Mod to LOG_DIR");
			try {
				Process process = Runtime.getRuntime().exec("chown 1000:1000 " + f.getAbsolutePath());
				process.waitFor();
				process = Runtime.getRuntime().exec("chmod 700 " + f.getAbsolutePath());
				process.waitFor();
			} catch (IOException e) {
				Log.e(Util.STATUS_TAG + getClass().getSimpleName(), e.toString());
			} catch (InterruptedException e) {
				Log.e(Util.STATUS_TAG + getClass().getSimpleName(), e.toString());
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class DumpBinder extends Binder {
		public StatusMonitorDump getService() {
			return StatusMonitorDump.this;
		}
	}
}
