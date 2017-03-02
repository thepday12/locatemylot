package neublick.locatemylot.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import neublick.locatemylot.activity.LocateMyLotActivity;
/**
 * Created by Tung Nguyen on 8/31/2015.
 */
public class BootUpReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        try
        {
            Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
            notificationIntent.setClass(context, LocateMyLotActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(notificationIntent);
        }
        catch (Exception e) {
            Toast.makeText(context,  e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}