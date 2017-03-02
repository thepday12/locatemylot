package neublick.locatemylot.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import neublick.locatemylot.R;
import neublick.locatemylot.adapter.DetailChargeAdapter;
import neublick.locatemylot.adapter.DetailSurchargeAdapter;
import neublick.locatemylot.adapter.PaperSleepAdapter;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.app.LocateMyLotApp;
import neublick.locatemylot.database.CLBeacon;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.database.CLHoliday;
import neublick.locatemylot.database.CLParkingHistory;
import neublick.locatemylot.database.CLParkingRates;
import neublick.locatemylot.database.CLParkingSurcharge;
import neublick.locatemylot.dialog.DialogGetSharedLocation;
import neublick.locatemylot.dialog.DialogSelectCarPark;
import neublick.locatemylot.dialog.DialogUserLocation;
import neublick.locatemylot.model.BeaconPoint;
import neublick.locatemylot.model.Carpark;
import neublick.locatemylot.model.DetailCharge;
import neublick.locatemylot.model.Holiday;
import neublick.locatemylot.model.ParkingHistory;
import neublick.locatemylot.model.ParkingRates;
import neublick.locatemylot.model.ParkingSurcharge;
import neublick.locatemylot.receiver.BluetoothBroadcastReceiver;
import neublick.locatemylot.service.BackgroundService;
import neublick.locatemylot.service.ServiceManager;
import neublick.locatemylot.ui.MapView;
import neublick.locatemylot.ui.RoundedImageView;
import neublick.locatemylot.ui.SquareImageButton;
import neublick.locatemylot.ui.ToggleSquareImageButton;
import neublick.locatemylot.util.GPSHelper;
import neublick.locatemylot.util.LightweightTimer;
import neublick.locatemylot.util.LightweightTimerExt;
import neublick.locatemylot.util.ParkingSession;
import neublick.locatemylot.util.ShareLocationUtil;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

import static neublick.locatemylot.util.ParkingSession.DEFAULT_CARPARK_NULL;

public class LocateMyLotActivity extends BaseActivity {

    public static String CURRENT_RATES_TEXT = "--";
    private ParkingSession mParkingSession;
    LocateMyLotActivity mContext;
    DrawerLayout mRootView;
    MapView mMapView;

    // MapView's width && height
    int mMapWidth;
    int mMapHeight;

    ServiceManager serviceManager;

    int mRootWidth;
    //Thep update 2016/08/12
    private TextView tvParkingRates;
    private LinearLayout llParkingRates;
    private RelativeLayout rlSlideHelp, rlMap, rlHandlerRates, rlDetailRatesTop;
    private ImageButton btHandler, btHideHandler;
    private CircleImageView btCamera;
    private float sumRates = 0;
    private List<ParkingRates> parkingRatesList = new ArrayList<>();
    private List<ParkingSurcharge> parkingSurchargesList = new ArrayList<>();
    private List<Holiday> holidays = new ArrayList<>();
    private ViewPager vpPaperSleep;
    private PaperSleepAdapter paperSleepAdapter;
    private final int SLEEP_PAGE_SIZE = 3;
    private final int ANIMATION_DURATION = 280;
    private int currentPosition = 0;
    private long lastTimeSleep = 0;
    private boolean isAnamationRun=false;
    private boolean isUpdateLotRunning =false;
    public static List<Carpark> carParkAndLots = new ArrayList<>();
    public static final String BROADCAST_UPDATE_LOT = "BROADCAST_UPDATE_LOT";
    private Dialog dialogNotice;
    //end
    //Thep update 2016/08/05
    private int currentCapark = DEFAULT_CARPARK_NULL;
    private int lastCarParkLoadMap = DEFAULT_CARPARK_NULL;
    private String lastFloorLoadMap = "";

    private String currentFloor = "";
    private String currentMapLiftLobby = "";
    private String currentMapCar = "";
    //end
    //Thep update 2016/08/06
    private String oldZoneText = "";
    private ProgressBar pbLoadingMap;
    //end
    float server_width = 720;
    float server_height;

    TextView mCarparkName;
    TextView mCarparkFloor;
    TextView mCarZone;
    TextView mLiftZone;

    // ====================================================
    // =================== TOOLBARS =======================
    ToggleSquareImageButton funcWay;
    ToggleSquareImageButton funcCheckIn;
    ToggleSquareImageButton funcTime;

    // ====================================================
    RoundedImageView viewAttached, viewAttached2;
    TextView funcDuration;
    TextView funcEntry;

    LightweightTimerExt mDurationTimer;
    LightweightTimerExt mEntryTimer;


    long timeCheckIn = -1;
    boolean isCheckIn = false;
    Runnable secTickTimerRunner = new Runnable() {
        @Override
        public void run() {
            long t = System.currentTimeMillis();
            if (lastTimeSleep == 0) {
                lastTimeSleep = t;
            }
            if (t - lastTimeSleep > 5000) {
                if (currentPosition < SLEEP_PAGE_SIZE) {
                    currentPosition++;
                } else {
                    currentPosition = 0;
                }
                vpPaperSleep.setCurrentItem(currentPosition);
                lastTimeSleep = t;
            }
            if (Global.timeBeaconFound > 0 && (t - Global.timeBeaconFound) > 10000) {
                setNotfoundBeacon();
            }

            boolean timeValid = false;
            if (Global.entryTime > 0) {
                timeValid = true;
            } else if (mParkingSession.isCheckIn() && mParkingSession.getTimeCheckIn() > 0)

            {
                Global.entryTime = mParkingSession.getTimeCheckIn();
                timeValid = true;
            }
            if (timeValid) {
                String time = Utils.makeDurationReadable((int) (t - Global.entryTime) / 1000);
                getDuration().setText(time);
                if (getEntryTime().getText().equals("--:--:--")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
                    getEntryTime().setText(sdf.format(new Date(Global.entryTime)));
                }

                //Thep update 2016/08/12
                int carpark = mParkingSession.getCarParkCheckIn();
                updateData(carpark);
//222
                if (parkingRatesList.size() > 0) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    String checkInTime = sdf.format(new Date(Global.entryTime));
                    sumRates = getSumRates();

                } else {
                    sumRates = 0;
                }
                sumRates += sumSurcharge();


                String result = Utils.formatRates(sumRates);
//                Log.e("RESPONSE_SERVER",result+"");
                getTvParkingRates().setText(result);

///end
            } else {
                getDuration().setText("--:--:--");
                getEntryTime().setText("--:--:--");
                getTvParkingRates().setText("--");
                if (!mParkingSession.isCheckIn())
                    funcCheckIn.modifyState(false);
            }
        }
    };

    SensorManager sensorManager;
    Sensor sensorOrientation;


    LightweightTimer secTickTimer;
    BluetoothBroadcastReceiver bluetoothReceiver;

    public void updateCheckInStatus() {
        //SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        isCheckIn = mParkingSession.isCheckIn();
        timeCheckIn = -1;
        String sTimeCheckIn = "--:--:--";
        if (isCheckIn && Global.entryTime <= 0) {
            Global.entryTime = mParkingSession.getTimeCheckIn();
        }
        if (Global.entryTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
            sTimeCheckIn = sdf.format(new Date(Global.entryTime));
            /*
            timeCheckIn = parkingSession.getLong("TIME_CHECKIN", -1);
			if(timeCheckIn>0) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
				Date resultdate = new Date(timeCheckIn);
				sTimeCheckIn=sdf.format(resultdate);
			}
			*/
        }
        getEntryTime().setText(sTimeCheckIn);
    }

    //Thep - 2016/02/24
    private AlertDialog.Builder alertDialogGPS = null;
    private AlertDialog.Builder alertDialogWarning = null;
    private AlertDialog alertDialog = null;
    private AlertDialog alertDialogDetect = null;
    private Dialog mWellComeDialog = null;
    //	private static int mType=0;
    //Thep update 2016/08/09
    private int currentIdLiftLobby = -1;
    private long lastTimeQuestion = -1;
    private long lastTimeCheckOut = -1;
    private BroadcastReceiver myBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isWelcomeBroadcast = intent.getBooleanExtra(Global.IS_WELCOME_BROADCAST_KEY, false);
            if (isWelcomeBroadcast) {
                showDialogIsWelcome(intent.getIntExtra(Global.WELCOME_CARPARK_ID_KEY, DEFAULT_CARPARK_NULL), intent.getBooleanExtra(Global.CONFIRM_WELCOME, false));
            } else {
                boolean isCheckOut = intent.getBooleanExtra(Global.IS_CHECKOUT_BROADCAST_KEY, false);
                int liftLobbyId = intent.getIntExtra(Global.LIFT_LOBBY_BEACON_ID_KEY, -1);
                if (isCheckOut) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTimeCheckOut > 30000) {
                        actionCheckOut(liftLobbyId, true);
//                        Toast.makeText(context, "beacon check-out id: "+liftLobbyId, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int selectedCarPark = intent.getIntExtra(Global.SELECT_BEACON_ID_KEY, -1);
                    boolean isNormalCheckIn = mParkingSession.isNormalCheckIn();
                    if (selectedCarPark > 0) {
                        String data = intent.getStringExtra(GPSHelper.CARPARK_ID_LIST_KEY);
                        showDialogDetailCharge(true, selectedCarPark, data);
                    } else if (isNormalCheckIn) {//Neu check in binh thuong thi hien liftLobby
                        int carpark = mParkingSession.getCarParkCheckIn();
                        BeaconPoint beaconPoint = CLBeacon.getBeaconById(liftLobbyId);
                        if (carpark == beaconPoint.mCarparkId) {//Cung carpark moi hoi liftLobby
                            if (beaconPoint.mBeaconType == 0 || beaconPoint.mBeaconType == 1 || beaconPoint.mBeaconType == 2) {//Beacon co toa do map
                                setMap(beaconPoint.mCarparkId, beaconPoint.mFloor);
                            }
                            String cmp = beaconPoint.mX + ";" + beaconPoint.mY;
                            if (!mParkingSession.getLiftLobby().contains(cmp) || !mParkingSession.getLiftLobby().contains(beaconPoint.mFloor)) {
                                if (currentIdLiftLobby != beaconPoint.mId) {
                                    currentIdLiftLobby = beaconPoint.mId;
                                    boolean isShowing = false;
                                    if (!isCheckIn && alertDialogDetect != null && alertDialogDetect.isShowing())
                                        isShowing = true;
                                    showDialogLiftLobby(beaconPoint, isShowing);
                                }
                            } else {
                                long currentTime = System.currentTimeMillis();
                                if (lastTimeQuestion < 0) {
                                    lastTimeQuestion = currentTime;
                                } else if (currentTime - lastTimeQuestion > 60000) {
                                    showDialogLiftLobby(beaconPoint, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    };//end


    //Thep update 2016/08/25 - update detail
    private float currentSumRates = 0;
    private float currentSurcharge = 0;
    private List<DetailCharge> currentListRates = new ArrayList<>();
    private List<DetailCharge> currentListSurCharge = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(myBroadcastReceiver1, new IntentFilter(Global.DETECT_LIFT_LOBBY_BEACON));
        mParkingSession = ParkingSession.getInstanceSharedPreferences(LocateMyLotActivity.this);
        Global.activityMain = this;
        if(dialogNotice==null){
            dialogNotice = new Dialog(LocateMyLotActivity.this);
            dialogNotice.setCanceledOnTouchOutside(true);
            dialogNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        //getAppHash();onCre
        GPSHelper.clearNotificationGPS(LocateMyLotActivity.this);
        bluetoothReceiver = new BluetoothBroadcastReceiver();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main2);
        pbLoadingMap = (ProgressBar) findViewById(R.id.pbLoadingMap);
        btCamera = (CircleImageView) findViewById(R.id.func_camera);
        btHandler = (ImageButton) findViewById(R.id.btHandler);
        llParkingRates = (LinearLayout) findViewById(R.id.llParkingRates);
        rlHandlerRates = (RelativeLayout) findViewById(R.id.rlHandlerRates);
        rlDetailRatesTop = (RelativeLayout) findViewById(R.id.rlDetailRatesTop);
        btHideHandler = (ImageButton) findViewById(R.id.btHideHandler);

        rlSlideHelp = (RelativeLayout) findViewById(R.id.rlSlideHelp);
        rlMap = (RelativeLayout) findViewById(R.id.rlMap);
        llParkingRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogDetailCharge(false, mParkingSession.getCarParkCheckIn(), "");
            }
        });
        vpPaperSleep = (ViewPager) findViewById(R.id.vpPaperSleep);
        paperSleepAdapter = new PaperSleepAdapter(getSupportFragmentManager(), SLEEP_PAGE_SIZE);
        vpPaperSleep.setAdapter(paperSleepAdapter);
        mContext = this;
        updateSignInMenu();


        // initialize Sensor monitor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        getMapViewCarObject().visible(false);
        ViewTreeObserver vto = getRootView().getViewTreeObserver();

        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String photoName = mParkingSession.getPhotoName();//parkingSession.getString("PHOTO_NAME", "");
                // fileName chac chan khac "" ?!
                if (!photoName.equals("")) {
                    File file = Utils.getImageFile(photoName);
                    if (file != null && file.exists()) {
                        showDialogZoomPhoto();
                        return;
                    }
                }
                verifyPermissions(LocateMyLotActivity.this);
            }
        });

        btHandler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_UP)
                showDetailCarParkRates();
                return true;
            }
        });
        rlDetailRatesTop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_UP)
                    hideDetailCarParkRates();
                return true;
            }
        });


        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);

                getMapViewUserObject().measure(
                        getMapViewUserObject().view.getMeasuredWidth(),
                        getMapViewUserObject().view.getMeasuredHeight()
                );

                getMapViewCarObject().measure(
                        getMapViewCarObject().view.getMeasuredWidth(),
                        getMapViewCarObject().view.getMeasuredHeight()
                );
                getMapViewLiftLobbyObject().measure(
                        getMapViewLiftLobbyObject().view.getMeasuredWidth(),
                        getMapViewLiftLobbyObject().view.getMeasuredHeight()
                );

                // promotion chay tu phai sang trai la o day :))


                secTickTimer = new LightweightTimer(secTickTimerRunner, 1000);
                secTickTimer.start();

                updateCheckInStatus();

                sensorManager.registerListener(getMapViewUserObject(), sensorOrientation, SensorManager.SENSOR_DELAY_UI);

                mMapWidth = getMapView().getMeasuredWidth();
                mMapHeight = getMapView().getMeasuredHeight();


                serviceManager = new ServiceManager(LocateMyLotActivity.this, BackgroundService.class, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        String name = currentCapark + "_" + currentFloor + ".png";
                        if (LocateMyLotApp.locateMyLotActivityVisible && (msg.what == BackgroundService.MOBILE_LOCATION_RETRIEVAL)) {
                            Bundle retrieveData = msg.getData();
//                            getMapView().setVisibility(View.VISIBLE);
                            float serverX = (float) (retrieveData.getDouble("X"));
                            float serverY = (float) (retrieveData.getDouble("Y"));

                            float localX = Global.mRatioX * serverX;
                            float localY = Global.mRatioY * serverY;
//222String name = currentCapark + "_" + currentFloor + ".png";
                            //Thep update 2016/08/05
                            String floor = retrieveData.getString("FLOOR");
                            String zone = retrieveData.getString("ZONE");
                            int capark = retrieveData.getInt(DialogSelectCarPark.CARPARK_ID, DEFAULT_CARPARK_NULL);
                            currentCapark = capark;
                            currentFloor = floor;
                            name = capark + "_" + floor + ".png";
                            setMap(capark, floor);
                            getMapViewUserObject()
                                    .original(serverX, serverY)
                                    .applyMatrix(getMapView().drawMatrix)
                                    .zone(zone)
                                    .floor(floor)
//                                    .visible(true)
                            ;
                            //end
//Thep update 2016/08/06
                            setTextDetails(capark, floor);
                            //show car and lift lobby
//                            getMapView().setVisibility(View.VISIBLE);
//                            if (name.equals(currentMapLiftLobby)) {
//                                getMapViewLiftLobbyObject().visible(true);
//                            }
//                            if (name.equals(currentMapCar)) {
//                                getMapViewCarObject().visible(true);
//                            }

                            //end

                            if (isCheckIn && getMapViewCarObject().visible())
                                getMapViewCarObject().applyMatrix(getMapView().drawMatrix);
                            if (isCheckIn && getMapViewLiftLobbyObject().visible())
                                getMapViewLiftLobbyObject().applyMatrix(getMapView().drawMatrix);
                            Global.isUserPositionVisible = true;

                            // neu chua check in thi hien thi floor ma user dang dung
                            if (getMapViewCarObject().view.getVisibility() == View.INVISIBLE) {
//                                getCarparkFloor().setText(retrieveData.getString("AREA_LOCATION_FLOOR"));
                            } else
                                // neu dang o trang thai da checkIn va dang o che do wayMode = true
                                if (getMapView().wayMode && getMapViewCarObject().view.getVisibility() == View.VISIBLE) {
                                    getMapView().wayClear();
                                    getMapView().wayDraw();
                                }
                        } else if (LocateMyLotApp.locateMyLotActivityVisible && msg.what == BackgroundService.MAP_RELOAD_AFTER_CHECKOUT) {
                            reloadAfterCheckout();
                        } else if (LocateMyLotApp.locateMyLotActivityVisible && msg.what == BackgroundService.BEACON_NOT_FOUND) {
                            setNotfoundBeacon();
                            //end
                        } else if (LocateMyLotApp.locateMyLotActivityVisible && msg.what == BackgroundService.BEACON_FOUND_NOT_ENOUGH) {
                        } else if (LocateMyLotApp.locateMyLotActivityVisible && msg.what == BackgroundService.SET_PARKING_LOCATION) {
                            showDialogIsWelcome(BackgroundService.carparkId, false);
                        }//Thep update 2016/08/25 ra khoi khu vuc beacon
                        else if (LocateMyLotApp.locateMyLotActivityVisible && msg.what == BackgroundService.LOST_MY_BEACON) {
                            showDialogCheckInWithBeaconExit();
                        }//end
                    }
                });
                serviceManager.start();
