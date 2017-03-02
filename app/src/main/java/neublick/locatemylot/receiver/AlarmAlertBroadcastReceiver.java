package neublick.locatemylot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import neublick.locatemylot.activity.BalkActivity;

public class AlarmAlertBroadcastReceiver extends BroadcastReceiver {
	@Override public void onReceive(Context context, Intent intent) {
		Intent balkActivityIntent = new Intent(context, BalkActivity.class);
		balkActivityIntent.putExtra("TEN_MINS_LEFT", intent.getBooleanExtra("TEN_MINS_LEFT", false));
		balkActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(balkActivityIntent);
	}
}