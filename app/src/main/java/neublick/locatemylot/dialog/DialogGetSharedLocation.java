package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.service.fcm.MyFirebaseMessagingService;
import neublick.locatemylot.util.ShareLocationUtil;

public class DialogGetSharedLocation extends Activity {
    String shared_data="";
    @Override protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);

        setContentView(R.layout.dialog_get_shared_location);

        // set below the setContentView()
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        Bundle extras = getIntent().getExtras();
        shared_data = extras.getString(Global.SHARE_DATA_EXTRA);
        try {
            JSONObject jsonObject = new JSONObject(shared_data);
            String from_user = jsonObject.getString("user_from_name");
            TextView txtMessage=(TextView)findViewById(R.id.message);
            txtMessage.setText(Html.fromHtml("<b>"+from_user+"</b> "+getString(R.string.text_share_location)));

            Button accept 			= (Button)findViewById(R.id.action_accept);
            Button cancel 			= (Button)findViewById(R.id.action_cancel);

            cancel.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    Global.isGetSharedLocationDialogShown=false;
                    clearNotification(DialogGetSharedLocation.this);

                    finish();
                }
            });

            accept.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if(Global.activityMain!=null)
                        Global.activityMain.acceptSharedLocation(shared_data);
                    Global.isGetSharedLocationDialogShown=false;
                    clearNotification(DialogGetSharedLocation.this);
                    finish();
                }
            });
        } catch (JSONException e) {

        }

    }



    private void clearNotification(Context context) {
        if(ShareLocationUtil.getLastShareLocation(context).equals(shared_data)){
            ShareLocationUtil.setLastShareLocation(context,"");
        }
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(MyFirebaseMessagingService.NOTIFICATION_ID);
        } catch (Exception ex) {

        }
    }
}