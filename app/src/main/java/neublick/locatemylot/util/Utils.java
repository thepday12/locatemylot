package neublick.locatemylot.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.BaseActivity;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.database.CLHoliday;
import neublick.locatemylot.database.CLParkingRates;
import neublick.locatemylot.database.CLParkingSurcharge;
import neublick.locatemylot.dialog.DialogPhoneCode;
import neublick.locatemylot.model.Holiday;
import neublick.locatemylot.model.ParkingRates;
import neublick.locatemylot.model.ParkingSurcharge;
import neublick.locatemylot.receiver.AlarmAlertBroadcastReceiver;

// helper class :-)
public class Utils {
    public static final String METHOD_POST = "POST" ;

    public static void showMessage(final Dialog dialogNotice, String message, String title, final Activity context, final boolean isClose) {
        if (context == null) return;
        if (dialogNotice != null && !dialogNotice.isShowing()) {

            dialogNotice.setContentView(R.layout.dialog_ok);
            dialogNotice.getWindow()
                    .setLayout((int) (getScreenWidth(context) * .85), ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogNotice.setCanceledOnTouchOutside(false);
            Button btOk = (Button) dialogNotice.findViewById(R.id.btOk);
            TextView tvTitle = (TextView) dialogNotice.findViewById(R.id.tvTitle);
            TextView tvContent = (TextView) dialogNotice.findViewById(R.id.tvContent);

            tvTitle.setText(title.isEmpty() ? "LocateMyLot" : title);
            tvContent.setText(message);
            btOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogNotice.dismiss();
                    if (isClose)
                        context.finish();
                }
            });


