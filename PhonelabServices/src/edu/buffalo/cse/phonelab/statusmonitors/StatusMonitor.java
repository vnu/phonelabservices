package edu.buffalo.cse.phonelab.statusmonitors;

/**
 * @author Phonelab
 *
 */
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathExpressionException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.util.Log;
import edu.buffalo.cse.phonelab.manifest.PhoneLabManifest;
import edu.buffalo.cse.phonelab.manifest.PhoneLabParameter;
import edu.buffalo.cse.phonelab.utilities.Util;

/**
 * StatusMonitor is a service that has an Alarm Manager set periodically based on time interval defined in the Manifest.
 * 
 */

public class StatusMonitor extends Service {
	long counter;

	long timerInterval = 1000 * 60 * 1;
	LocationManager locationManager;
	LocationListener locationListener = null;
	TelephonyManager mTelManager;
	TelephonyManager telManager;
	PhoneStateListener mSignalListener;
	BroadcastReceiver myBatteryReceiver;
	String TAG = State.STATUS_TAG;
	int mPhoneType;
	private Handler mHandler;
	private ScheduledThreadPoolExecutor mExecutor;
	Boolean passiveFlag;
	Boolean activeFlag;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		counter = 0;
		mHandler = new Handler();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		passiveFlag = false;
		activeFlag = false;
		Log.i(TAG, "New Status Monitoring is started");
		mExecutor = new ScheduledThreadPoolExecutor(7);

		setIntervalValues();
		getLocation();
		// scheduleServices();
		scheduleBattery();
		scheduleCellLocation();
		scheduleDump();
		schedulePassiveLocation();
		scheduleActiveLocation();
		scheduleSignalStrength();

