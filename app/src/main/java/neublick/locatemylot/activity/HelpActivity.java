package neublick.locatemylot.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.PaperHelpAdapter;
import neublick.locatemylot.app.Global;

public class HelpActivity extends FragmentActivity {

    private ViewPager vpPaper;
    private PaperHelpAdapter adapter;
    private TextView tvPosition;
    private ImageButton btBack;
    private final int HELP_PAGE_SIZE=9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        init();
    }

    private void init() {
        vpPaper = (ViewPager) findViewById(R.id.vpPaper);
        btBack = (ImageButton) findViewById(R.id.btBack);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        adapter = new PaperHelpAdapter(getSupportFragmentManager(), HELP_PAGE_SIZE);
        vpPaper.setAdapter(adapter);
        vpPaper.setCurrentItem(0);
        tvPosition.setText("1/"+HELP_PAGE_SIZE);
        vpPaper.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Intent intent = new Intent(Global.TAG_HELP_FRAGMENT + position);
                sendBroadcast(intent);
                HelpFragment.showHelp(btBack);
                tvPosition.setText((position+1)+"/"+HELP_PAGE_SIZE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
