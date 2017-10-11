package neublick.locatemylot.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.Database;
import neublick.locatemylot.dialog.DialogSignInSignUp;
import neublick.locatemylot.receiver.BluetoothBroadcastReceiver;
import neublick.locatemylot.util.GPSHelper;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

public class LoadingScreenActivity extends AppCompatActivity {


    private SharedPreferences parkingSession;
    private Dialog dialog;
    private ProgressBar pbLoading;
    private Dialog dialogNeedUpdate;
    private boolean isUpdateIUNumber = false;
    private boolean isWaitInternet = false;
    private boolean isEnableBluetooth = false;
    private String updateIUNumberString = "";
    private final int REQ_SIGN_IN_SIGN_UP = 1;
    private Dialog dialogEnterPhone;
    private boolean isDownloadIng = false;
    private boolean isShowing = false;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(
                    "android.net.conn.CONNECTIVITY_CHANGE")) {
                if (isWaitInternet) {
                    if (Utils.isInternetConnected(LoadingScreenActivity.this)) {
                        try {
                            if (dialogNeedUpdate != null && dialogNeedUpdate.isShowing())
                                dialogNeedUpdate.dismiss();
                            new AppUpdateTask(LoadingScreenActivity.this).execute(getUrlSyncData()).get();
                        } catch (InterruptedException e) {

                        } catch (ExecutionException e) {
                        }
                    }
                }
            }
        }
    };

    private String getUrlSyncData() {
        if (parkingSession == null)
            parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        float versionData = parkingSession.getFloat(Global.LAST_DATA_VERSION_KEY, 0);
        String url = Config.CMS_URL + "/getsynchdata.php?dataversion=" + versionData;
        return url;
    }


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
//        FirebaseMessaging.getInstance().subscribeToTopic("LocateMyLot_topic");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.mBatInfoReceiver,
                filter);
        setContentView(R.layout.activity_loading_screen);
        dialogEnterPhone = new Dialog(LoadingScreenActivity.this);
//
        Global.activityLoading = this;
        isEnableBluetooth = getIntent().getBooleanExtra(Global.IS_TURN_ON_BLUETOOTH, false);
        parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        parkingSession.edit().putFloat(Global.VERSION_KEY, 2.0f).putString(Global.TEXT_VERSION_KEY, getString(R.string.text_version)).apply();
