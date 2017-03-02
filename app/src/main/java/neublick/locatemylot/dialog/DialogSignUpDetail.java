package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

public class DialogSignUpDetail extends AppCompatActivity {
    private EditText email, passwd, passwdAgain, username, etIU, phone;
    private Dialog dialogNotice;
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.9);

        setContentView(R.layout.dialog_signup_detail);

        // set below the setContentView()
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button signUp = (Button) findViewById(R.id.action_sign_up);
        Button cancel = (Button) findViewById(R.id.action_cancel);

        email = (EditText) findViewById(R.id.email);
        passwd = (EditText) findViewById(R.id.passwd);
        passwdAgain = (EditText) findViewById(R.id.passwd_again);
        username = (EditText) findViewById(R.id.username);
        etIU = (EditText) findViewById(R.id.etIU);
        phone = (EditText) findViewById(R.id.phone);
        String iuTmp=UserUtil.getIUNumberTMP(DialogSignUpDetail.this);
        if(iuTmp!=null&&!iuTmp.isEmpty()){
            etIU.setText(iuTmp);
        }
        phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.submit || id == EditorInfo.IME_NULL) {
                    signUpNewUser();
                    return true;
                }
                return false;
            }
        });
        signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpNewUser();
            }
        });

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void signUpNewUser() {
        String tEmail = email.getText().toString().trim();
        String tPasswd = passwd.getText().toString().trim();
        String tPasswdAgain = passwdAgain.getText().toString().trim();
        String tUsername = username.getText().toString().trim();
        String tIu = etIU.getText().toString().trim();
        String tPhone = UserUtil.formatPhone(phone.getText().toString().trim());
        if(!Utils.isValidEmail(tEmail)){
            email.setError(getString(R.string.edit_text_email_invalid));
            email.requestFocus();
            return;
        }
        if (tPasswd.isEmpty()||!tPasswd.equals(tPasswdAgain)) {
            passwdAgain.setError("Password does not match the confirm password");
            passwdAgain.requestFocus();
            return;
        }
        if(tPhone==null||tPhone.isEmpty()){
            phone.setError("Plz enter your phone");
            phone.requestFocus();
            return;
        }

        new SignUpTask().execute(tUsername, tUsername, tPasswd, tEmail, tIu, tPhone);
    }

    // http://neublick.com/demo/carlocation/act.php?act=signup
    // &u=khang&f=KhangTran&p=123456&e=magicalmoon17@gmail.com&n=123&phone=841639777584
    // thu tu cac tham so: userName, fullName, passWord, email, NRIC, phone
    class SignUpTask extends AsyncTask<String, Void, String> {
        String username;
        String password;
        String iuNumber;
        String email;
        private ProgressDialog mDialog;
        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignUpDetail.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }
        @Override
        public String doInBackground(String... args) {
            String link = Config.CMS_URL + "/act.php";
//                    (args[0] != null) ? args[0] : "",
//                    (args[1] != null) ? args[1] : "",
//                    (args[2] != null) ? args[2] : "",
//                    (args[3] != null) ? args[3] : "",
//                    (args[4] != null) ? args[4] : "",
//                    (args[5] != null) ? args[5] : ""
//            );
            username = args[0];
            password = args[2];
            email = args[3];
            iuNumber=args[4];
            HashMap hashMap = new HashMap();
//            hashMap.put("u", String.valueOf(args[0]));
            hashMap.put("u", String.valueOf(args[3]));
            hashMap.put("f", String.valueOf(args[1]));
            hashMap.put("p", String.valueOf(password));
            hashMap.put("e", String.valueOf(email));
            hashMap.put("n", String.valueOf(iuNumber));
            hashMap.put("i", String.valueOf(iuNumber));//IU
            hashMap.put("phone", String.valueOf(args[5]));
            hashMap.put("act", "signup");
//            hashMap.put("carpark_id",  String.valueOf(params[1]));

            return Utils.getResponseFromUrl(link, hashMap);
//            URL url = null;
//            try {
//                url = new URL(link);
//            } catch (MalformedURLException e) {
//
//            }
//            if (url == null) {
//                return null;
//            }
//            BufferedReader reader = null;
//            try {
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                InputStreamReader bis = new InputStreamReader(conn.getInputStream());
//                reader = new BufferedReader(bis);
//                return reader.readLine();
//            } catch (IOException e) {
//                return null;
//            } finally {
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (IOException e) {
//
//                    }
//                }
//            }
        }

        @Override
        public void onPostExecute(String result) {
            mDialog.dismiss();
            if(!result.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        String userId = jsonObject.getString("userid");
                        String phone = jsonObject.getString("phone");
                        String avatar = jsonObject.getString("avatar");
                        new RegisterToken(FirebaseInstanceId.getInstance().getToken(),userId,phone,username,password,iuNumber,email,avatar,jsonObject.getString("error_description")).execute();

                    } else {
                        showDialogDetail(jsonObject.getString("error_description"),false);
//                        toastM(jsonObject.getString("error_description"));
                    }
                } catch (JSONException e) {
                    Toast.makeText(DialogSignUpDetail.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(DialogSignUpDetail.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void toastM(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    class RegisterToken extends AsyncTask<Void, Void, String> {
        private String url;
        private HashMap hashMap;
        private ProgressDialog mDialog;
        private String mUserId;
        private String mPhone;
        private String mUsername;
        private String mPassword;
        private String mIUNumber;
        private String mEmail;
        private String mAvatar;
        private String mErrorDescription;
        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignUpDetail.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        public RegisterToken(String token,String userId,String phone,String username,String password,String iuNumber,String email,String avatar,String errorDescription) {
            url = Config.CMS_URL + "/act.php";
            hashMap = new HashMap();
            hashMap.put("gid", token);
            hashMap.put("act", "register");
//            String user= UserUtil.getUserId(getBaseContext());
            hashMap.put("u", userId);
            mErrorDescription = errorDescription;
            mUserId =userId;
            mPhone=phone;
            mUsername=username;
            mPassword=password;
            mEmail=email;
            mIUNumber=iuNumber;
            mAvatar=avatar;

        }

        @Override
        protected String doInBackground(Void... params) {
            return Utils.getResponseFromUrl(url, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if(!s.isEmpty()){
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if(jsonObject.getBoolean("state")){
//                        UserUtil.setDataLogin(DialogSignUpDetail.this,mUsername,mPassword,mUserId,mPhone,mIUNumber,mEmail,mUsername,mAvatar);
                        if (Global.activityMain != null)
                            Global.activityMain.updateSignInMenu(mUsername);
                        showDialogDetail(mErrorDescription,true);
                    }else{
                        showDialogDetail(jsonObject.getString("error_description"),false);

//                        Toast.makeText(DialogSignUpDetail.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
//                    Global.sendBroadCastSignInSuccess(DialogSignUpDetail.this);
                } catch (JSONException e) {
                    Toast.makeText(DialogSignUpDetail.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }

            }
            super.onPostExecute(s);
        }
    }

    private void showDialogDetail(String mErrorDescription, boolean isClose) {
        if(dialogNotice==null||(dialogNotice!=null&&!dialogNotice.isShowing())) {
            dialogNotice = new Dialog(DialogSignUpDetail.this);
            dialogNotice.setCanceledOnTouchOutside(true);
            dialogNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogNotice.setContentView(R.layout.dialog_ok);
            dialogNotice.getWindow()
                    .setLayout((int) (Utils.getScreenWidth(DialogSignUpDetail.this) * .85), ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogNotice.setCanceledOnTouchOutside(false);
            Button btOk = (Button) dialogNotice.findViewById(R.id.btOk);
            TextView tvTitle = (TextView) dialogNotice.findViewById(R.id.tvTitle);
            TextView tvContent = (TextView) dialogNotice.findViewById(R.id.tvContent);

            tvTitle.setText("LocateMyLot");
            tvContent.setText(mErrorDescription);
            btOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogNotice.dismiss();

                }
            });
            if(isClose) {
                dialogNotice.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
            }
            dialogNotice.show();
        }
    }
}