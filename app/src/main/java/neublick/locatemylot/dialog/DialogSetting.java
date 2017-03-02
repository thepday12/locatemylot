package neublick.locatemylot.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import static android.view.View.OnClickListener;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.EditText;

import com.squareup.picasso.Picasso;

import java.io.File;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.BaseActivity;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.ui.ExtendedImageView;

public class DialogSetting extends Activity implements SensorEventListener {

    LinearLayout layoutMapDirection;
    CheckBox checkBoxUseMapDirection;
    FrameLayout mapDirection;

    ExtendedImageView calibArrow;
//    EditText txt_notify_before;

    // xac dinh huong ban do
    Sensor sensorOrientation;
    SensorManager sensorManager;

    float alpha = 0;
//    int notify_before = 10;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        alpha = Global.calibAngle;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);

        setContentView(R.layout.dialog_setting);
        // set below the setContentView()
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        layoutMapDirection = (LinearLayout) findViewById(R.id.layout_map_direction);
//        txt_notify_before = (EditText) findViewById(R.id.txt_notify_before);

        checkBoxUseMapDirection = (CheckBox) findViewById(R.id.checkbox_use_map_direction);
        checkBoxUseMapDirection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
                boolean value = settings.getBoolean("use_map_direction", false);
                value = !value;
                settings.edit().putBoolean("use_map_direction", value).apply();
                checkBoxUseMapDirection.setChecked(value);
                updateCheckBoxState();
            }
        });
        Button actionSave = (Button) findViewById(R.id.action_save_setting);
        actionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
//                try {
//                    notify_before = Integer.parseInt(txt_notify_before.getText().toString());
//                } catch (Exception ignored) {
//                    notify_before = 10;
//                }
                Global.calibAngle = alpha;
//                settings.edit().putFloat("alpha", alpha).putInt("notify_before", notify_before).apply();
                settings.edit().putFloat("alpha", alpha).apply();
                finish();
            }
        });
    }

    // khoi phuc cac trang thai vua moi thiet lap
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(DialogSetting.this, sensorOrientation, SensorManager.SENSOR_DELAY_UI);
        final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);

//        notify_before = settings.getInt("notify_before", 10);
//        txt_notify_before.setText(notify_before + "");

        // khoi phuc trang thai cua checkbox
        checkBoxUseMapDirection.setChecked(settings.getBoolean("use_map_direction", false));
        updateCheckBoxState();

        // khoi phuc trang thai cua arrow tren map
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        try {
            layoutMapDirection.removeView(getMapDirection());
        } catch (Exception ignored) {

        }
        super.onPause();
    }

    void updateCheckBoxState() {
        if (checkBoxUseMapDirection.isChecked()) {
            layoutMapDirection.addView(getMapDirection());
            final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
            float get_alpha = settings.getFloat("alpha", -1);
            if (get_alpha != -1) {
                alpha = get_alpha;
            }
        } else {
            layoutMapDirection.removeView(getMapDirection());
            /*
			final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
			settings.edit().putBoolean("use_map_direction", false).apply();
			*/
            alpha = 0;
        }
    }

    // GETTER
    public FrameLayout getMapDirection() {
        if (mapDirection == null) {
            mapDirection = (FrameLayout) LayoutInflater.from(DialogSetting.this).inflate(R.layout.map_direction, null);
            ImageView mapView = (ImageView) mapDirection.findViewById(R.id.map);
            if (BaseActivity.CURRENT_MAP != null) {
                File file = new File(BaseActivity.CURRENT_MAP);
               Picasso.with(DialogSetting.this).load( Uri.fromFile(file)).error(R.raw.map3).into(mapView);
            } else {
                mapView.setImageBitmap(
                        BitmapFactory.decodeStream(getResources().openRawResource(R.raw.map3))
                );
            }
            Button actionCalib = (Button) mapDirection.findViewById(R.id.action_calib);
            actionCalib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
                    alpha = -currentCompassAngle;
                }
            });
        }
        return mapDirection;
    }

    public ExtendedImageView getCalibArrow() {
        if (calibArrow == null) {
            calibArrow = (ExtendedImageView) getMapDirection().findViewById(R.id.calib_arrow);
        }
        return calibArrow;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    float currentCompassAngle = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            currentCompassAngle = event.values[0];
            getCalibArrow().setRotation((alpha + currentCompassAngle) % 360);
        }
    }
}