//        ImageView welcomeImage = (ImageView) findViewById(R.id.welcome_image);
        dialog = new Dialog(LoadingScreenActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    verifyPermissions(LoadingScreenActivity.this);
                } else {

                    syncData();
                }
            }
        }, 300);

    }

    public void showSignInSignUp() {
        String id = UserUtil.getUserId(LoadingScreenActivity.this);
        if(pbLoading!=null)
        pbLoading.setVisibility(View.GONE);
        if (id.isEmpty()) {
            Intent intent = new Intent(LoadingScreenActivity.this, DialogSignInSignUp.class);
            startActivityForResult(intent, REQ_SIGN_IN_SIGN_UP);
        }

    }

    private void syncData() {
        String oldData = getOldData();
        if (Utils.isInternetConnected(this)) {
//            if (oldData
// .isEmpty()) {
            try {
                new AppUpdateTask(this).execute(getUrlSyncData()).get();
            } catch (Exception e) {
            }
//            } else {
//                checkBluetooth();
//            }
        } else {
            if (oldData.isEmpty()) {
                showDialogNeedUpdate();
            } else {
                checkBluetooth();
            }
        }
    }

    private void checkBluetooth() {
        if (isEnableBluetooth) {
            Handler $h = new Handler();
            $h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestBluetooth();
                }
            }, 1000);
        } else {
            showDialogEnterIU();
        }
    }

    private void requestBluetooth() {
        if (!isShowing) {
            BluetoothBroadcastReceiver.requestBluetoothEnabled(LoadingScreenActivity.this, isShowing);
            isShowing = true;
        }
    }

    public void showDialogNeedUpdate() {
        if (dialogNeedUpdate != null && dialogNeedUpdate.isShowing())
            return;
        isWaitInternet = true;
        dialogNeedUpdate = new Dialog(LoadingScreenActivity.this);
        dialogNeedUpdate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNeedUpdate.setContentView(R.layout.dialog_need_update);
        dialogNeedUpdate.setCanceledOnTouchOutside(false);

         pbLoading = (ProgressBar) dialogNeedUpdate.findViewById(R.id.pbLoading);
        Button btOk = (Button) dialogNeedUpdate.findViewById(R.id.btOk);
        Button btExitApp = (Button) dialogNeedUpdate.findViewById(R.id.btExitApp);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNeedUpdate.dismiss();
            }
        });

        btExitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNeedUpdate.dismiss();
                finish();
            }
        });
        dialogNeedUpdate.show();

    }

    public void showDialogEnterIU() {
        String id = UserUtil.getUserId(LoadingScreenActivity.this);
        String uiNumber = UserUtil.getIUNumber(LoadingScreenActivity.this);
        String phone = UserUtil.getUserPhone(LoadingScreenActivity.this);
        String uiNumberTmp = UserUtil.getIUNumberTMP(LoadingScreenActivity.this);
        if (!id.isEmpty() && phone.isEmpty()) {
            showDialogEnterPhone();
            return;
        }
        if (!id.isEmpty() && uiNumber.isEmpty() && uiNumberTmp.isEmpty()) {
//        if (uiNumber.isEmpty() && uiNumberTmp.isEmpty()) {
            if (dialog != null && dialog.isShowing())
                return;

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_enter_iu);
            dialog.setCanceledOnTouchOutside(true);

            Button btOk = (Button) dialog.findViewById(R.id.btOk);
            Button btCancel = (Button) dialog.findViewById(R.id.btCancel);


            final EditText etIU = (EditText) dialog.findViewById(R.id.etIU);
            // if button is clicked, close the custom dialog
            btOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String iu = etIU.getText().toString();
                    if (iu != null && !iu.isEmpty()) {
                        UserUtil.setIUNumberTMP(LoadingScreenActivity.this, iu);

                        if (Utils.isInternetConnected(LoadingScreenActivity.this) && !UserUtil.getUserId(LoadingScreenActivity.this).isEmpty()) {
                            new UpdateIUNumber(false).execute();
                        } else {
                            isUpdateIUNumber = true;
                            updateIUNumberString = getString(R.string.enter_IU);
                        }
                        dialog.dismiss();
                    } else {
                        etIU.setError(getString(R.string.edit_text_null_value));
                    }
                }
            });

            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserUtil.setNotShowEnterIUAgain(LoadingScreenActivity.this, true);
                    dialog.dismiss();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    startHomeActivity();
                }
            });
