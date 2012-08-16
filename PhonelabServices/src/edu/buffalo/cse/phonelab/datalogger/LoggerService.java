package edu.buffalo.cse.phonelab.datalogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * Maintains the log services. 
 * 
 * @author mike
 *
 */
public class LoggerService extends Service {
	private final IBinder mBinder = new LogBinder();
	private Timer timer = new Timer();
	private final String LOG_DIR = Environment.getExternalStorageDirectory() + "/" + Util.LOG_DIR + "/";
	private SharedPreferences settings;
	private Editor editor;
	boolean isFailed = false;
	int transferedFileCounter = 0;  //temp global variables. check TODO

	/**
	 * start the logging data
	 */
	public void onCreate() {
		super.onCreate();
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Start Logger Service");
		settings = getApplicationContext().getSharedPreferences(Util.SHARED_PREFERENCES_FILE_NAME, 0);
		editor = settings.edit();

		start();
	}

	public void start() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Log.i("PhoneLab-" + getClass().getSimpleName(), "Service Running");
				// Check for LogCat Process
				checkLogCatProcess();

			}
		},0, Util.UPDATE_INTERVAL);
	}

	/**
	 * Checks whether LogCat Process is running or not, if not initiates the process 
	 */
	private void checkLogCatProcess() {
		List<Integer> pids = getPID("logcat");
		int pidFromDb = settings.getInt(Util.SHARED_PREFERENCES_DATA_LOGGER_PID, -1);
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Logcat pid from database " + pidFromDb);
		boolean foundLogCat = false;
		if (pids.size() > 0) {
			for(int pid:pids) {
				// If logcat is found
				if(pidFromDb == pid) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Logcat process found " + pid);
					// Process Found
					if (logFileThreshold()) {
						renameLogFiles();
					}
					foundLogCat = true;
					//break;
				} else {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Killing bogus process " + pid);
					// Kill other bogus processes
					android.os.Process.killProcess(pid);
					// Start a new LogCat process
					foundLogCat = false;
				}
			}
			// Start LogCat if no process found
			if (!foundLogCat) {
				startLogcat();
			}
		} else {
			startLogcat();
		}
	}
	
	
	private void stopLogCatProcess() {
		List<Integer> pids = getPID("logcat");
		if (pids.size() > 0) {
			for(int pid:pids) {
				
					Log.i("PhoneLab-" + getClass().getSimpleName(), "Killing logcat process " + pid);
					// Kill other bogus processes
					android.os.Process.killProcess(pid);
					
				}
			}
		editor.putInt(Util.SHARED_PREFERENCES_DATA_LOGGER_PID, -1);
		editor.commit();
			
		} 
	

	/**
	 * Checks if there exist a auxilary waiting to transfer log file 
	 * 
	 * @return true if there exist a auxilary waiting to transfer log file, false otherwise
	 */
	private boolean logFileThreshold() {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Checking Threshold");
		File[] allFiles = new File(LOG_DIR).listFiles();
		if (allFiles != null) {
			if (allFiles.length > 1) {
				for(int i=0; i<allFiles.length; i++) {
					String fileName = allFiles[i].getName();
					if (fileName.startsWith("log.out.") || fileName.startsWith("1")) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Check for valid Log Files.
	 * Log.out is continously written
	 * Log.out.1
	 * Log.out.2
	 * ...
	 * Log.out.n will be created in worst case
	 * else Log.out.1 will be transferred successfully
	 * After Successful transfer, delete all files except Log.out
	 * 
	 * @param String filename - filename
	 * @return boolean
	 */
	private void renameLogFiles() {
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Starting to rename files");
		File[] allFiles = new File(LOG_DIR).listFiles();
		Log.i("PhoneLab-" + getClass().getSimpleName(), "Files found .. " + allFiles.length);
		if (allFiles.length > 1) {
			for(int i=0; i<allFiles.length; i++) {
				String fileName = allFiles[i].getName();
				if (fileName != "log.out" && fileName.startsWith("log.out.")) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), "File " + i + " " + allFiles[i].getName());
					if (allFiles[i].renameTo(new File(LOG_DIR + System.currentTimeMillis() + ".log"))) {
						Log.i("PhoneLab-" + getClass().getSimpleName(), "Renamed successfully");
					} else {
						Log.w("PhoneLab-" + getClass().getSimpleName(), "Renamed failed");
					}
				}
			}
		} else { // No File
			Log.i("PhoneLab-" + getClass().getSimpleName(), "No Log file exist");
		}

		ConnectivityManager myConnManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(myConnManager != null){
			if(myConnManager.getActiveNetworkInfo() != null){
				if(myConnManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI || myConnManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE) {
					isFailed = false;
				} else {
					isFailed = true;
				}
			} 
		}
	}

	/**
	 * Create Log Dir if it doesn`t exist
	 * @return boolean
	 */
	private void createLogDir() {
		File f = new File(LOG_DIR);
		if (f.exists() && f.isDirectory()) {
			return;
		} else {
			f.mkdirs();
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Changing Access to LOG_DIR");
			try {
				Process process = Runtime.getRuntime().exec("chown 1000:1000 " + f.getAbsolutePath() );
				process.waitFor();
				process = Runtime.getRuntime().exec("chmod 700 " + f.getAbsolutePath() );
				process.waitFor();
			} catch (IOException e) {
				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
			} catch (InterruptedException e) {
				Log.e("PhoneLab-" + getClass().getSimpleName(), e.toString());
			}
		}
	}

	/**
	 * Returns PIDs of process
	 * @param String processName - process name
	 * @return List<Integer> pids - 0 is not found and pid > 0 is found
	 */
	private List<Integer> getPID(String processName) {
		List<Integer> pids = new ArrayList<Integer>();

		StringBuilder psString = new StringBuilder();
		try {
			Process process = Runtime.getRuntime().exec("ps");// Verbose filter
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				// check whether processName exists in line
				if (line.endsWith(processName)) {
					psString.append(line + "\n");
				}
			}
		} catch (IOException e) {
			Log.e("PhoneLab-" + getClass().getSimpleName(),e.toString());
		}
		// Check whether some processes were collected & Terminated is not contained
		if(psString.length() > 0 && !psString.toString().matches("Terminated")) {
			String[] splitString = psString.toString().split("\n");

			for(int i = 0; i<splitString.length; i++) {
				if (splitString[i].startsWith("app_")) {
					Log.i("PhoneLab-" + getClass().getSimpleName(), splitString[i].toString());
					String[] splitPs = splitString[i].split("\\s+");
					pids.add(Integer.parseInt(splitPs[1]));
				}
			}
		}

		return pids;
	}

	/**
	 * Create Logcat process if it doesn`t exist
	 */
	private void startLogcat() {
		//Process process;
		int pid = 0;
		try {
			Log.i("PhoneLab-" + getClass().getSimpleName(), "Logcat process not found, starting process");
			
			String filters = settings.getString(Util.SHARED_PREFERENCES_LOGCAT_FILTERS, "*:V");
			
			
			if(!filters.equals("*:V")){
				filters = filters + " *:S";//setting all to silent except the specified filters
			}
			
			// create log dir if it doesn`t exist
			createLogDir();
			System.out.println(LOG_DIR);
			
			String logcatcommand = "logcat -v threadtime"+" -f " + LOG_DIR + "log.out  -r " + Util.LOG_FILE_SIZE + " -n " + Util.AUX_LOG_FILES + " " + filters +" &";
			Log.d("PhoneLab-" + getClass().getSimpleName(), "Logcat COMMAND " + logcatcommand);
			Runtime.getRuntime().exec(logcatcommand);
			pid = getPID("logcat").iterator().next();
			editor.putInt(Util.SHARED_PREFERENCES_DATA_LOGGER_PID, pid);
			editor.commit();
			Log.i("PhoneLab-" + getClass().getSimpleName(), "PID to db " + pid);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(),e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(),e.toString());
		}
	}
	
	
	
	/**
	 * Stop Logging. Service Killed. Timer Stopped.
	 */
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}

		Log.i("PhoneLab-" + getClass().getSimpleName(), "Logger Service is destroyed");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class LogBinder extends Binder {
		public LoggerService getService() {
			return LoggerService.this;
		}
	}
}
