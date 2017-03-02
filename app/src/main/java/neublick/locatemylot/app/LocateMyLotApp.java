package neublick.locatemylot.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.database.CLBeacon;
import neublick.locatemylot.database.CLPromotion;
import neublick.locatemylot.database.Database;
import neublick.locatemylot.model.BeaconPoint;
import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.util.AppUpdateTask;
import neublick.locatemylot.util.Utils;

public class LocateMyLotApp extends Application implements Application.ActivityLifecycleCallbacks {

    public static boolean locateMyLotActivityVisible = false;
    public static int APP_NOTIFICATION_ID = 5;

    @Override
    public void onCreate() {
        super.onCreate();
        Database.initialize(this);
        registerActivityLifecycleCallbacks(this);
//Thep update 2016/08/05
//		if (Utils.isInternetConnected(this)) {
//			try {
//				new AppUpdateTask().execute(Config.CMS_URL + "/getsynchdata.php").get();
//			} catch(Exception e) {
//				le("Exception: " + e.getMessage());
//			}
//		}
//end

//        List<BeaconPoint> allWelcomeItems = CLBeacon.allWelcomeBeacons();
//        le("allWelcomeItems.size=" + allWelcomeItems.size());
//        for (BeaconPoint bcItem : allWelcomeItems) {
//            le("$$$" + bcItem.toString());
//        }
//
//        le("ttt" + CLPromotion.getAllByUserName("magicalmoon17@Gmail.com"));
    }

    static void le(String s) {
        final String TAG = LocateMyLotApp.class.getSimpleName();
        android.util.Log.e(TAG, s);
    }

    void printBeaconList(List<BeaconPoint> list) {
        StringBuilder sb = new StringBuilder("app [");
        for (BeaconPoint beaconItem : list) {
            sb.append(beaconItem.mId).append(", ");
        }
        le(sb.append("]").toString());
    }


    // implements ActivityLifecycleCallbacks
    @Override
    public void onActivityPaused(Activity activity) {
//        if (activity instanceof LocateMyLotActivity) {
            locateMyLotActivityVisible = false;
//        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
//        if (activity instanceof LocateMyLotActivity) {
            locateMyLotActivityVisible = true;
//        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        locateMyLotActivityVisible = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }
}