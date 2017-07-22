package neublick.locatemylot.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.UpdateInfoActivity;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

public class DialogPhoneCode extends AppCompatActivity {

    private EditText etCode;
    private Button btOk;
    private TextView tvResend, tvChangePhone, tvDescription;
    private LinearLayout llResend;
    private CheckResend checkResend;
    private CallResendCode resendCode;
    private Boolean isValid = false;
    private Dialog dialogEnterPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.9);

        setContentView(R.layout.dialog_phone_code);

        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        init();
    }

    private void init() {
        dialogEnterPhone = new Dialog(DialogPhoneCode.this);
        dialogEnterPhone.requestWindowFeature(Window.FEATURE_NO_TITLE);

        etCode = (EditText) findViewById(R.id.etCode);
        btOk = (Button) findViewById(R.id.btOk);
        tvResend = (TextView) findViewById(R.id.tvResend);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvChangePhone = (TextView) findViewById(R.id.tvChangePhone);
        llResend = (LinearLayout) findViewById(R.id.llResend);
        String phone = UserUtil.getUserPhone(DialogPhoneCode.this);

        setDescriptionPhone(phone);
        startResendTimer();
        if (Utils.isInternetConnected(DialogPhoneCode.this)) {
            new CheckOTPStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        tvChangePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogEnterPhone();
            }
        });

        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetConnected(DialogPhoneCode.this)) {
                    callResendCode();
                    startResendTimer();
                } else {
                    Toast.makeText(DialogPhoneCode.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isInternetConnected(DialogPhoneCode.this)) {
                    if (!isValid) {
                        String code = etCode.getText().toString();
                        if (code.length() == 6) {
                            new ValidCode(code).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                } else {
                    Toast.makeText(DialogPhoneCode.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void callResendCode() {
        if (resendCode != null) {
            resendCode.cancel(true);
        }
        resendCode = new CallResendCode();
        resendCode.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setDescriptionPhone(String phone){
        String description = getString(R.string.verification_phone_description) + " <b>" + getHidenPhone(phone) + "</b>";
        tvDescription.setText(Html.fromHtml(description));
    }

    public void showDialogEnterPhone() {
        if (dialogEnterPhone.isShowing())
            return;
        dialogEnterPhone.setContentView(R.layout.dialog_enter_phone);
        dialogEnterPhone.setCanceledOnTouchOutside(true);
        Button btOk = (Button) dialogEnterPhone.findViewById(R.id.btOk);
        Button btCancel = (Button) dialogEnterPhone.findViewById(R.id.btCancel);
        btCancel.setText("Cancel");
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEnterPhone.dismiss();
            }
        });


        final EditText etPhone = (EditText) dialogEnterPhone.findViewById(R.id.etPhone);
        // if button is clicked, close the custom dialogEnterPhone
        etPhone.setText(UserUtil.getUserPhone(DialogPhoneCode.this));
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etPhone.getText().toString();
                if (phone != null && !phone.isEmpty()) {
                    new UpdatePhone(phone, dialogEnterPhone).execute();
                } else {
                    etPhone.setError(getString(R.string.edit_text_null_value));
                }
            }
        });


        dialogEnterPhone.show();
    }

    private String getHidenPhone(String phone) {
        int length = phone.length();
        String result = "";
        for (int i = 0; i < length - 3; i++) {
            result += "*";
        }
        String data = phone;
        if (phone.length() > 3) {
            data = phone.substring(phone.length() - 3);
        }
        result += data;
        return result;
    }

    private void startResendTimer() {
        llResend.setVisibility(View.GONE);

        //Check startResendTimer
        if (checkResend != null) {
            checkResend.cancel(true);
        }
        checkResend = new CheckResend();
        checkResend.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }


    class CheckResend extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            llResend.setVisibility(View.VISIBLE);
        }
    }

    public class UpdatePhone extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private Dialog mDialogEnterPhone;
        private HashMap hashMap;
        private String mPhone;

        public UpdatePhone(String phone, Dialog dialogEnterPhone) {
            mPhone = UserUtil.formatPhone(phone);
            hashMap = new HashMap();
            hashMap.put("act", "updateinfo");
            hashMap.put("p", mPhone);
            hashMap.put("i", UserUtil.getUserId(DialogPhoneCode.this));
            mDialogEnterPhone = dialogEnterPhone;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogPhoneCode.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getBoolean("state")) {
                        UserUtil.setUserPhone(DialogPhoneCode.this, mPhone);
                        setDescriptionPhone(mPhone);
                        mDialogEnterPhone.dismiss();

                    } else {
                        Toast.makeText(DialogPhoneCode.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(DialogPhoneCode.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DialogPhoneCode.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }

    public class CheckOTPStatus extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private HashMap hashMap;

        public CheckOTPStatus() {
            hashMap = new HashMap();
            hashMap.put("act", "check_otp_status");
            hashMap.put("i", UserUtil.getUserId(DialogPhoneCode.this));
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogPhoneCode.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean otpStatus = jsonObject.getInt("otp_status") > 0;
                    UserUtil.setPhoneVerification(DialogPhoneCode.this,otpStatus );
                    if(otpStatus){
                        finish();
                    }else{
                        callResendCode();
                    }
                } catch (JSONException e) {
                }
            } else {
            }
            super.onPostExecute(s);
        }
    }

    public class CallResendCode extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private HashMap hashMap;

        public CallResendCode() {
            hashMap = new HashMap();
            hashMap.put("act", "send_otp");
            hashMap.put("i", UserUtil.getUserId(DialogPhoneCode.this));
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogPhoneCode.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getBoolean("status")) {

                    } else {
                        Toast.makeText(DialogPhoneCode.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(DialogPhoneCode.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DialogPhoneCode.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }

    public class ValidCode extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private HashMap hashMap;

        public ValidCode(String code) {
            hashMap = new HashMap();
//            http://neublick.com/demo/carlocation/act.php?i=1599&act=valid_otp&i=159&o=123456
            hashMap.put("act", "valid_otp");
            hashMap.put("o", code);
            hashMap.put("i", UserUtil.getUserId(DialogPhoneCode.this));
        }

        @Override
        protected void onPreExecute() {
            isValid = true;
            mDialog = ProgressDialog.show(DialogPhoneCode.this, null,
                    "Loading...", true);
            Utils.hiddenKeyboard(DialogPhoneCode.this);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getBoolean("status")) {
                        UserUtil.setPhoneVerification(DialogPhoneCode.this, true);
                        Toast.makeText(DialogPhoneCode.this, jsonObject.getString("error_description"), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(DialogPhoneCode.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(DialogPhoneCode.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DialogPhoneCode.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
            isValid = false;
            super.onPostExecute(s);
        }
    }

}
