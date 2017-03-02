package neublick.locatemylot.receiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Global;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

	public static Dialog requestBluetoothEnabledDialog;

	@Override public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            boolean state=false;
            if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                state=true;
            }else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {

                try {
//					getRequestBluetoothEnabledDialog(context).show();
				}
				catch(Exception ignored){}
			}
            Global.sendBroadCastChangeBluetoothState(context,state);
        }

	}

	public Dialog getRequestBluetoothEnabledDialog(final Context context) {
		if (requestBluetoothEnabledDialog == null) {
//            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//            int screenWidth = (int) (metrics.widthPixels * 0.85);
			requestBluetoothEnabledDialog =
				new AlertDialog.Builder(context).setTitle("LocateMyLot")
					.setMessage("Bluetooth is not enabled")
					.setCancelable(false)
						.setPositiveButton("Enable bluetooth", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								requestBluetoothEnabled(context);
							}
						})
						.setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Global.exitApplication();
							}
						})
					.create();
//            requestBluetoothEnabledDialog.getWindow().setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		return requestBluetoothEnabledDialog;
	}

	public static void requestBluetoothEnabled(Context context) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			Activity activity = (Activity)context;
			activity.startActivityForResult(enableBluetoothIntent, LocateMyLotActivity.REQUEST_BLUETOOTH);
		}
	}
}
