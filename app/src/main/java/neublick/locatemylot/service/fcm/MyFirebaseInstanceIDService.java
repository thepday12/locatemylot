package neublick.locatemylot.service.fcm;

/**
 * Created by theptokim on 10/12/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import neublick.locatemylot.app.Config;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p/>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        new RegisterToken(token).execute();
    }

    class RegisterToken extends AsyncTask<Void, Void, String> {
        private String url;
        private HashMap hashMap;

        public RegisterToken(String token) {
            url = Config.CMS_URL + "/act.php";
            hashMap = new HashMap();
            hashMap.put("gid", token);
            hashMap.put("act", "register");

        }

        @Override
        protected String doInBackground(Void... params) {
            String user= UserUtil.getUserId(getBaseContext());
            String tmp="";
            if(user!=null&&!user.isEmpty()) {
                hashMap.put("u", user);
                 tmp = Utils.getResponseFromUrl(url, hashMap);
            }
            return tmp;
        }


    }
}