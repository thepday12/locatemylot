package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import static android.view.View.OnClickListener;

import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import neublick.locatemylot.R;

public class DialogInformation extends Activity {
    private  Dialog dialog;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int screenWidth = (int) (metrics.widthPixels * 0.85);

        setContentView(R.layout.dialog_information);

        // set below the setContentView()
//        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        final WebView wvAbout = (WebView) findViewById(R.id.wvAbout);
        final Button close = (Button) findViewById(R.id.action_close);
        final Button btLegal = (Button) findViewById(R.id.btLegal);
        final String copyright = "<b>\"Locate My Lot\"</b> is an app that helps you locate your car in the carpark!<br/>" +
                "<br/>"+
                "By continuing with our app, you engage in our “Service” and agree to be bound by the following terms and conditions (“Terms of Service”, “Terms”), including those additional terms and conditions and policies referenced herein and/or available by hyperlink. These Terms of Service apply to all users of the app, including without limitation users who are browsers, vendors, customers, merchants, and/ or contributors of content.<br/>" +
                "<br/>" +
                "Please read the Terms of Service carefully before accessing or using LOCATE MY LOT. By accessing or using any part of the app, you agree to be bound by these Terms of Service. If you do not agree to all the terms and conditions of this agreement, then you may not access the app or use any services. If these Terms of Service are considered an offer, acceptance is expressly limited to these Terms of Service.<br/>" +
                "<br/>" +
                "Users are solely responsible for the use of LOCATE MY LOT and its services, the use of which is done at their own risk and falls entirely under their responsibility.<br/>" +
                "<br/>" +
                "Any explanatory texts provided in correspondence of the available services are merely intended to facilitate use and understanding of the Service."
                + "<br/><br/>"
                + "Neublick Pte Ltd<br/>"
                + "50 Kian Teck Road<br/>"
                + "Singapore 628788"
                + "<br/><br/>" + getString(R.string.text_version);

        String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #000000; background-color: #ffffffff;}"
                + "</style></head>"
                + "<body>"
                + copyright
                + "</body></html>";
        float fontSize = getResources().getDimension(R.dimen.txt_size_about);
//        wvAbout.getSettings().setDefaultFontSize((int)fontSize);
        wvAbout.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
        wvAbout.setBackgroundColor(Color.TRANSPARENT);
        wvAbout.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

//
//companyDetails.postDelayed(new Runnable() {
//			@Override public void run() {
////                String text = getString(R.string.copyright)+"<br/><br/>"
//                String text = copyright+"<br/><br/>"
//				+ "Neublick Pte Ltd<br/>"
//				+ "50 Kian Teck Road<br/>"
//				+ "Singapore 628788";
//                text+="<br/><br/>"+getString(R.string.text_version);
//				companyDetails.setText(text);
//			}
//		}, 10);

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btLegal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogLegal();
            }
        });
    }

    private void showDialogLegal() {
        if(dialog!=null&&dialog.isShowing()) return;

        dialog = new Dialog(DialogInformation.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_legal);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.85);
        dialog.getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btTerms = (Button) dialog.findViewById(R.id.btTerms);
        Button btPrivacyPolicy = (Button) dialog.findViewById(R.id.btPrivacyPolicy);
        btTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        btPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.show();
    }
}