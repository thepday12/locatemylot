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
import neublick.locatemylot.activity.DetailImageActivity;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.app.LocateMyLotApp;
import neublick.locatemylot.database.CLShareReceive;
import neublick.locatemylot.dialog.DialogGetSharedLocation;
import neublick.locatemylot.util.ShareLocationUtil;
import neublick.locatemylot.util.UserUtil;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final int PENDING_INTENT_ID = 15;
    public static final int NOTIFICATION_ID = 35;

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
        if (remoteMessage.getFrom().contains("LocateMyLot_topic")) {//Co update ko?

        } else {//Share location
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                String result = remoteMessage.getData().get("message").toString();

//                result = "{\"type\":\"2\",\"user_from\":\"178\", \"user_to\":\"178\", \"user_from_name\":\"Thép Tô Kim\", \"x\":\"\", \"y\":\"\", \"floor\":\"\", \"zone\":\"\", \"carpark_id\":\"\", \"check_in_time\":\"\",\"image_url\":\"http://neublick.com/demo/carlocation/cms/upload_files/screen_share20171107143015.jpg\"}";
                /***
                 * Neu data cu va thong tin nhan duoc khac nhau (co the bi goi 2 lan)
                 * Luu lai thong tin trong bang du lieu va lastData nhan dc
                 * Kiem tra
                 * Type  = share location (thuc hien show dialog confirm)
                 * Type = nguoc lai show detail Image share
                 * Luu y: NEW_TASK de tranh bi leak activity
                 */
                if (!result.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject != null) {

                            String lastData = ShareLocationUtil.getLastShareLocation(getBaseContext());
                            if(lastData.equals(result)){
                                return;
                            }
                            int type = jsonObject.getInt("type");
                            CLShareReceive.addItem(result, type);
                            ShareLocationUtil.setLastShareLocation(getBaseContext(), result);
                            if (LocateMyLotApp.locateMyLotActivityVisible) {

                                String fromUserName = jsonObject.getString("user_from_name");

                                switch (type) {
                                    case Global.TYPE_SHARE_LOCATION: {
                                        if (!jsonObject.getString("carpark_id").isEmpty()) {
                                            if (jsonObject.getString("user_to").equals(UserUtil.getUserId(getBaseContext()))) {
                                                Global.isGetSharedLocationDialogShown = true;
                                                Intent intent = new Intent(getBaseContext(), DialogGetSharedLocation.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.putExtra(Global.SHARE_DATA_EXTRA, result);
                                                getBaseContext().startActivity(intent);

                                                showNotification(getBaseContext(), result, "Location sharing", fromUserName + " " + getBaseContext().getString(R.string.text_share_location));
                                            }
                                        }
                                    }
                                    break;
                                    default:
                                        String imageUrl = jsonObject.getString("image_url");
                                        Intent intent = new Intent(getBaseContext(), DetailImageActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("IMAGE_URL", imageUrl);
                                        intent.putExtra("FROM", fromUserName);
                                        intent.putExtra("IS_NEW", true);
                                        getBaseContext().startActivity(intent);

                                        showNotification(getBaseContext(), result, "Photo sharing", fromUserName + " " + getBaseContext().getString(R.string.text_share_photo));
                                        break;
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