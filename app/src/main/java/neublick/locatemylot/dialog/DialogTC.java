package neublick.locatemylot.dialog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import neublick.locatemylot.R;

public class DialogTC extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_tc);
        final WebView wvInfo = (WebView) findViewById(R.id.wvInfo);
        final Button btClose = (Button) findViewById(R.id.btClose);

        wvInfo.loadUrl("file:///android_asset/tc.htm");
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
