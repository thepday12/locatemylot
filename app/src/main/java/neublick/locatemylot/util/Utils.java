package neublick.locatemylot.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.database.CLHoliday;
import neublick.locatemylot.database.CLParkingRates;
import neublick.locatemylot.database.CLParkingSurcharge;
import neublick.locatemylot.model.Holiday;
import neublick.locatemylot.model.ParkingRates;
import neublick.locatemylot.model.ParkingSurcharge;
import neublick.locatemylot.receiver.AlarmAlertBroadcastReceiver;

// helper class :-)
public class Utils {
    public static void showMessage(final Dialog dialogNotice, String message, String title, final Activity context, final boolean isClose) {
        if (context == null) return;
        if (dialogNotice!=null&&!dialogNotice.isShowing()) {

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
                    if(isClose)
                    context.finish();
                }
            });


            dialogNotice.show();
        }
    }

    public static   boolean isValidEmail(String email) {
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
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android" );
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
        final Typeface typeFace = Typeface.createFromAsset(context.getAssets(), fontName + ".ttf" );
        textView.setTypeface(typeFace, size);
    }

    public static void loadFont(AppCompatActivity context, TextView textView, String fontName) {
        final Typeface typeFace = Typeface.createFromAsset(context.getAssets(), fontName + ".ttf" );
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

    //Thep update 2016/08/05
    public static String getCurrentCarparkNameWithCarparkId(int carparkId) {
        if (carparkId == 0) {
            return "";
        }
        return CLCarpark.getCarparkNameByCarparkId(carparkId);
    }

    //end
    public static String getResponseFromUrl(String url,HashMap<String,String>data) {
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
        return performPostCall(url,data);
    }
    public static String getResponseFromUrlNoEncode(String url,HashMap<String,String>data) {
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
        return performPostCallNoEncode(url,data);
    }
    public static String getResponseFromUrl(String url) {
        return performPostCall(url,new HashMap<String, String>());
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
                    response +="\n"+ line;
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
    } private static String performPostCall(String requestURL,
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
                    response +="\n"+ line;
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
        Log.e("RESPONSE_SERVER_DATA",result.toString());
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
        Log.e("RESPONSE_SERVER_DATA",result.toString());
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
            date = new SimpleDateFormat("yyyy-MM-dd" ).parse(dateString);
        } catch (ParseException e) {

        }
        return date;
    }

    public static Date convertStringToDateTime(String dateString) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" ).parse(dateString);
        } catch (ParseException e) {
        }
        return date;
    }

    public static String convertDateTimeToString(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
        String time = sdf.format(c.getTime());
        return time;
    }

    public static String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd" );
        String time = sdf.format(c.getTime());
        return time;
    }

    public static Date getCurrentDate() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    public static boolean inTime(String startTime, String endTime, Date currentTime) {
        String start[] = startTime.split(":" );
        String end[] = endTime.split(":" );
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
        String[] timeSplit = result.split(" " );
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
                int startHour = Integer.valueOf(rates.getBeginTime().split(":" )[0]);
                int endHour = Integer.valueOf(rates.getEndTime().split(":" )[0]);
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
        String[] time1 = convertDateTimeToString(beginTime).split(" " );

        int startHour = Integer.valueOf(parkingSurcharge.getBeginTime().split(":" )[0]);
        int endHour = Integer.valueOf(parkingSurcharge.getEndTime().split(":" )[0]);
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
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context,Uri uri,
                                                    int reqWidth, int reqHeight) {

        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            if(reqHeight>0&&reqWidth>0) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

//            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), options);


                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            }else{
                return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            }
        } catch (Exception e) {
            return null;
        }

    }


    public static String formatRates(float sumRates) {
        String result = String.format("%.2f", sumRates);
        result = result.replace(",", ".");
        return result;
    }
}