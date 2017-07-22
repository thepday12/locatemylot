package neublick.locatemylot.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import neublick.locatemylot.activity.LoadingScreenActivity;
import neublick.locatemylot.activity.LocateMyLotActivity;

/**
 * Created by Tung Nguyen on 9/2/2015.
 */
public class Global {
    //Thep - 2016/02/24
    public static final String CHECK_IN_MY_BEACON="CHECK_IN_MY_BEACON";
    public static final String SHOW_DIALOG_START="SHOW_DIALOG_START";
    public static final String CARPARK_ID="CARPARK_ID";
    public static final String SHOW_ADV_BROADCAST ="SHOW_ADV_BROADCAST";
    public static final String DETECT_LIFT_LOBBY_BEACON="DETECT_LIFT_LOBBY_BEACON";
    public static final String SIGN_IN_OR_SIGN_UP_SUCCESS="SIGN_IN_OR_SIGN_UP_SUCCESS";
    public static final String CHANGE_STATE_BLUETOOTH="CHANGE_STATE_BLUETOOTH";
    public static final String LIFT_LOBBY_BEACON_ID_KEY="LIFT_LOBBY_BEACON_ID";
    public static final String WELCOME_CARPARK_ID_KEY ="WELCOME_CARPARK_ID";
    public static final String IS_WELCOME_BROADCAST_KEY="IS_WELCOME_BROADCAST";
    public static final String ADV_DATA="ADV_DATA";
    public static final String IS_ADV_LOCAL="IS_ADV_LOCAL";
    public static final String IS_CHECKOUT_BROADCAST_KEY="IS_CHECKOUT_BROADCAST";
    public static final String UPDATE_INFO_BROADCAST_KEY="UPDATE_INFO_BROADCAST";
    public static final String UPDATE_VIEW_ADV_KEY="UPDATE_VIEW_ADV_KEY";
    public static final String VERSION_KEY="VERSION_LML";
    public static final String LAST_VERSION_KEY="LAST_VERSION_LML";
    public static final String LAST_DATA_VERSION_KEY="LAST_DATA_VERSION";
    public static final String LAST_VERSION_COMPULSORY_KEY="LAST_VERSION_COMPULSORY";
    public static final String TEXT_VERSION_KEY="TEXT_VERSION";
    public static final String SELECT_BEACON_ID_KEY="SELECT_BEACON_ID";


    public static final String SHARE_DATA_EXTRA="shared_data";
    public static final String BLUETOOTH_STATE_EXTRA="LUETOOTH_STATE";


    public static final String IS_TURN_ON_BLUETOOTH ="IS_TURN_ON_BLUETOOTH";
    public static final String IS_CHECKOUT_EXTRA ="IS_CHECKOUT";
    public static final String IS_WELCOME_EXTRA ="IS_WELCOME";
    public static final String IS_UPDATE_IU_EXTRA ="IS_UPDATE_IU_EXTRA";
    public static final String STRING_UPDATE_IU_EXTRA ="STRING_UPDATE_IU_EXTRA";
    public static final String IS_LIFT_LOBBY_EXTRA="IS_LIFT_LOBBY";
    public static final String CAR_PARK_ID_WELCOME_EXTRA ="CAR_PARK_ID_WELCOME";
    public static final String CONFIRM_WELCOME ="CONFIRM_WELCOME";
    public static final String CAR_PARK_ID_CHECKOUT_EXTRA ="CAR_PARK_ID_CHECKOUT";
    public static final String ID_LIFT_LOBBY_EXTRA ="ID_LIFT_LOBBY";
    public static final String EXTRA_POSITION_FRAGMENT ="EXTRA_POSITION_FRAGMENT";
    public static final String TAG_HELP_FRAGMENT ="HELP_FRAGMENT_TAG";

    public static int currentCarparkID=1;
    public static int lastLiftLobby=-1;
    public static float calibAngle=0;
    public static LocateMyLotActivity activityMain;
    public static LoadingScreenActivity activityLoading;
    public static long lastTimestampDismissWelcome=0;
    public static long lastTimestampDismissLostBeacon=0;
    public static boolean isGetSharedLocationDialogShown=false;
    public static boolean isUserPositionVisible=false;
    public static long entryTime=-1;
    public static int entryCarparkId=-1;
    public static final String MY_DIR= Environment.getExternalStorageDirectory() + "/locatemylot_data/";
    public static final String MY_ADV_DIR= Environment.getExternalStorageDirectory() + "/locatemylot_data/adv/";
    public static float mRatioX=0;
    public static float mRatioY=0;
    public static long timeBeaconFound = 0;
    public static long timeCheckOut = 0;
    public static boolean isUpdateInfo = false;


    static SharedPreferences usr;
    static SharedPreferences parkingSession;

    public static  void sendBroadCastSignInSuccess(Activity activity){
        Intent intent = new Intent(Global.SIGN_IN_OR_SIGN_UP_SUCCESS);
        activity.sendBroadcast(intent);
        activity.finish();
    }
    public static  void sendBroadCastChangeBluetoothState(Context context,boolean state){
        Intent intent = new Intent(Global.CHANGE_STATE_BLUETOOTH);
        intent.putExtra(BLUETOOTH_STATE_EXTRA,state);
        context.sendBroadcast(intent);
    }
    public static String getCurrentUser(){
        if(activityMain==null) return "";
        if(usr==null) usr = activityMain.getSharedPreferences("user", activityMain.MODE_PRIVATE);
        return usr.getString("usr", "");
    }

    public static int getCarparkID(){
        if(activityMain==null) return -1;
        if(parkingSession==null) parkingSession = activityMain.getSharedPreferences("PARKING_SESSION", activityMain.MODE_PRIVATE);
        return parkingSession.getInt("CARPARK_ID", 1);
    }

    public static boolean isCheckedIn(){
        if(activityMain==null) return false;
        if(parkingSession==null) parkingSession = activityMain.getSharedPreferences("PARKING_SESSION", activityMain.MODE_PRIVATE);
        return parkingSession.getBoolean("CHECK_IN", false);
    }

    public static boolean exitApplication()
    {
        if(activityMain!=null) {
            activityMain.finish();
            return true;
        }
        else if(activityLoading!=null) {
            activityLoading.finish();
            return true;
        }
        return false;
    }
}
