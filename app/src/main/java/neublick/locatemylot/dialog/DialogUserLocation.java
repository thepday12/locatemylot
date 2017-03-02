package neublick.locatemylot.dialog;

import android.app.Dialog;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import neublick.locatemylot.R;

import static android.view.View.OnClickListener;

public abstract class DialogUserLocation extends Dialog implements OnClickListener {

	Activity mActivity;
	int mScreenWidth = 500;
	Button mButtonCheckIn;
	Button mButtonCancel;

	TextView carparkName;
	TextView carparkFloor;
	TextView carparkZone;

	String carparkNameCaption = "";
	String carparkFloorCaption = "";
	String carparkZoneCaption = "";

	public DialogUserLocation(Activity activity, String carparkNameCaption, String carparkFloorCaption, String carparkZoneCaption) {
		this(activity);
		this.carparkNameCaption 	= carparkNameCaption;
		this.carparkFloorCaption 	= carparkFloorCaption;
		this.carparkZoneCaption 	= carparkZoneCaption;
	}

	public DialogUserLocation(Activity activity) {
		super(activity);
		mActivity = activity;
		DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
		mScreenWidth = (int) (metrics.widthPixels * 0.80);
	}

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog_user_location);
		getWindow().setLayout(mScreenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

		mButtonCheckIn 		= (Button)findViewById(R.id.action_checkin);
		mButtonCancel 		= (Button)findViewById(R.id.action_cancel);

		mButtonCheckIn.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		carparkName 	= (TextView)findViewById(R.id.user_carpark_name);
		carparkFloor 	= (TextView)findViewById(R.id.user_carpark_floor);
		carparkZone 	= (TextView)findViewById(R.id.user_carpark_zone);

		final Typeface typeMedium = Typeface.createFromAsset(
			mActivity.getAssets(),
			"RobotoMedium.ttf"
		);

		final Typeface typeRegular = Typeface.createFromAsset(
			mActivity.getAssets(),
			"RobotoMedium.ttf"
		);

		carparkName.postDelayed(new Runnable() {
			@Override public void run() {
				carparkName.setTypeface(typeMedium);
				carparkName.setText(carparkNameCaption);
			}
		}, 0);
		carparkFloor.postDelayed(new Runnable() {
			@Override public void run() {
				carparkFloor.setTypeface(typeRegular);
				carparkFloor.setText(carparkFloorCaption);
			}
		}, 0);
		carparkZone.postDelayed(new Runnable() {
			@Override public void run() {
				carparkZone.setTypeface(typeRegular);
				carparkZone.setText(carparkZoneCaption);
			}
		}, 0);

	}

	// GETTER
	public TextView carparkName() { return carparkName; }
	public TextView carparkFloor() { return carparkFloor; }
	public TextView carparkZone() { return carparkZone; }

	@Override public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.action_checkin) {
			onCheckIn();
		} else if (id == R.id.action_cancel) {
			dismiss();
			this.dismiss();
		}
	}

	// khi ta nhan nut checkIn thi ham onCheckIn() duoc goi
	public abstract void onCheckIn();
}