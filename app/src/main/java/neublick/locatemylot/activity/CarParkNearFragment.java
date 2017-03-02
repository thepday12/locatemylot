package neublick.locatemylot.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.CarParkListAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.model.Carpark;
import neublick.locatemylot.util.GPSHelper;


/**
 * Created by Thep on 1/28/2016.
 */
public class CarParkNearFragment extends Fragment {

    public static final CarParkNearFragment newInstance(int position) {
        CarParkNearFragment helpFragment = new CarParkNearFragment();
        Bundle args = new Bundle();
        args.putInt(Global.EXTRA_POSITION_FRAGMENT, position);
        helpFragment.setArguments(args);
        return helpFragment;
    }

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Context mContext;
    private int position;
    private ListView lvCarPark;
    private List<Carpark> carparks;
    private TextView tvNoData, tvRadius;
    private ProgressBar pbLoading;
    private RelativeLayout rlRadius, rlLoading;
    private SeekBar sbRadius;
    private double currentLat, currentLon;
    private final int MAX_RADIUS = 9;
    private final int PER_DISTANCE = 100;
    private BroadcastReceiver broadCastReload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_carpark_near, container, false);
        lvCarPark = (ListView) rootView.findViewById(R.id.lvCarPark);
        tvNoData = (TextView) rootView.findViewById(R.id.tvNoData);
        rlRadius = (RelativeLayout) rootView.findViewById(R.id.rlRadius);
        pbLoading = (ProgressBar) rootView.findViewById(R.id.pbLoading);
        rlLoading = (RelativeLayout) rootView.findViewById(R.id.rlLoading);
        tvRadius = (TextView) rootView.findViewById(R.id.tvRadius);
        sbRadius = (SeekBar) rootView.findViewById(R.id.sbRadius);
        rootView.getContext().registerReceiver(broadCastReload, new IntentFilter(LocateMyLotActivity.BROADCAST_UPDATE_LOT));
        mContext = rootView.getContext();
        loadData();
        sbRadius.setMax(MAX_RADIUS);
        sbRadius.setProgress(MAX_RADIUS);
        tvRadius.setText(getStringCurrentDistance());

        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                searchCarPark();
                tvRadius.setText(getStringCurrentDistance());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        lvCarPark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
//                intent.putExtra(Global.SHOW_DIALOG_START, true);
//                mContext.sendBroadcast(intent);
                Carpark carpark = (Carpark) parent.getAdapter().getItem(position);
                Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
                intent.putExtra(Global.SELECT_BEACON_ID_KEY, carpark.id);
                intent.putExtra(GPSHelper.CARPARK_ID_LIST_KEY, carpark.id);

                mContext.sendBroadcast(intent);
            }
        });

        initLocation();

        return rootView;
    }

    private void loadData() {
        new UpdateData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void initLocation() {
        mLocationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation != null) {
            setCurrentLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setCurrentLocation(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

//                Log.e("RESPONSE_LOCATION", currentLat + "-" + currentLon);
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);
    }

    private void setCurrentLocation(double lat, double lon) {
        currentLat = lat;
        currentLon = lon;
        rlLoading.setVisibility(View.GONE);
        rlRadius.setVisibility(View.VISIBLE);
        searchCarPark();
    }

    private String getStringCurrentDistance() {
        String distance = "??m";
        int radius = getRadius();
        ;
        if (radius < 1000) {
            distance = radius + "m";
        } else {
            int km = radius / 1000;
            int afterDot = (radius - (km * 1000)) / 100;
            if (afterDot > 0) {
                distance = km + "." + afterDot + "km";
            } else {
                distance = km + "km";

            }
        }
        return distance;

    }

    private void searchCarPark() {
        int radius = getRadius();
        if(carparks==null)
            return;
        List<Carpark> carParkSearch = new ArrayList<Carpark>();
        for (Carpark carpark : carparks) {
            if (carpark.getRange(currentLat, currentLon) < radius) {
                carParkSearch.add(carpark);

            }
        }
        if (carParkSearch.size() > 0) {
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
//                        android.R.layout.simple_dropdown_item_1line, hintList);
//                etCarParkSearch.setAdapter(adapter);
            lvCarPark.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
            lvCarPark.setAdapter(new CarParkListAdapter(getContext(), carParkSearch));
        } else {
            tvNoData.setVisibility(View.VISIBLE);
            lvCarPark.setVisibility(View.INVISIBLE);
        }
    }

    private int getRadius() {
        return sbRadius.getProgress() * PER_DISTANCE + PER_DISTANCE;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    class UpdateData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            carparks = LocateMyLotActivity.carParkAndLots;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            lvCarPark.setAdapter(new CarParkListAdapter(mContext, carparks));
            pbLoading.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
        }
    }

}
