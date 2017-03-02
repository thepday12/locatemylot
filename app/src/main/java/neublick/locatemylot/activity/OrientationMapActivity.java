package neublick.locatemylot.activity;

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

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import neublick.locatemylot.R;
import neublick.locatemylot.ui.ExtendedImageView;

public class OrientationMapActivity extends Activity implements SensorEventListener {

	ImageView mapView;
	ExtendedImageView calibArrow;

	Sensor sensorOrientation;
	SensorManager sensorManager;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_orientation_map);

		mapView = (ImageView)findViewById(R.id.map);
		calibArrow = (ExtendedImageView)findViewById(R.id.calib_arrow);

		mapView.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.raw.map3)));

		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		Button save = (Button)findViewById(R.id.action_save);
		save.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				final SharedPreferences calibOrientation = getSharedPreferences("calib_orientation", MODE_PRIVATE);
				calibOrientation.edit().putFloat("alpha", calibArrow.alpha).apply();
			}
		});
	}

	@Override protected void onResume() {
		super.onResume();
		sensorManager.registerListener(OrientationMapActivity.this, sensorOrientation, SensorManager.SENSOR_DELAY_UI);
	}

	@Override protected void onPause() {
		sensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			calibArrow.setRotation(event.values[0]);
		}
	}
}