package edu.buffalo.cse.phonelab.statusmonitors;

import java.util.ArrayList;

/**
 * @author Phonelab A class for various Status Monitor states and function to calculate runIntervals etc,
 * 
 */
public class State {

	public static final String STATUS_TAG = "Phonelab-Status ";

	// Various Run Interval to set granularity
	private static long monitorInterval;
	private static long mBatteryInterval;
	private static long mCellLocationInterval;
	private static long mDumpInterval;
	private static long mActiveLocationInterval;// Network or Gps
	private static long mPassiveLocationInterval;
	private static long mSignalInterval;
	private static Boolean statusMonitorLock = false;

	// DumpServices
	public static ArrayList<String> dumpServices;
	public static final String DUMP_DIR = ".dump";
	private static Boolean dumpLock = false;

	// Battery
	private static String mBatteryLevel;
	private static Boolean batteryLock = false;

	// CellLocation
	private static String mCountry;
	private static String mNetworkOperator;
	private static String mOperator;
	private static String mNetworkType;

	private static String mBaseStationID;
	private static String mBaseStationLatitude;
	private static String mBaseStationLongitude;
	private static String mNetworkID;
	private static String mSystemID;
	private static Boolean cellLocationLock = false;

	// StatusMonitorLocation States
	private static String mLatitude;
	private static String mLongitude;
	private static String mAccuracy;
	private static String mProvider;
	private static String mSpeed;
	private static String mLocationFixTime;
	private static Boolean activeLocationLock = false;
	private static Boolean passiveLocationLock = false;

	// Signal Strength
	private static String mSignalStrength;
	private static String mASU;
	private static Boolean signalLock = false;
	
	//Getters and Setters for  Service Intervals

	public static long getMonitorInterval() {
		return monitorInterval;
	}

	public static void setMonitorInterval(long monitorInterval) {
		State.monitorInterval = monitorInterval;
	}

	public static long getmBatteryInterval() {
		return mBatteryInterval;
	}

	public static void setmBatteryInterval(long mBatteryInterval) {
		State.mBatteryInterval = mBatteryInterval;
	}

	public static long getmCellLocationInterval() {
		return mCellLocationInterval;
	}

	public static void setmCellLocationInterval(long mCellLocationInterval) {
		State.mCellLocationInterval = mCellLocationInterval;
	}

	public static long getmDumpInterval() {
		return mDumpInterval;
	}

	public static void setmDumpInterval(long mDumpInterval) {
		State.mDumpInterval = mDumpInterval;
	}

	public static long getmActiveLocationInterval() {
		return mActiveLocationInterval;
	}

	public static void setmActiveLocationInterval(long mActiveLocationInterval) {
		State.mActiveLocationInterval = mActiveLocationInterval;
	}

	public static long getmPassiveLocationInterval() {
		return mPassiveLocationInterval;
	}

	public static void setmPassiveLocationInterval(long mPassiveLocationInterval) {
		State.mPassiveLocationInterval = mPassiveLocationInterval;
	}

	public static long getmSignalInterval() {
		return mSignalInterval;
	}

	public static void setmSignalInterval(long mSignalInterval) {
		State.mSignalInterval = mSignalInterval;
	}
	
	//Getters and Setters for Battery Service

	public static String getmBatteryLevel() {
		return mBatteryLevel;
	}

	public static void setmBatteryLevel(String mBatteryLevel) {
		State.mBatteryLevel = mBatteryLevel;
	}
	
	//Getters and Setters for Cell Location Service

	public static String getmCountry() {
		return mCountry;
	}

	public static void setmCountry(String mCountry) {
		State.mCountry = mCountry;
	}

	public static String getmNetworkOperator() {
		return mNetworkOperator;
	}

	public static void setmNetworkOperator(String mNetworkOperator) {
		State.mNetworkOperator = mNetworkOperator;
	}

	public static String getmOperator() {
		return mOperator;
	}

	public static void setmOperator(String mOperator) {
		State.mOperator = mOperator;
	}

	public static String getmNetworkType() {
		return mNetworkType;
	}

	public static void setmNetworkType(String mNetworkType) {
		State.mNetworkType = mNetworkType;
	}

	public static String getmBaseStationID() {
		return mBaseStationID;
	}

	public static void setmBaseStationID(String mBaseStationID) {
		State.mBaseStationID = mBaseStationID;
	}

