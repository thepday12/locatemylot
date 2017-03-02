package neublick.locatemylot.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import neublick.locatemylot.R;
import neublick.locatemylot.receiver.AlarmAlertBroadcastReceiver;
import neublick.locatemylot.util.Utils;
import neublick.locatemylot.app.Config;

public class BalkActivity extends Activity {
	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		);
		setContentView(R.layout.activity_balk);

		AudioManager audioMgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

		//final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.mai_yeu);
		//mp.setLooping(true);
		//mp.start();

		Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		final Ringtone ringtoneAlarm = RingtoneManager.getRingtone(getApplicationContext(), alarmTone);
		ringtoneAlarm.play();

		Intent intentPassed 	= getIntent();
		boolean isTenMinsLeft 	= intentPassed.getBooleanExtra("TEN_MINS_LEFT", false);

		SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
		long futureFired = parkingSession.getLong("FUTURE_FIRED", -1);

		TextView textTitle 		= (TextView)findViewById(R.id.balk_activity_title);
		Button btnOk 			= (Button)findViewById(R.id.btnOk);
		Button btnSnooze 		= (Button)findViewById(R.id.btnSnooze);

		if (!isTenMinsLeft) {
			btnSnooze.setVisibility(View.INVISIBLE);
			textTitle.setText("Time fired: "
							+ Utils.getCalendarReadable(Utils.retrieveCalendarFromMillis(futureFired))
			);
		} else {
			// we have 10 minutes left
			textTitle.setText(Config.THRESHOLD_SNOOZE_TIME/1000/60 + " minutes left as of "
				+ Utils.getCalendarReadable(Utils.retrieveCalendarFromMillis(futureFired))
			);
		}

		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				// turn off the media-player
				//mp.release();
				ringtoneAlarm.stop();

				// turn off alarm service
				Intent receiverIntent = new Intent(getApplicationContext(), AlarmAlertBroadcastReceiver.class);
				PendingIntent pi = PendingIntent.getBroadcast(
						getApplicationContext(),
						0,
						receiverIntent,
						PendingIntent.FLAG_CANCEL_CURRENT
				);
				AlarmManager alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
				alarmMgr.cancel(pi);
				finish();
			}
		});

		btnSnooze.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//mp.release();
				ringtoneAlarm.stop();
				SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);

				long timeInMillis = parkingSession.getLong("FUTURE_FIRED", -1);
				logError("HAY NHAM MAT KHI ANH DEN");
				logError("timeInMillis=" + timeInMillis);

				if (timeInMillis != -1) {
					logError("HAY NHAM MAT KHI ANH DEN");
					Utils.schedule(getApplicationContext(), timeInMillis, false);
				}
				finish();
			}
		});
	}

	void toastMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	void logError(String s) {
		final String TAG = BalkActivity.class.getSimpleName();
		android.util.Log.e(TAG, s);
	}
}