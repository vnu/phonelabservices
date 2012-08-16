package edu.buffalo.cse.phonelab.statusmonitors;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class StatusMonitorDump extends IntentService {
	private final String DUMP_DIR = Environment.getExternalStorageDirectory() + "/" + State.DUMP_DIR + "/";
	boolean isFailed = false;
	int transferedFileCounter = 0;
	private static String Timestamp = null;
	String TAG = State.STATUS_TAG;

	public StatusMonitorDump() {
		super("StatusMonitorDump");
	}

	public StatusMonitorDump(String name) {
		super(name);
	}

	private void retreiveDump() {
		try {
			createDumpDir();
			Timestamp = String.valueOf(System.currentTimeMillis());
			String dumpsys = "dumpsys ";
			String dir = " >> " + DUMP_DIR + Timestamp + ".dump";
			// String[] dumpServices ={"battery","batteryinfo","cpuinfo","location","meminfo","netstats","wifi","usagestats"};
			Process p = Runtime.getRuntime().exec("/system/bin/sh -");

			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			for (String service : State.dumpServices) {
				os.writeBytes("echo " + service + ": " + dir + "\n");
				os.writeBytes(dumpsys + service + dir + "\n");
			}
			Log.i(TAG + "Dump", "Dump Successful");
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
			Log.i(TAG + "Dump", "Changing Access and Mod to LOG_DIR");
			try {
				Process process = Runtime.getRuntime().exec("chown 1000:1000 " + f.getAbsolutePath());
				process.waitFor();
				process = Runtime.getRuntime().exec("chmod 700 " + f.getAbsolutePath());
				process.waitFor();
			} catch (IOException e) {
				Log.e(TAG + getClass().getSimpleName(), e.toString());
			} catch (InterruptedException e) {
				Log.e(TAG + getClass().getSimpleName(), e.toString());
			}
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG + "Dump", "In " + getClass().getSimpleName());
		retreiveDump();
	}

}