//                startService(new Intent(LocateMyLotActivity.this,BackgroundService.class));
                mRootWidth = getRootView().getMeasuredWidth();
            }
        });

        getFuncWay();
        getFuncCheckIn();
        getFuncTime();

        // chinh lai toan bo text sang font moi
        getCarparkName();
        sangFontMoi();

        boolean isNormalCheckIn = mParkingSession.isNormalCheckIn();
        boolean isCheckIn = mParkingSession.isCheckIn();
        if (!isCheckIn || !isNormalCheckIn) {
            Intent retreiveIntent = getIntent();
            String dataGPS = retreiveIntent.getStringExtra(GPSHelper.CARPARK_ID_LIST_KEY);

            if (dataGPS != null && !dataGPS.isEmpty()) {
                if (isCheckIn) {
                    showDialogCheckOut(dataGPS);
                } else {
                    showCarParkNear(true, dataGPS);
                }
            }
        }
        showSleepLayoutAndHiddenMap();
        setTextDetails(DEFAULT_CARPARK_NULL, "");
        updateInfo();

        getExtra();
        boolean isIUUpdate=getIntent().getBooleanExtra(Global.IS_UPDATE_IU_EXTRA,false);
        if(isIUUpdate) {
            String stringIUUpdate = getIntent().getStringExtra(Global.STRING_UPDATE_IU_EXTRA);
            if (stringIUUpdate == null || stringIUUpdate.isEmpty())
                stringIUUpdate = getString(R.string.enter_IU);

//            if(dialogNotice!=null&&!dialogNotice.isShowing())
//                Utils.showMessage(dialogNotice, stringIUUpdate, "", LocateMyLotActivity.this, false);
            if (dialogNotice!=null&&!dialogNotice.isShowing()) {

                dialogNotice.setContentView(R.layout.dialog_ok);
                dialogNotice.getWindow()
                        .setLayout((int) (Utils.getScreenWidth(LocateMyLotActivity.this) * .85), ViewGroup.LayoutParams.WRAP_CONTENT);
                dialogNotice.setCanceledOnTouchOutside(false);
                Button btOk = (Button) dialogNotice.findViewById(R.id.btOk);
                TextView tvTitle = (TextView) dialogNotice.findViewById(R.id.tvTitle);
                TextView tvContent = (TextView) dialogNotice.findViewById(R.id.tvContent);

                tvTitle.setText("LocateMyLot");
                tvContent.setText(stringIUUpdate);
                btOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogNotice.dismiss();

                    }
                });
                dialogNotice.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showMySignInSignUp();
                    }
                });
                dialogNotice.show();
            }else{
                showMySignInSignUp();
            }
        }else{
            showMySignInSignUp();
        }
        carParkAndLots = CLCarpark.getAllEntries();
    }

    private void showMySignInSignUp() {
        String userId = UserUtil.getUserId(LocateMyLotActivity.this);
        String email = UserUtil.getUserEmail(LocateMyLotActivity.this);
        String fullName = UserUtil.getUserFullName(LocateMyLotActivity.this);
        String s = userId + email + fullName;
        if (s.isEmpty() && Utils.isInternetConnected(LocateMyLotActivity.this)) {
//            showSignInSignUp();
            startActivity(new Intent(LocateMyLotActivity.this,LoadingScreenActivity.class));

        }
    }

    private void hideDetailCarParkRates() {
        if(isAnamationRun)
            return;
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, rlHandlerRates.getHeight());
        animate.setDuration(ANIMATION_DURATION);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnamationRun=true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnamationRun=false;
                btHandler.setVisibility(View.VISIBLE);
                rlHandlerRates.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rlHandlerRates.setAnimation(animate);
    }

    private void showDetailCarParkRates() {
        if(isAnamationRun)
            return;
        int carpark = currentCapark;
        if (carpark == DEFAULT_CARPARK_NULL)
            carpark = mParkingSession.getCarParkCheckIn();
        if (carpark != DEFAULT_CARPARK_NULL) {
            TextView tvBeforeTime = (TextView) findViewById(R.id.tvBeforeTime);
            TextView tvAfterTime = (TextView) findViewById(R.id.tvAfterTime);
            TextView tvSat = (TextView) findViewById(R.id.tvSat);
            TextView tvSunPH = (TextView) findViewById(R.id.tvSunPH);
            Carpark carparkObject = CLCarpark.getCarparkByCarparkId(carpark);
            String[] details = carparkObject.ratesInfo.split("%");
            if (details.length >= 1)
                tvBeforeTime.setText(details[0]);
            if (details.length >= 2)
                tvAfterTime.setText(details[1]);
            if (details.length >= 3)
                tvSat.setText(details[2]);
            if (details.length >= 4)
                tvSunPH.setText(details[3]);
        }
        btHandler.setVisibility(View.INVISIBLE);
        rlHandlerRates.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, rlHandlerRates.getHeight(), 0);
        animate.setDuration(ANIMATION_DURATION);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnamationRun=true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnamationRun=false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        rlHandlerRates.setAnimation(animate);
    }


    private void showDialogZoomPhoto() {
        final Dialog dialog = new Dialog(LocateMyLotActivity.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_zoom_photo);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button

        ImageView ivImageZoom = (ImageView) dialog.findViewById(R.id.ivImageZoom);
        Button btRemovePhoto = (Button) dialog.findViewById(R.id.btRemovePhoto);
        final Button btCancel = (Button) dialog.findViewById(R.id.btCancel);
//        Picasso.with(LocateMyLotActivity.this).load(file).into(ivImageZoom);
        ivImageZoom.setImageDrawable(btCamera.getDrawable());


        btRemovePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParkingSession.setPhotoName("");
                Picasso.with(LocateMyLotActivity.this).load(R.drawable.ic_photo_camera).into(btCamera);
                dialog.dismiss();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void reloadAfterCheckout() {
        getFuncCheckIn().modifyState(false);
//        getFuncTime().modifyState(false);
        getFuncWay().modifyState(false);
        LocateMyLotActivity.this.updateCheckInStatus();
        setTextDetails(DEFAULT_CARPARK_NULL, "");
        // insert parking_history data

        // xoa di CAR_OBJECT
        getMapViewCarObject().visible(false);
        //thep 2016/02/25
        getMapViewLiftLobbyObject().visible(false);
        //end
        // chuyen statusCar sang "You have not checked in your car yet"

        // neu dang su dung che do ve duong di thi xoa bitmap
        if (getMapView().wayMode) {
            getMapView().wayClear();
        }

        // khi check out thi che do ve duong di cung can sua lai = false
        mParkingSession.setWayMode(false);
        Global.entryTime = -1;
        updateCheckInStatus();
        //Thep update 2016/08/19 Hien thi lai map
//                       showMapHiddenSleepSlide();
//                        llTime.setVisibility(View.INVISIBLE);
//        showSleepLayoutAndHiddenMap();
    }

    private void setNotfoundBeacon() {
        if (rlMap.getVisibility() == View.VISIBLE) {
            getMapViewUserObject().visible(false);
            Global.isUserPositionVisible = false;
            //Thep update 2016/08/06
            //get old text
            //text to default
            currentCapark = DEFAULT_CARPARK_NULL;
            currentFloor = "";
            dismissMyDialog();
            //hiden map and car and lift lobby
            showSleepLayoutAndHiddenMap();
            getMapViewLiftLobbyObject().visible(false);
            getMapViewCarObject().visible(false);
            setTextDetails(DEFAULT_CARPARK_NULL, "");
        }
    }

    private void actionCheckOut(int carparkId, boolean isStartNotification) {
        // neu LocateMyLotActivity dang foreground
        // neu dialog van ton tai thi thoat
        if (mParkingSession.isCheckIn() && mParkingSession.isNormalCheckIn()) {
            if (carparkId == mParkingSession.getCarParkCheckIn()) {
                if (!LocateMyLotApp.locateMyLotActivityVisible) {
                    // chuyen locateMyLotActivity sang foreground
                    Intent resumeIntent = new Intent(getApplicationContext(), LocateMyLotActivity.class);
                    resumeIntent.putExtra(Global.IS_CHECKOUT_EXTRA, true);
                    resumeIntent.putExtra(Global.CAR_PARK_ID_CHECKOUT_EXTRA, carparkId);
                    resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(resumeIntent);
                }
                //Thep update 2016/08/16 - Lay ten tu dong
                String name = neublick.locatemylot.util.Utils.getCurrentCarparkNameWithCarparkId(carparkId);
                //end
                BackgroundService.isWellCome = false;
                forceCheckIn(carparkId, true, 0);

                String title = "Confirm Check-out";
                String text = "Do you really want to check out?";

//                if (isStartNotification)
//                    showNotification(LocateMyLotActivity.this, CHECKOUT_VALUE, carparkId, title, text);
                if (alertDialogDetect != null && alertDialogDetect.isShowing()) {
                    return;
                }
//                if (isStartNotification) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
//                }
                lastTimeCheckOut = System.currentTimeMillis();
                alertDialogDetect = new AlertDialog.Builder(LocateMyLotActivity.this, R.style.AppTheme_AlertDialog)
                        .setTitle(title)
                        .setMessage(text)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearNotification(LocateMyLotActivity.this);
                                checkout();
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)

                        .create();
//        alertDialogDetect.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                alertDialogDetect.setCanceledOnTouchOutside(false);
                alertDialogDetect.show();
            }
        }

    }

    private void showDialogIsWelcome(final int carparkId, boolean isShowConfirm) {
//        if (isShowConfirm && mParkingSession.getLastCarparkShowConfirm() == carparkId) {
//            return;
//        }
//        if(System.currentTimeMillis()-mParkingSession.getTimeCheckOut()<300000)
//            return;
        if (mWellComeDialog != null && mWellComeDialog.isShowing()) {
            return;
        }
        if(!mParkingSession.isCheckIn())
            return;
        mWellComeDialog = new Dialog(LocateMyLotActivity.this);
        mWellComeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mWellComeDialog.setContentView(R.layout.dialog_welcome_cp);
        mWellComeDialog.setCanceledOnTouchOutside(false);
        TextView tvCarparkName = (TextView) mWellComeDialog.findViewById(R.id.tvCarparkName);
        Button btOk = (Button) mWellComeDialog.findViewById(R.id.btOk);
        Button btCheckOut = (Button) mWellComeDialog.findViewById(R.id.btCheckOut);
        String name = neublick.locatemylot.util.Utils.getCurrentCarparkNameWithCarparkId(carparkId);
        tvCarparkName.setText(name);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearNotification(LocateMyLotActivity.this);
                mWellComeDialog.dismiss();
            }
        });

        if (isShowConfirm) {
            btCheckOut.setVisibility(View.VISIBLE);
            btCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkout();
                    mParkingSession.setLastCarparkShowConfirm(carparkId);
                    mWellComeDialog.dismiss();
                }
            });

        }
        try {
            mWellComeDialog.show();
        }catch (Exception e){

        }
    }

    private void showDialogCheckInWithBeaconExit() {
        if (alertDialogDetect != null && alertDialogDetect.isShowing()) {
            return;
        }
        String title = "LocateMyLot";
        String text = "Would you like to set parking location?";
        showNotification(LocateMyLotActivity.this, EXIT_CAR_VALUE, DEFAULT_CARPARK_NULL, title, text);
        BackgroundService.lostExitBeacon = false;
        alertDialogDetect = new AlertDialog.Builder(LocateMyLotActivity.this, R.style.AppTheme_AlertDialog)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Global.lastTimestampDismissLostBeacon = System.currentTimeMillis();
                        showDialogGPS();
                        clearNotification(LocateMyLotActivity.this);
                    }
                })
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Global.lastTimestampDismissLostBeacon = System.currentTimeMillis();
                        alertDialogDetect.dismiss();
                        clearNotification(LocateMyLotActivity.this);
                    }
                })
                .create();
