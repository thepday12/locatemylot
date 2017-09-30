package neublick.locatemylot.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UserUtil {
    public static boolean isLoggedIn(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return !user.getString("usr", "").equals("");
    }

    public static String getUserName(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("usr", "");
    }

    public static String getUserFullName(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("full_name", "");
    }

    public static void setUserFullName(Context context, String name) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        try {
            name = URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
        user.edit().putString("full_name", name).apply();
    }

    public static String getUserFullNameNew(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("full_name_new", "");
    }

    public static void setUserFullNameNew(Context context, String name) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("full_name_new", name).apply();
    }

    public static String getUserEmail(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("email", "");
    }

    public static String getPassword(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("pwd", "");
    }

    public static String getUserId(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("user_id", "");
    }

    public static String getUserPhone(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("user_phone", "");
    }

    public static void setUserPhone(Context context, String phone) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("user_phone", phone).apply();
    }

    public static String getAvatar(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("avt", "");
    }

    public static void setAvatar(Context context, String avatar) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("avt", avatar).apply();
    }
    public static boolean isLoginSocial(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getBoolean("social", false);
    }

    public static void setLoginSocial(Context context, boolean isLoginSocial) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putBoolean("social", isLoginSocial).apply();
    }

    /***
     * Format phone in Singapore
     *
     * @param phone
     * @return
     */
    public static String formatPhone(String phone) {
        if (!phone.isEmpty()) {
            if (phone.startsWith("+")) {
                return phone;
            } else {
                return "+65" + phone;
            }
        } else {
            return phone;
        }
    }

    public static void signOut(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String iuNumber = getIUNumber(context);
        String key = "tmp";
        if (iuNumber.isEmpty()) {
            iuNumber = getIUNumberTMP(context);
            key = "iu_tmp";
        }

        user.edit().clear().apply();
        user.edit().putString(key, iuNumber).apply();
//        .putBoolean("not_show_enterPhone", false).putBoolean("not_show_IU", false).putString("user_phone", "").putString("user_id", "").putString("pwd", "").putString("usr", "").putString("iu", "").putString("iu_tmp", "").apply();

    }

    public static boolean isNotShowEnterPhoneAgain(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getBoolean("not_show_enterPhone", false);
    }

    public static void setNotShowEnterPhoneAgain(Context context, Boolean isNotShowAgain) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putBoolean("not_show_enterPhone", isNotShowAgain).apply();
    }

    public static boolean isNotShowEnterIUAgain(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getBoolean("not_show_IU", false);
    }

    public static void setNotShowEnterIUAgain(Context context, Boolean isNotShowAgain) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putBoolean("not_show_IU", isNotShowAgain).apply();
    }


    public static String getIUNumber(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("iu", "");
    }

    public static void setIUNumber(Context context, String iuNumber) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("iu", iuNumber).apply();
    }

    public static String getIUNumberTMP(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("iu_tmp", "");
    }

    public static void setIUNumberTMP(Context context, String iuNumber) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("iu_tmp", iuNumber).apply();
    }


    public static String getHistory(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("history", "");
    }

    /***
     *checkInInfo.timeCheckIn.stringValue+"~"+timeCheckOut.stringValue+"~"+beaconCar.x.stringValue+"~"+beaconCar.y.stringValue+"~"+beaconCar.zone+"~"+beaconCar.floor+"~"+checkInInfo.carParkId.stringValue+"~"+checkInInfo.beaconLift.stringValue+"~"+beaconCar.id.stringValue+"~"+rates.stringValue+"~"+isNormal.stringValue
     * @param context
     * @param history
     */

    public static void addHistory(Context context, String history) {
        String data = getHistory(context);
        if(data.isEmpty())
            data = history;
        else{
            data +=":" +history;
        }
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("history", data).apply();
    }

    public static void clearHistory(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("history", "").apply();
    }


    public static String getAddAdv(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("add_adv", "");
    }


    public static void addAddAdv(Context context, String adv) {
        String data = getHistory(context);
        if(data.isEmpty())
            data = adv;
        else{
            data +=":" +adv;
        }
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("add_adv", data).apply();
    }

    public static void clearAddAdv(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("add_adv", "").apply();
    }

    public static String getRemoveAdv(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getString("add_adv", "");
    }


    public static void addRemoveAdv(Context context, String adv) {
        String data = getHistory(context);
        if(data.isEmpty())
            data = adv;
        else{
            data +=":" +adv;
        }
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("remove_adv", data).apply();
    }

    public static void clearRemoveAdv(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("remove_adv", "").apply();
    }

    public static boolean isUpdateFirst(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getBoolean("update_first", false);
    }

    public static void setUpdateFirst(Context context, Boolean isUpdateFirst) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putBoolean("update_first", isUpdateFirst).apply();
    }

    public static boolean isPhoneVerification(Context context) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return user.getBoolean("otp_status", false);
    }

    public static void setPhoneVerification(Context context, Boolean status) {
        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putBoolean("otp_status", status).apply();
    }
    public static void setDataLogin(Context context, String mUsername, String password, String mUserId, String mPhone, String mIUNumber, String email, String fullName,String avatar,boolean status) {

        final SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.edit().putString("usr", mUsername)
                .putString("pwd", password)
                .putString("user_id", mUserId)
                .putString("user_phone", mPhone)
                .putString("iu", mIUNumber)
                .putString("iu_tmp", "")
                .putString("email", email)
                .putString("full_name", fullName)
                .putString("avt", avatar)
                .putBoolean("otp_status", status)
                .putString("history", "")
                .putString("add_adv", "")
                .putString("remove_adv", "")
                .putBoolean("update_first", false)
                .apply();

    }

}