            dialogNotice.show();
        }
    }

    public static void hiddenKeyboard(Context context) {
//        View view = this.getCurrentFocus();
//        if (view != null) {
        View view = new View(context);
//        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static void setStatusBarColor(LocateMyLotActivity context, int color) {
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(context);

        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);

        // set a custom tint color for all system bars
        tintManager.setTintColor(color);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // return TRUE if wifi enabled :)
    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }

    // PHOTO functions
    public static File getImageFile(String fileName) {
        File parentDir = new File(Environment.getExternalStorageDirectory(), Config.PHOTO_SAVE_DIR);
        parentDir.mkdirs();
        return new File(parentDir, fileName);
    }

    public static String getFileName(String fileUrl) {
        String name = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        return name;
    }
    public static String getFileExtend(String fileUrl) {
        String name = fileUrl.substring(fileUrl.lastIndexOf(".") + 1);
        return name;
    }

    public static void saveAvatar(final String imageUrl, final Context context) {
        class DownloadAvatar extends AsyncTask<Void,Void,Boolean>{
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(aBoolean){
                    Intent intent = new Intent(Global.UPDATE_INFO_BROADCAST_KEY);
                    context.sendBroadcast(intent);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return saveImage(imageUrl, "avatar.jpg");
            }
        }
        new DownloadAvatar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static File getAvatar() {
        String dir = Global.MY_DIR + "avatar.jpg";
        File file = new File(dir);
        return file;
    }

    public static void loadAvatar(Context context, ImageView ivAvatar, String avatarUrl){
        File avatar = Utils.getAvatar();
        if (!avatar.exists()) {
            if(avatarUrl.isEmpty()) {
                Picasso.with(context).load(R.drawable.default_avatar).into(ivAvatar);
            }else{
                Picasso.with(context).load(avatarUrl).memoryPolicy(MemoryPolicy.NO_CACHE).error(R.drawable.default_avatar).into(ivAvatar);
            }
        } else {
            Picasso.with(context).load(avatar).memoryPolicy(MemoryPolicy.NO_CACHE).error(R.drawable.default_avatar).into(ivAvatar);
        }
    }


    public static boolean saveImage(String imageUrl, String name) {
        // Store image to default external storage directory
//        String imageUrl = "http://neublick.com/demo/carlocation/cms/upload_files/map_" + name;
        try {

            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            File myDir = new File(Global.MY_DIR);//LocateMyLot
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            myDir = new File(myDir, name);
//            if (!myDir.exists()) {

                FileOutputStream out = new FileOutputStream(myDir);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
//            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    // ===============================================================
    // TIMER functions
    public static Calendar retrieveCalendarFromMillis(long millis) {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(millis);
        return result;
    }

    public static String getCalendarReadable(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return String.format("%d/%d/%d %d:%d",
                year, month, dayOfMonth, hourOfDay, minute
        );
    }

    public static String getCalendarReadableForEntryTime(Calendar c) {
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return String.format("%d/%d %d:%d",
                dayOfMonth,
                month + 1,
                hourOfDay,
                minute
        );
    }

    // ALARM functions
    public static void schedule(Context context, long timeInMillis, boolean isTenMinsLeft) {
        Intent receiverIntent = new Intent(context, AlarmAlertBroadcastReceiver.class);
        receiverIntent.putExtra("TEN_MINS_LEFT", isTenMinsLeft);
        receiverIntent.putExtra("TIME_IN_MILLIS", timeInMillis);
        PendingIntent pi = PendingIntent.getBroadcast(
                context.getApplicationContext(),
                0,
                receiverIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        AlarmManager alarmMgr = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
    }

    // duration in seconds
    // hour*3600 + minute*60 + second
    public static String makeDurationReadable(int duration) {
        int hour = duration / 3600;
        int minute = (duration - hour * 3600) / 60;
        int second = duration % 60;
        return String.format("%s:%s:%s",
                twoZeroPadding(hour),
                twoZeroPadding(minute),
                twoZeroPadding(second)
        );
    }

    public static String twoZeroPadding(int number) {
        if (number < 10) {
            return "0" + number;
        }
        return String.valueOf(number);
    }


    // HAM VE DO LUONG
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static void loadFont(Context context, TextView textView, String fontName, int size) {
        final Typeface typeFace = Typeface.createFromAsset(context.getAssets(), fontName + ".ttf");
        textView.setTypeface(typeFace, size);
    }

    public static void loadFont(AppCompatActivity context, TextView textView, String fontName) {
        final Typeface typeFace = Typeface.createFromAsset(context.getAssets(), fontName + ".ttf");
        textView.setTypeface(typeFace);
    }

    // chuc nang history
    public static String getDate(long time) {
        Calendar calendar = retrieveCalendarFromMillis(time);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%s/%s/%s", year, twoZeroPadding(month), twoZeroPadding(dayOfMonth));
    }

    public static String getTime(long time) {
        Calendar calendar = retrieveCalendarFromMillis(time);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int secs = calendar.get(Calendar.SECOND);
        return String.format("%s:%s:%s", twoZeroPadding(hourOfDay), twoZeroPadding(minute), twoZeroPadding(secs));
    }

    // kiem tra xem co ket noi internet hay ko
    public static boolean isInternetConnected(Context context) {
        return new ConnectionDetector(context).isConnectingToInternet();
    }

    // HAM tra ve CARPARK NAME
    public static String getCurrentCarparkName2(Context context) {
        final SharedPreferences parkingSession = context.getSharedPreferences("PARKING_SESSION", Context.MODE_PRIVATE);
        int carparkId = parkingSession.getInt("CARPARK_ID", 0);
        if (carparkId == 0) {
            return "";
        }
        return CLCarpark.getCarparkNameByCarparkId(carparkId);
    }


    public static String shareCarPhoto(Context context,HashMap hashMap,Uri uriImage) {
            String url = Config.CMS_URL + "/act.php";
            String pathToImageFile = Utils.getRealPathFromUri(context, uriImage);
            String fileMimeType = Utils.getMimeType(context, uriImage);
            return performPostCallUseHeader(url, hashMap, METHOD_POST, pathToImageFile, "image", fileMimeType,Global.MAX_WIDTH_IMAGE, Global.MAX_HEIGHT_IMAGE);

    }

    public static String shareScreenBitmap(HashMap hashMap,Bitmap bitmap,String userId) {
            String url = Config.CMS_URL + "/act.php";
            String imageName =  "screen_share"+userId;
            return performPostCallUseHeader(url, hashMap,imageName, METHOD_POST, bitmap, Global.MAX_WIDTH_IMAGE, Global.MAX_HEIGHT_IMAGE);

    }

    //Thep update 2016/08/05
    public static String getCurrentCarparkNameWithCarparkId(int carparkId) {
        if (carparkId == 0) {
            return "";
        }
        return CLCarpark.getCarparkNameByCarparkId(carparkId);
    }

    //end
    public static String getResponseFromUrl(String url, HashMap<String, String> data) {
//        String xml = "";
//        try {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(url);
//            HttpResponse httpResponse = httpClient.execute(httpPost);
//            HttpEntity httpEntity = httpResponse.getEntity();
//            xml = EntityUtils.toString(httpEntity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return performPostCall(url, data);
    }

    public static String getResponseFromUrlNoEncode(String url, HashMap<String, String> data) {
//        String xml = "";
//        try {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(url);
//            HttpResponse httpResponse = httpClient.execute(httpPost);
//            HttpEntity httpEntity = httpResponse.getEntity();
//            xml = EntityUtils.toString(httpEntity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return performPostCallNoEncode(url, data);
    }

    public static String getResponseFromUrl(String url) {
        return performPostCall(url, new HashMap<String, String>());
    }

    private static String performPostCallNoEncode(String requestURL,
                                                  HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataStringNotEncode(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += "\n" + line;
                }
            } else {
                response = "";

            }
        } catch (Exception e) {
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        Log.e("RESPONSE_SERVER", response);
        return response.trim();
    }

    private static String performPostCall(String requestURL,
                                          HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += "\n" + line;
                }
            } else {
                response = "";

            }
        } catch (Exception e) {
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        Log.e("RESPONSE_SERVER", response);
        return response.trim();
    }


    private static String performPostCallUseHeader(String requestURL, HashMap<String, Object> postDataParams, String method, String filepath, String fileField, String fileMimeType, int maxWidth, int maxHeight) {

        URL url;
        String response = "";
        HttpURLConnection conn = null;
        ByteArrayOutputStream bos = null;

        DataOutputStream outputStream = null;
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;


        try {

            File file = new File(filepath);

            Bitmap bitmap = BitmapUtil.resizeImage(file, maxWidth, maxHeight);
            if (bitmap != null) {

                bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            }
//            byte[] bitmapdata = bos.toByteArray();
            url = new URL(requestURL);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
//            String accessToken = UtilSharedPreferences.getInstanceSharedPreferences(mContext).getAccessToken();

//            conn.setRequestProperty("User-Agent", "SosieApp");
//            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


//            if (!accessToken.isEmpty()) {
////                String basicAuth = "Bearer " +accessToken;
//                String basicAuth = accessToken;
//                conn.setRequestProperty("Authorization", basicAuth);
//            }
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setDoInput(true);
            if (method == METHOD_POST)
                conn.setDoOutput(true);

//            if (postDataParams.size() > 0) {
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, "UTF-8"));
//                writer.write(getPostDataString(postDataParams));
//            }

            outputStream = new DataOutputStream(conn.getOutputStream());


            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + q[idx] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);
            if (bos == null) {
                FileInputStream fileInputStream = new FileInputStream(file);

                outputStream.writeBytes(getPostData(postDataParams));

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

            } else {
                outputStream.write(bos.toByteArray());
            }
            outputStream.writeBytes(lineEnd);

            for (Map.Entry<String, Object> entry : postDataParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);

            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