//        alertDialogDetect.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        alertDialogDetect.setCanceledOnTouchOutside(false);
        alertDialogDetect.show();
    }

    private void setMap(final int capark, final String floor) {
//        if (mParkingSession.isCheckIn() && mParkingSession.isNormalCheckIn()) {
        if (capark == DEFAULT_CARPARK_NULL) {
            showSleepLayoutAndHiddenMap();
            return;
        }
        if (lastCarParkLoadMap == capark && lastFloorLoadMap.equals(floor)) {
            String name = capark + "_" + floor + ".png";
            String dir = Global.MY_DIR + name;
            CURRENT_MAP = dir;
            File file = new File(dir);
            if (file.exists()) {
                if (rlMap.getVisibility() != View.VISIBLE && pbLoadingMap.getVisibility() != View.VISIBLE || rlSlideHelp.getVisibility() == View.VISIBLE)
                    showMapHiddenSleepSlide(name);
            } else {
                loadMapFailed();
            }
            return;
        }
        currentCapark = capark;
        currentFloor = floor;
        lastCarParkLoadMap = capark;
        lastFloorLoadMap = floor;
        new AsyncSetBitmapToMapView(capark, floor).execute();
//        }
    }

    private void loadMapFailed() {
        pbLoadingMap.setVisibility(View.GONE);
        getMapViewUserObject().visible(false);
        getMapViewCarObject().visible(false);
        getMapViewLiftLobbyObject().visible(false);
        showSleepLayoutAndHiddenMap();
//        if (alertDialogDetect != null && alertDialogDetect.isShowing())
//            alertDialogDetect.dismiss();
    }

    // chinh lai toan bo text sang font moi
    public void sangFontMoi() {
        final TextView textEntryTime = (TextView) findViewById(R.id.text_entry_time);
        final TextView textDuration = (TextView) findViewById(R.id.text_duration);

        final Typeface typeFace = Typeface.createFromAsset(
                LocateMyLotActivity.this.getAssets(),
                "RobotoRegular.ttf"
        );
        final Typeface typeMedium = Typeface.createFromAsset(
                LocateMyLotActivity.this.getAssets(),
                "RobotoMedium.ttf"
        );
        textEntryTime.setTypeface(typeFace);
        textDuration.setTypeface(typeFace);
        getEntryTime().setTypeface(typeFace);
        getDuration().setTypeface(typeFace);
        getCarparkFloor().setTypeface(typeMedium);
        getCarZone().setTypeface(typeFace);
    }

    // =========================================================
    // ==================== GETTERS ============================
    public DrawerLayout getRootView() {
        if (mRootView == null) {
            mRootView = (DrawerLayout) findViewById(R.id.drawer_layout);
        }
        return mRootView;
    }


    public MapView getMapView() {
        if (mMapView == null) {
            mMapView = (MapView) findViewById(R.id.map);
        }
        return mMapView;
    }

    public MapView.UserObjectOverlap getMapViewUserObject() {
        if (getMapView().userObject.view == null) {
            getMapView().userObject.view(findViewById(R.id.user_object));
        }
        return getMapView().userObject;
    }

    //thep 2016/02/24
    public MapView.ObjectOverlap getMapViewCarObject() {
        if (getMapView().carObject.view == null) {
            ImageView imageView = (ImageView) findViewById(R.id.car_object);
            getMapView().carObject.view(imageView);
        }
        return getMapView().carObject;
    }

    public MapView.ObjectOverlap getMapViewLiftLobbyObject() {
        if (getMapView().liftLobbyObject.view == null) {
            ImageView imageView = (ImageView) findViewById(R.id.lift_lobby_object);
            getMapView().liftLobbyObject.view(imageView);
        }
        return getMapView().liftLobbyObject;
    }//end

    public TextView getCarparkName() {
        if (mCarparkName == null) {
            mCarparkName = (TextView) findViewById(R.id.carpark_name);
            mCarparkName.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Typeface typeFace = Typeface.createFromAsset(
                            LocateMyLotActivity.this.getAssets(),
                            "RobotoMedium.ttf"
                    );
                    mCarparkName.setTypeface(typeFace);
                }
            }, 100);
        }
        return mCarparkName;
    }

    public TextView getCarparkFloor() {
        if (mCarparkFloor == null) {
            mCarparkFloor = (TextView) findViewById(R.id.car_floor);
        }
        return mCarparkFloor;
    }

    public TextView getCarZone() {
        if (mCarZone == null) {
            mCarZone = (TextView) findViewById(R.id.car_zone);
        }
        return mCarZone;
    }

    public TextView getLiftZone() {
        if (mLiftZone == null) {
            mLiftZone = (TextView) findViewById(R.id.lift_zone);
        }
        return mLiftZone;
    }

    public ToggleSquareImageButton getFuncWay() {
        if (funcWay == null) {
            funcWay = (ToggleSquareImageButton) findViewById(R.id.func_way);
            funcWay.setMyOnClickListener(new ToggleSquareImageButton.MyOnClickListener() {
                @Override
                public void onClick(View view) {
                    //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
                    boolean isCheckIn = mParkingSession.isCheckIn();
                    boolean isNormalCheckIn = mParkingSession.isNormalCheckIn();
                    if (!isCheckIn) {
                        new AlertDialog.Builder(LocateMyLotActivity.this)
                                .setTitle("Information")
                                .setMessage("You need to check in first before using this function")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        if (isNormalCheckIn || mParkingSession.isCarCheckLocation()) {
                            if(funcWay.statePressed)
                            funcWay.modifyState(false);
                            else
                                funcWay.modifyState(true);

                            // xoa map neu chuyen tu che do ve duong di sang ko ve duong di
//                            if (getMapView().wayMode) {
//                                getMapView().wayClear();
//
//                                // System.gc() se danh dau nhung bien can xoa neu ta set bien = null
//                                getMapView().deallocate();
//                            }
//                            getMapView().wayMode = !getMapView().wayMode;
//                            mParkingSession.setWayMode(getMapView().wayMode);
//                            getMapView().centerByCarObject();
                        }
                    }
                }
            });
        }
        return funcWay;
    }

    public ToggleSquareImageButton getFuncCheckIn() {
        if (funcCheckIn == null) {
            funcCheckIn = (ToggleSquareImageButton) findViewById(R.id.func_check_in);
            funcCheckIn.setMyOnClickListener(new ToggleSquareImageButton.MyOnClickListener() {
                @Override
                public void onClick(View view) {
                    checkInAction(true);
                }
            });
        }
        return funcCheckIn;
    }

    public void checkInAction(final boolean isNormalCheckIn) {
        //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        boolean isCheckIn = mParkingSession.isCheckIn();
        final int carparkId = currentCapark;
        // NOW WE CHECK IN
        if (!isCheckIn) {
            if (isNormalCheckIn) {
                if (currentCapark >= 0 && !currentFloor.isEmpty()) {
                    showDialogCheckIn(2, carparkId);
                } else {
                    Toast.makeText(LocateMyLotActivity.this, getString(R.string.no_carpark), Toast.LENGTH_SHORT).show();
                }
            } else {//Thep update 2016/08/19 - Update xu ly click timer
                forceCheckIn(currentCapark, isNormalCheckIn, 0);
            }
        } else {
            if (isNormalCheckIn) {
                if (!mParkingSession.isCarCheckLocation()) {
                    if (currentCapark >= 0 && !currentFloor.isEmpty() && mParkingSession.isNormalCheckIn()) {
                        showDialogCheckIn(1, carparkId);
                    } else {
                        showDialogCheckOut("");
//                        Toast.makeText(LocateMyLotActivity.this, getString(R.string.no_carpark), Toast.LENGTH_SHORT).show();
                    }
                    return;
                } else {
                    showDialogCheckOut("");
                }
            } else {
                showDialogGPS();
            }
            // now checkOut
            // ask confirm checking-out
//            showDialogCheckOut("");
        }

    }

    private void showDialogCheckOut(final String dataGPS) {
        try {
            new android.support.v7.app.AlertDialog.Builder(LocateMyLotActivity.this, R.style.AppTheme_AlertDialog)
                    .setTitle("Confirm Check-out")
                    .setMessage("Do you really want to check out?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!dataGPS.isEmpty()) {
                                showCarParkNear(true, dataGPS);
                            }
                            //Thep update 2016/08/06
                            checkout();
                        }
                    })
                    .show();
        }catch (Exception e){
//            Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkout() {
        currentMapCar = "";
        currentMapLiftLobby = "";
        lastTimeQuestion = -1;
        //end
        ParkingHistory newEntry = new ParkingHistory();
        newEntry.carpackId = mParkingSession.getCarParkCheckIn();// parkingSession.getInt("CHECKIN_CARPARK",DEFAULT_CARPARK_NULL);
        if (mParkingSession.isNormalCheckIn() && mParkingSession.isCarCheckLocation()) {
            newEntry.floor = mParkingSession.getFloor();//parkingSession.getString("FLOOR", "");
            newEntry.zone = mParkingSession.getZone();//parkingSession.getString("ZONE", "");
            newEntry.x = mParkingSession.getX();//parkingSession.getFloat("ORIGINAL_X", 0);
            newEntry.y = mParkingSession.getY();//parkingSession.getFloat("ORIGINAL_Y", 0);
        } else {
            newEntry.floor = "";//parkingSession.getString("FLOOR", "");
            newEntry.zone = "";//parkingSession.getString("ZONE", "");
            newEntry.x = 0;//parkingSession.getFloat("ORIGINAL_X", 0);
            newEntry.y = 0;//parkingSession.getFloat("ORIGINAL_Y", 0);
        }
        newEntry.rates = Float.valueOf(tvParkingRates.getText().toString());
        newEntry.photoName = mParkingSession.getPhotoName();
        long timeCheckOut = Calendar.getInstance().getTimeInMillis();
        Global.timeCheckOut = timeCheckOut;
        newEntry.timeCheckIn = mParkingSession.getTimeCheckIn();//parkingSession.getLong("TIME_CHECKIN",-1);
        newEntry.timeCheckOut = timeCheckOut;
        if (mParkingSession.isNormalCheckIn()) {
            newEntry.liftData = mParkingSession.getLiftLobby();
            newEntry.isNormal = 1;
        } else {
            newEntry.liftData = "";
            newEntry.isNormal = 0;
        }

        CLParkingHistory.addEntry(newEntry);
        mParkingSession.setCheckOut(timeCheckOut);
        reloadAfterCheckout();

    }

    private void showDialogCheckIn(final int type, final int carparkId) {
        String zone = getMapViewUserObject().zone;
        if (zone != null && !zone.isEmpty()) {
            zone = "Zone: " + zone;
        } else {
            zone = "";
        }
        final DialogUserLocation kdialog = new DialogUserLocation(
                LocateMyLotActivity.this,
                CLCarpark.getCarparkNameByCarparkId(carparkId),
                "Floor: " + getMapViewUserObject().floor,
                zone
        ) {
            @Override
            public void onCheckIn() {
                // force to checkIn, do not care if checked in
//                if(mMapView.getVisibility()==View.VISIBLE) {
                forceCheckIn(carparkId, true, type);
                this.dismiss();
//                }else{
//                    Toast.makeText(LocateMyLotActivity.this, getString(R.string.not_found_location), Toast.LENGTH_SHORT).show();
//                }
            }
        };
        kdialog.show();
    }


    public RoundedImageView getViewAttached() {
        if (viewAttached == null) {
            viewAttached = (RoundedImageView) findViewById(R.id.view_attached);
            viewAttached.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent rotationIntent = new Intent(LocateMyLotActivity.this, RotationActivity.class);
                    startActivity(rotationIntent);
                }
            });
        }
        return viewAttached;
    }

    public RoundedImageView getViewAttached2() {
        if (viewAttached2 == null) {
            viewAttached2 = (RoundedImageView) findViewById(R.id.view_attached2);
            viewAttached2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent rotationIntent = new Intent(LocateMyLotActivity.this, RotationActivity.class);
                    startActivity(rotationIntent);
                }
            });
        }
        return viewAttached2;
    }

    public ToggleSquareImageButton getFuncTime() {
        if (funcTime == null) {
            funcTime = (ToggleSquareImageButton) findViewById(R.id.func_time);
            funcTime.setMyOnClickListener(new SquareImageButton.MyOnClickListener() {
                @Override
                public void onClick(View view) {
//                    boolean isCheckIn = mParkingSession.isCheckIn();
//                    if (!isCheckIn) {
//                        if (GPSHelper.isGPSEnable(LocateMyLotActivity.this)) {
//                            showCarParkNear(true, "");
//                        } else {
//                            showDialogGPS();
//                        }
//                    } else {
//                        checkInAction(false);
//                    }
                    funcTime.modifyState();
                    if (funcTime.statePressed) {
                        vpCarParkType.setCurrentItem(0);
                        updateStatusCarParkType(0);
                        vpCarParkType.setVisibility(View.VISIBLE);
                        llCarParkType.setVisibility(View.VISIBLE);
                        if(!isUpdateLotRunning){
                            new UpdateLotOfCarPark().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    } else {
                        hiddenFunTime();
                    }
                }
            });
        }
        return funcTime;
    }

    private void hiddenFunTime() {
        vpCarParkType.setVisibility(View.GONE);
        llCarParkType.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getFuncTime().statePressed) {
            getFuncTime().modifyState();
            hiddenFunTime();
        } else {
            super.onBackPressed();
        }
    }

    public TextView getDuration() {
        if (funcDuration == null) {
            funcDuration = (TextView) findViewById(R.id.duration);
        }
        return funcDuration;
    }

    public TextView getTvParkingRates() {
        if (tvParkingRates == null) {
            tvParkingRates = (TextView) findViewById(R.id.tvParkingRates);
            tvParkingRates.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    CURRENT_RATES_TEXT = s.toString();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        return tvParkingRates;
    }


    public TextView getEntryTime() {
        if (funcEntry == null) {
            funcEntry = (TextView) findViewById(R.id.entry_time);
        }
        return funcEntry;
    }

    // =========================================================
    // =========================================================
    //Thep update 2016/08/05

    /***
     * Load map tu anh da down ve
     * Hien thi progressBar khi load va an sau qua trinh load
     * Khi bat dau chuyen sang map moi thi an map va chi hien khi load duoc anh
     */
    class AsyncSetBitmapToMapView extends android.os.AsyncTask<Integer, Void, Bitmap> {
        private File fileMap;
        private String name;
        private int mCarparkId;
        private String mFloor;

        public AsyncSetBitmapToMapView(int carparkId, String floor) {
            pbLoadingMap.setVisibility(View.VISIBLE);
            getMapViewCarObject().visible(false);
            getMapViewLiftLobbyObject().visible(false);
            mCarparkId = carparkId;
            mFloor = floor;
            name = carparkId + "_" + floor + ".png";
            String dir = Global.MY_DIR + name;
            CURRENT_MAP = dir;
            fileMap = new File(dir);
        }


        @Override
        public Bitmap doInBackground(Integer... resourceIDs) {
            Bitmap bitmap = null;
            if (fileMap.exists()) {
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(LocateMyLotActivity.this.getContentResolver(), Uri.fromFile(fileMap));
//                } catch (IOException e) {
//                    return bitmap;
//                }
                return Utils.decodeSampledBitmapFromUri(LocateMyLotActivity.this, Uri.fromFile(fileMap), 0, 0);
            }
            return bitmap;
        }

        @Override
        public void onPostExecute(Bitmap result) {
            pbLoadingMap.setVisibility(View.GONE);
            if (result != null) {

                getMapView().setImageBitmap(result);
                //Hien thi lai thiet lap



                //hien thi ten map
                setTextDetails(mCarparkId, mFloor);
                showMapHiddenSleepSlide(name);
//                resumeLiftLobby(name);
//                resumeLocationCar(name);
//                getMapViewUserObject().visible(true);

//                String liftLobbySave = mParkingSession.getLiftLobby();//parkingSession.getString("LIFT_LOBBY_SAVE", "");
//                if (!liftLobbySave.isEmpty())
//                    getMapViewLiftLobbyObject().visible(true);
//
//                if (name.equals(currentMapCar)) {
//                    getMapViewCarObject().visible(true);
//                } else {
//                    getMapViewCarObject().visible(false);
//                }

                lw("mMapWidth=" + mMapWidth + ", mMapHeight=" + mMapHeight);
                //getMapView().setInitialScale(mMapWidth, mMapHeight);

                // tinh bitmap_height tren server
                server_height = server_width * getMapView().bitmapHeight / getMapView().bitmapWidth;

                // tinh ti so quy doi tu ban do tren server sang local
                Global.mRatioX = (float) (getMapView().bitmapWidth * 1.0 / server_width);
                Global.mRatioY = (float) (getMapView().bitmapHeight * 1.0 / server_height);

                lw("mRatioX = " + Global.mRatioX + ", mRatioY = " + Global.mRatioY);
            } else {
                loadMapFailed();
//                if(mCarparkId!=DEFAULT_CARPARK_NULL)
//                Toast.makeText(LocateMyLotActivity.this, getString(R.string.load_map_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMapHiddenSleepSlide(String name) {
        rlMap.setVisibility(View.VISIBLE);
        hiddenSleepLayout();
        getMapViewUserObject().visible(true);

        resumeLocationCar(name);
        resumeLiftLobby(name);
    }

    //end
    @Override
    protected void onResume() {
        super.onResume();
        le("LocateMyLotActivity.onResume()");
//        clearNotification(LocateMyLotActivity.this);
        // hien thi trang thai checkIn (hay con goi la carStatus)
        isCheckIn = mParkingSession.isCheckIn();
        final boolean wayMode = mParkingSession.isWayMode();//parkingSession.getBoolean("WAY_MODE", false);
        if (isCheckIn) {
            setTextDetails(currentCapark, currentFloor);

        }
        // khoi phuc trang thai cua nut "ve duong"
        final SharedPreferences toggleState = getSharedPreferences("toggle_state", MODE_PRIVATE);
        final boolean isNormalCheckIn = mParkingSession.isNormalCheckIn();//parkingSession.getBoolean("IS_NORMAL_CHECK_IN", true);
        final boolean isCheckCar = mParkingSession.isCarCheckLocation();//parkingSession.getBoolean("CHECK_CAR_LOCATION", false);
//        getFuncWay().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getFuncWay().restoreState(toggleState.getString("func_way", ""));
//                if (isCheckIn && wayMode) {
//                    getFuncWay().statePressed = true;
//                    getFuncWay().updateBackgroundColor();
////                    getMapView().wayMode = true;
//                }
//            }
//        }, 0);

        // khoi phuc trang thai cua nut "check in"
        getFuncCheckIn().postDelayed(new Runnable() {
            @Override
            public void run() {
                getFuncCheckIn().restoreState(toggleState.getString("func_check_in", ""));
                if (isCheckIn && ((isNormalCheckIn && isCheckCar) || !isNormalCheckIn)) {
                    getFuncCheckIn().statePressed = true;
                    getFuncCheckIn().updateBackgroundColor();
                } else {
                    getFuncCheckIn().statePressed = false;
                    getFuncCheckIn().updateBackgroundColor();
                }
            }
        }, 0);
        //Thep update 2016/08/19 khoi phuc trang thai cua nut "time"
//        getFuncTime().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getFuncTime().restoreState(toggleState.getString("func_time", ""));
//                if (isCheckIn && !isNormalCheckIn) {
//                    getFuncTime().statePressed = true;
//                    getFuncTime().updateBackgroundColor();
//                } else {
//                    getFuncTime().statePressed = false;
//                    getFuncTime().updateBackgroundColor();
//                }
//            }
//        }, 0);

        // khoi phuc trang thai cua MapView
        getMapView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCheckIn) {
                    // truoc tien khoi phuc ban do voi cac tham so (zoom, transX, transY)
                    // sau do khoi phuc trang thai cua carObject
                    // luu y rang carObject duoc bieu dien tren ban do khi ta da check in
                    if (isNormalCheckIn) {
//                        llTime.setVisibility(View.INVISIBLE);
                        Matrix savedMatrix = getMapView().restoreState();


//222

//Thep update 2016/08/05
                        String name = currentCapark + "_" + currentFloor + ".png";
                        resumeLiftLobby(name);
                        resumeLocationCar(name);

//end
                        if (savedMatrix != null) {
                            getMapViewCarObject().applyMatrix(savedMatrix);
                            getMapViewLiftLobbyObject().applyMatrix(savedMatrix);
                        }
                    /*else {
                        getMapViewCarObject().applyMatrix(getMapView().MATRIX_INITIAL_SCALE);
					}
					*/

                        setMap(currentCapark, currentFloor);
                    } else {
                        showSleepLayoutAndHiddenMap();

                    }
                }
            }
        }, 500);

        // RESTORE SCREEN VIEW ATTACHED TO THE MAP
        final String photoName = mParkingSession.getPhotoName();//parkingSession.getString("PHOTO_NAME", "");
        // fileName chac chan khac "" ?!
        if (!photoName.equals("")) {
            File file = Utils.getImageFile(photoName);
            loadAttachView(file);
        } else {
//            if (llTime.getVisibility() == View.VISIBLE)
//                getViewAttached2().setVisibility(View.GONE);
//            else
//            getViewAttached().setVisibility(View.GONE);

        }


        if (sensorManager != null) {
            sensorManager.registerListener(getMapViewUserObject(), sensorOrientation, SensorManager.SENSOR_DELAY_UI);
        }


        if (secTickTimer != null) {
            if (!secTickTimer.isRunning())
                secTickTimer.start();
        }

        // dang ky receiver
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        boolean useMapDirection = settings.getBoolean("use_map_direction", false);
        if (useMapDirection) {
            Global.calibAngle = settings.getFloat("alpha", -1);
        }


        checkShareLocation();
        String userId = UserUtil.getUserId(LocateMyLotActivity.this);
