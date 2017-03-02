package neublick.locatemylot.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareLocationUtil {

//     {"user_from":"", "user_to":"", "user_from_name":"", "x":"", "y":"", "floor":"", "zone":"", "carpark_id":"", "check_in_time":""}
    public static String getLastShareLocation(Context context) {
        final SharedPreferences user = context.getSharedPreferences("share_location", Context.MODE_PRIVATE);
        return user.getString("data", "");
    }

    public static void setLastShareLocation(Context context, String data) {
        final SharedPreferences user = context.getSharedPreferences("share_location", Context.MODE_PRIVATE);
        user.edit().putString("data", data).apply();
    }

    public static boolean isNewData(Context context, String userId){
        boolean result =false;
        String data =getLastShareLocation(context);
        if(!data.isEmpty()){
            try {
                JSONObject jsonObject = new JSONObject(data);
                return userId.equals(jsonObject.getString("user_to"));
            } catch (JSONException e) {
                return false;
            }
        }
        return result;
    }

   
}