		return START_REDELIVER_INTENT;
	}

	public void scheduleBattery() {
		mExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				getBattery();
			}
		}, 0, State.getmBatteryInterval(), TimeUnit.MILLISECONDS);
	}

	public void scheduleCellLocation() {
		mExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				getCellLocation();
			}
		}, 0, State.getmCellLocationInterval(), TimeUnit.MILLISECONDS);
	}

	public void scheduleDump() {
		mExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Intent dumpIntent = new Intent(getApplicationContext(), StatusMonitorDump.class);
				startService(dumpIntent);
			}
		}, 0, State.getmDumpInterval(), TimeUnit.MILLISECONDS);
	}

	public void schedulePassiveLocation() {
		mExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (locationListener == null || !passiveFlag) {
							passiveFlag = true;
							locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, State.getmPassiveLocationInterval(), 0,locationListener);
							// getLocation();
						}
					}
				});
			}
		}, 0, State.getmPassiveLocationInterval(), TimeUnit.MILLISECONDS);
	}

	public void scheduleActiveLocation() {
		mExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (locationListener == null || !activeFlag) {
							activeFlag = true;
							locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, State.getmActiveLocationInterval(), 0,locationListener);
						//	getLocation();
						}
					}
				});
			}
		}, 0, State.getmActiveLocationInterval(), TimeUnit.MILLISECONDS);
	}

	public void scheduleSignalStrength() {
		mExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						getSignalStrength();
					}
				});
			}
		}, 0, State.getmSignalInterval(), TimeUnit.MILLISECONDS);

	}

	public void getBattery() {
		Log.i(TAG + "Battery", "Retreiving Battery Status");
		myBatteryReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				try {
					StatusMonitor.this.unregisterReceiver(this);
					int bLevel = arg1.getIntExtra("level", 0);
					State.setmBatteryLevel(String.valueOf(bLevel));
					Log.i(TAG + "Battery", "Battery_level: " + State.getmBatteryLevel());
				} catch (Exception e) {
					Log.e("PhoneLab-" + "StatusMonitorBattery", e.toString());
				}
			}
		};
		this.registerReceiver(myBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	public void getCellLocation() {
		Log.i(TAG + "CellLocation", "Retrieving CDMA Cell Location Status");
		telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneType = telManager.getPhoneType(); // Retrieving CDMA or GSM PhoneType
		State.setmCountry(telManager.getNetworkCountryIso());
		State.setmNetworkOperator(telManager.getNetworkOperator());
		State.setmOperator(telManager.getNetworkOperatorName());
		State.setmNetworkType(String.valueOf(telManager.getNetworkType()));

		// Checking for CDMA phone type
		if (mPhoneType == TelephonyManager.PHONE_TYPE_CDMA) {
			try {
				CdmaCellLocation CDMALocation = (CdmaCellLocation) telManager.getCellLocation();
				// Retrieve information Related to CDMACellLocation
				State.setmBaseStationID(String.valueOf(CDMALocation.getBaseStationId()));
				State.setmBaseStationLatitude(String.valueOf(CDMALocation.getBaseStationLatitude()));
				State.setmBaseStationLongitude(String.valueOf(CDMALocation.getBaseStationLongitude()));
				State.setmNetworkID(String.valueOf(CDMALocation.getNetworkId()));
				State.setmSystemID(String.valueOf(CDMALocation.getSystemId()));

				String Network = "Country: " + State.getmCountry() + " MCC+MNC: " + State.getmNetworkOperator() + " Operator: " + State.getmOperator() + " NetworkType: "
						+ State.getmNetworkType();
				String cellLocation = " BaseStationID: " + State.getmBaseStationID() + " BaseStation_Latitude: " + State.getmBaseStationLatitude()
						+ " BaseStation_Longitude: " + State.getmBaseStationLongitude() + " NetworkID: " + State.getmNetworkID() + " SystemID: " + State.getmSystemID();
				Log.i(TAG + "CellLocation", Network + cellLocation);

			} catch (Exception e) {
				Log.i(TAG, e.toString());
			}

		} else {
			Log.i(TAG, "Phone type not recognized");
		}
	}

	public void getLocation() {
		Log.i(TAG + "Location", "Retrieving Location Status");

		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				Log.i(TAG + "Location", "Location Interval - Passive: " + State.getmPassiveLocationInterval() + " Active: " + State.getmActiveLocationInterval());
				makeUseOfNewLocation(location);

			}

			private void makeUseOfNewLocation(Location location) {

				State.setmLatitude(String.valueOf(location.getLatitude()));
				State.setmLongitude(String.valueOf(location.getLongitude()));
				State.setmAccuracy(String.valueOf(location.getAccuracy()));
				State.setmProvider(location.getProvider());
				State.setmSpeed(String.valueOf(location.getSpeed()));
				State.setmLocationFixTime(String.valueOf(location.getTime()));
				Log.i(TAG + "Location", "Loc_FixTime: " + State.getmLocationFixTime() + " Provider: " + State.getmProvider() + " Latitude: " + State.getmLatitude()
						+ " Longitude: " + State.getmLongitude() + " Accuracy: " + State.getmAccuracy() + " Speed: " + State.getmSpeed());
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		};
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, State.getmActiveLocationInterval(), 0, locationListener);

	}

	public void getSignalStrength() {
		/* Get signal strength */
		Log.i(TAG + "Signal", "Retreiving Signal Status");
		mSignalListener = new PhoneStateListener() {
			int mStrength;

			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				try {
					mTelManager.listen(this, PhoneStateListener.LISTEN_NONE);// unregistering

					if (signalStrength.isGsm()) {
						mStrength = signalStrength.getGsmSignalStrength();
					} else {
						int strength = -1;
						if (signalStrength.getEvdoDbm() < 0)
							strength = signalStrength.getEvdoDbm();
						else if (signalStrength.getCdmaDbm() < 0)
							strength = signalStrength.getCdmaDbm();
						if (strength < 0) {
							// convert to asu
							mStrength = Math.round((strength + 113f) / 2f);
						}
						State.setmASU(String.valueOf(mStrength));
						State.setmSignalStrength(String.valueOf(strength));
						Log.i(TAG + "Signal", "Signal_Strength: " + State.getmSignalStrength() + " asu: " + State.getmASU());
					}
				} catch (Exception e) {
					Log.e(TAG + "Signal", e.toString());
				}

			}
		};

		mTelManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelManager.listen(mSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

	}

	public void setIntervalValues() {
		PhoneLabManifest manifest = new PhoneLabManifest(Util.CURRENT_MANIFEST_DIR, getApplicationContext());
		if (manifest.getManifest()) {
			try {
				State.dumpServices = manifest.getDumpServices();

				ArrayList<PhoneLabParameter> parameters = manifest.getMonitorServices();

				for (PhoneLabParameter param : parameters) {

					if (param.getUnits() != null && param.getValue() != null) {

						long value = Long.parseLong(param.getValue());
						String units = param.getUnits();

						long runInterval = State.getRunInterval(value, units);

						if (param.getName().equals("monitorInterval")) {
							State.setMonitorInterval(runInterval);
							State.setStatusMonitorLock(Boolean.parseBoolean(param.getWakelock()));
						}else if (param.getName().equals("dumpInterval")) {
							State.setmDumpInterval(runInterval);
							State.setDumpLock(Boolean.parseBoolean(param.getWakelock()));
						} else if (param.getName().equals("batteryInterval")) {
							State.setmBatteryInterval(runInterval);
							State.setBatteryLock(Boolean.parseBoolean(param.getWakelock()));
						} else if (param.getName().equals("cellLocationInterval")) {
							State.setmCellLocationInterval(runInterval);
							State.setCellLocationLock(Boolean.parseBoolean(param.getWakelock()));
						} else if (param.getName().equals("signalInterval")) {
							State.setmSignalInterval(runInterval);
							State.setSignalLock(Boolean.parseBoolean(param.getWakelock()));
						} else if (param.getName().equals("passiveLocation")) {
							State.setmPassiveLocationInterval(runInterval);
							State.setPassiveLocationLock(Boolean.parseBoolean(param.getWakelock()));
						} else if (param.getName().equals("activeLocation")) {
							State.setmActiveLocationInterval(runInterval);
							State.setActiveLocationLock(Boolean.parseBoolean(param.getWakelock()));
						}
					}
				}
			} catch (XPathExpressionException e) {
				Log.e(getClass().getSimpleName(), e.toString());
			}
		}

	}

	/*
	 * public void scheduleServices() { Log.i(TAG, "Status Monitor Services Scheduler"); mExecutor = new ScheduledThreadPoolExecutor(5);
	 * mExecutor.scheduleAtFixedRate(new Runnable() {
	 * @Override public void run() { if (counter == 1440) counter = 0; counter++; // Log.i(TAG, counter + " : Setting Intervals"); // Log.i(TAG,
	 * State.getmCellLocationInterval() + " : " + State.getmBatteryInterval() + " " + State.getmSignalInterval() + " " + //
	 * State.getmLocationInterval()); if ((counter * timerInterval) % State.getmCellLocationInterval() == 0) { Log.i(Util.STATUS_TAG + "CellLocation",
	 * "Learning Cell Location"); getCellLocation(); } if ((counter * timerInterval) % State.getmBatteryInterval() == 0) { Log.i(Util.STATUS_TAG +
	 * "Battery", "Learning Battery Level"); getBattery(); } if ((counter * timerInterval) % State.getmSignalInterval() == 0) { Log.i(Util.STATUS_TAG
	 * + "Signal", "Learning Signal Level"); mHandler.post(new Runnable() {
	 * @Override public void run() { getSignalStrength(); } }); } if ((counter * timerInterval) % State.getmLocationInterval() == 0) {
	 * Log.i(Util.STATUS_TAG + "Location", "Learning Location"); mHandler.post(new Runnable() {
	 * @Override public void run() { if (locationListener == null) { // To avoid multiple location updates getLocation(); } } }); } if ((counter *
	 * timerInterval) % State.getmDumpInterval() == 0) { Intent dumpIntent = new Intent(getApplicationContext(), StatusMonitorDump.class);
	 * startService(dumpIntent); } } }, 0, 1, TimeUnit.MINUTES); }
	 */

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