//        if(!UserUtil.isNotShowEnterPhoneAgain(LocateMyLotActivity.this)) {
        String phone = UserUtil.getUserPhone(LocateMyLotActivity.this);
        if (phone.isEmpty() && !userId.isEmpty()) {
            showDialogEnterPhone(true);
        }
//        }

//        if (userId.isEmpty()&&Utils.isInternetConnected(LocateMyLotActivity.this)) {
//            showSignInSignUp();
//        }
        getExtra();
    }

    private void loadAttachView(File file) {
//        if (llTime.getVisibility() == View.VISIBLE) {
//            Picasso.with(LocateMyLotActivity.this).load(file).into(getViewAttached2(), new Callback() {
//                @Override
//                public void onSuccess() {
//                    getViewAttached2().setVisibility(View.VISIBLE);
//
//                }
//
//                @Override
//                public void onError() {
//                    getViewAttached2().setVisibility(View.GONE);
//
//                }
//            });
//        } else {
        Picasso.with(LocateMyLotActivity.this).load(file).into(btCamera, new Callback() {
            @Override
            public void onSuccess() {
//                getViewAttached().setVisibility(View.VISIBLE);

            }

            @Override
            public void onError() {
//                getViewAttached().setVisibility(View.GONE);

            }
        });
//        }
    }

    private void checkShareLocation() {
        if (ShareLocationUtil.isNewData(LocateMyLotActivity.this, UserUtil.getUserId(LocateMyLotActivity.this))) {
            Global.isGetSharedLocationDialogShown = true;
            Intent intent = new Intent(getBaseContext(), DialogGetSharedLocation.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Global.SHARE_DATA_EXTRA, ShareLocationUtil.getLastShareLocation(LocateMyLotActivity.this));
            startActivity(intent);
        }
    }

    private void getExtra() {
        //GetExtra
        Intent intent = getIntent();
        int typeNotifyCation = intent.getIntExtra(DATA_EXTRA_NOTIFICATION_KEY, -1);
        if (typeNotifyCation == WELLCOME_VALUE) {
            showDialogIsWelcome(intent.getIntExtra(DATA_EXTRA_CARPARK_KEY, DEFAULT_CARPARK_NULL), false);
        } else if (typeNotifyCation == LIFT_LOBBY_VALUE) {
            BeaconPoint beaconPoint = CLBeacon.getBeaconById(intent.getIntExtra(DATA_EXTRA_CARPARK_KEY, DEFAULT_CARPARK_NULL));
            setMap(beaconPoint.mCarparkId, beaconPoint.mFloor);
            showDialogLiftLobby(beaconPoint, false);

        } else if (typeNotifyCation == EXIT_CAR_VALUE) {
            showDialogCheckInWithBeaconExit();
        } else if (typeNotifyCation == CHECKOUT_VALUE) {
            actionCheckOut(intent.getIntExtra(DATA_EXTRA_CARPARK_KEY, DEFAULT_CARPARK_NULL), false);
        }
        if (intent.getBooleanExtra(Global.IS_WELCOME_EXTRA, false)) {
            showDialogIsWelcome(intent.getIntExtra(Global.CAR_PARK_ID_WELCOME_EXTRA, DEFAULT_CARPARK_NULL), intent.getBooleanExtra(Global.CONFIRM_WELCOME, false));
        } else if (intent.getBooleanExtra(Global.IS_LIFT_LOBBY_EXTRA, false)) {
            showDialogLiftLobby(CLBeacon.getBeaconById(intent.getIntExtra(Global.ID_LIFT_LOBBY_EXTRA, 0)), false);
        } else if (intent.getBooleanExtra(Global.IS_CHECKOUT_EXTRA, false)) {
            actionCheckOut(intent.getIntExtra(Global.CAR_PARK_ID_CHECKOUT_EXTRA, DEFAULT_CARPARK_NULL), false);
        }
        
        intent.removeExtra(Global.IS_WELCOME_EXTRA);
        intent.removeExtra(Global.CAR_PARK_ID_WELCOME_EXTRA);
        intent.removeExtra(DATA_EXTRA_CARPARK_KEY);
        intent.removeExtra(DATA_EXTRA_NOTIFICATION_KEY);
    }


    private void resumeLiftLobby(String mapName) {
        //currentMapLiftLobby+";"+x+";"+y+";"+zone+";"+floor;
        String data = mParkingSession.getLiftLobby();//parkingSession.getString("LIFT_LOBBY_SAVE", "");
        if (data.contains(mapName)) {
            float x_lift_lobby = 0;
            float y_lift_lobby = 0;
            String zone_lift_lobby = "";
            String floor_lift_lobby = "";
            String[] dataArr = data.split("~");
            for (int i = 0; i < dataArr.length; i++) {
                if (dataArr[i].contains(mapName)) {
                    String[] tmp = dataArr[i].split(";");
                    currentMapLiftLobby = tmp[0];
                    x_lift_lobby = Float.valueOf(tmp[1]);
                    y_lift_lobby = Float.valueOf(tmp[2]);
                    try {
                        zone_lift_lobby = tmp[3];
                    } catch (Exception e) {
                        zone_lift_lobby = "";
                    }
                    try {
                        floor_lift_lobby = tmp[4];
                    } catch (Exception e) {
                        floor_lift_lobby = "";
                    }

                }
            }
//            getMapViewLiftLobbyObject().original(x_lift_lobby, y_lift_lobby).applyMatrix(getMapView().drawMatrix).floor(floor_lift_lobby).zone(zone_lift_lobby).visible(true);
            getMapViewLiftLobbyObject().original(x_lift_lobby, y_lift_lobby).applyMatrix(getMapView().drawMatrix).floor(floor_lift_lobby).zone(zone_lift_lobby).visible(true);
        } else {
            getMapViewLiftLobbyObject().visible(false);
        }

    }

    private void resumeLocationCar(String name) {
        boolean isCheckCar = mParkingSession.isCarCheckLocation();// parkingSession.getBoolean("CHECK_CAR_LOCATION", false);
        if (isCheckCar) {
            String zone = mParkingSession.getZone();//parkingSession.getString("ZONE", "...");
            String floor = mParkingSession.getFloor();//parkingSession.getString("FLOOR", "...");
            float originalX = mParkingSession.getX();// parkingSession.getFloat("ORIGINAL_X", 0);
            float originalY = mParkingSession.getY();//parkingSession.getFloat("ORIGINAL_Y", 0);
            int carpark = mParkingSession.getCarParkCheckIn();//parkingSession.getInt("CHECKIN_CARPARK", DEFAULT_CARPARK_NULL);
            currentMapCar = carpark + "_" + floor + ".png";
            if (name.equals(currentMapCar)) {
                getMapViewCarObject().original(originalX, originalY).zone(zone).floor(floor).visible(true);
            } else {
                getMapViewCarObject().visible(false);
            }
        }
    }

    @Override
    protected void onPause() {
        le("LocateMyLotActivity.onPause()");
        // luu lai trang thai cua nut "ve duong" va "check in"
        SharedPreferences toggleState = getSharedPreferences("toggle_state", MODE_PRIVATE);
        toggleState.edit()
                .putString("func_way", getFuncWay().saveState())
                .putString("func_check_in", getFuncCheckIn().saveState())
                .putString("func_time", getFuncTime().saveState())
                .apply();

        if (mDurationTimer != null) {
            mDurationTimer.stop();
        }

        if (mEntryTimer != null) {
            mEntryTimer.stop();
        }

        // luu lai trang thai cua MapView
        getMapView().saveState();

        if (sensorManager != null) {
            sensorManager.unregisterListener(getMapViewUserObject());
        }

        if (secTickTimer != null) {
            secTickTimer.stop();
        }

        unregisterReceiver(bluetoothReceiver);


        // [van con nua]
        super.onPause();
    }

    @Override
    public void onDestroy() {
//        serviceManager.unbind();
        unregisterReceiver(myBroadcastReceiver1);
        super.onDestroy();
    }


    void lw(String s) {
//        final String TAG = LocateMyLotActivity.class.getSimpleName();
//        android.util.Log.w(TAG, s);
    }

    private void dispatchCaptureIntent() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File parentDir = new File(Environment.getExternalStorageDirectory(), Config.PHOTO_SAVE_DIR);
        parentDir.mkdirs();

        final String fileName = java.util.UUID.randomUUID().toString() + ".jpg";
        //SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        File file = new File(parentDir, fileName);

        final String previousFileName = mParkingSession.getPhotoName();//parkingSession.getString("PHOTO_NAME", "");
        mParkingSession.setPhotoName(fileName);
        mParkingSession.setPreviousPhotoName(previousFileName);

        Uri uri = Uri.fromFile(file);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            if (resultCode == RESULT_OK) {
                File parentDir = new File(Environment.getExternalStorageDirectory(), Config.PHOTO_SAVE_DIR);
                parentDir.mkdirs();
                //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
                final String fileName = mParkingSession.getPhotoName();//parkingSession.getString("PHOTO_NAME", "");

                // fileName chac chan khac "" ?!
                if (!fileName.equals("")) {
                    File file = Utils.getImageFile(fileName);
                    loadAttachView(file);
//                    new TaskShowImage<RoundedImageView>(LocateMyLotActivity.this, getViewAttached()).execute(fileName);
                }

            } else {
                // restore the previous photoName
                //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
                mParkingSession.setPhotoName(mParkingSession.getPreviousPhotoName());
            }
        } else if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_CANCELED) {
                //BluetoothBroadcastReceiver.requestBluetoothEnabled(LocateMyLotActivity.this);
            }
        } else if (requestCode == RESULT_SELECT_CARPARK) {
//            if (resultCode == Activity.RESULT_OK) {
//                dismissMyDialog();
//                int carparkId = data.getIntExtra(DialogSelectCarPark.CARPARK_ID, DEFAULT_CARPARK_NULL);
//                if (carparkId >= 0) {
//                    currentCapark = carparkId;
//                    checkInAction(false);
//                }
//                showDialogWarning();
//            }
        }
    }

    private void dismissMyDialog() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        if (alertDialogDetect != null && alertDialogDetect.isShowing())
            alertDialogDetect.dismiss();
    }

    private void showDialogWarning() {
        String txt = "";
        for (ParkingRates parkingRates : parkingRatesList) {
            int status = parkingRates.getStatus();
            if (status == 1) {
                continue;
            } else {
                if (status == 2) {
                    txt = "Reserve valuable spaces with tenant only parking signs";
                    break;
                } else if (status == 3) {
                    if (parkingRates.getDayType() == 0) {
                        txt = "Parking closed";
                        break;
                    } else {
                        Date date = new Date();
                        boolean isHoliday = false;
                        if (Utils.getDayOfWeek(date) == Calendar.SUNDAY && parkingRates.getDayType() == 2) {
                            isHoliday = true;
                        } else {
                            for (Holiday holiday : holidays) {
                                if (holiday.isHoliday(date)) {
                                    isHoliday = true;
                                    break;
                                }
                            }
                        }
                        if (isHoliday) {
                            txt = "All our car parks are closed on Sunday and holidays";
                            break;
                        }
                    }
                } else if (status == 4) {
                    txt = "Reserved for Season Parking";
                    break;
                }

            }
        }
        if (!txt.isEmpty()) {
            if (alertDialog.isShowing()) {
                return;
            }
            if (alertDialogWarning == null) {
                alertDialogWarning = new AlertDialog.Builder(LocateMyLotActivity.this, R.style.AppTheme_AlertDialog);
                // Setting Dialog Title
                alertDialogWarning.setTitle("LocateMyLot");
                // Setting Dialog Message
                alertDialogWarning.setMessage(txt);
                alertDialogWarning.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
//

                alertDialog = alertDialogWarning.create();
            }

            alertDialog.show();
        }
    }

    public static final int REQUEST_CAPTURE_IMAGE = 123;
    public static final int REQUEST_BLUETOOTH = 456;
    public static final int RESULT_SELECT_CARPARK = 79;


    public void forceCheckIn(int carparkId, boolean isNormalCheckIn, int type) {
        mParkingSession.setZone(getMapViewUserObject().zone);//parkingSession.edit().putString("ZONE", getMapViewUserObject().zone).apply();
        mParkingSession.setCarParkCheckIn(carparkId);
        //Thep update 2016/08/19 - update trang thai chung
        mParkingSession.setNormalCheckIn(isNormalCheckIn);
        currentCapark = carparkId;
        checkInManual(-1, type);
        if (isNormalCheckIn) {
            String name = carparkId+"_"+currentFloor+".png";
            showMapHiddenSleepSlide(name);
//            llTime.setVisibility(View.INVISIBLE);
            if (type != 0)
                getFuncCheckIn().modifyState();
        } else {
            showSleepLayoutAndHiddenMap();
            getFuncCheckIn().modifyState();

        }//end
        //thep 2016/02/24
    }


    /***
     * Neu isCheckCar = 0 check in tính giá
     * 1- Check car location
     * Còn lại - Check cả 2
     *
     * @param time
     * @param isCheckCar
     */
    public void checkInManual(long time, int isCheckCar) {
        //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        // operating on user/mobile && car objects
        // dim car object on the map
        switch (isCheckCar) {
            case 0:
                checkIn(time);
                break;
            case 1:
                checkCar();
                break;
            default:
                checkIn(time);
                checkCar();
                break;
        }


    }

    private void checkIn(long time) {
        long t = time > 0 ? time : (Global.entryTime > 0 ? Global.entryTime : System.currentTimeMillis());
        Global.entryTime = t;
        mParkingSession.setTimeCheckIn(t);
        mParkingSession.setCheckIn(true);
        updateCheckInStatus();
    }

    private void checkCar() {
        currentMapCar = currentCapark + "_" + currentFloor + ".png";
        //end
        float x = getMapViewUserObject().location.originalX;
        float y = getMapViewUserObject().location.originalY;
        String floor = getMapViewUserObject().floor;
        String zone = getMapViewUserObject().zone;

        //thep 2016/02/24
        getMapViewCarObject().original(x, y).applyMatrix(getMapView().drawMatrix).visible(true).floor(floor).zone(zone);
        //end


        // carStatus chinh la zone cua car-object da check in


        // save to preference that user did checked in
        mParkingSession.setX(x);
        mParkingSession.setY(y);
        mParkingSession.setZone(zone);
        mParkingSession.setFloor(floor);
        mParkingSession.setCheckCarLocation(true);
        mParkingSession.setLastCarParkCheckIn(currentCapark);
        mParkingSession.setLastFloorCheckIn(floor);
        setTextDetails(currentCapark, floor);
    }

    public void checkInLiftLobby(BeaconPoint beaconPoint) {
        //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        // operating on user/mobile && car objects
        // dim car object on the map
        //Thep update 2016/08/05
        int carpark = beaconPoint.mCarparkId;
        String floor = beaconPoint.mFloor;
        currentMapLiftLobby = carpark + "_" + floor + ".png";
        //end
        float x = beaconPoint.mX;//getMapViewUserObject().location.originalX;
        float y = beaconPoint.mY;//getMapViewUserObject().location.originalY;
//        String floor = getMapViewUserObject().floor;
        String zone = getMapViewUserObject().zone;


//		getCarparkFloor().setText(String.format("Floor %s", floor));

        //thep 2016/02/24
        getMapViewLiftLobbyObject().original(x, y).applyMatrix(getMapView().drawMatrix).visible(true).floor(floor).zone(zone);
        //end

        //Thep update 2016/08/05
        String dataSave = currentMapLiftLobby + ";" + x + ";" + y + ";" + zone + ";" + floor;
        mParkingSession.setLiftLobby(dataSave);
        setTextLiftLobby(zone);
        //end
//		updateCheckInStatus();

/*
        // thiet lap thoi gian check in
		long current = Calendar.getInstance().getTimeInMillis();
		parkingSession.edit().putLong("DURATION_ENTRY_TIME", current).apply();
		getEntryTime().setText(
			Utils.getCalendarReadableForEntryTime(Utils.retrieveCalendarFromMillis(current))
		);

*/

/*
        String fmt = String.format(
				"Your vehicle is now parked on\n\t\tFloor: %s,\n\t\tZone: %s",
				parkingSession.getString("FLOOR", "..."),
				parkingSession.getString("ZONE", "...")
		);
		// hien thi ban da check in tai vi tri XXX va thoi gian check in?
		new android.support.v7.app.AlertDialog.Builder(LocateMyLotActivity.this)
				.setTitle("Amara Hotel Carpark")
				.setMessage(fmt)
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
*/
    }

    public void acceptSharedLocation(String shared_data) {
//        le("acceptSharedLocation: " + shared_data);
        //final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        // operating on user/mobile && car objects
        // dim car object on the map
        //{"user_from":"106742956078043764885", "x":"224", "y":"135", "floor":"B2", "zone":"Z21",
        // "carpark_id":"9", "check_in_time":"1474720128301"}
        try {
            JSONObject jsonObject = new JSONObject(shared_data);
            timeCheckIn = jsonObject.getLong("check_in_time");
            int carpark = jsonObject.getInt("carpark_id");
            Global.entryTime = timeCheckIn;
            mParkingSession.setCheckIn(true);
            mParkingSession.setCarParkCheckIn(carpark);
            mParkingSession.setTimeCheckIn(timeCheckIn);

            String floor = jsonObject.getString("floor");
            if (!floor.isEmpty()) {
                float x = Float.parseFloat(jsonObject.getString("x"));
                float y = Float.parseFloat(jsonObject.getString("y"));
                String zone = jsonObject.getString("zone");
                getCarparkName().setText(CLCarpark.getCarparkNameByCarparkId(carpark));
                getCarparkFloor().setText(String.format("Floor %s", floor));

                getMapViewCarObject().original(x, y).applyMatrix(getMapView().drawMatrix).visible(true).floor(floor).zone(zone);

                lastCarParkLoadMap = DEFAULT_CARPARK_NULL;
                currentCapark = DEFAULT_CARPARK_NULL;
                showSleepLayoutAndHiddenMap();

                // save to preference that user did checked in

                mParkingSession.setCheckCarLocation(true);
                mParkingSession.setNormalCheckIn(true);
                mParkingSession.setX(x);
                mParkingSession.setY(y);
                mParkingSession.setZone(zone);
                mParkingSession.setFloor(floor);
                mParkingSession.setLastCarParkCheckIn(carpark);
                mParkingSession.setLastFloorCheckIn(floor);

                //update lift lobby 16/09/26
//                    .putString("LIFT_LOBBY_SAVE", "")
                setTextDetails(carpark, floor);
//            setTextLiftLobby();
                //end
            } else {
                mParkingSession.setCheckIn(true);
                mParkingSession.setNormalCheckIn(false);
                mParkingSession.setX(0);
                mParkingSession.setY(0);
                mParkingSession.setZone("");
                mParkingSession.setFloor("");
            }
            updateCheckInStatus();

        } catch (JSONException e) {

        }
    }

    public void sendMessageToBackgroundService(Message m) {
        try {
            if (serviceManager != null) {
                serviceManager.send(m);
            }
        } catch (RemoteException e) {
            lw("RemoteException at sendMessageToBackgroundService()");
        }
    }

    void getAppHash() {
        le("getAppHash");
        try {
            String packageName = getPackageName();
            PackageInfo info = getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                le("MY KEY HASH=" + sign);
                //  Toast.makeText(getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            le("getAppHash error " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            le("getAppHash error " + e.getMessage());
        }
    }

    void le(String s) {
        final String TAG = LocateMyLotActivity.class.getSimpleName();
        android.util.Log.e(TAG, s);
    }

    public void updateSignInMenu() {
        updateSignInMenu("");
    }

    public void updateSignInMenu(String user) {
        if (user.isEmpty())
            user = UserUtil.getUserId(LocateMyLotActivity.this);
        btEdit = (ImageButton) ((NavigationView) findViewById(R.id.navigation)).getHeaderView(0).findViewById(R.id.btEdit);
        etDisplayName = (EditText) ((NavigationView) findViewById(R.id.navigation)).getHeaderView(0).findViewById(R.id.etDisplayName);
        ivAvatar = (CircleImageView) ((NavigationView) findViewById(R.id.navigation)).getHeaderView(0).findViewById(R.id.ivAvatar);
        etDisplayName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    editDisplayName();
                    return true;
                }
                return false;
            }
        });
        String email = UserUtil.getUserEmail(LocateMyLotActivity.this);
        String fullName = UserUtil.getUserFullName(LocateMyLotActivity.this);
        String s = user + email + fullName;
        if (UserUtil.isLoginSocial(LocateMyLotActivity.this)){
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_change_pass).setVisible(false);
        }else{
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_change_pass).setVisible(true);
        }
        if (!s.isEmpty()) {
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_sign_in).setVisible(false);
//            String userNameText = "Sign Out (" + fullName + ")";
//            String userId = UserUtil.getUserId(LocateMyLotActivity.this);
            if (fullName.isEmpty()) {
                fullName = getString(R.string.anonymous_user);
            }
            if (email.isEmpty())
                email = fullName;
            email = email.substring(0, 1).toUpperCase() + email.substring(1);
            etDisplayName.setVisibility(View.VISIBLE);
            ivAvatar.setVisibility(View.VISIBLE);
            btEdit.setVisibility(View.VISIBLE);
