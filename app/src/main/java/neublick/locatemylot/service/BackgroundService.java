package neublick.locatemylot.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.LruCache;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.app.LocateMyLotApp;
import neublick.locatemylot.database.CLADV;
import neublick.locatemylot.database.CLBeacon;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.database.CLParkingHistory;
import neublick.locatemylot.dialog.DialogSelectCarPark;
import neublick.locatemylot.model.ADVObject;
import neublick.locatemylot.model.BeaconPoint;
import neublick.locatemylot.model.Carpark;
import neublick.locatemylot.model.ParkingHistory;
import neublick.locatemylot.util.BitmapUtil;
import neublick.locatemylot.util.GPSHelper;
import neublick.locatemylot.util.ParkingSession;

import static neublick.locatemylot.activity.LocateMyLotActivity.ADV_VALUE;
import static neublick.locatemylot.activity.LocateMyLotActivity.DATA_EXTRA_KEY;
import static neublick.locatemylot.activity.LocateMyLotActivity.DATA_EXTRA_NOTIFICATION_KEY;
import static neublick.locatemylot.activity.LocateMyLotActivity.NOTIFICATION_ADV_ID;
import static neublick.locatemylot.activity.LocateMyLotActivity.NOTIFICATION_ID;
import static neublick.locatemylot.activity.LocateMyLotActivity.PENDING_INTENT_ID;

public class BackgroundService extends AbstractService {

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static long lastTimeDetectMyBeacon = -1;
    // entry point interacting with EstimoteSDK
    BeaconManager beaconManager;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    public static double currentLat = 0;
    public static double currentLon = 0;
    private Handler handler;
    private ParkingSession mParkingSession;

    private static String lastCarParkListGPS = "";
    private static long lastTimeDetectGPS = 0;
    private static int lastCountDetectGPS = 0;
    private boolean isGetAdv=false;
    private Context mContext;
    private static long lastTimeAdvShow = 0;

    // to cache Beacons data
    private LruCache<String, BeaconPoint> beaconCache = new LruCache<>(4 * 1024 * 1024);


