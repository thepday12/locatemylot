package neublick.locatemylot.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.CarParkListAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.model.Carpark;
import neublick.locatemylot.util.GPSHelper;
import neublick.locatemylot.util.Utils;

import static neublick.locatemylot.R.id.sbRadius;
import static neublick.locatemylot.R.id.tvRadius;


/**
 * Created by Thep on 1/28/2016.
 */
public class CarParkMapFragment extends Fragment {

    public static final CarParkMapFragment newInstance(int position) {
        CarParkMapFragment helpFragment = new CarParkMapFragment();
        Bundle args = new Bundle();
        args.putInt(Global.EXTRA_POSITION_FRAGMENT, position);
        helpFragment.setArguments(args);
        return helpFragment;
    }

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Context mContext;
    private RelativeLayout rlLoading;
    private WebView wvMap;
    private double currentLat, currentLon;
    private TextView tvLoading, tvNoData;
    private boolean isLoaded = false;
    private boolean isWebLoaded = false;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(
                    "android.net.conn.CONNECTIVITY_CHANGE")) {
                if (mContext != null)
                    if (Utils.isInternetConnected(mContext)) {
                        if (isLoaded) {
                            initLocation();
                        }
                    }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_carpark_map, container, false);
        wvMap = (WebView) rootView.findViewById(R.id.wvMap);
        rlLoading = (RelativeLayout) rootView.findViewById(R.id.rlLoading);
        tvLoading = (TextView) rootView.findViewById(R.id.tvLoading);
        tvNoData = (TextView) rootView.findViewById(R.id.tvNoData);

        wvMap.setWebViewClient(new myWebClient());
        wvMap.getSettings().setJavaScriptEnabled(true);
        wvMap.clearHistory();
        mContext = rootView.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(this.mBatInfoReceiver,
                filter);
        if (!Utils.isInternetConnected(mContext)) {
            rlLoading.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            initLocation();
        }
        isLoaded = true;

        return rootView;
    }

    private void initLocation() {
        isLoaded = false;
        tvNoData.setVisibility(View.GONE);
        rlLoading.setVisibility(View.VISIBLE);
        tvLoading.setText(getString(R.string.text_find_location));
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
//        rlLoading.setVisibility(View.GONE);
        updateLocation();
    }

    private void updateLocation() {
        if (Utils.isInternetConnected(mContext)) {
            if (isWebLoaded) {
                wvMap.loadUrl("javascript:updateGPSPosition(" + currentLat + "," + currentLon + ")");
            } else {
                String url = "http://neublick.com/demo/carlocation/cms/carparks_duc.php?lat=" + currentLat + "&lon=" + currentLon;
                wvMap.loadUrl(url);
                isWebLoaded = true;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mBatInfoReceiver);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            if (mLocationManager != null)
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

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            rlLoading.setVisibility(View.VISIBLE);
            tvLoading.setText(getString(R.string.text_loading));
            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            rlLoading.setVisibility(View.GONE);
            super.onPageFinished(view, url);

        }
    }
}