//            loadAvatar();
            etDisplayName.setText(fullName);
//            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_sign_out).setTitle(email);
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_sign_out).setVisible(true);
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_sign_in).setVisible(false);

        } else {
            etDisplayName.setVisibility(View.GONE);
            Picasso.with(LocateMyLotActivity.this).load(R.drawable.default_avatar).into(ivAvatar);
            ivAvatar.setVisibility(View.GONE);
            btEdit.setVisibility(View.GONE);
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_sign_in).setVisible(true);
            ((NavigationView) findViewById(R.id.navigation)).getMenu().findItem(R.id.action_sign_out).setVisible(false);
        }
    }

    //Thep - 2016/02/24
    private void showDialogLiftLobby(final BeaconPoint beaconPoint, boolean isChange) {
        if (beaconPoint.mCarparkId != mParkingSession.getCarParkCheckIn()) {
            //Khac carpark ko hien lift lobby -> ko thong nhat du lieu share, save,..
            return;
        }
        if (alertDialogDetect != null && alertDialogDetect.isShowing()) {
            return;
        }

//        if (getMapView().getVisibility() != View.VISIBLE) {
//            return;
//        }
        //        update 2016/09/27 - checkin voi nhieu lift lobby theo kieu thay the
//        String liftLobbySave = parkingSession.getString("LIFT_LOBBY_SAVE", "");
//        if (!liftLobbySave.isEmpty())
//            return;
//        if (!LocateMyLotApp.locateMyLotActivityVisible) {
//            // chuyen locateMyLotActivity sang foreground
//            Intent resumeIntent = new Intent(getApplicationContext(), LocateMyLotActivity.class);
//            resumeIntent.putExtra(Global.IS_LIFT_LOBBY_EXTRA, true);
//            resumeIntent.putExtra(Global.ID_LIFT_LOBBY_EXTRA, beaconPoint.mId);
//            resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(resumeIntent);
//        }
        lastTimeQuestion = System.currentTimeMillis();
        String dataCurrentLiftLobby = mParkingSession.getLiftLobby();
        if (dataCurrentLiftLobby.isEmpty()) {
            checkInLiftLobby(beaconPoint);
        } else if (!dataCurrentLiftLobby.equals(getDataSaveLift(beaconPoint))) {
            if (isChange)
                alertDialogDetect.dismiss();

            String title = "LocateMyLot";
            String text = "Would you like to set marker for this lift lobby?";
            showNotification(LocateMyLotActivity.this, LIFT_LOBBY_VALUE, beaconPoint.mId, title, text);


            if (!currentFloor.isEmpty()) {

                alertDialogDetect = new AlertDialog.Builder(LocateMyLotActivity.this, R.style.AppTheme_AlertDialog)
                        .setTitle(title)
                        .setMessage(text)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkInLiftLobby(beaconPoint);
                                lastTimeQuestion = -1;
                                dialog.dismiss();
                                clearNotification(LocateMyLotActivity.this);

                            }
                        })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        clearNotification(LocateMyLotActivity.this);
                                    }
                                })

                        .create();
                currentIdLiftLobby = beaconPoint.mId;
