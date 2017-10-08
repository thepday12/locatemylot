package neublick.locatemylot.app;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import neublick.locatemylot.database.Database;
import neublick.locatemylot.model.BeaconPoint;

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

        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

//        Intent intent = new Intent ();
//        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
//        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
//        startActivity (intent);
        extractLogToFile();
        System.exit(1); // kill off the crashed app

    }

    private String extractLogToFile()
    {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo (this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
//        String path = Environment.getExternalStorageDirectory() + "/" + "LML/";
        String fullName =  "LOG_LML.txt";
//        String fullName =  "LOG_LML"+System.currentTimeMillis()+".txt";
//
//        // Extract to file.
//        File file = new File (fullName);
        File myDir = new File(Global.MY_DIR);//LocateMyLot
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File file = new File(myDir, fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try
        {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader (process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
            writer.write ("Device: " + model + "\n");
            writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do
            {
                int n = reader.read (buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write (buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();

        }
        catch (IOException e)
        {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        return fullName;
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