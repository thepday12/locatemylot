package neublick.locatemylot.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;

public class WebDetailActivity extends AppCompatActivity {
    private WebView wvContent;
    private ProgressBar pbLoadWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advweb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        wvContent = (WebView) findViewById(R.id.wvContent);
        pbLoadWeb = (ProgressBar) findViewById(R.id.pbLoadWeb);
        wvContent.setWebViewClient(new myWebClient());
        wvContent.getSettings().setJavaScriptEnabled(true);
        wvContent.clearHistory();
        wvContent.getSettings().setLoadWithOverviewMode(true);
        wvContent.getSettings().setUseWideViewPort(true);
        Intent intent = getIntent();
        String url = intent.getStringExtra(Global.EXTRA_DATA);
        String title = intent.getStringExtra(Global.EXTRA_TITLE);
        if(title == null || title.isEmpty()){
            title = "Promotion detail";
        }
        getSupportActionBar().setTitle(title);
        wvContent.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.visit_website_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.itemReload:
                pbLoadWeb.setVisibility(View.VISIBLE);
                wvContent.reload();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            pbLoadWeb.setVisibility(View.VISIBLE);
            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            pbLoadWeb.setVisibility(View.GONE);
            super.onPageFinished(view, url);

        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


}