//            alertDialogDetect.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                alertDialogDetect.setCanceledOnTouchOutside(false);
                alertDialogDetect.show();
            }
        }
    }//end

    private String getDataSaveLift(BeaconPoint beaconPoint) {
        int carpark = beaconPoint.mCarparkId;
        String floor = beaconPoint.mFloor;
        String currentMapLiftLobby = carpark + "_" + floor + ".png";
        //end
        float x = beaconPoint.mX;//getMapViewUserObject().location.originalX;
        float y = beaconPoint.mY;//getMapViewUserObject().location.originalY;
        String zone = getMapViewUserObject().zone;
        String dataSave = currentMapLiftLobby + ";" + x + ";" + y + ";" + zone + ";" + floor;
        return dataSave;
    }

    //Thep update 2016/08/23
    private void showDialogGPS() {
        showCarParkNear(false, "");
        /*
        if (alertDialogGPS == null) {
            alertDialogGPS = new AlertDialog.Builder(LocateMyLotActivity.this);
            // Setting Dialog Title
            alertDialogGPS.setTitle("LocateMyLot");
            // Setting Dialog Message
            alertDialogGPS.setMessage("Enabled GPS receivers to improve position accuracy.");
            alertDialogGPS.setPositiveButton("Enabled",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            GPSHelper.turnGPSOn(LocateMyLotActivity.this);
                            showCarParkNear(true);
                            dialog.dismiss();
                        }
                    });
//
            // Setting Negative "NO" Button
            alertDialogGPS.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showCarParkNear(false);
                            dialog.dismiss();
                        }
                    });


            alertDialog = alertDialogGPS.create();
        }
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }*/
    }//end


    //Thep update 2016/08/12
    private void updateData(int carparkId) {
        parkingRatesList = CLParkingRates.getListParkingRatesByCarparkId(carparkId);
        parkingSurchargesList = CLParkingSurcharge.getListParkingSurchargeByCarparkId(carparkId);
        holidays = CLHoliday.getAllHoliday();
    }


    private Date updateTime(Date newTime, int time) {
        return Utils.addTime(newTime, Calendar.MINUTE, time);
    }

    private Date updateTime(Date newTime, String endTime) {

        String newTimeString = Utils.convertDateTimeToString(newTime);
        String[] timeArr = newTimeString.split(" ");

        String[] beginArr = timeArr[1].split(":");
        String[] endArr = endTime.split(":");
        int beginHour = Integer.valueOf(beginArr[0]);
        int endHour = Integer.valueOf(endArr[0]);

        if (beginHour > endHour) {//Qua dem-sang ngay moi
            int cmp = 24 - beginHour + endHour;
            Date tmp = Utils.addTime(newTime, Calendar.HOUR, cmp);
            String[] arrDateTime = Utils.convertDateTimeToString(tmp).split(" ");
            return Utils.convertStringToDateTime(arrDateTime[0] + " " + endTime);
        } else {
            return Utils.convertStringToDateTime(timeArr[0] + " " + endTime);
        }

    }

    private Date updateTime(Date newTime, String beginTime, String endTime) {

        String[] beginArr = beginTime.split(":");
        String[] endArr = endTime.split(":");
        int beginHour = Integer.valueOf(beginArr[0]);
        int endHour = Integer.valueOf(endArr[0]);

        if (beginHour > endHour) {//Qua dem-sang ngay moi
            int cmp = 24 - beginHour + endHour;
            Date tmp = Utils.addTime(newTime, Calendar.HOUR, cmp);
            String[] arrDateTime = Utils.convertDateTimeToString(tmp).split(" ");
            return Utils.convertStringToDateTime(arrDateTime[0] + " " + endTime);
        } else {
            String newTimeString = Utils.convertDateTimeToString(newTime);
            String[] timeArr = newTimeString.split(" ");
            return Utils.convertStringToDateTime(timeArr[0] + " " + endTime);
        }

    }

    /***
     * Thuc hien vong lap (newTime< thoi gian hien tai)
     * Lay ra parkingRate hop le voi khoang thoi gian tren(Neu roi vao ngay le hoac chu nhat thi phai lay gia cho holiday)
     * Neu =null -> Exception
     * Kiem tra gia tri truoc do (lastParkingRates = gia tri vua lay)
     * --Dung: Thi tinh gia theo subRate va tang gia tri NewTime theo subMins
     * --Sai: Thi tinh gia theo First Rate
     * ---- Kiem tra tinh gia theo zone hay theo gio
     * -----Neu theo Zone thi tang gia tri NewTime = endTime cua parkingRates +1 mins de sang khung gio moi
     * -----Nguoc lai tang newTime theo firstMins
     * ------------LUU Y TANG NEWTIME VA KIEM TRA-------
     * **Tang time kiem tra neu NewTime sau tang nam trong khung gio cua parkingRates vua nhan dc thi continude
     * **Nguoc lai thi newTime = endTime cua parkingRates +1 mins de sang khung gio moi
     * ***Luu y update + time can update day du de tang ca ngay va gio
     * ***Luu y kiem tra khung gio qua dem thi endTime<BeginTime **EndTime= ngay tiep theo
     *
     * @return
     */
    private float getSumRates() {
        float sum = 0;
        Date checkInTime = new Date(Global.entryTime);
        Date newTime = checkInTime;
        ParkingRates lastParkingRates = new ParkingRates();
        List<DetailCharge> detailCharges = new ArrayList<>();
        while (Utils.validTime(newTime)) {
            ParkingRates currentParkingRates = Utils.getParkingRates(holidays, parkingRatesList, newTime);
            if (currentParkingRates == null)
                break;
            if (lastParkingRates.equals(currentParkingRates)) {
                detailCharges.add(new DetailCharge(newTime, lastParkingRates.getSubMins(), lastParkingRates.getSubRates(), 1, convertStringTime(lastParkingRates.getBeginTime(), lastParkingRates.getEndTime())));
                sum += lastParkingRates.getSubRates();
                Date tmp = updateTime(newTime, lastParkingRates.getSubMins());
                Date lastTime = updateTime(newTime, lastParkingRates.getBeginTime(), lastParkingRates.getEndTime());
                if (tmp.compareTo(lastTime) <= 0) {
                    newTime = tmp;
                } else {
                    newTime = updateTime(lastTime, 1);
                }
            } else {
                lastParkingRates = currentParkingRates;
                sum += lastParkingRates.getFirstRates();
                Date lastTime = updateTime(newTime, lastParkingRates.getBeginTime(), lastParkingRates.getEndTime());
                if (lastParkingRates.getSubMins() != 0) {
                    detailCharges.add(new DetailCharge(newTime, lastParkingRates.getFirstMins(), lastParkingRates.getFirstRates(), 0, convertStringTime(lastParkingRates.getBeginTime(), lastParkingRates.getEndTime())));
                    Date tmp = updateTime(newTime, lastParkingRates.getFirstMins());
                    if (tmp.compareTo(lastTime) <= 0) {
                        newTime = tmp;
                    } else {
                        newTime = updateTime(lastTime, 1);
                    }
                } else {
                    detailCharges.add(new DetailCharge(newTime, lastParkingRates.getFirstMins(), lastParkingRates.getFirstRates(), 2, convertStringTime(lastParkingRates.getBeginTime(), lastParkingRates.getEndTime())));
                    newTime = updateTime(lastTime, 1);
                }
            }
        }
        currentListRates = detailCharges;
        currentSumRates = sum;
        return sum;
    }

    private String convertMonthDay(int data) {
        if (data > 9)
            return String.valueOf(data);
        else
            return "0" + data;

    }

    /***
     * Kiem tra tu luc checkIn cho den thoi diem hien tai la trong cung 1 ngay ko?
     * Neu trong cung 1 ngay: Duyet list surcharge hop le de +
     * Neu khac ngay: Thi duyet tu khoang thoi gian tu thoi diem checkIn -> 23:59:00
     * ---Lay ra list surcharge hop le
     * ---Tang time len 1 mins -> ngay moi.
     * -----Neu ngay moi nay khac ngay hien tai -> checkIn = ngay moi 00:00:00  va continude
     * -----Nguoc lai Break
     * ---Tinh surcharge ngay hien tai tu thoi diem: ngay hien tai 00:00:00 den currentTimeStamp
     * -----------LUU Y CACH CONG GIO
     * ***Luu y: + time van can xac dinh tang ca ngay lan gio
     * ***Luu y: Khoang thoi gian hop le(Utils.betweenDate)
     * *** Khoang thoi gian khac # 2 truong hop :
     * ******Thoi diem end< BeginTimeSurCharge(Bao gom ngay gio)... (begin..end)...[Valid]..
     * ******Thoi diem end> EndTimeSurCharge va begin>EndTimeSurCharge(Bao gom ngay gio)... ...[Valid]..(begin..end)
     *
     * @return
     */
    public float sumSurcharge() {
        float sum = 0;
        Date checkInTime = new Date(Global.entryTime);
        Date newTime = checkInTime;
        Date currentTime = new Date(System.currentTimeMillis());
        String startDate = Utils.convertDateTimeToString(checkInTime).split(" ")[0];
        String currentDate = Utils.convertDateTimeToString(currentTime).split(" ")[0];
        List<DetailCharge> detailCharges = new ArrayList<>();
        List<ParkingSurcharge> parkingSurcharges = surchargePerDay(holidays, parkingSurchargesList, newTime);
        if (startDate.equals(currentDate)) {
            for (ParkingSurcharge parkingSurcharge : parkingSurcharges) {
                if (Utils.betweenDate(newTime, currentTime, parkingSurcharge)) {
                    sum += parkingSurcharge.getSurcharge();
                    detailCharges.add(new DetailCharge(newTime, parkingSurcharge.getSurcharge(), convertStringTime(parkingSurcharge.getBeginTime(), parkingSurcharge.getEndTime())));
                }
            }
        } else if (currentTime.compareTo(newTime) >= 0) {

            while (true) {

                Date newEndTime = Utils.convertStringToDateTime(startDate + " 23:59:00");
                for (ParkingSurcharge parkingSurcharge : parkingSurcharges) {
                    if (Utils.betweenDate(newTime, newEndTime, parkingSurcharge)) {
                        sum += parkingSurcharge.getSurcharge();
                        detailCharges.add(new DetailCharge(newTime, parkingSurcharge.getSurcharge(), convertStringTime(parkingSurcharge.getBeginTime(), parkingSurcharge.getEndTime())));
                    }
                }


                newEndTime = updateTime(newEndTime, 1);
                startDate = Utils.convertDateTimeToString(newEndTime).split(" ")[0];
                newTime = Utils.convertStringToDateTime(startDate + " 00:00:00");

                if (startDate.equals(currentDate)) {
                    newEndTime = Utils.convertStringToDateTime(startDate + " 23:59:00");
                    parkingSurcharges = surchargePerDay(holidays, parkingSurchargesList, newEndTime);
                    for (ParkingSurcharge parkingSurcharge : parkingSurcharges) {
                        if (Utils.betweenDate(newTime, newEndTime, parkingSurcharge)) {
                            sum += parkingSurcharge.getSurcharge();
                            detailCharges.add(new DetailCharge(newTime, parkingSurcharge.getSurcharge(), convertStringTime(parkingSurcharge.getBeginTime(), parkingSurcharge.getEndTime())));
                        }
                    }
                    break;
                }
            }

        } else {
            return 0;
        }
        currentListSurCharge = detailCharges;
        currentSurcharge = sum;
        return sum;
    }

    private List<ParkingSurcharge> surchargePerDay(List<Holiday> holidays, List<ParkingSurcharge> parkingSurcharges, Date beginTime) {
        boolean isHoliday = false;
        List<ParkingSurcharge> surcharges = new ArrayList<>();
        for (ParkingSurcharge parkingSurcharge : parkingSurcharges) {
            for (Holiday holiday : holidays) {
                if (holiday.isHoliday(beginTime)) {
                    isHoliday = true;
                    break;
                }
            }
            if (!isHoliday) {
                isHoliday = (Utils.getDayOfWeek(beginTime) == Calendar.SUNDAY);
            }
            int dateType = parkingSurcharge.getDataType();
            if (dateType == 1 && isHoliday)
                continue;
            if (dateType == 2 && !isHoliday)
                continue;
            surcharges.add(parkingSurcharge);
        }
        return surcharges;
    }


    private void showCarParkNear(boolean isEnableGPS, String data) {
        Intent intent = new Intent(LocateMyLotActivity.this, DialogSelectCarPark.class);
        intent.putExtra(DialogSelectCarPark.IS_GPS_ENABLE, isEnableGPS);
        intent.putExtra(GPSHelper.CARPARK_ID_LIST_KEY, data);
        startActivityForResult(intent, RESULT_SELECT_CARPARK);
    }

    public void showDialogDetailCharge(final boolean isSelectCarpark, final int carpark, final String data) {
        if (mParkingSession.isCheckIn() || isSelectCarpark) {
            final Dialog dialog = new Dialog(LocateMyLotActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_detail_charge);
//        dialog.setTitle("Title...");

            // set the custom dialog components - text, image and button

            TextView tvCarparkName = (TextView) dialog.findViewById(R.id.tvCarparkName);
            TextView tvSumCharge = (TextView) dialog.findViewById(R.id.tvSumCharge);
            TextView tvSumSurCharge = (TextView) dialog.findViewById(R.id.tvSumSurCharge);
            TextView tvBeforeTime = (TextView) dialog.findViewById(R.id.tvBeforeTime);
            TextView tvAfterTime = (TextView) dialog.findViewById(R.id.tvAfterTime);
            TextView tvSat = (TextView) dialog.findViewById(R.id.tvSat);
            TextView tvSunPH = (TextView) dialog.findViewById(R.id.tvSunPH);
            ListView lvCharge = (ListView) dialog.findViewById(R.id.lvCharge);
            ListView lvSurCharge = (ListView) dialog.findViewById(R.id.lvSurCharge);
            Button btOk = (Button) dialog.findViewById(R.id.btOk);
            final Button btCancel = (Button) dialog.findViewById(R.id.btCancel);

            //noi dung quy dinh gia
//            int carpark = mParkingSession.getCarParkCheckIn();
            Carpark carparkObject = CLCarpark.getCarparkByCarparkId(carpark);
            tvCarparkName.setText(carparkObject.name);
            String[] details = carparkObject.ratesInfo.split("%");
            if (details.length >= 1)
                tvBeforeTime.setText(details[0]);
            if (details.length >= 2)
                tvAfterTime.setText(details[1]);
            if (details.length >= 3)
                tvSat.setText(details[2]);
            if (details.length >= 4)
                tvSunPH.setText(details[3]);

            //chi tiet gia
            tvSumCharge.setText("Charge: " + String.format("%.2f", currentSumRates));
            tvSumSurCharge.setText("Surcharge: " + String.format("%.2f", currentSurcharge));
            lvCharge.setAdapter(new DetailChargeAdapter(LocateMyLotActivity.this, currentListRates));
            lvSurCharge.setAdapter(new DetailSurchargeAdapter(LocateMyLotActivity.this, currentListSurCharge));
            // if button is clicked, close the custom dialog
            if (isSelectCarpark) {
                btOk.setText("START");
                btCancel.setVisibility(View.VISIBLE);
            } else {
                btOk.setText("OK");
                btCancel.setVisibility(View.GONE);
            }

            btOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSelectCarpark) {
                        boolean isCheckIn = mParkingSession.isCheckIn();
                        if (!isCheckIn) {
                            dismissMyDialog();
                            if (carpark >= 0) {
                                getCarparkName().setText(CLCarpark.getCarparkNameByCarparkId(carpark));
                                currentCapark = carpark;
                                checkInAction(false);
                                llCarParkType.setVisibility(View.GONE);
                                funcTime.modifyState(false);
                                dialog.dismiss();
                            }
                            showDialogWarning();
                        } else {
                            //Neu da checkin confirm lai
                            showDialogCheckOut("");
                        }
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent resultIntent = new Intent(LocateMyLotActivity.this, DialogSelectCarPark.class);
//                    resultIntent.putExtra(GPSHelper.CARPARK_ID_LIST_KEY, data);
//                    startActivity(resultIntent);
                    dialog.dismiss();
                }
            });


            dialog.show();
        }
    }

    private String convertStringTime(String beginTime, String endTime) {
        return beginTime.substring(0, 5) + "-" + endTime.substring(0, 5);
    }

    /***
     * Thiet lap text details hien thi theo chuan chung
     *
     * @param carPark
     * @param floor
     */
    private void setTextDetails(int carPark, String floor) {
        if (carPark != DEFAULT_CARPARK_NULL && floor != null && !floor.isEmpty()) {
            getCarparkName().setText(CLCarpark.getCarparkNameByCarparkId(carPark));
            getCarparkFloor().setText("Floor " + floor);

        } else {
            if (!mParkingSession.isCheckIn()) {
                getCarparkName().setText(getString(R.string.no_carpark));
            } else {
                String carparkName = CLCarpark.getCarparkNameByCarparkId(mParkingSession.getCarParkCheckIn());
                getCarparkName().setText(carparkName);
            }
            getCarparkFloor().setText("");
        }
        setTextCar(carPark);
        setTextLiftLobby();
    }

    private void setTextCar(int carPark) {
//        int checkInCarPark = parkingSession.getInt("CHECKIN_CARPARK", DEFAULT_CARPARK_NULL);
        boolean isCheckCar = mParkingSession.isCarCheckLocation();
        if (isCheckCar) {
            String floor = mParkingSession.getFloor();
            String zone = mParkingSession.getZone();
            if (zone != null && !zone.isEmpty()) {
                zone = " Zone " + zone;
            } else {
                zone = "";
            }
            getCarZone().setVisibility(View.VISIBLE);
            getCarZone().setText("You are parked at Floor " + floor + zone);
        } else {
            getCarZone().setText("");
            getCarZone().setVisibility(View.GONE);
        }

    }

    private void setTextLiftLobby() {
        String liftLobbySave = mParkingSession.getLiftLobby();//parkingSession.getString("LIFT_LOBBY_SAVE", "");
        String[] dataSave = liftLobbySave.split(";");//currentMapLiftLobby + ";" + x + ";" + y + ";" + zone + ";" + floor;
        if (!liftLobbySave.isEmpty()) {
            getLiftZone().setVisibility(View.VISIBLE);
            String zone = dataSave[3];
            if (zone.isEmpty() || zone.equals("null"))
                zone = dataSave[4];
            getLiftZone().setText("You have exited at Lift Lobby " + zone);
        } else {
            getLiftZone().setText("");
            getLiftZone().setVisibility(View.GONE);
        }
    }

    private void setTextLiftLobby(String zone) {
        if (zone != null && !zone.isEmpty()) {
            getLiftZone().setVisibility(View.VISIBLE);
            getLiftZone().setText("You have exited at Lift Lobby  " + zone);
        } else {
            getLiftZone().setText("");
            getLiftZone().setVisibility(View.GONE);
        }
    }

    public static final String DATA_EXTRA_NOTIFICATION_KEY = "DATA_EXTRA_NOTIFICATION";
    public static final String DATA_EXTRA_CARPARK_KEY = "DATA_EXTRA_CARPARK";
    public static final int PENDING_INTENT_ID = 20;
    public static final int NOTIFICATION_ID = 20;
    public static final int WELLCOME_VALUE = 1;
    public static final int LIFT_LOBBY_VALUE = 2;
    public static final int EXIT_CAR_VALUE = 3;
    public static final int CHECKOUT_VALUE = 4;

    /***
     * @param context
     * @param data
     * @param carpark Voi liftloby la beacon id
     * @param title
     * @param text
     */
    private void showNotification(Context context, int data, int carpark, String title, String text) {
//        if (!screenIsLocked())
//            return;
        if (LocateMyLotApp.locateMyLotActivityVisible) {
            return;
        }
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);


        Intent resultIntent = new Intent(context, LocateMyLotActivity.class);
        resultIntent.putExtra(DATA_EXTRA_NOTIFICATION_KEY, data);
        resultIntent.putExtra(DATA_EXTRA_CARPARK_KEY, carpark);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID,
                resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
        //turn on screen
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
        wl.acquire(10000);
