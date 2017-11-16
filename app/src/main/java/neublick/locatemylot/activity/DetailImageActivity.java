package neublick.locatemylot.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import neublick.locatemylot.R;
import neublick.locatemylot.service.fcm.MyFirebaseMessagingService;
import neublick.locatemylot.util.ShareLocationUtil;

public class DetailImageActivity extends AppCompatActivity {

    private boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image);
        ImageView ivImage = (ImageView) findViewById(R.id.ivImage);
        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        TextView tvFrom = (TextView) findViewById(R.id.tvFrom);
        final ProgressBar pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("IMAGE_URL");
        tvFrom.setText(intent.getStringExtra("FROM"));
        isNew = intent.getBooleanExtra("IS_NEW",false);


        Picasso.with(DetailImageActivity.this).load(imageUrl).into(ivImage, new Callback() {
            @Override
            public void onSuccess() {
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(DetailImageActivity.this, "Image load fail", Toast.LENGTH_SHORT).show();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void finish() {
        if(isNew){
            ShareLocationUtil.setLastShareLocation(DetailImageActivity.this,"");
            try {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(MyFirebaseMessagingService.NOTIFICATION_ID);
            } catch (Exception ex) {

            }
        }
        super.finish();
    }
}