//            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                @Override
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK)
//                        return true;
//                    return false;
//                }
//            });
            dialog.show();
        } else {
            if (id.isEmpty()) {
                showSignInSignUp();
            } else {

                startHomeActivity();
            }
        }
    }


    private void startHomeActivity() {
        if (isDownloadIng)
            return;
        Intent mainActivityIntent = new Intent(LoadingScreenActivity.this, LocateMyLotActivity.class);
        mainActivityIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        mainActivityIntent.putExtra(Global.IS_UPDATE_IU_EXTRA, isUpdateIUNumber);
        mainActivityIntent.putExtra(Global.STRING_UPDATE_IU_EXTRA, updateIUNumberString);
        startActivity(mainActivityIntent);
    }

    @Override
    protected void onDestroy() {
        try {
            View view = new View(LoadingScreenActivity.this);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            unregisterReceiver(mBatInfoReceiver);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocateMyLotActivity.REQUEST_BLUETOOTH: {
                if (resultCode == RESULT_OK) {
                    showDialogEnterIU();
                } else if (resultCode == RESULT_CANCELED) {
                    showDialogEnterIU();
//                    new AlertDialog.Builder(this)
//                            .setTitle("LocateMyLot")
//                            .setMessage("Bluetooth is not enabled")
//                            .setNegativeButton("Enable bluetooth", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    BluetoothBroadcastReceiver.requestBluetoothEnabled(LoadingScreenActivity.this);
//                                }
//                            })
//                            .setPositiveButton("Exit application", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    finish();
//                                }
//                            })
//                            .show();
                }
                isShowing = false;

            }
            break;
            case REQ_SIGN_IN_SIGN_UP:
                if (resultCode == RESULT_OK) {
                    validData();
                } else {
                    if (data.getBooleanExtra("IS_BACK", false)) {
                        finish();
                    } else {
                        showSignInSignUp();
                    }
                }
                break;
        }
    }

    private void validData() {

        String phone = UserUtil.getUserPhone(LoadingScreenActivity.this);
        if (phone.isEmpty() && !UserUtil.getUserId(LoadingScreenActivity.this).isEmpty()) {
            showDialogEnterPhone();
        } else {
            startHomeActivity();
        }
    }

    public void showDialogEnterPhone() {
        if (dialogEnterPhone.isShowing())
            return;
        dialogEnterPhone.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEnterPhone.setContentView(R.layout.dialog_enter_phone);
        dialogEnterPhone.setCanceledOnTouchOutside(false);
        TextView tvTitle = (TextView) dialogEnterPhone.findViewById(R.id.tvTitle);
        TextView tvContent = (TextView) dialogEnterPhone.findViewById(R.id.tvContent);
        Button btOk = (Button) dialogEnterPhone.findViewById(R.id.btOk);
        Button btCancel = (Button) dialogEnterPhone.findViewById(R.id.btCancel);

        btCancel.setVisibility(View.GONE);

        final EditText etPhone = (EditText) dialogEnterPhone.findViewById(R.id.etPhone);
        // if button is clicked, close the custom dialogEnterPhone
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetConnected(LoadingScreenActivity.this)) {
                    String phone = etPhone.getText().toString();
                    if (phone != null && !phone.isEmpty()) {
                        new UpdatePhone(phone, dialogEnterPhone).execute();
                    } else {
                        etPhone.setError(getString(R.string.edit_text_null_value));
                    }
                } else {
                    Toast.makeText(LoadingScreenActivity.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserUtil.setNotShowEnterPhoneAgain(LoadingScreenActivity.this, true);
                dialogEnterPhone.dismiss();
            }
        });
        dialogEnterPhone.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    finish();