//            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
//            wl_cpu.acquire(10000);

    }

    private void clearNotification(Context context) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception ex) {

        }
    }

    private boolean screenIsLocked() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;
        boolean isScreenLock;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //it is locked
            isScreenLock = true;
        } else {
            //it is not locked
            isScreenLock = false;
        }
        if (isScreenOn && !isScreenLock) {
            return false;
        } else {
            return true;
        }
    }

    private void verifyPermissions(Activity activity) {
        // Here, thisActivity is the current activity
        int permissionCamera = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            dispatchCaptureIntent();
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
                        dispatchCaptureIntent();
                        return;
                    }
                }
                Toast.makeText(LocateMyLotActivity.this, "Can't open camera!", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }


    private void hiddenSleepLayout() {
        rlSlideHelp.setVisibility(View.INVISIBLE);
    }


    private void hiddenMap() {
        rlMap.setVisibility(View.INVISIBLE);
    }

    private void showSleepLayoutAndHiddenMap() {
        hiddenMap();
        if (rlSlideHelp.getVisibility() != View.VISIBLE) {
            currentPosition = 0;
            rlSlideHelp.setVisibility(View.VISIBLE);
        }
        vpPaperSleep.setCurrentItem(currentPosition);
    }

    class UpdateLotOfCarPark extends AsyncTask<Void,Void,String>{
        @Override
        protected void onPreExecute() {
            if(carParkAndLots.size()<=0){
                carParkAndLots = CLCarpark.getAllEntries();
            }
            isUpdateLotRunning=true;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/cms/get_lots_avai.php";
            return Utils.getResponseFromUrl(link);
        }

        @Override
        protected void onPostExecute(String s) {
            isUpdateLotRunning=false;
            if(carParkAndLots.size()>0&&s!=null&&!s.isEmpty()){
                List<Carpark> tmp = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    if(jsonArray!=null){
                        int leng = jsonArray.length();
                        for(int i=0;i<leng;i++){
                            try {
                                JSONArray array = new JSONArray(jsonArray.getString(i));
                                if (array.length() > 0) {
                                    int id = array.getInt(0);
                                    int lot = array.getInt(1);
                                    for(Carpark carpark:carParkAndLots){
                                        if(carpark.id==id){
                                            carpark.lot =lot;
                                        }
                                        tmp.add(carpark);
                                    }
                                }
                            }catch (Exception e){

                            }
                        }
                        carParkAndLots=tmp;
                        Intent intent = new Intent(BROADCAST_UPDATE_LOT);
                        sendBroadcast(intent);
                    }
                } catch (JSONException e) {

                }
            }
            super.onPostExecute(s);
        }
    }
}