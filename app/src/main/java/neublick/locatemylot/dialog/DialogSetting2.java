package neublick.locatemylot.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import neublick.locatemylot.R;
import neublick.locatemylot.ui.ExtendedImageView;

public class DialogSetting2 extends Activity implements SensorEventListener {

	LinearLayout layoutMapDirection;
	CheckBox checkBoxUseMapDirection;
	FrameLayout mapDirection;

	ExtendedImageView calibArrow;

	// xac dinh huong ban do
	Sensor sensorOrientation;
	SensorManager sensorManager;

	float alpha = 0;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = (int) (metrics.widthPixels * 0.80);

		setContentView(R.layout.dialog_setting);
		// set below the setContentView()
		getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		layoutMapDirection = (LinearLayout)findViewById(R.id.layout_map_direction);

		checkBoxUseMapDirection = (CheckBox)findViewById(R.id.checkbox_use_map_direction);
		checkBoxUseMapDirection.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
				boolean value = settings.getBoolean("use_map_direction", false);
				value = !value;
				settings.edit().putBoolean("use_map_direction", value).apply();
				checkBoxUseMapDirection.setChecked(value);
				updateCheckBoxState();
			}
		});
	}

	// khoi phuc cac trang thai vua moi thiet lap
	@Override protected void onResume() {
		super.onResume();
		sensorManager.registerListener(DialogSetting2.this, sensorOrientation, SensorManager.SENSOR_DELAY_UI);
		final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);

		// khoi phuc trang thai cua checkbox
		checkBoxUseMapDirection.setChecked(settings.getBoolean("use_map_direction", false));
		updateCheckBoxState();

		// khoi phuc trang thai cua arrow tren map
	}

	@Override protected void onPause() {
		sensorManager.unregisterListener(this);
		try {
			layoutMapDirection.removeView(getMapDirection());
		} catch(Exception ignored) {

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
			mapDirection = (FrameLayout)LayoutInflater.from(DialogSetting2.this).inflate(R.layout.map_direction, null);
			ImageView mapView = (ImageView)mapDirection.findViewById(R.id.map);
			mapView.setImageBitmap(
					BitmapFactory.decodeStream(getResources().openRawResource(R.raw.map3))
			);
			Button actionSave = (Button)mapDirection.findViewById(R.id.action_save);
			actionSave.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) {
					final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
					// ta luu vao preference gia tri alpha = 360 - alpha
					float CONSTANT_ALPHA = 360 - getCalibArrow().alpha;
					settings.edit().putFloat("alpha", CONSTANT_ALPHA).apply();
					alpha = CONSTANT_ALPHA;
				}
			});
		}
		return mapDirection;
	}

	public ExtendedImageView getCalibArrow() {
		if (calibArrow == null) {
			calibArrow = (ExtendedImageView)getMapDirection().findViewById(R.id.calib_arrow);
		}
		return calibArrow;
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			getCalibArrow().setRotation((alpha + event.values[0])%360);
		}
	}
}