//                    dialogEnterPhone.dismiss();
                } else {
                    return false;
                }
                return true;
            }
        });
        dialogEnterPhone.show();
    }

    class AppUpdateTask extends AsyncTask<String, Void, List<String>> {
        private Context mContext;
        private String data = "";

        public AppUpdateTask(Context context) {
            mContext = context;
        }

        @Override
        public List<String> doInBackground(String... urls) {
            URL url = getURL(urls[0]);
            if (url == null) return null;
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line;
                List<String> result = new ArrayList<String>();
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                    data += line;
                }
                return result;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(List<String> result) {
            if (result == null) {
                le("CAN NOT UPDATE");
                requestBluetooth();
                return;
            } else if (result.size() == 1 && result.get(0).equals("[UPTODATE]")) {
                requestBluetooth();
                return;
            }

            if (data.equals(getOldData())) {
                if (parkingSession.getBoolean(Global.LAST_VERSION_COMPULSORY_KEY, false) && (parkingSession.getFloat(Global.VERSION_KEY, 0) < parkingSession.getFloat(Global.LAST_VERSION_KEY, 0))) {
                    showDialogUpdate();
                } else {
                    requestBluetooth();
                }
                return;
            }

            clearDataSQLite();
            List<String> listImageName = new ArrayList<>();
            boolean isLowVersion = false;
            for (String s : result) {
                String[] ss = s.split("~");
                if (ss[0].equals("D") && ss.length >= 8) {
//                   CL_BEACONS (ID, NAME,MAJOR,MINOR X, Y, ZONE, FLOOR, CARPARK_ID, BEACON_TYPE)
//                             values(1,3001,3001,1,169,236,'C3','B3','1',1)
//                    near "X": syntax error (code 1): , while compiling: insert or replace into CL_BEACONS (ID, NAME,MAJOR,MINOR X, Y, ZONE, FLOOR, CARPARK_ID, BEACON_TYPE) values(1,3001,3001,,1,169,236,'C3','B3','1',1)
                    String isPromotion = "0";
                    try {
                        isPromotion = ss[11];
                    } catch (Exception e) {

                    }
                    if (!isPromotion.equals("1")) {
                        isPromotion = "0";
                    }
                    //D~3345~92~612~275~B Level 7~L7~122~2~7092~3~0
                    Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_BEACON + "(ID, NAME,MAJOR,MINOR, X, Y, ZONE, FLOOR, CARPARK_ID, BEACON_TYPE,IS_PROMOTION) values" +
                            "(" + ss[1] + ",'" + ss[2] + "'," + ss[9] + "," + ss[10] + "," + ss[3] + "," + ss[4] + ",'" + ss[5] + "','" + ss[6] + "'," + ss[7] + "," + ss[8] + "," + isPromotion + ")");
                } else if (ss[0].equals("C") && ss.length >= 7) {
                    if (ss.length >= 8)
                        if (ss.length > 10) {
                                Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_CARPARKS + "(ID, NAME,FLOORS,CP_TYPE,LAT,LON,RATES_INFO,WEB_LINK,WEB_NAME) values" +
                                        "(" + ss[1] + ",'" + ss[2] + "','" + ss[3] + "'," + ss[4] + "," + ss[5] + "," + ss[6] + ",'" + ss[7] + "','" + ss[10] + "','" + ss[11] + "')");
                        } else {
                            Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_CARPARKS + "(ID, NAME,FLOORS,CP_TYPE,LAT,LON,RATES_INFO) values" +
                                    "(" + ss[1] + ",'" + ss[2] + "','" + ss[3] + "'," + ss[4] + "," + ss[5] + "," + ss[6] + ",'" + ss[7] + "')");
                        }
                    else
                        Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_CARPARKS + "(ID, NAME,FLOORS,CP_TYPE,LAT,LON,RATES_INFO) values" +
                                "(" + ss[1] + ",'" + ss[2] + "','" + ss[3] + "'," + ss[4] + "," + ss[5] + "," + ss[6] + ",'')");
                    if (!ss[3].isEmpty()) {
                        String[] ss3 = ss[3].split(",");
                        for (int i = 0; i < ss3.length; i++) {
                            listImageName.add(ss[1] + "_" + ss3[i] + ".png");
                        }
                    }
                } else if (ss[0].equals("P") && ss.length >= 7) {
                    String floor = "";
                    if(ss.length>= 8){
                        floor = ss[7];
                    }
                    Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_PATH + "(ID, X, Y, LABEL, ADJ, CARPARK_ID,FLOOR) values" +
                            "(" + ss[1] + "," + ss[2] + "," + ss[3] + ",'" + ss[4] + "','" + ss[5] + "'," + ss[6] + ",'"+floor+"')");
                } else if (ss[0].equals("PR") && ss.length >= 9) {

                    Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_PARKING_RATES + "(ID, CARPARK_ID, DAY_TYPE, BEGIN_TIME, END_TIME, FIRST_MINS,FIRST_RATE,SUB_MINS,SUB_RATES,STATUS) values" +
                            "(" + ss[1] + "," + ss[2] + "," + ss[3] + ",'" + ss[4] + "','" + ss[5] + "'," + ss[6] + "," + ss[7] + "," + ss[8] + "," + ss[9] + "," + ss[10] + ")");
                } else if (ss[0].equals("PS") && ss.length >= 6) {

                    Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_PARKING_SURCHARGE + "(ID, CARPARK_ID, DAY_TYPE, BEGIN_TIME, END_TIME, SURCHARGE,STATUS) values" +
                            "(" + ss[1] + "," + ss[2] + "," + ss[3] + ",'" + ss[4] + "','" + ss[5] + "'," + ss[6] + "," + ss[7] + ")");
                } else if (ss[0].equals("PH") && ss.length >= 2) {

                    Database.getDatabase().execSQL("insert or replace into " + Database.TABLE_HOLIDAY + "(ID, HOLIDAY) values" +
                            "(" + ss[1] + ",'" + ss[2] + "')");
                } else if (ss[0].equals("VER")) {
//                    VER~2.0~Y~Version 2.0 Build 2016093001\n
                    float version = Float.parseFloat(ss[1]);
                    boolean isCompulsory = ss[2].toLowerCase().equals("y");
                    if (version > parkingSession.getFloat(Global.VERSION_KEY, 0)) {
                        parkingSession.edit().putFloat(Global.LAST_VERSION_KEY, version).putBoolean(Global.LAST_VERSION_COMPULSORY_KEY, isCompulsory).apply();
                        isLowVersion = isCompulsory;
                    }
                } else if (ss[0].equals("DATA-VER")) {
                    try {
                        parkingSession.edit().putFloat(Global.LAST_DATA_VERSION_KEY, Float.valueOf(ss[1])).apply();
                    } catch (Exception e) {

                    }
                } else if (ss[0].equals("GPS-DST")) {
                    GPSHelper.setCarparkRange(LoadingScreenActivity.this, Integer.valueOf(ss[1]));
                }
            }
            new DownloadImage(listImageName, isLowVersion).executeOnExecutor(THREAD_POOL_EXECUTOR);
            setOldData(data);

        }


        class DownloadImage extends AsyncTask<Void, Void, Boolean> {
            private ProgressDialog mDialog;
            private List<String> mListImageName;
            private boolean mIsCompulsory;

            public DownloadImage(List<String> listImageName, boolean isCompulsory) {
                mListImageName = listImageName;
                mIsCompulsory = isCompulsory;
            }

            @Override
            protected void onPreExecute() {
                mDialog = ProgressDialog.show(LoadingScreenActivity.this, null,
                        "Maps downloading..", true);
                isDownloadIng = true;
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean result = true;
                if (mListImageName.size() > 0) {
                    int i = 0;
                    String[] links = {"https://www.amcharts.com/lib/3/maps/svg/afghanistanHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/egyptHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/estoniaHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg",
                            "https://www.amcharts.com/lib/3/maps/svg/moroccoHigh.svg"};
                    for (String imageName : mListImageName) {
//                        saveImageSVG(links[i],imageName+".svg");
//                        i++;
                        if (!saveImage(imageName))
                            result = false;
//						break;
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if (!aBoolean) {
                    Toast.makeText(mContext, "Can't download maps", Toast.LENGTH_SHORT).show();
                }
                if (mIsCompulsory) {
                    showDialogUpdate();
                } else {
                    requestBluetooth();
                }
                isDownloadIng = false;
                startHomeActivity();
                super.onPostExecute(aBoolean);
            }
        }

        private boolean saveImage(String name) {
            // Store image to default external storage directory
            String imageUrl = "http://neublick.com/demo/carlocation/cms/upload_files/map_" + name;
            try {

                URL url = new URL(imageUrl);
                URLConnection conn = url.openConnection();
                Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                File myDir = new File(Global.MY_DIR);//LocateMyLot
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                myDir = new File(myDir, name);
                if (!myDir.exists()) {

                    FileOutputStream out = new FileOutputStream(myDir);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                }
                return true;
            } catch (Exception e) {
                return false;
            }

        }

        //Load image svg
        private boolean saveImageSVG(String linkImage, String name) {
            int count;
            name = name.replace(".png", "");
            try {
                URL url = new URL(linkImage);
                URLConnection conection = url.openConnection();
                conection.connect();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(Global.MY_DIR + name);

                byte data[] = new byte[1024];


                while ((count = input.read(data)) != -1) {

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return true;

            } catch (Exception e) {
                return false;
            }
        }


        private URL getURL(String s) {
            try {
                return new URL(s);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        void le(String s) {
            final String TAG = AppUpdateTask.class.getSimpleName();
            android.util.Log.e(TAG, s);
        }
    }

    private void clearDataSQLite() {
        Database.deleteAll(Database.TABLE_BEACON);
        Database.deleteAll(Database.TABLE_CARPARKS);
        Database.deleteAll(Database.TABLE_PATH);
        Database.deleteAll(Database.TABLE_PARKING_RATES);
        Database.deleteAll(Database.TABLE_PARKING_SURCHARGE);
        Database.deleteAll(Database.TABLE_HOLIDAY);
    }


    private void setOldData(String oldData) {
        parkingSession.edit().putString("DATA_UPDATE_STRING", oldData).apply();
    }

    private String getOldData() {
        return parkingSession.getString("DATA_UPDATE_STRING", "");
    }

    private void showDialogUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update are ready to install")
                .setMessage("It won't take long to upgrade - and you'll get all the lastest improvements and fixes.")
                .setCancelable(false)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void verifyPermissions(Activity activity) {
        // Here, thisActivity is the current activity
        int permissionWrite = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCamera = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);
        int permissionLocation = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionWrite != PackageManager.PERMISSION_GRANTED && permissionRead != PackageManager.PERMISSION_GRANTED && permissionLocation != PackageManager.PERMISSION_GRANTED && permissionCoarseLocation != PackageManager.PERMISSION_GRANTED && permissionCamera != PackageManager.PERMISSION_GRANTED) {

//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//
//            } else {


            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA},
                    100);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
//            }
        } else {
            syncData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    int result = 0;
                    for (int grant : grantResults) {
                        result += grant;
                    }
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        syncData();
                        return;
                    }
                }
                Toast.makeText(LoadingScreenActivity.this, "Please allow all permissions of app!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

        }
    }

    class UpdateIUNumber extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private String mUserId;
        private String mIUNumber;
        private boolean updateBackground;


        public UpdateIUNumber(boolean updateBackground) {
            mUserId = UserUtil.getUserId(LoadingScreenActivity.this);
            mIUNumber = UserUtil.getIUNumberTMP(LoadingScreenActivity.this);
            this.updateBackground = updateBackground;
        }

        @Override
        protected void onPreExecute() {
            if (!updateBackground)
                mDialog = ProgressDialog.show(LoadingScreenActivity.this, null,
                        "Loading...", true);
            super.onPreExecute();
        }

        // chi co 1 tham so dau vao, la username can share-location :))
        // args[0] la username can chia se location
        @Override
        public String doInBackground(Void... args) {
            String link = Config.CMS_URL + "/act.php";
            HashMap hashMap = new HashMap();
            hashMap.put("act", "updateinfo");
            hashMap.put("i", mUserId);
            hashMap.put("p", UserUtil.getUserPhone(LoadingScreenActivity.this));
            hashMap.put("iu", mIUNumber);

            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        public void onPostExecute(String result) {
            if (!updateBackground && mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            if (result == null || result.isEmpty()) {
                if (!updateBackground)
                    Toast.makeText(LoadingScreenActivity.this, R.string.text_empty_json, Toast.LENGTH_LONG).show();
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        UserUtil.setIUNumber(LoadingScreenActivity.this, mIUNumber);
                        UserUtil.setIUNumberTMP(LoadingScreenActivity.this, "");
                        if (!updateBackground) {
                            isUpdateIUNumber = true;
                            updateIUNumberString = jsonObject.getString("error_description");
                        }
                    } else {
                    }
                } catch (JSONException e) {
                }


            }
        }


    }

    public class UpdatePhone extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private Dialog mDialogEnterPhone;
        private HashMap hashMap;
        private String mPhone;

        public UpdatePhone(String phone, Dialog dialogEnterPhone) {
            mPhone = UserUtil.formatPhone(phone);
            hashMap = new HashMap();
            hashMap.put("act", "updateinfo");
            hashMap.put("p", mPhone);
            hashMap.put("i", UserUtil.getUserId(LoadingScreenActivity.this));
            mDialogEnterPhone = dialogEnterPhone;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(LoadingScreenActivity.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getBoolean("state")) {
                        UserUtil.setUserPhone(LoadingScreenActivity.this, mPhone);
                        mDialogEnterPhone.dismiss();
                        showDialogEnterIU();
                    } else {
                        Toast.makeText(LoadingScreenActivity.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(LoadingScreenActivity.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoadingScreenActivity.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }
}
