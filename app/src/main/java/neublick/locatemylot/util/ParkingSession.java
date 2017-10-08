package neublick.locatemylot.util;

import android.content.Context;
import android.content.SharedPreferences;

import neublick.locatemylot.app.Global;
import neublick.locatemylot.dialog.DialogSelectCarPark;

/**
 * Created by theptokim on 10/6/16.
 */
public class ParkingSession {

    public static final int DEFAULT_CARPARK_NULL = -100;


    private SharedPreferences parkingSession;

    private final String CHECK_IN_CARPARK = "CHECKIN_CARPARK";
    private final String LAST_CHECK_IN_CARPARK = "LAST_CHECK_IN_CARPARK";
    private final String LAST_CARPARK_SHOW_CONFIRM = "LAST_CARPARK_SHOW_CONFIRM";
    private final String LAST_CARPARK_CHECKOUT_CONFIRM = "LAST_CARPARK_CHECKOUT_CONFIRM";
    private final String LAST_CHECK_IN_FLOOR = "LAST_CHECK_IN_FLOOR";
    private final String PARKING_SESSION = "PARKING_SESSION";
    private final String IS_SHOW_CHECKIN_CONFIRM = "IS_SHOW_CHECKIN_CONFIRM";
    private final String CHECK_IN = "CHECK_IN";
    private final String TIME_CHECKIN = "TIME_CHECKIN";
    private final String LAST_TIME_QUESTION_LIFT = "LAST_TIME_QUESTION_LIFT";
    private final String LAST_BEACON_QUESTION_LIFT = "LAST_BEACON_QUESTION_LIFT";
    private final String IS_NORMAL_CHECK_IN = "IS_NORMAL_CHECK_IN";
    private final String CHECK_CAR_LOCATION = "CHECK_CAR_LOCATION";
    private final String WAY_MODE = "WAY_MODE";
    private final String ORIGINAL_X = "ORIGINAL_X";
    private final String ORIGINAL_Y = "ORIGINAL_Y";
    private final String ZONE = "ZONE";
    private final String FLOOR = "FLOOR";
    private final String TIME_CHECKOUT = "TIME_CHECKOUT";
    private final String LIFT_LOBBY_SAVE = "LIFT_LOBBY_SAVE";
    private final String PHOTO_NAME = "PHOTO_NAME";
    private final String PREVIOUS_PHOTO_NAME = "PREVIOUS_PHOTO_NAME";
    private final String SET_LOCATION_FROM_HISTORY = "set_location_from_history";
    private static ParkingSession myParkingSession = null;

    private ParkingSession(Context context) {
        parkingSession = context.getSharedPreferences(PARKING_SESSION, Context.MODE_PRIVATE);
    }


    public static ParkingSession getInstanceSharedPreferences(Context context) {
        if (myParkingSession == null) {
            myParkingSession = new ParkingSession(context);
        }
        return myParkingSession;
    }

    public int getLastCarParkCheckIn() {
        return parkingSession.getInt(LAST_CHECK_IN_CARPARK, DEFAULT_CARPARK_NULL);
    }

    public void setLastCarParkCheckIn(int carParkId) {
        parkingSession.edit().putInt(LAST_CHECK_IN_CARPARK, carParkId).apply();
    }

    public String getLastFloorCheckIn() {
        return parkingSession.getString(LAST_CHECK_IN_FLOOR, "");
    }

    public void setLastFloorCheckIn(String floor) {
        parkingSession.edit().putString(LAST_CHECK_IN_FLOOR, floor).apply();
    }

    //1-carpark id
    public int getCarParkCheckIn() {
        return parkingSession.getInt(CHECK_IN_CARPARK, DEFAULT_CARPARK_NULL);
    }

    public void setCarParkCheckIn(int carParkId) {
        parkingSession.edit().putInt(CHECK_IN_CARPARK, carParkId).apply();
    }

    //2-is check in
    public boolean isCheckIn() {
        return parkingSession.getBoolean(CHECK_IN, false);
    }

    public void setCheckIn(boolean isCheckIn) {
        parkingSession.edit().putBoolean(CHECK_IN, isCheckIn).apply();
    }

    //3-Time check in
    public long getTimeCheckIn() {
        return parkingSession.getLong(TIME_CHECKIN, -1);
    }

    public void setTimeCheckIn(long time) {
        parkingSession.edit().putLong(TIME_CHECKIN, time).apply();
    }

    //4-is normal check in
    public boolean isNormalCheckIn() {
        return parkingSession.getBoolean(IS_NORMAL_CHECK_IN, true);
    }

    public void setNormalCheckIn(boolean isNormalChekIn) {
        parkingSession.edit().putBoolean(IS_NORMAL_CHECK_IN, isNormalChekIn).apply();
    }

    //5- check car location
    public boolean isCarCheckLocation() {
        return parkingSession.getBoolean(CHECK_CAR_LOCATION, false);
    }

    public void setCheckCarLocation(boolean isCarCheckLocation) {
        parkingSession.edit().putBoolean(CHECK_CAR_LOCATION, isCarCheckLocation).apply();
    }

    //6- Way mode
    public boolean isWayMode() {
        return parkingSession.getBoolean(WAY_MODE, false);
    }

    public void setWayMode(boolean isWaymode) {
        parkingSession.edit().putBoolean(WAY_MODE, isWaymode).apply();
    }

    //7-x
    public float getX() {
        return parkingSession.getFloat(ORIGINAL_X, 0);
    }

