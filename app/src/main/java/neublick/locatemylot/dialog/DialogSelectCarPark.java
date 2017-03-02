package neublick.locatemylot.dialog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.CarparkAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.model.Carpark;
import neublick.locatemylot.service.BackgroundService;
import neublick.locatemylot.util.GPSHelper;

public class DialogSelectCarPark extends Activity {

    public static final String IS_GPS_ENABLE = "IS_GPS_ENABLE";
    public static final String CARPARK_ID = "CARPARK_ID";
    //Thep update 2016/08/23 - Tao bo lang nghe GPS
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private final double DEFAULT_VALUE = -100;
    private double currentLat = DEFAULT_VALUE;
    private double currentLon = DEFAULT_VALUE;
    //end
    private ListView lvCarpark;
    private ProgressBar pbLoading;
    private String data;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.e("RESPONSE_SERVER", BackgroundService.currentLat + "-" + BackgroundService.currentLon);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        int screenHeight = (int) (metrics.heightPixels * 0.60);

        setContentView(R.layout.dialog_select_car_park);

        // set below the setContentView()
        getWindow().setLayout(screenWidth, screenHeight);
        lvCarpark = (ListView) findViewById(R.id.lvCarpark);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        Button btCancel = (Button) findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent returnIntent = new Intent();
//                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        //thep update 2016/08/23 - dang ky lang nghe GPS
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Intent intent = getIntent();
        data = intent.getStringExtra(GPSHelper.CARPARK_ID_LIST_KEY);
        new ShowCarParkNear(intent.getBooleanExtra(IS_GPS_ENABLE, false), data).execute();
        //end

    }

    private void startListenerLocation() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(DialogSelectCarPark.this, "ENABLE", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(DialogSelectCarPark.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
            return;
        }
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    finish();
                    return;
                }
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);

            }

        }
    }

    class ShowCarParkNear extends AsyncTask<Void, Void, List<Carpark>> {
        private boolean mIsEnableGPS;
        private String mData;

        public ShowCarParkNear(boolean isEnableGPS, String data) {
            mIsEnableGPS = isEnableGPS;
            mData = data;
//            if (isEnableGPS) {
//                startListenerLocation();
//            }
            currentLat = BackgroundService.currentLat;
            currentLon = BackgroundService.currentLon;
        }

        @Override
        protected List<Carpark> doInBackground(Void... params) {
            //Neu enableGPS thi check
            List<Carpark> carparks = new ArrayList<>();
            if (mData != null && !mData.isEmpty()) {
                carparks = CLCarpark.getListCarParksWithListID(mData);

            } else {
                carparks = CLCarpark.getAllEntries();
            }
//            if (mIsEnableGPS && currentLat == DEFAULT_VALUE) {
//                for (int i = 0; i < 5; i++) {
//                    if (currentLat != DEFAULT_VALUE)
//                        break;
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//
//                    }
//                }
//                if(currentLat==DEFAULT_VALUE){
//                    try {
//                        Location location = mLocationManager
//                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                        currentLat = location.getLatitude();
//                        currentLon = location.getLongitude();
//                    }catch (Exception e){
//
//                    }
//                }
//            }

            if (currentLat == DEFAULT_VALUE)
                return carparks;
            else {
//                Collections.sort(carparks, new Comparator<Carpark>() {
//                    @Override
//                    public int compare(Carpark carpark1, Carpark carpark2) {
//                        double range1 = carpark1.getRange(currentLat, currentLon);
//                        double range2 = carpark2.getRange(currentLat, currentLon);
//
//                        if (range1 > range2) {
//                            return 1;
//                        } else {
//                            if (range1 == range2) {
//                                return 0;
//                            } else {
//                                return -1;
//                            }
//                        }
//                    }
//                });

                List<String> s = new ArrayList<>();
                Collections.sort(carparks, new Comparator<Carpark>() {
                    @Override
                    public int compare(Carpark carpark1, Carpark carpark2) {
                        return carpark1.name.compareToIgnoreCase(carpark2.name);
                    }
                });
                return carparks;
            }

        }


        @Override
        protected void onPostExecute(List<Carpark> carparks) {
            pbLoading.setVisibility(View.GONE);
            lvCarpark.setAdapter(new CarparkAdapter(DialogSelectCarPark.this, carparks));
            lvCarpark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Carpark carpark = (Carpark) parent.getAdapter().getItem(position);
//                    Intent returnIntent = new Intent();
//                    returnIntent.putExtra(CARPARK_ID, carpark.id);
//                    setResult(Activity.RESULT_OK, returnIntent);
                    Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
                    intent.putExtra(Global.SELECT_BEACON_ID_KEY, carpark.id);
                    intent.putExtra(GPSHelper.CARPARK_ID_LIST_KEY, data);

                    sendBroadcast(intent);
                    finish();
                }
            });
            super.onPostExecute(carparks);
        }
    }
}