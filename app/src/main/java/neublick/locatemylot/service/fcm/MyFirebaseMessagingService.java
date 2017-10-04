package neublick.locatemylot.service.fcm;

/**
 * Created by theptokim on 10/12/16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.app.LocateMyLotApp;
import neublick.locatemylot.dialog.DialogGetSharedLocation;
import neublick.locatemylot.util.ShareLocationUtil;
import neublick.locatemylot.util.UserUtil;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final int PENDING_INTENT_ID =15;
    public static final int NOTIFICATION_ID =35;
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if(remoteMessage.getFrom().contains("LocateMyLot_topic")){//Co update ko?

        }else {//Share location
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String result = remoteMessage.getData().get("message").toString();
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject != null) {
                        if (!jsonObject.getString("carpark_id").isEmpty()) {
                            ShareLocationUtil.setLastShareLocation(getBaseContext(), result);
                            if (jsonObject.getString("user_to").equals(UserUtil.getUserId(getBaseContext()))) {
                                if (LocateMyLotApp.locateMyLotActivityVisible) {
                                    Global.isGetSharedLocationDialogShown = true;
                                    Intent intent = new Intent(getBaseContext(), DialogGetSharedLocation.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra(Global.SHARE_DATA_EXTRA, result);
                                    getBaseContext().startActivity(intent);

//                                } else {
                                    String from_user = jsonObject.getString("user_from_name");
                                    showNotification(getBaseContext(), result, "Location sharing", from_user + " " + getBaseContext().getString(R.string.text_share_location));
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                }

            }
        }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
        }
    }
        // [END receive_message]


    private void showNotification(Context context, String data, String title, String text) {

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);
        Intent resultIntent = new Intent(context, DialogGetSharedLocation.class);

        resultIntent.putExtra(Global.SHARE_DATA_EXTRA, data);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID,
                resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        //turn on screen
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
        wl.acquire(10000);
//            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
//            wl_cpu.acquire(10000);

    }
}