//                    response += "\n" + line;
                    response += line;
                }
            } else {
                String line;

                BufferedReader br;
                if (responseCode > 200 && responseCode < 400)
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                else
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));


                while ((line = br.readLine()) != null) {
//                    response += "\n" + line;
                    response += line;
                }
            }
        } catch (Exception e) {

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();

                }
            } catch (IOException e) {

            }
            try{
                if(bos!=null){
                    bos.close();
                    bos.flush();
                }
            }catch (Exception e){

            }
            if (conn != null)
                conn.disconnect();
        }
        Log.e("RESPONSE_SERVER", response);
        return response;
    }

    private static String performPostCallUseHeader(String requestURL, HashMap<String, Object> postDataParams,String imageName, String method, Bitmap screenBitmap, int maxWidth, int maxHeight) {

        URL url;
        String response = "";
        HttpURLConnection conn = null;
        ByteArrayOutputStream bos = null;

        DataOutputStream outputStream = null;
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;



        try {



//            Bitmap bitmap = BitmapUtil.resizeImage(screenBitmap, maxWidth, maxHeight);
            Bitmap bitmap = screenBitmap;
            if (bitmap != null) {

                bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            }
//            byte[] bitmapdata = bos.toByteArray();
            url = new URL(requestURL);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);

//            conn.setRequestProperty("User-Agent", "SosieApp");
//            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setDoInput(true);
            if (method == METHOD_POST)
                conn.setDoOutput(true);

            outputStream = new DataOutputStream(conn.getOutputStream());


            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+imageName +".png\"" + lineEnd);
            outputStream.writeBytes("Content-Type: image/png" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);
            if (bos != null) {
                outputStream.write(bos.toByteArray());
            }else {
                return response;
            }
            outputStream.writeBytes(lineEnd);

            for (Map.Entry<String, Object> entry : postDataParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);

            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                String line;

                BufferedReader br;
                if (responseCode > 200 && responseCode < 400)
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                else
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));


                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }
        } catch (Exception e) {

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();

                }
            } catch (IOException e) {

            }
            try{
                if(bos!=null){
                    bos.close();
                    bos.flush();
                }
            }catch (Exception e){

            }
            if (conn != null)
                conn.disconnect();
        }
        Log.e("RESPONSE_SERVER", response);
        return response;
    }

    /***
     * Data encode
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        Log.e("RESPONSE_SERVER_DATA", result.toString());
        return result.toString();
    }

    private static String getPostData(HashMap<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        return result.toString();
    }

    private static String getPostDataStringNotEncode(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }
        Log.e("RESPONSE_SERVER_DATA", result.toString());
        return result.toString();
    }


    /***
     * Lay ngay trong tuan
     *
     * @param currentDate
     * @return Calendar.SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, and SATURDAY.
     */
    public static int getDayOfWeek(Date currentDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate); // yourdate is an object of type Date
        return c.get(Calendar.DAY_OF_WEEK);
    }

    /***
     * Lay ngay trong tuan
     *
     * @param dateString
     * @return Calendar.SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, and SATURDAY.
     */
    public static int getDayOfWeek(String dateString) {
        return getDayOfWeek(convertStringToDate(dateString));
    }

    public static Date convertStringToDate(String dateString) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {

        }
        return date;
    }

    public static Date convertStringToDateTime(String dateString) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        } catch (ParseException e) {
        }
        return date;
    }

    public static String convertDateTimeToString(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(c.getTime());
        return time;
    }

    public static String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(c.getTime());
        return time;
    }

    /***
     * @param date
     * @param type  Calendar.SECONDS
     * @param value
     * @return
     */
    public static Date addTime(Date date, int type, int value) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(type, value);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String time = sdf.format(c.getTime());