    public void setX(float x) {
        parkingSession.edit().putFloat(ORIGINAL_X, x).apply();
    }

    //8-y
    public float getY() {
        return parkingSession.getFloat(ORIGINAL_Y, 0);
    }

    public void setY(float y) {
        parkingSession.edit().putFloat(ORIGINAL_Y, y).apply();
    }

    //9-Zone
    public String getZone() {
        return parkingSession.getString(ZONE, "");
    }

    public void setZone(String zone) {
        parkingSession.edit().putString(ZONE, zone).apply();
    }

    //10-floor
    public String getFloor() {
        return parkingSession.getString(FLOOR, "");
    }

    public void setFloor(String floor) {
        parkingSession.edit().putString(FLOOR, floor).apply();
    }

    //11-LiftLobby
    public String getLiftLobby() {
        return parkingSession.getString(LIFT_LOBBY_SAVE, "");
    }

    public void setLiftLobby(String liftLoby) {
        parkingSession.edit().putString(LIFT_LOBBY_SAVE, liftLoby).apply();
    }//12-Photo

    public String getPhotoUri() {
        return parkingSession.getString(PHOTO_NAME, "");
    }

    public void setPhotoUri(String photoName) {
        parkingSession.edit().putString(PHOTO_NAME, photoName).apply();
    }//13- Previous Photo

    public String getPreviousPhotoName() {
        return parkingSession.getString(PREVIOUS_PHOTO_NAME, "");
    }

    public void setPreviousPhotoName(String photoName) {
        parkingSession.edit().putString(PREVIOUS_PHOTO_NAME, photoName).apply();
    }

    //14-CheckInCarFloor
    public boolean isCheckInFromHistory() {
        return parkingSession.getBoolean(SET_LOCATION_FROM_HISTORY, false);
    }

    public void setCheckInFromHistory(boolean fromHistory) {
        parkingSession.edit().putBoolean(SET_LOCATION_FROM_HISTORY, fromHistory).apply();
    }

    //15-Time checkout
    public long getTimeCheckOut() {
        return parkingSession.getLong(TIME_CHECKOUT, -1);
    }

    public void setTimeCheckOut(long time) {
        parkingSession.edit().putLong(TIME_CHECKOUT, time).apply();
    }
    //16-Show confirm checkIn
    public boolean isShowCheckInConfirm() {
        return parkingSession.getBoolean(IS_SHOW_CHECKIN_CONFIRM, false);
    }

    public void setShowCheckInConfirm(boolean isShow) {
        parkingSession.edit().putBoolean(IS_SHOW_CHECKIN_CONFIRM, isShow).apply();
    }
    //17-Last carpark showConfirm
    public int getLastCarparkShowConfirm() {
        return parkingSession.getInt(LAST_CARPARK_SHOW_CONFIRM, DEFAULT_CARPARK_NULL);
    }

    public void setLastCarparkShowConfirm(int carpark) {
        parkingSession.edit().putInt(LAST_CARPARK_SHOW_CONFIRM, carpark).apply();
    }
    //18-Last carpark showConfirm checkout
    public int getLastCarparkCheckOutConfirm() {
        return parkingSession.getInt(LAST_CARPARK_CHECKOUT_CONFIRM, DEFAULT_CARPARK_NULL);
    }

    public void setLastCarparkCheckOutConfirm(int carpark) {
        parkingSession.edit().putInt(LAST_CARPARK_CHECKOUT_CONFIRM, carpark).apply();
    }
    //check out
    //19-Last time question lift
    public long getLastTimeQuestionLift() {
        return parkingSession.getLong(LAST_TIME_QUESTION_LIFT, -1);
    }

    public void setLastTimeQuestionLift(long time) {
        parkingSession.edit().putLong(LAST_TIME_QUESTION_LIFT, time).apply();
    } //20-Last time question lift
    public int getLastBeaconQuestionLift() {
        return parkingSession.getInt(LAST_BEACON_QUESTION_LIFT, -1);
    }

    public void setLastBeaconQuestionLift(int beaconId) {
        parkingSession.edit().putInt(LAST_BEACON_QUESTION_LIFT, beaconId).apply();
    }
    public void setCheckOut(long timeCheckOut) {
        parkingSession.edit()
                .putBoolean(CHECK_IN, false)
                .putBoolean(CHECK_CAR_LOCATION, false)
                .putFloat(ORIGINAL_X, 0)
                .putFloat(ORIGINAL_Y, 0)
                .putString(ZONE, "")
                .putString(FLOOR, "")
                .putString(PHOTO_NAME, "")
                .putLong(TIME_CHECKOUT, timeCheckOut)
                .putLong(LAST_TIME_QUESTION_LIFT, -1)
                .putInt(LAST_BEACON_QUESTION_LIFT, DEFAULT_CARPARK_NULL)
                .putInt(DialogSelectCarPark.CARPARK_ID, Global.currentCarparkID)
                .putInt(CHECK_IN_CARPARK, DEFAULT_CARPARK_NULL)
                .putString(LIFT_LOBBY_SAVE, "")
                .putInt(LAST_CARPARK_SHOW_CONFIRM, DEFAULT_CARPARK_NULL)
                .putInt(LAST_CARPARK_CHECKOUT_CONFIRM, DEFAULT_CARPARK_NULL)
                .putBoolean(IS_SHOW_CHECKIN_CONFIRM, false)
                .apply();
    }
}