    // luu tru toan bo welcome beacons co trong CSDL
    List<BeaconPoint> allWelcomeBeacons;
    private BroadcastReceiver broadCastSuccessSignIn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(Global.BLUETOOTH_STATE_EXTRA, false)) {
                startBeaconRanging();
            } else {
                stopBeaconRanging();
            }
        }
    };//end

    @Override
    public void onStartService() {
        /*
        if (neublick.locatemylot.util.Utils.isInternetConnected(this)) {
			try {
				new AppUpdateTask().execute(Config.CMS_URL + "/getsynchdata.php").get();
			} catch(Exception e) {
				le("Exception: " + e.getMessage());
			}
		}
		*/
        mContext =getApplicationContext();
        mParkingSession = ParkingSession.getInstanceSharedPreferences(mContext);
        handler = new Handler();
        registerReceiver(broadCastSuccessSignIn, new IntentFilter(Global.CHANGE_STATE_BLUETOOTH));

        addListenerLocation();
        if (beaconManager == null) {
            beaconManager = new BeaconManager(mContext);
            beaconManager.setBackgroundScanPeriod(300, 0);
            beaconManager.setForegroundScanPeriod(300, 0);
        }


        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> found) {
                lw("SIZE= " + found.size());

                // neu khong thay beacon nao thi gui message bao la BEACON_NOT_FOUND


                // luu lai thoi gian phat hien ra beacon co luc dung den
                boolean isDetectMyBeacon = false;
                ArrayList<BeaconPoint> beaconItems = new ArrayList<BeaconPoint>();
                String beaconIdList = "";
                for (int i = 0; i < found.size(); ++i) {
//                     Log.e("MAJOR_DETECT", found.get(i).getMajor()+"_"+found.get(i).getMinor());

                    // lay ve beacon theo beacon_id cua no
                    BeaconPoint beaconItem = getBeaconById(found.get(i).getMajor(), found.get(i).getMinor());
//                    BeaconPoint beaconItem = getBeaconById(4010, 3);
//                    BeaconPoint beaconItem = getBeaconById(5004, 3);

//                    BeaconPoint beaconItem = getBeaconById(1012, 1);
//                    BeaconPoint beaconItem = null;
//                    if(found.get(i).getMajor()==3001) {
//                         beaconItem = getBeaconById(102);
//                    }
// else{

//                    }

                    if (beaconItem != null) {
//                        Log.e("MAJOR_DETECT", beaconItem.mMajor + "-" + beaconItem.mMajor+"-"+beaconItem.mZone);
                        double distance = Utils.computeAccuracy(found.get(i));
//                        if(distance>=0) {//<0 la exception
                            beaconItem.mRSSI = found.get(i).getRssi();
                            beaconItem.mDistance = distance;
                            beaconItems.add(beaconItem);
                            beaconIdList+=beaconItem.mId+",";
//                        if(beaconItem.mMajor==3001)
//                            beaconItem.mBeaconType=4;
                            if (!isDetectMyBeacon && beaconItem.mBeaconType == 3) {
                                lastTimeDetectMyBeacon = System.currentTimeMillis();
                                isDetectMyBeacon = true;
                            }
//                        }
                    }
                }
                //Thep update 2016/08/06
                if (beaconItems.size() <= 0) {
//                    if (timeBeaconFound > 0) {
//                        if (Calendar.getInstance().getTimeInMillis() - timeBeaconFound > 10000) {
//                            Message msg = new Message();
//                            msg.what = BEACON_NOT_FOUND;
//                            send(msg);
//                            //Thep update demo 2016/08/26
//                            lostMyBeacon();
//                            //end
//                        }
//                    } else {
//                        timeBeaconFound = Calendar.getInstance().getTimeInMillis();
//                    }
//                    Log.e("MAJOR_DETECT","NOT FOUND");

                    return;
                } else {
//                    String s="";
//                    List<BeaconPoint> points=beaconItems;
//                    Collections.sort(points, new Comparator<BeaconPoint>() {
//                        @Override
//                        public int compare(BeaconPoint lhs, BeaconPoint rhs) {
//                            if(lhs.mDistance<rhs.mDistance)
//                            return 1;
//                            else if(lhs.mDistance>rhs.mDistance)
//                                return -1;
//                            else
//                                return 0;
//                        }
//                    });
//                    for(BeaconPoint beaconPoint:points){
//                        s+=beaconPoint.mMajor+"*"+beaconPoint.mMinor+":"+beaconPoint.mDistance+"\n";
//                    }
//                    s+="------------------------------------";
//                    Log.e("MAJOR_DETECT",s);
                    Global.timeBeaconFound = Calendar.getInstance().getTimeInMillis();
                }
                //Thep update demo 2016/08/26

                // tim xem co gap welcomeBeacon nao ko?
                // allWelcomeBeacons() tra ve toan bo welcome Beacon co trong CSDL

                BeaconPoint welcomeBeacon = null;//   BeaconPoint welcomeBeacon = BeaconUtil.intersect(allWelcomeBeacons(), beaconItems, 5);

                for (BeaconPoint beaconPoint : beaconItems) {
                    if (beaconPoint.mBeaconType == 1) {
                        welcomeBeacon = beaconPoint;
                        break;
                    }
                }

                BeaconPoint beaconCheckOut = null;//   BeaconPoint welcomeBeacon = BeaconUtil.intersect(allWelcomeBeacons(), beaconItems, 5);

                for (BeaconPoint beaconPoint : beaconItems) {
                    if (beaconPoint.mBeaconType == 4) {
                        beaconCheckOut = beaconPoint;
                        break;
                    }
                }
                //welcomeBeacon=beaconItems.get(0);
                if (welcomeBeacon != null) {
                    // ta bat gap welcome beacon
                    boolean isCheckIn = mParkingSession.isCheckIn();
                    // neu chua checkIn thi ta gap new Carpark
                    if (!isCheckIn && isTimeCheckOutValid()) {
                        resumeWelcomeActivity(welcomeBeacon);
                    }
                }

                if (!isDetectMyBeacon) {
                    lostMyBeacon();
                }
                /***
                 * Neu da check in - loai thuong - va chua notification hoi check out
                 * Va thoi gian check in gan nhat >5p thi show checkout
                 */
                if(beaconCheckOut!=null&&mParkingSession.isCheckIn()&&mParkingSession.isNormalCheckIn()&&beaconCheckOut.mCarparkId!=mParkingSession.getLastCarparkCheckOutConfirm()&&(System.currentTimeMillis()-mParkingSession.getTimeCheckIn())>300000){
                    mParkingSession.setLastCarparkCheckOutConfirm(beaconCheckOut.mCarparkId);
                    if (LocateMyLotApp.locateMyLotActivityVisible) {
                        Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
                        intent.putExtra(Global.IS_CHECKOUT_BROADCAST_KEY, true);
                        intent.putExtra(Global.LIFT_LOBBY_BEACON_ID_KEY, beaconItems.get(0).mCarparkId);
                        sendBroadcast(intent);
                    }else{
                        String title = "Locate My Lot";
                        String text = "Would you like want to check out??";
                        showNotification(mContext, LocateMyLotActivity.CHECKOUT_VALUE, beaconCheckOut.mCarparkId, title, text);
                    }
                }
                //end


                // sap xep tang dan theo khoang cach 161118
                Collections.sort(beaconItems, new Comparator<BeaconPoint>() {
                    @Override
                    public int compare(BeaconPoint lhs, BeaconPoint rhs) {
                        return (int) (lhs.mDistance - rhs.mDistance);
                    }
                });

//                logBeaconsFound(beaconItems);



/*
                if (beaconItems.size() < 2) {
					Message msg = new Message();
					msg.what = BEACON_FOUND_NOT_ENOUGH;
					send(msg);
					return;
				}
*/
                // So luong beacon >= 2, tinh toan newX, newY
                int size = Math.min(beaconItems.size(), Config.BEACON_FOUND_TRIM_FOR_CALCULATING);
                double totalD = 0;
                double tx = 0, ty = 0;
                for (int i = 0; i < size; ++i) {
                    BeaconPoint beaconPoint = beaconItems.get(i);
                    totalD += beaconPoint.mDistance;
                    tx += beaconPoint.mX * beaconPoint.mDistance;
                    ty += beaconPoint.mY * beaconPoint.mDistance;
                }

                Bundle bundle = new Bundle();

                // retrieve the location base the map on server
                double serverX = tx / totalD;
                double serverY = ty / totalD;

                //Thep - 2016/11/11 - Sau khi sort khoang cach tien hanh kiem tra
                boolean isCheckIn = mParkingSession.isCheckIn();//lay sau khi qua welcome
                if (beaconItems.get(0).mBeaconType == 2) {
                    int carparkId = beaconItems.get(0).mCarparkId;
                    if (isCheckIn) {
                        //currentMapLiftLobby+";"+x+";"+y+";"+zone+";"+floor;
                        String currentLift = mParkingSession.getLiftLobby();
                        BeaconPoint beaconPoint = beaconItems.get(0);
                        String cmp = beaconPoint.mX + ";" + beaconPoint.mY;
                        if (beaconPoint.mCarparkId== mParkingSession.getCarParkCheckIn()&&!currentLift.contains(cmp) || !currentLift.endsWith(";" + beaconPoint.mFloor)) {
                           long currentTime= System.currentTimeMillis();
                            if((beaconPoint.mId==mParkingSession.getLastBeaconQuestionLift())&&currentTime-mParkingSession.getLastTimeQuestionLift()<300000){
                                //Neu cung  beacon id ma time question <3000000(5p)
                            }else {
                                mParkingSession.setLastBeaconQuestionLift(beaconPoint.mId);
                                mParkingSession.setLastTimeQuestionLift(currentTime);
                                if (LocateMyLotApp.locateMyLotActivityVisible) {
                                    Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
                                    intent.putExtra(Global.LIFT_LOBBY_BEACON_ID_KEY, beaconItems.get(0).mId);
                                    sendBroadcast(intent);
                                } else {
                                    // chuyen locateMyLotActivity sang foreground
                                    Intent resumeIntent = new Intent(mContext, LocateMyLotActivity.class);
                                    resumeIntent.putExtra(Global.IS_LIFT_LOBBY_EXTRA, true);
                                    resumeIntent.putExtra(Global.ID_LIFT_LOBBY_EXTRA, beaconItems.get(0).mId);
                                    resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(resumeIntent);
                                    String title = "Locate My Lot";
                                    String text = "Would you like to set marker for this lift lobby?";
                                    if (currentLift.isEmpty()) {
                                        String zone = beaconItems.get(0).mZone;
                                        if (zone.isEmpty())
                                            zone = beaconItems.get(0).mFloor;
                                        text = "You have exited at Lift Lobby " + zone;
                                        String currentMapLiftLobby = beaconPoint.mCarparkId + "_" + beaconPoint.mFloor + ".png";
                                        String dataSave = currentMapLiftLobby + ";" + beaconPoint.mX + ";" + beaconPoint.mY + ";" + zone + ";" + beaconPoint.mFloor ;
                                        mParkingSession.setLiftLobby(dataSave);
                                    }
                                    showNotification(mContext, LocateMyLotActivity.LIFT_LOBBY_VALUE, beaconItems.get(0).mId, title, text);

                                }
                            }
                        }
                    } else {
                        autoCheckIn(welcomeBeacon, isCheckIn, beaconItems);

                    }
                } else if (beaconItems.get(0).mBeaconType == 4) {

                } else {//loai thuong
                    autoCheckIn(welcomeBeacon, isCheckIn, beaconItems);
                }
                //end


                String fmt = String.format(
                        "Floor: %s, Zone: %s",
                        beaconItems.get(0).mFloor,
                        beaconItems.get(0).mZone
                );

                String fmtFloor = String.format("Floor %s", beaconItems.get(0).mFloor);

                // (localX, localY) is the location based on local bitmap
                if(!beaconIdList.isEmpty()) {
                    beaconIdList = beaconIdList.substring(0,beaconIdList.length()-1);
                }
                //Hien promotion 21/07/2017
                long currentTime = System.currentTimeMillis();

                if(neublick.locatemylot.util.Utils.isInternetConnected(mContext) && !isGetAdv && currentTime-lastTimeAdvShow>300000 ){
                    if (!beaconIdList.isEmpty()) {
                        List<BeaconPoint> beaconPoints = CLBeacon.getBeaconsById(beaconIdList);
                        beaconIdList = "";
                        for (int i = 0; i < beaconPoints.size(); i++) {
                            BeaconPoint beaconPoint = beaconPoints.get(i);
                            if (beaconPoint.isPromotion) {
                                beaconIdList += beaconPoint.mId + ",";
                            }
                        }
//                        beaconIdList = "1234";
                        if (!beaconIdList.isEmpty()) {
                            beaconIdList = beaconIdList.substring(0, beaconIdList.length() - 1);
                            String beaconID =beaconIdList.split(",")[0];
                            new GetADV(beaconID).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                    }
                }
                //ENd


                bundle.putDouble("X", serverX);
                bundle.putDouble("Y", serverY);
                bundle.putString("ZONE", beaconItems.get(0).mZone);
                bundle.putString("FLOOR", beaconItems.get(0).mFloor);
                bundle.putString("AREA_LOCATION", fmt);
                bundle.putString("AREA_LOCATION_FLOOR", fmtFloor);
                bundle.putInt(DialogSelectCarPark.CARPARK_ID, beaconItems.get(0).mCarparkId);
                Message msg = new Message();
//                Log.e("MAJOR_DETECT***",beaconItems.get(0).mMajor+"-"+beaconItems.get(0).mMinor+":"+beaconItems.get(0).mDistance);
                msg.what = MOBILE_LOCATION_RETRIEVAL;
                msg.setData(bundle);
                send(msg);
            }
        });

        startBeaconRanging();
        startTimer();
    }

    private void autoCheckIn(BeaconPoint welcomeBeacon, boolean isCheckIn, List<BeaconPoint> beaconItems) {
//        if(welcomeBeacon==null&&!isCheckIn) {
        if (!mParkingSession.isCheckIn() && welcomeBeacon == null && isTimeCheckOutValid() && (!mParkingSession.isShowCheckInConfirm() || mParkingSession.getLastCarparkShowConfirm() != beaconItems.get(0).mCarparkId)) {
            if (beaconItems.get(0).mCarparkId != mParkingSession.getLastCarparkShowConfirm()) {
                updateStateCheckIn(beaconItems.get(0));//cap nhat trang thai checkin
                if (LocateMyLotApp.locateMyLotActivityVisible) {//showDialog confirm
                    Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
                    intent.putExtra(Global.WELCOME_CARPARK_ID_KEY, beaconItems.get(0).mCarparkId);
                    intent.putExtra(Global.IS_WELCOME_BROADCAST_KEY, true);
                    intent.putExtra(Global.CONFIRM_WELCOME, true);
                    sendBroadcast(intent);
                } else {//hoac mo lai app va showDialog confirm (1 lan duy nhat)
                    turnOnScreen(mContext);
                    mParkingSession.setShowCheckInConfirm(true);
                    mParkingSession.setLastCarparkShowConfirm(beaconItems.get(0).mCarparkId);
                    Intent resumeIntent = new Intent(mContext, LocateMyLotActivity.class);
                    resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    resumeIntent.putExtra(Global.IS_WELCOME_EXTRA, true);
                    //Thep update 2016/08/16 - truyen Carpark_id check in
                    resumeIntent.putExtra(Global.CAR_PARK_ID_WELCOME_EXTRA, beaconItems.get(0).mCarparkId);
                    resumeIntent.putExtra(Global.CONFIRM_WELCOME, true);
                    startActivity(resumeIntent);

                }
            }
        }
    }

    private boolean isTimeCheckOutValid() {
        return (System.currentTimeMillis() - mParkingSession.getTimeCheckOut()) > 300000;
    }

    Runnable runnableTick;

    void startTimer() {
        if (runnableTick != null) {
            handler.removeCallbacks(runnableTick);
            runnableTick = null;
        }
        runnableTick = new Runnable() {
            public void run() {

                try {

//                    Log.e("TIME_AJS",""+(System.currentTimeMillis()-Global.entryTime));
                    if (mParkingSession.isCheckIn()) {
                        if (Global.entryTime <= 0) {
                            Global.entryTime = mParkingSession.getTimeCheckIn();
                        }
                        if ((System.currentTimeMillis() - Global.entryTime) > 86400000) {
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
                            newEntry.photoName = mParkingSession.getPhotoName();
                            long timeCheckOut = Calendar.getInstance().getTimeInMillis();
                            newEntry.timeCheckIn = mParkingSession.getTimeCheckIn();//parkingSession.getLong("TIME_CHECKIN",-1);
                            newEntry.timeCheckOut = timeCheckOut;
                            if (mParkingSession.isNormalCheckIn()) {
                                newEntry.liftData = mParkingSession.getLiftLobby();
                                newEntry.isNormal = 1;
                            } else {
                                newEntry.liftData = "";
                                newEntry.isNormal = 0;
                            }
                            Global.entryTime = -1;
                            mParkingSession.setTimeCheckIn(-1);
                            CLParkingHistory.addEntry(newEntry);
                            mParkingSession.setCheckOut(timeCheckOut);
                            Message msg = new Message();
                            msg.what = MAP_RELOAD_AFTER_CHECKOUT;
                            send(msg);
                        }

                    }
                } catch (Exception e) {
                }
                handler.postDelayed(this, 1000);
            }
        };
        runnableTick.run();
    }

    private void addListenerLocation() {
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();

//                Log.e("RESPONSE_LOCATION",currentLat+"-"+currentLon);
//                Toast.makeText(BackgroundService.this, currentLat+"-"+currentLon, Toast.LENGTH_SHORT).show();
                boolean isCheckIn = parkingSession.getBoolean("CHECK_IN", false);
                boolean isNormalCheckIn = parkingSession.getBoolean("IS_NORMAL_CHECK_IN", true);

                if (!isCheckIn || !isNormalCheckIn) {
                    List<Carpark> carparks = CLCarpark.getAllEntries();
                    List<Carpark> carparkValidList = new ArrayList<>();
                    for (Carpark carpark : carparks) {
//                        Log.e("RESPONSE_SERVER_R", carpark.getRange(currentLat, currentLon) + "");
                        if (carpark.getRange(currentLat, currentLon) <= GPSHelper.getIntCarparkRange(mContext)) {
                            carparkValidList.add(carpark);
                        }
                    }
                    //Sap xep carpark valid
                    Collections.sort(carparkValidList, new Comparator<Carpark>() {
                        @Override
                        public int compare(Carpark lhs, Carpark rhs) {
                            if (lhs.id > rhs.id)
                                return 1;
                            else if (lhs.id < rhs.id)
                                return -1;
                            else
                                return 0;
                        }
                    });
                    String currentCarParkListGPS = "";
                    for (Carpark carpark : carparkValidList) {
                        currentCarParkListGPS += carpark.id + "-";
                    }
                    Boolean aBoolean = false;
                    //Neu khac carpark thi thong bao
                    //Nguoc lai chi toi da 2 lan cho cung 1 vi tri gian cach la 20p/lan
                    if (!lastCarParkListGPS.contains(currentCarParkListGPS)&&!lastCarParkListGPS.equals(currentCarParkListGPS)) {
                        aBoolean = true;
                        lastCountDetectGPS = 0;
                    } else if ((lastTimeDetectGPS > 0 && System.currentTimeMillis() - lastTimeDetectGPS > 1200000 && lastCountDetectGPS < 2)) {
                        aBoolean = true;
                        lastCountDetectGPS++;
                    }

                    if (aBoolean) {
                        lastTimeDetectGPS = System.currentTimeMillis();
                        lastCarParkListGPS = currentCarParkListGPS;
                        if (carparkValidList.size() > 0) {
                            String data = "";
                            boolean isMapBluetooth = false;
                            String foundName = "";
                            for (Carpark carpark : carparkValidList) {
                                data += carpark.id + ",";
                                if (carpark.cpType == 1) {
                                    isMapBluetooth = true;
                                    if (foundName.isEmpty()) {
                                        foundName = carpark.name;
                                    } else {
                                        foundName += ", " + carpark.name;
                                    }
                                }
                            }
                            data = data.substring(0, data.length() - 1);
                            String title = "";
                            title = "Locate My Lot";
//                            if (carparkValidList.size() > 1) {
//                                title = "Found " + carparkValidList.size() + " car parks near you";
//                            } else {
//                                title = carparkValidList.get(0).name + " car park near you";
//                            }

                            GPSHelper.showNotificationDetectCarpark(mContext, data, title, carparkValidList.size());
//                            if (!LocateMyLotApp.locateMyLotActivityVisible) {
                                if (isMapBluetooth) {
                                    title = "Turn on Bluetooth";
                                    String text = "Map of carpark";
                                    if (foundName.contains(",")) {
                                        text += "s:";
                                    }
                                    text += " " + foundName.toUpperCase() + " work with Bluetooth";

                                    GPSHelper.showNotificationTurnBluetooth(mContext, data, title, text);
                                }
//                            }
                        }
                    }
                } else {
                    GPSHelper.clearNotification(mContext);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    currentLat = lastKnownLocation.getLatitude();
                    currentLon = lastKnownLocation.getLongitude();
                }
//                Log.e("RESPONSE_LOCATION", currentLat + "-" + currentLon);
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);
        /* Test GPS
        Location location = new Location("");
        location.setLatitude(1.305748);
        location.setLongitude(103.831635);
        mLocationListener.onLocationChanged(location);*/

    }


    private void lostMyBeacon() {
        if (lastTimeDetectMyBeacon > 0) {
            if (System.currentTimeMillis() - lastTimeDetectMyBeacon > 10000) {
                lastTimeDetectMyBeacon = -1;
                checkInWithMyBeacon();
            }
        }
    }

    private List<BeaconPoint> allWelcomeBeacons() {
        if (allWelcomeBeacons == null) {
            allWelcomeBeacons = CLBeacon.allWelcomeBeacons();
        }
        return allWelcomeBeacons;
    }

    BeaconPoint getBeaconById(int beaconMajor, int beaconMinor) {
        String key = beaconMajor + "_" + beaconMinor;
        BeaconPoint beaconItem = beaconCache.get(key);
        if (beaconItem == null) {
            // if the beacon item not exists on cache, get it from database and cache it
            beaconItem = CLBeacon.getBeaconByMajorAndMinor(beaconMajor, beaconMinor);
            lw("Ko co du lieu trong cache, lay tu database");
            if (beaconItem != null) {
                // cache it :-)
                beaconCache.put(key, beaconItem);
                lw("cache thanh cong");
                return beaconItem;
            }
            return null;
        } else {
            lw("Co du lieu trong cache");
            return beaconItem;
        }
    }

    void startBeaconRanging() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (Exception e) {
                }
            }
        });
    }

    void stopBeaconRanging() {
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
            lw("Now stop ranging service");
        } catch (Exception e) {
            lw("Can't stop ranging service");
        }
    }

    @Override
    public void onStopService() {
        beaconManager.disconnect();
    }


    public static int START_BEACON_RANGING = 10;
    public static int END_BEACON_RANGING = 11;
    public static int START_UPDATE = 12;

    // found beacons
    public static int MOBILE_LOCATION_RETRIEVAL = 13;

    // not found beacons
    public static int BEACON_NOT_FOUND = 14;
    public static int LOST_MY_BEACON = 144;
    public static int BEACON_FOUND_NOT_ENOUGH = 15;

    public static int START_TIMER = 16;

    public static int START_RETRIEVE_LOCATION_SHARED = 22;

    public static int SET_PARKING_LOCATION = 24;

    public static int LOCATE_MY_LOT_ACTIVITY_FOREGROUND = 25;
    //reloadMap
    public static int MAP_RELOAD_AFTER_CHECKOUT = 79;


    @Override
    public void onReceiveMessage(Message msg) {
        if (msg.what == START_BEACON_RANGING) {
            startBeaconRanging();
        } else if (msg.what == END_BEACON_RANGING) {
            stopBeaconRanging();
        } else if (msg.what == START_UPDATE) {

        } else if (msg.what == START_TIMER) {
            lw("START_TIMER");
            // retrieve the data
            Bundle data = msg.getData();

            int durationHour = data.getInt("DURATION_HOUR");
            int durationMinute = data.getInt("DURATION_MINUTE");

            Calendar time = Calendar.getInstance(); // now
            time.add(Calendar.HOUR_OF_DAY, durationHour);
            time.add(Calendar.MINUTE, durationMinute);

            String formatted = String.format("Time will fire at %d/%d/%d, %d:%d",
                    time.get(Calendar.YEAR),
                    time.get(Calendar.MONTH),
                    time.get(Calendar.DAY_OF_MONTH) + 1,
                    time.get(Calendar.HOUR_OF_DAY),
                    time.get(Calendar.MINUTE)
            );
            lw("formatted" + formatted);

            SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
            long timeInMillis = time.getTimeInMillis();
            parkingSession.edit().putLong("FUTURE_FIRED", timeInMillis).apply();

            // it will fire on next THRESHOLD_SNOOZE_TIME minutes
            neublick.locatemylot.util.Utils.schedule(mContext, timeInMillis - Config.THRESHOLD_SNOOZE_TIME, true);
        } else if (msg.what == START_RETRIEVE_LOCATION_SHARED) {

        } else if (msg.what == LOCATE_MY_LOT_ACTIVITY_FOREGROUND) {
            // neu LocateMyLotActivity dang onPaused thi ta giai phong 1 so bien de lay heap cho nhung thu khac

        }
    }

    static void lw(String s) {
//        final String TAG = BackgroundService.class.getSimpleName();
//        android.util.Log.w(TAG, s);
    }

    static void logBeaconsFound(List<BeaconPoint> beaconItems) {
//        StringBuilder sb = new StringBuilder("[");
//        for (BeaconPoint beaconItem : beaconItems) {
//            sb.append("(")
//                    .append(beaconItem.mMajor+"-"+beaconItem.mMinor)
//                    .append(", ")
//                    .append(String.format("%.0f", beaconItem.mDistance * 1000))
//                    .append("), ");
//        }
//        //le("log_beacon_found: " + sb.append("]").toString());
//        Log.e("DTE_BEACON",sb.append("]").toString());
    }


    //	public static Dialog requestCheckInDialog;
    public static boolean lostExitBeacon = false;
    public static boolean isWelcome = false;
    public static int carparkId = -100;

    // khoi phuc locateMyLotActivity ko quan tam den no da check in hay chua?
    public void resumeWelcomeActivity(final BeaconPoint welcomeBeacon) {

        if (System.currentTimeMillis() - Global.lastTimestampDismissWelcome < 30 * 1000)//end 30-> 300
            return;
        // for debug
        // xoa dong ben duoi de quay tro lai phien ban day du hon
        //return;
        Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
        intent.putExtra(Global.WELCOME_CARPARK_ID_KEY, welcomeBeacon.mCarparkId);
        intent.putExtra(Global.IS_WELCOME_BROADCAST_KEY, true);
        intent.putExtra(Global.CONFIRM_WELCOME, false);
        sendBroadcast(intent);

        le("_locateMyLotActivityVisible= " + LocateMyLotApp.locateMyLotActivityVisible);
        //Khoi dong lai
        if (!LocateMyLotApp.locateMyLotActivityVisible) {
            String title = "Locate My Lot";
            String text = "Welcome to " + CLCarpark.getCarparkNameByCarparkId(welcomeBeacon.mCarparkId) + "! Remember turn on your Bluetooth and Check-in";
            showNotification(mContext, LocateMyLotActivity.WELCOME_VALUE, welcomeBeacon.mCarparkId, title, text);
            isWelcome = true;
            // chuyen locateMyLotActivity sang foreground
            Intent resumeIntent = new Intent(mContext, LocateMyLotActivity.class);
            resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            resumeIntent.putExtra(Global.IS_WELCOME_EXTRA, true);
            //Thep update 2016/08/16 - truyen Carpark_id check in
            resumeIntent.putExtra(Global.CAR_PARK_ID_WELCOME_EXTRA, welcomeBeacon.mCarparkId);
            resumeIntent.putExtra(Global.CONFIRM_WELCOME, false);
            //end
            // khoi dong mainActivity voi dialog ask user co muon check-in kg?
//            startActivity(resumeIntent);
        }
        //setup welcome
        updateStateCheckIn(welcomeBeacon);

        //Ban broadcast thong bao
        Message msg = new Message();
        msg.what = SET_PARKING_LOCATION;
        msg.obj = Calendar.getInstance().getTimeInMillis();
        //Thep update 2016/08/16 - truyen Carpark_id check in
        //end
        send(msg);

    }

    /***
     * Cap nhat thong tin checkin
     *
     * @param welcomeBeacon
     */
    private void updateStateCheckIn(BeaconPoint welcomeBeacon) {
        carparkId = welcomeBeacon.mCarparkId;
        Global.entryCarparkId = welcomeBeacon.mCarparkId;
//						if(Global.entryTime<=0) Global.entryTime=System.currentTimeMillis();
        Global.entryTime = System.currentTimeMillis();
        mParkingSession.setNormalCheckIn(true);
        mParkingSession.setTimeCheckIn(Global.entryTime);
        mParkingSession.setCarParkCheckIn(welcomeBeacon.mCarparkId);
        mParkingSession.setCheckIn(true);
    }

    private void showNotification(Context context, int data, int carpark, String title, String text) {
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(context, LocateMyLotActivity.class);
        resultIntent.putExtra(DATA_EXTRA_NOTIFICATION_KEY, data);
        resultIntent.putExtra(LocateMyLotActivity.DATA_EXTRA_CARPARK_KEY, carpark);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID,
                resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
        //turn on screen
        turnOnScreen(context);
//            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
//            wl_cpu.acquire(10000);

    }

    private void turnOnScreen(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
        wl.acquire(10000);
    }


    public void checkInWithMyBeacon() {
        final SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
        boolean isCheckIn = parkingSession.getBoolean("CHECK_IN", false);
        // neu LocateMyLotActivity dang foreground
        if (isCheckIn)
            return;
        if (System.currentTimeMillis() - Global.lastTimestampDismissLostBeacon < 10 * 1000)//end 30-> 300
            return;

        if (!LocateMyLotApp.locateMyLotActivityVisible) {
//            Log.e("MAJOR_DETECT", "EXIT");
            lostExitBeacon = true;
            // chuyen locateMyLotActivity sang foreground
            Intent resumeIntent = new Intent(mContext, LocateMyLotActivity.class);
            resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            resumeIntent.putExtra(Global.CHECK_IN_MY_BEACON, true);
//            startActivity(resumeIntent);
        }

        Message msg = new Message();
        msg.what = LOST_MY_BEACON;
        msg.obj = Calendar.getInstance().getTimeInMillis();
        send(msg);

    }

    static void le(String s) {
//        final String TAG = BackgroundService.class.getSimpleName();
//        android.util.Log.e(TAG, s);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
    }

    class GetADV extends AsyncTask<Void,Void,String>{
        private String mBeaconId;

        public GetADV(String beaconId){
            mBeaconId = beaconId;
        }

        @Override
        protected void onPreExecute() {
            isGetAdv = true;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
//  {"status":"true","error_description":"OK","adv_info":"You have new offer! Check it out!",
// "adv_list":[{"adv_id":"1","adv_img":"adv1.jpg"},{"adv_id":"2","adv_img":"adv2.jpg"},{"adv_id":"3","adv_img":"adv3.jpg"}]}
                if(jsonObject.getBoolean("status")){
                        new DownloadImageAdv(jsonObject).execute();
                }
            }catch (JSONException e){

            }

            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            HashMap hashMap = new HashMap();
            http://neublick.com/demo/carlocation/act.php?act=changepass&e=talentcat@gmail.com&o=123456&n=1212121
            hashMap.put("act", "get_adv");
            hashMap.put("beacon_id", mBeaconId);
            return neublick.locatemylot.util.Utils.getResponseFromUrlNoEncode(link, hashMap);
        }
    }

    private void showNotificationAdv(Context context, String data, String text) {
//        if (!screenIsLocked())
//            return;
//        if (LocateMyLotApp.locateMyLotActivityVisible) {
//            return;
//        }
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("LocateMyLot")
                        .setContentText(text)
                        .setAutoCancel(true);


        Intent resultIntent = new Intent(context, LocateMyLotActivity.class);
        resultIntent.putExtra(DATA_EXTRA_NOTIFICATION_KEY, ADV_VALUE);
        resultIntent.putExtra(DATA_EXTRA_KEY, data);
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
        mNotificationManager.notify(NOTIFICATION_ADV_ID, notification);
        //turn on screen
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
        wl.acquire(10000);
        neublick.locatemylot.util.Utils.effectNotificationDetectBeacon(context);
//            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
//            wl_cpu.acquire(10000);

    }

    private void sendBroadcastShowDialogADV(String data) {
        Intent intent = new Intent(Global.SHOW_ADV_BROADCAST);
        intent.putExtra(Global.EXTRA_ADV_DATA, data);
        sendBroadcast(intent);
    }



    class DownloadImageAdv extends AsyncTask<Void, Void, Boolean> {

        private  JSONObject jsonObject;
        private  JSONArray jsonArray;
        private  List<ADVObject> advDeletes;
        public DownloadImageAdv(JSONObject jsonObject){
            advDeletes = CLADV.getAllADVDelete();
            this.jsonObject = jsonObject;
            try {
                this.jsonArray = jsonObject.getJSONArray("adv_list");
            } catch (JSONException e) {
                this.jsonArray = new JSONArray();
            }

        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                try {
                    String advData = jsonArray.toString();
                    lastTimeAdvShow = System.currentTimeMillis();
                    sendBroadcastShowDialogADV(advData);
                    showNotificationAdv(mContext, advData, jsonObject.getString("adv_info"));
                } catch (JSONException e) {
                }
            }
            isGetAdv = false;
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(jsonArray.length()>0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                     try {
                         ADVObject advObject = new ADVObject(jsonArray.getJSONObject(i));
                         boolean isExist = false;
                         for(ADVObject advDelete:advDeletes){
                             if(advDelete.getId().equals(advObject.getId())){
                                 isExist = true;
                                 break;
                             }
                         }
                         if(!isExist) {
                             if (BitmapUtil.saveImage(advObject.getImageFullLink())) {
                                 CLADV.addItem(advObject);
                             }
                         }
                    } catch (JSONException e) {
                    }
                }
                return true;
            }else{
                return false;
            }
        }
    }
}