package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

// Form 18 - Sign In 2
public class DialogSignIn extends Activity {
    Button signIn;
    LinearLayout rootLayout;
    private Dialog dialogForgotPassword;
    private EditText etForgotEmail;
    private Dialog dialogNotice;
    private Dialog dialogResend;
    private EditText etEmail;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.9);

        setContentView(R.layout.dialog_signin);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);

        // set below the setContentView()
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialogForgotPassword = new Dialog(DialogSignIn.this);
        dialogForgotPassword.setCanceledOnTouchOutside(true);
        dialogForgotPassword.requestWindowFeature(Window.FEATURE_NO_TITLE);

        etEmail = (EditText) findViewById(R.id.email);
        final EditText passwd = (EditText) findViewById(R.id.passwd);
        signIn = (Button) findViewById(R.id.action_sign_in);
        Button btCancel = (Button) findViewById(R.id.btCancel);
        TextView tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setPaintFlags(tvForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForgotPassword();
            }
        });

        passwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.submit || id == EditorInfo.IME_NULL) {
                    String sEmail = etEmail.getText().toString();
                    if (sEmail.isEmpty() || passwd.getText().toString().isEmpty()) {
                        toastM("Enter your user name and password");
                        return false;
                    }
                    if (!Utils.isValidEmail(sEmail)) {
                        toastM("Email address is invalid");
                        return false;
                    }
                    new SignInTask().execute(sEmail, passwd.getText().toString());
                    return true;
                }
                return false;
            }
        });
        signIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sEmail = etEmail.getText().toString();
                String password = passwd.getText().toString();
                if (sEmail.isEmpty()) {
                    etEmail.setError(getString(R.string.edit_text_null_value));
                    etEmail.requestFocus();
                } else {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
                        if(password.isEmpty()){
                            passwd.setError(getString(R.string.edit_text_null_value));
                            passwd.requestFocus();
                        }else {
                            new SignInTask().execute(sEmail, password);
                        }
                    } else {
                        etEmail.setError(getString(R.string.edit_text_email_invalid));
                        etEmail.requestFocus();
                    }
                }


            }
        });
        btCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showDialogForgotPassword() {

        if (!dialogForgotPassword.isShowing()) {
            dialogForgotPassword.setContentView(R.layout.dialog_forgot_password);

            Button btCancel = (Button) dialogForgotPassword.findViewById(R.id.btCancel);
            Button btGetPassword = (Button) dialogForgotPassword.findViewById(R.id.btGetPassword);
            etForgotEmail = (EditText) dialogForgotPassword.findViewById(R.id.etEmail);
            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogForgotPassword.dismiss();
                }
            });

            etForgotEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.getPassword || id == EditorInfo.IME_NULL) {
                        getPassword();
                        return true;
                    }
                    return false;
                }
            });

            btGetPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPassword();
                }
            });
            dialogForgotPassword.show();
        }

    }

    private void getPassword() {
        String email = etForgotEmail.getText().toString();

        if (email.isEmpty()) {
            etForgotEmail.setError(getString(R.string.edit_text_null_value));
            etForgotEmail.requestFocus();
        } else {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                new GetPassword(email).execute();
            } else {
                etForgotEmail.setError(getString(R.string.edit_text_email_invalid));
                etForgotEmail.requestFocus();
            }
        }
    }

    // PHAN LOGIN VOI FACEBOOK DE TRONG HAM onPostCreate()
    @Override
    protected void onPostCreate(Bundle savedState) {
        super.onPostCreate(savedState);
    }

    // tham so vao la email va password
    // tra ve string
    class SignInTask extends AsyncTask<String, Void, String> {
        String username;
        String password;
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignIn.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        public String doInBackground(String... args) {
            // args[0] la user, args[1] la passwd
            username = args[0];
            password = args[1];
            String link = Config.CMS_URL + "/act.php";

            HashMap hashMap = new HashMap();
            hashMap.put("u", String.valueOf(username));
            hashMap.put("p", String.valueOf(password));
            hashMap.put("act", "login");
//            hashMap.put("carpark_id",  String.valueOf(params[1]));

            return Utils.getResponseFromUrl(link, hashMap);
//			URL url = null;
//			try {
//				url = new URL(link);
//			} catch(MalformedURLException e) {
//
//			}
//			if (url == null) {
//				return null;
//			}
//			BufferedReader reader = null;
//			try {
//				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//				InputStreamReader bis = new InputStreamReader(conn.getInputStream());
//				reader = new BufferedReader(bis);
//				return reader.readLine();
//			} catch(IOException e) {
//				return null;
//			} finally {
//				if (reader != null) {
//					try {
//						reader.close();
//					} catch(IOException e) {
//
//					}
//				}
//			}
        }

        @Override
        public void onPostExecute(String result) {
            mDialog.dismiss();

            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        String userId = jsonObject.getString("userid");
                        String phone = jsonObject.getString("phone");
                        String iuNumber = jsonObject.getString("iu");
                        String email = jsonObject.getString("email");
                        String fullName = jsonObject.getString("fullname");
                        String avatar = jsonObject.getString("avatar");
                        new RegisterToken(FirebaseInstanceId.getInstance().getToken(), userId, phone, username, password, iuNumber, email, fullName, avatar).execute();

                    } else {
                        String errorCode = jsonObject.getString("error_code");
                        boolean isShowRecent = errorCode.equals("ACC_NOT_ACTIVATED");
                        if(isShowRecent){
                            showDialogResend(jsonObject.getString("error_description"));
                        }else {
                            showDialogDetail(jsonObject.getString("error_description"));
                        }

//                        toastM(jsonObject.getString("error_description"));
                    }
                } catch (JSONException e) {
                    Toast.makeText(DialogSignIn.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(DialogSignIn.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
        }


//			String rs = result.toLowerCase();
//			if (rs.contains("not found")) {
//				toastM("User Not found");
//			} else if (rs.startsWith("Wrong")) {
//				toastM("wrong password");
//			} else if (rs.startsWith("ok:")) {
//                String userId = rs.substring(3);
//				final SharedPreferences user = getSharedPreferences("user", MODE_PRIVATE);
//				user.edit().putString("usr", username.toLowerCase()).putString("pwd", password).putString("user_id",userId).apply();
//				if(Global.activityMain!=null)
//					Global.activityMain.updateSignInMenu(username);
//                toastM("Sign in successfully");
//                Global.sendBroadCastSignInSuccess(DialogSignIn.this);
////				try {
////					new AlertDialog.Builder(DialogSignIn.this)
////							.setTitle("LocateMyLot")
////							.setMessage("Sign in successfully")
////							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
////								@Override
////								public void onClick(DialogInterface dialog, int which) {
////									dialog.dismiss();
////                                    Global.sendBroadCastSignInSuccess(DialogSignIn.this);
////                                    finish();
////								}
////							})
////							.show();
////				}
////				catch(Exception ignore){}
//			}
//			else{
////				Utils.showMessage("Can not sign in", "", DialogSignIn.this);
//                toastM("Can not sign in");
//			}
//		}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Logout From Facebook
     */


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
        private String mFullName;
        private String mAvatar;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignIn.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        public RegisterToken(String token, String userId, String phone, String username, String password, String iuNumber, String email, String fullName, String avatar) {
            url = Config.CMS_URL + "/act.php";
            hashMap = new HashMap();
            hashMap.put("gid", token);
            hashMap.put("act", "register");
//            String user= UserUtil.getUserId(getBaseContext());
            hashMap.put("u", userId);

            mUserId = userId;
            mPhone = phone;
            mUsername = username;
            mPassword = password;
            mIUNumber = iuNumber;
            mEmail = email;
            mFullName = fullName;
            mAvatar = avatar;

        }

        @Override
        protected String doInBackground(Void... params) {
            return Utils.getResponseFromUrl(url, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getBoolean("state")) {
                        Utils.saveAvatar(mAvatar,DialogSignIn.this);
                        UserUtil.setDataLogin(DialogSignIn.this, mUsername, mPassword, mUserId, mPhone, mIUNumber, mEmail, mFullName, mAvatar);
                        if (Global.activityMain != null)
                            Global.activityMain.updateSignInMenu(mUsername);
                        toastM("Sign in successfully");
                        Global.sendBroadCastSignInSuccess(DialogSignIn.this);
                        finish();
                    } else {
                        showDialogDetail(jsonObject.getString("error_description"));

//                        Toast.makeText(DialogSignIn.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(DialogSignIn.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }

            }
            super.onPostExecute(s);
        }
    }

    class GetPassword extends AsyncTask<Void, Void, String> {
        private String mEmail;


        private ProgressDialog mDialog;

        public GetPassword(String email) {
            mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignIn.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
//            http://neublick.com/demo/carlocation/act.php?act=forgetpass&e=talentcat1@gmail.com
            HashMap hashMap = new HashMap();
            hashMap.put("act", "forgetpass");
            hashMap.put("e", mEmail);
//
            return Utils.getResponseFromUrl(Config.CMS_URL + "/act.php", hashMap);
        }

        @Override
        protected void onPostExecute(String result) {
            mDialog.dismiss();
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        Toast.makeText(DialogSignIn.this, "The system will send a temporary password to your email address", Toast.LENGTH_LONG).show();
                        dialogForgotPassword.dismiss();
                    } else {
                            showDialogDetail(jsonObject.getString("error_description"));
//                        Toast.makeText(DialogSignIn.this, jsonObject.getString("error_description"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    connectFailed();
                }

            } else {
                connectFailed();
            }
            super.onPostExecute(result);
        }

        private void connectFailed() {
            Toast.makeText(DialogSignIn.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
        }
    }
    class ReSend extends AsyncTask<Void, Void, String> {
        private String mEmail;


        private ProgressDialog mDialog;

        public ReSend(String email) {
            mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignIn.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
//            http://neublick.com/demo/carlocation/act.php?act=forgetpass&e=talentcat1@gmail.com
            HashMap hashMap = new HashMap();
            hashMap.put("act", "Resend");
            hashMap.put("email", mEmail);
            return Utils.getResponseFromUrl(Config.CMS_URL + "/act.php", hashMap);
        }

        @Override
        protected void onPostExecute(String result) {
            mDialog.dismiss();
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    showDialogDetail(jsonObject.getString("err_description"));
//                        Toast.makeText(DialogSignIn.this, jsonObject.getString("error_description"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    connectFailed();
                }

            } else {
                connectFailed();
            }
            super.onPostExecute(result);
        }

        private void connectFailed() {
            Toast.makeText(DialogSignIn.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
        }
    }



    private void showDialogDetail(String mErrorDescription) {
        if(dialogNotice==null||(dialogNotice!=null&&!dialogNotice.isShowing())) {
            dialogNotice = new Dialog(DialogSignIn.this);
            dialogNotice.setCanceledOnTouchOutside(true);
            dialogNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogNotice.setContentView(R.layout.dialog_ok);
            dialogNotice.getWindow()
                    .setLayout((int) (Utils.getScreenWidth(DialogSignIn.this) * .85), ViewGroup.LayoutParams.WRAP_CONTENT);
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

            dialogNotice.show();
        }
    }
    private void showDialogResend(String mErrorDescription) {
        if(dialogResend==null||(dialogResend!=null&&!dialogResend.isShowing())) {
            dialogResend = new Dialog(DialogSignIn.this);
            dialogResend.setCanceledOnTouchOutside(true);
            dialogResend.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogResend.setContentView(R.layout.dialog_resend_email);
            dialogResend.setCanceledOnTouchOutside(false);
            Button btCancel = (Button) dialogResend.findViewById(R.id.btCancel);
            Button btResend = (Button) dialogResend.findViewById(R.id.btResend);
            TextView tvTitle = (TextView) dialogResend.findViewById(R.id.tvTitle);
            TextView tvContent = (TextView) dialogResend.findViewById(R.id.tvContent);

            tvTitle.setText("LocateMyLot");
            tvContent.setText(mErrorDescription);
            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogResend.dismiss();
                }
            });

            btResend.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = etEmail.getText().toString();
                    if (email.isEmpty()) {
                        etEmail.setError(getString(R.string.edit_text_null_value));
                        etEmail.requestFocus();
                    } else {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            new ReSend(email).execute();
                        } else {
                            etEmail.setError(getString(R.string.edit_text_email_invalid));
                            etEmail.requestFocus();
                        }
                    }

                }
            });

            dialogResend.show();
        }
    }
}