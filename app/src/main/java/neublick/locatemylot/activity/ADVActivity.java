package neublick.locatemylot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.ADVAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLADV;
import neublick.locatemylot.model.ADVObject;
import neublick.locatemylot.util.Utils;

public class ADVActivity extends AppCompatActivity {
    private ViewPager vpContent;
    private ADVAdapter mPaperAdapter;
    private RelativeLayout rlMain;
    private LinearLayout dotsLayout;
    private TextView tvTitle;
    private TextView[] dots;
    private int activeDotColor;
    private int inactiveDocsColor;
    private int numberOfSlides =0;
    private boolean isAdvLocal=false;
    private BroadcastReceiver broadCastReload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
            mPaperAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(broadCastReload, new IntentFilter(Global.UPDATE_VIEW_ADV_KEY));
        setContentView(R.layout.activity_adv);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.95);
        int screenHeight = (int) (metrics.heightPixels * 0.85);
        getWindow().setLayout(screenWidth, screenHeight);



        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        vpContent = (ViewPager) findViewById(R.id.vpHomePaper);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//             int color = Color.parseColor("#3498db");
//            GradientDrawable gd = new GradientDrawable(
//                    GradientDrawable.Orientation.TOP_BOTTOM,
//                    new int[]{color, color});
//            gd.setCornerRadius(Utils.convertDpToPixel(16, ADVActivity.this));
//            rlMain.setBackground(gd);
//        }

        activeDotColor = Color.RED;
        inactiveDocsColor = Color.WHITE;
        dotsLayout = (LinearLayout) findViewById(R.id.viewPagerCountDots);


        ImageButton btClose = (ImageButton) findViewById(R.id.btClose);
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        init();
    }

    private void init() {
        vpContent.setOffscreenPageLimit(3);
        Intent intent = getIntent();
        isAdvLocal = intent.getBooleanExtra(Global.IS_ADV_LOCAL,false);
        loadData();

    }

    private void loadData() {
        Intent intent = getIntent();
        if(intent.getBooleanExtra(Global.IS_PROMOTION,false)){
            tvTitle.setText("Promotions");
        }else{
            tvTitle.setText("Latest Ads");
        }

        try {
            List<ADVObject> advObjects = new ArrayList<>();

//            if(isAdvLocal) {
                advObjects = CLADV.getAllADV();
//            }else{
//                JSONArray jsonArray = new JSONArray(intent.getStringExtra(Global.EXTRA_ADV_DATA));
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    advObjects.add(new ADVObject(jsonArray.getJSONObject(i)));
//                }
//            }
            numberOfSlides = advObjects.size();
            if(numberOfSlides>0){
                mPaperAdapter = new ADVAdapter(getSupportFragmentManager(),ADVActivity.this,advObjects,isAdvLocal);
                setViewPagerDots(numberOfSlides);
                vpContent.setAdapter(mPaperAdapter);
                vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (numberOfSlides > 1) {
                            //Set current inactive dots color
                            for (int i = 0; i < numberOfSlides; i++) {
                                dots[i].setTextColor(inactiveDocsColor);
                            }

                            //Set current active dot color
                            dots[position].setTextColor(activeDotColor);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }else{
                Toast.makeText(this, "No ads", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {

        }
    }

    private void setViewPagerDots(int size) {
        dotsLayout.removeAllViews();
        dots = new TextView[size];

        //Set first inactive dots color
        for (int i = 0; i < size; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(30);
            dots[i].setTextColor(inactiveDocsColor);
            dotsLayout.addView(dots[i]);
        }

        //Set first active dot color
        dots[0].setTextColor(activeDotColor);
    }

    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(broadCastReload);
        }catch (Exception e){

        }
        super.onDestroy();
    }
}