//        return time;
        return c.getTime();
    }

    public static String getOfCurrentDateString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String time = sdf.format(c.getTime());
        return time;
    }

    public static Date getCurrentDate() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    public static boolean inTime(String startTime, String endTime, Date currentTime) {
        String start[] = startTime.split(":");
        String end[] = endTime.split(":");
        int hourStart = Integer.valueOf(start[0]);
        int minuteStart = Integer.valueOf(start[1]);
        int hourEnd = Integer.valueOf(end[0]);
        int minuteEnd = Integer.valueOf(end[1]);
        int hourCurrent = currentTime.getHours();
        int minuteCurrent = currentTime.getMinutes();
        if (hourStart < hourEnd) {
            if ((hourStart <= hourCurrent) && (hourEnd >= hourCurrent)) {
                if (hourStart == hourCurrent) {
                    if (minuteCurrent < minuteStart) {
                        return false;
                    } else {
                        return true;
                    }
                } else if (hourEnd == hourCurrent) {
                    if (minuteCurrent <= minuteEnd) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        } else if (hourStart > hourEnd) {
            if (hourCurrent > hourStart || hourCurrent < hourEnd) {
                return true;
            } else if (hourStart == hourCurrent) {
                if (minuteCurrent < minuteStart) {
                    return false;
                } else {
                    return true;
                }
            } else if (hourEnd == hourCurrent) {
                if (minuteCurrent > minuteEnd) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }

        } else {
            if (minuteStart <= minuteCurrent && minuteEnd >= minuteCurrent) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /***
     * Lay ra thong tin ve gia va time de tinh toan moc time tiep theo
     *
     * @param parkingRatesList
     * @param newDate
     * @return null neu khong co data(exception)
     */
    public static ParkingRates getParkingRates(List<Holiday> holidays, List<ParkingRates> parkingRatesList, Date newDate) {
        boolean isHoliday = false;

        if (parkingRatesList.size() > 0) {
            for (Holiday holiday : holidays) {
                if (holiday.isHoliday(newDate)) {
                    isHoliday = true;
                    break;
                }
            }
//            if (!isHoliday) {
//                isHoliday = (getDayOfWeek(newDate) == Calendar.SUNDAY);
//            }

            int calendarDate = getDayOfWeek(newDate);
            for (ParkingRates rates : parkingRatesList) {
                int dateType = rates.getDayType();//=1: ngày thường =2: ngày chủ nhật/holiday =3: Thứ 7 =0: mọi ngày
//                if (dateType == 1 && isHoliday)
//                    continue;
//                if (dateType == 2 && !isHoliday)
//                    continue;
                if (dateType != 0) {
                    if (isHoliday || calendarDate == Calendar.SUNDAY) {
                        if (dateType != 2) {
                            continue;
                        }
                    } else if (calendarDate == Calendar.SATURDAY) {
                        if (dateType != 3) {
                            continue;
                        }
                    }
                }
                if (inTime(rates.getBeginTime(), rates.getEndTime(), newDate)) {
                    return rates;
                }
            }
        }
        return null;
    }

    /***
     * Lay ra phu thu cho khoang thoi gian
     *
     * @param parkingSurcharges
     * @param newDate
     * @return
     */
    public static ParkingSurcharge getParkingSurcharge(List<Holiday> holidays, List<ParkingSurcharge> parkingSurcharges, Date newDate) {
        boolean isHoliday = false;

        if (parkingSurcharges.size() > 0) {

            for (Holiday holiday : holidays) {
                if (holiday.isHoliday(newDate)) {
                    isHoliday = true;
                    break;
                }
            }

            int calendarDate = getDayOfWeek(newDate);
            for (ParkingSurcharge parkingSurcharge : parkingSurcharges) {
                int dateType = parkingSurcharge.getDataType();//=1: ngày thường =2: ngày chủ nhật/holiday =3: Thứ 7 =0: mọi ngày
                if (dateType != 0) {
                    if (isHoliday || calendarDate == Calendar.SUNDAY) {
                        if (dateType != 2) {
                            continue;
                        }
                    } else if (calendarDate == Calendar.SATURDAY) {
                        if (dateType != 3) {
                            continue;
                        }
                    }
                }
                if (inTime(parkingSurcharge.getBeginTime(), parkingSurcharge.getEndTime(), newDate)) {
                    return parkingSurcharge;
                }
            }
        }
        return null;
    }

    public static boolean validTime(Date date) {
        return (getCurrentDate().compareTo(date) > 0);
    }

    public static Date getEndTime(List<Holiday> holidays, List<ParkingRates> parkingRatesList, Date beginTime) {
        String result = convertDateTimeToString(beginTime);
        String[] timeSplit = result.split(" ");
        String endTime = timeSplit[1];
        boolean isHoliday = false;
        for (ParkingRates rates : parkingRatesList) {
            for (Holiday holiday : holidays) {
                if (holiday.isHoliday(beginTime)) {
                    isHoliday = true;
                    break;
                }
            }
            if (!isHoliday) {
                isHoliday = (getDayOfWeek(beginTime) == Calendar.SUNDAY);
            }
            int dateType = rates.getDayType();
            if (dateType == 1 && isHoliday)
                continue;
            if (dateType == 2 && !isHoliday)
                continue;
            if (inTime(rates.getBeginTime(), rates.getEndTime(), beginTime)) {
                Date date = new Date();
                int startHour = Integer.valueOf(rates.getBeginTime().split(":")[0]);
                int endHour = Integer.valueOf(rates.getEndTime().split(":")[0]);
                date = convertStringToDateTime(timeSplit[0] + " " + rates.getEndTime());
                if (startHour > endHour) {
                    int addTime = 24 - startHour + endHour;
                    date = addTime(date, Calendar.HOUR, addTime);
                }
                return date;
            }
        }
        return beginTime;
    }

    public static float getCurrentRates(String checkInTime, int carparkId) {
        float sum = 0;
        List<ParkingRates> parkingRatesList = CLParkingRates.getListParkingRatesByCarparkId(carparkId);
        List<ParkingSurcharge> parkingSurchargesList = CLParkingSurcharge.getListParkingSurchargeByCarparkId(carparkId);
        List<Holiday> holidays = CLHoliday.getAllHoliday();
        Date newTime = convertStringToDateTime(checkInTime);
//Xu ly tinh tien o day
        List<ParkingRates> parkingRatesSumList = new ArrayList<>();
        do {
            ParkingRates parkingRates = getParkingRates(holidays, parkingRatesList, newTime);
//Lay time begin
            newTime = addTime(newTime, Calendar.SECOND, 1);
            Date endTime = getEndTime(holidays, parkingRatesList, newTime);
            if (endTime.compareTo(newTime) == 0)
                return -1;
            //add rates
            if (sum == 0)
                sum += parkingRates.getFirstRates();
            else
                sum += parkingRates.getSubRates();
            //add surcharge
//            sum+=getParkingSurcharge(holidays,parkingSurchargesList,endTime);
            //change time
            newTime = endTime;
        } while (validTime(newTime));
        return sum;
    }

    public static boolean betweenDate(Date beginTime, Date endTime, ParkingSurcharge parkingSurcharge) {
        String[] time1 = convertDateTimeToString(beginTime).split(" ");

        int startHour = Integer.valueOf(parkingSurcharge.getBeginTime().split(":")[0]);
        int endHour = Integer.valueOf(parkingSurcharge.getEndTime().split(":")[0]);
        Date bg = convertStringToDateTime(time1[0] + " " + parkingSurcharge.getBeginTime());
        ;
        Date end = convertStringToDateTime(time1[0] + " " + parkingSurcharge.getEndTime());
        ;
        if (startHour > endHour) {
            int addTime = 24 - startHour + endHour;
            end = addTime(end, Calendar.HOUR, addTime);
        }
        if (end.compareTo(beginTime) < 0) {//Start..End ...[]...
            return false;
        } else if (end.compareTo(endTime) > 0 && bg.compareTo(endTime) > 0) {//...[...]..Start...End
            return false;
        }
        return true;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri,
                                                    int reqWidth, int reqHeight) {

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            if (reqHeight > 0 && reqWidth > 0) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

//            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), options);


                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            } else {
                return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            }
        } catch (Exception e) {
            return null;
        }

    }

    public static void showDialogPhoneCodeValid(Context context){
        Intent i = new Intent(context, DialogPhoneCode.class);
        context.startActivity(i);
    }
    public static String formatRates(float sumRates) {
        String result = String.format("%.2f", sumRates);
        result = result.replace(",", ".");
        return result;
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static void effectNotificationDetectBeacon(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, soundUri);
        r.play();
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

//    Animation fadeIn = new AlphaAnimation(0, 1);
//fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
//fadeIn.setDuration(1000);
//
//    Animation fadeOut = new AlphaAnimation(1, 0);
//fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
//fadeOut.setStartOffset(1000);
//fadeOut.setDuration(1000);
//
//    AnimationSet animation = new AnimationSet(false); //change to false
//animation.addAnimation(fadeIn);
//animation.addAnimation(fadeOut);
//this.setAnimation(animation);
}