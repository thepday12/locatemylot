package neublick.locatemylot.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;

import java.io.File;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.model.DetailMoveObject;
import neublick.locatemylot.ui.MapViewCopy;
import neublick.locatemylot.util.Utils;

public class DetailMoveActivity extends FragmentActivity {
    private MapViewCopy mMapView;
    private ProgressBar pbLoadingMap;
    private DetailMoveObject detailMoveObject;
    private TextView tvTitle;
    private ImageButton btBack;
    private float server_width = 720;
    private float server_height;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_move);

//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int screenWidth = (int) (metrics.widthPixels * 0.80);
//        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        pbLoadingMap = (ProgressBar) findViewById(R.id.pbLoadingMap);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        btBack = (ImageButton) findViewById(R.id.btBack);
        detailMoveObject = new Gson().fromJson(getIntent().getStringExtra(Global.EXTRA_DATA), DetailMoveObject.class);
        new AsyncSetBitmapToMapView().execute();
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        getMapViewLiftLobbyObject();

    }

    public MapViewCopy getMapView() {
        if (mMapView == null) {
            mMapView = (MapViewCopy) findViewById(R.id.newMap);
        }
        return mMapView;
    }

    public MapViewCopy.UserObjectOverlap getMapViewUserObject() {
        if (getMapView().userObject.view == null) {
            getMapView().userObject.view(findViewById(R.id.user_object));
        }
        return getMapView().userObject;
    }

    public MapViewCopy.ObjectOverlap getMapViewLiftLobbyObject() {
        if (getMapView().liftLobbyObject.view == null) {
            ImageView imageView = (ImageView) findViewById(R.id.lift_lobby_object);
            getMapView().liftLobbyObject.view(imageView);
        }
        return getMapView().liftLobbyObject;
    }

    //thep 2016/02/24
    public MapViewCopy.ObjectOverlap getMapViewCarObject() {
        if (getMapView().carObject.view == null) {
            ImageView imageView = (ImageView) findViewById(R.id.car_object);
            getMapView().carObject.view(imageView);
        }
        return getMapView().carObject;
    }

    public MapViewCopy.ObjectOverlap getMapViewDestinationObject() {
        if (getMapView().destinationObject.view == null) {
            ImageView imageView = (ImageView) findViewById(R.id.ivDestination);
            getMapView().destinationObject.view(imageView);
        }
        return getMapView().destinationObject;
    }


    @Override
    protected void onResume() {
        getMapView().postDelayed(new Runnable() {
            @Override
            public void run() {

//                        llTime.setVisibility(View.INVISIBLE);
                Matrix savedMatrix = getMapView().restoreState();

                if (savedMatrix != null) {
                    getMapViewCarObject().applyMatrix(savedMatrix);
                    getMapViewUserObject().applyMatrix(savedMatrix);
                    getMapViewDestinationObject().applyMatrix(savedMatrix);
                }
                    /*else {
                        getMapViewCarObject().applyMatrix(getMapView().MATRIX_INITIAL_SCALE);
					}
					*/


            }
        }, 500);
        super.onResume();
    }

    class AsyncSetBitmapToMapView extends android.os.AsyncTask<Integer, Void, Bitmap> {
        private File fileMap;

        public AsyncSetBitmapToMapView() {
            pbLoadingMap.setVisibility(View.VISIBLE);
            getMapViewCarObject().visible(false);
            String dir = Global.MY_DIR + detailMoveObject.getMapName();
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
                return Utils.decodeSampledBitmapFromUri(DetailMoveActivity.this, Uri.fromFile(fileMap), 0, 0);
            }
            return bitmap;
        }

        @Override
        public void onPostExecute(Bitmap result) {

//
            pbLoadingMap.setVisibility(View.GONE);
            if (result != null) {
                getMapView().setImageBitmap(result);
                //Hien thi lai thiet lap
                server_height = server_width * getMapView().bitmapHeight / getMapView().bitmapWidth;

                // tinh ti so quy doi tu ban do tren server sang local
                Global.mRatioXCopy = (float) (getMapView().bitmapWidth * 1.0 / server_width);
                Global.mRatioYCopy = (float) (getMapView().bitmapHeight * 1.0 / server_height);

                int resize = (int) Utils.convertDpToPixel(8, DetailMoveActivity.this);
                int resizeUser = (int) Utils.convertDpToPixel(14, DetailMoveActivity.this);

                if (detailMoveObject.getStartX() > 0 || detailMoveObject.getStartY() > 0) {
//                    getMapViewUserObject().original(detailMoveObject.getStartX(), detailMoveObject.getStartY()).applyMatrix(getMapView().drawMatrix).visible(true);
//                    getMapView().wayDrawXY(detailMoveObject.getDestinationX(), detailMoveObject.getDestinationY());
                    getMapViewUserObject().original(detailMoveObject.getStartX()-resizeUser, detailMoveObject.getStartY()-resizeUser).applyMatrix(getMapView().drawMatrix).visible(false);
                }

                if (detailMoveObject.isDestinationIsCar()) {
                    getMapViewCarObject().original(detailMoveObject.getDestinationX()-resizeUser, detailMoveObject.getDestinationY()-resizeUser).applyMatrix(getMapView().drawMatrix).visible(true);
                }
                getMapViewDestinationObject().original(detailMoveObject.getDestinationX() - resize, detailMoveObject.getDestinationY() - resize).applyMatrix(getMapView().drawMatrix).visible(true);


                String data[] = detailMoveObject.getMapName().replace(".png","").split("_");
//                getMapView().wayDrawXY(resize,Integer.valueOf(data[0]),data[1],getMapViewDestinationObject().location.originalX, getMapViewDestinationObject().location.originalY);



            } else {
                Toast.makeText(DetailMoveActivity.this, getString(R.string.load_map_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
