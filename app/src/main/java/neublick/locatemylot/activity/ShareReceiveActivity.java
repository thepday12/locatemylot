package neublick.locatemylot.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.ShareReceiveAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLShareReceive;
import neublick.locatemylot.model.ShareReceiveObject;

public class ShareReceiveActivity extends AppCompatActivity {
    private RecyclerView rvShareReceive;
    private Button btClose;
    private TextView tvNoData;
    private ShareReceiveAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_receive);

        rvShareReceive = (RecyclerView) findViewById(R.id.rvShareReceive);
        btClose = (Button) findViewById(R.id.btClose);
        tvNoData = (TextView) findViewById(R.id.tvNoData);

        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(ShareReceiveActivity.this, LinearLayoutManager.VERTICAL, false);
        rvShareReceive.setLayoutManager(layoutManager);

        List<ShareReceiveObject> shareReceiveObjects = CLShareReceive.getShareNotLikeType(Global.TYPE_SHARE_LOCATION);
        if(shareReceiveObjects.size()>0){
            tvNoData.setVisibility(View.GONE);
            adapter = new ShareReceiveAdapter(shareReceiveObjects);
            rvShareReceive.setAdapter(adapter);
        }else{
            tvNoData.setVisibility(View.VISIBLE);
        }
    }
}
