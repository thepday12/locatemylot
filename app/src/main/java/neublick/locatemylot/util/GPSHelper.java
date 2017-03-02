package neublick.locatemylot.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.LoadingScreenActivity;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Global;

/**
 * Created by theptokim on 8/23/16.
 */
public class GPSHelper {
    private static  final int NOTIFICATION_CARPARK_ID = 1109;
    private static  final int NOTIFICATION_BLUETOOTH_TURN_ON = 1110;
    private static  final int PENDING_INTENT_ID = 103;
    public static  final String CARPARK_ID_LIST_KEY = "CARPARK_ID_LIST";
    public static  final String CARPARK_RANGE_KEY = "CARPARK_RANGE";
    public static void clearNotification(Context context) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_CARPARK_ID);
        } catch (Exception ex) {

        }
    }   public static void clearNotificationGPS(Context context) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_BLUETOOTH_TURN_ON);
        } catch (Exception ex) {

        }
    }

    public static int getIntCarparkRange(Context context){
        return context.getSharedPreferences("PARKING_SESSION", context.MODE_PRIVATE).getInt(CARPARK_RANGE_KEY,500);
    }

    public static String getCarparkRange(Context context){
        int range = getIntCarparkRange(context);
        if(range<1000){
            if(range>1)
            return range+ " meters";
            else
                return range+ " meter";
        }else{
            float km = range/1000.0f;
            if(range>1999)
                return String.format("%.1f", km)+ " kilometers";
            else
                return String.format("%.1f", km)+ " kilometer";
        }
    }
    public static void setCarparkRange(Context context,int value){
        context.getSharedPreferences("PARKING_SESSION", context.MODE_PRIVATE).edit().putInt(CARPARK_RANGE_KEY,value).apply();
    }
    public static void showNotificationDetectCarpark(Context context,String data,String title,int size) {

//        String text = "In range of "+getCarparkRange(context)+" from your current location";
        String text = "There are "+size+" car park located your vicinity";
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);
        Intent resultIntent = new Intent(context, LocateMyLotActivity.class);
        resultIntent.putExtra(CARPARK_ID_LIST_KEY, data);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID,
                resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_CARPARK_ID, mBuilder.build());

        //Bat sang man hinh
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
        wl.acquire(10000);
    }
    public static void showNotificationTurnBluetooth(Context context,String data,String title,String text) {

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);
        Intent resultIntent = new Intent(context, LoadingScreenActivity.class);
//        resultIntent.putExtra(CARPARK_ID_LIST_KEY, data);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.putExtra(Global.IS_TURN_ON_BLUETOOTH,true);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID,
                resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_BLUETOOTH_TURN_ON, mBuilder.build());

        //Bat sang man hinh
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
        wl.acquire(10000);
    }

    public static double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk =  180.f/Math.PI;

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
        double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
        double t3 = Math.sin(a1)*Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000*tt;
    }

    public static void turnGPSOn(Context context){
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);
        }
    }

    public static  void turnGPSOff(Context context){
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);
        }
    }

    public static boolean isGPSEnable(Context context){
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