	public static String getmBaseStationLatitude() {
		return mBaseStationLatitude;
	}

	public static void setmBaseStationLatitude(String mBaseStationLatitude) {
		State.mBaseStationLatitude = mBaseStationLatitude;
	}

	public static String getmBaseStationLongitude() {
		return mBaseStationLongitude;
	}

	public static void setmBaseStationLongitude(String mBaseStationLongitude) {
		State.mBaseStationLongitude = mBaseStationLongitude;
	}

	public static String getmNetworkID() {
		return mNetworkID;
	}

	public static void setmNetworkID(String mNetworkID) {
		State.mNetworkID = mNetworkID;
	}

	public static String getmSystemID() {
		return mSystemID;
	}

	public static void setmSystemID(String mSystemID) {
		State.mSystemID = mSystemID;
	}
	
	//Getters and Setters for Location Service

	public static String getmLatitude() {
		return mLatitude;
	}

	public static void setmLatitude(String mLatitude) {
		State.mLatitude = mLatitude;
	}

	public static String getmLongitude() {
		return mLongitude;
	}

	public static void setmLongitude(String mLongitude) {
		State.mLongitude = mLongitude;
	}

	public static String getmAccuracy() {
		return mAccuracy;
	}

	public static void setmAccuracy(String mAccuracy) {
		State.mAccuracy = mAccuracy;
	}

	public static String getmProvider() {
		return mProvider;
	}

	public static void setmProvider(String mProvider) {
		State.mProvider = mProvider;
	}

	public static String getmSpeed() {
		return mSpeed;
	}

	public static void setmSpeed(String mSpeed) {
		State.mSpeed = mSpeed;
	}

	public static String getmLocationFixTime() {
		return mLocationFixTime;
	}

	public static void setmLocationFixTime(String mLocationFixTime) {
		State.mLocationFixTime = mLocationFixTime;
	}
	
	//Getters and Setters for Signal Services

	public static String getmSignalStrength() {
		return mSignalStrength;
	}

	public static void setmSignalStrength(String mSignalStrength) {
		State.mSignalStrength = mSignalStrength;
	}

	public static String getmASU() {
		return mASU;
	}

	public static void setmASU(String mASU) {
		State.mASU = mASU;
	}
	
	//Getters and Setters for various locks

	public static Boolean getStatusMonitorLock() {
		return statusMonitorLock;
	}

	public static void setStatusMonitorLock(Boolean statusMonitorLock) {
		State.statusMonitorLock = statusMonitorLock;
	}

	public static Boolean getDumpLock() {
		return dumpLock;
	}

	public static void setDumpLock(Boolean dumpLock) {
		State.dumpLock = dumpLock;
	}

	public static Boolean getBatteryLock() {
		return batteryLock;
	}

	public static void setBatteryLock(Boolean batteryLock) {
		State.batteryLock = batteryLock;
	}

	public static Boolean getCellLocationLock() {
		return cellLocationLock;
	}

	public static void setCellLocationLock(Boolean cellLocationLock) {
		State.cellLocationLock = cellLocationLock;
	}

	public static Boolean getActiveLocationLock() {
		return activeLocationLock;
	}

	public static void setActiveLocationLock(Boolean activeLocationLock) {
		State.activeLocationLock = activeLocationLock;
	}

	public static Boolean getPassiveLocationLock() {
		return passiveLocationLock;
	}

	public static void setPassiveLocationLock(Boolean passiveLocationLock) {
		State.passiveLocationLock = passiveLocationLock;
	}

	public static Boolean getSignalLock() {
		return signalLock;
	}

	public static void setSignalLock(Boolean signalLock) {
		State.signalLock = signalLock;
	}

	/**
	 * @param value
	 *            - value of the interval
	 * @param units
	 *            - unit of the interval ("min","sec","millisec","hour")
	 * @return total number of millisec for any interval value and units.
	 */
	public static long getRunInterval(long value, String units) {
		long runInterval = 0;

		if (units.equals("hour")) {
			runInterval = value * 60 * 60 * 1000;
		} else if (units.equals("min")) {
			runInterval = value * 60 * 1000;
		} else if (units.equals("sec")) {
			runInterval = value * 1000;
		} else if (units.equals("millisec")) {
			runInterval = value;
		}

		return runInterval;
	}

}
