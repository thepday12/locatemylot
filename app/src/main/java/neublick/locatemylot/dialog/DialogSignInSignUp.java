package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

public class DialogSignInSignUp extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private CallbackManager callbackManager;
    private LoginButton btSignInFacebook;
    private final int RC_SIGN_IN = 100;


    private SignInButton btSignInGoogle;
    private GoogleApiClient mGoogleApiClient;
    private int requestCode = RESULT_CANCELED;
    private boolean isBack = false;

    private BroadcastReceiver broadCastSuccessSignIn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestCode = RESULT_OK;
            finish();
        }
    };//end

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);

        registerReceiver(broadCastSuccessSignIn, new IntentFilter(Global.SIGN_IN_OR_SIGN_UP_SUCCESS));

        facebookSDKInitialize();

        setContentView(R.layout.dialog_choose_signin_signup);


        // set below the setContentView()
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);


        btSignInFacebook = (LoginButton) findViewById(R.id.btSignInFacebook);
        btSignInGoogle = (SignInButton) findViewById(R.id.btSignInGoogle);
        Button signIn = (Button) findViewById(R.id.action_sign_in);
        Button signUp = (Button) findViewById(R.id.action_sign_up);

        facebookButtonAction();
        configSignIn();
        btSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DialogSignInSignUp.this, DialogSignIn.class);
                startActivity(i);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DialogSignInSignUp.this, DialogSignUpDetail.class);
                startActivity(i);
            }
        });


    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("IS_BACK",isBack);
        DialogSignInSignUp.this.setResult(requestCode,returnIntent);
        super.finish();
    }

    private void facebookButtonAction() {
//        btSignInFacebook.setReadPermissions("public_profile");
        btSignInFacebook.setReadPermissions(Arrays.asList(
                "public_profile", "email"));//"user_birthday"
        btSignInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleSignInFacebook(loginResult.getAccessToken(), loginResult.getAccessToken().getUserId());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void facebookSDKInitialize() {
        FacebookSdk.sdkInitialize(DialogSignInSignUp.this);
        callbackManager = CallbackManager.Factory.create();
    }

    private void configSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
    }

    private void handleSignInFacebook(AccessToken accessToken, final String socialId) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.e("RESPONSE_SERVER", object.toString());
                        // Application code
                        String name = "";
                        String link = "";
                        String gender = "";
                        try {
                            name = object.getString("name");
                            JSONObject picture =object.getJSONObject("picture");
                            link = picture.getJSONObject("data").getString("url");// "http://graph.facebook.com/" + socialId + "/picture?width=100&height=100";
                        } catch (JSONException e) {
                        }
                        try {
                            gender = object.getString("gender");
                        } catch (JSONException e) {

                        }

                        String email = "";

                        try {
                            email = object.getString("email");
                        } catch (JSONException e) {
                        }

                        int sex = 0;
                        if (gender.equals("female") || gender.equals("nữ")) {
                            sex = 1;
                        }
                        new SignUpTask(2, socialId, name, email,link).execute();
//                        Log.e("RESPONSE_SERVER", socialId + "_" + name + "_" + sex + "_" + email + "link");
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,gender,birthday,age_range,picture.width(150).height(150)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onBackPressed() {
        isBack = true;
        super.onBackPressed();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            String birthday = "";
            int sex = 0;
            try {
                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                // Signed in successfully, show authenticated UI.
                sex = person.getGender();
                if (person.getBirthday() != null)
                    birthday = person.getBirthday();
            } catch (Exception ex) {

            }
            GoogleSignInAccount acct = result.getSignInAccount();
            String id = acct.getId();
            String email = acct.getEmail();
            String name = acct.getDisplayName();
            String linkAvatar = "";
            try {
                Uri uri = acct.getPhotoUrl();
//                String is = acct.getIdToken();
                if (uri != null)
                    linkAvatar = uri.toString();
            } catch (Exception ex) {
            }

            new SignUpTask(1, id, name, email,linkAvatar).execute();
//            String info = "ID: " + id + "\nEmail: " + email + "\nName: " + name + " - Gender: " + sex + "\nBirthday: " + birthday;
//            Log.e("RESPONSE_SERVER", info);
        } else {
            // Signed out, show unauthenticated UI.
//            Toast.makeText(MainActivity.this, "Không thể đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            if (callbackManager != null)
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(broadCastSuccessSignIn);
        } catch (Exception e) {
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    class SignUpTask extends AsyncTask<String, Void, String> {
        private String username;
        private HashMap hashMap;
        private ProgressDialog mDialog;
        private String iuTmp;
        private String mAvatar;

        /***
         * Sign up task
         *
         * @param type  1: google 2: facebook
         * @param id
         * @param name
         * @param email
         */
        public SignUpTask(int type, String id, String name, String email,String avatar) {
            iuTmp = UserUtil.getIUNumberTMP(DialogSignInSignUp.this);
            hashMap = new HashMap();
            hashMap.put("t", String.valueOf(type));
            hashMap.put("sid", id);
            hashMap.put("sn", name);
            hashMap.put("e", email);
            hashMap.put("act", "signup");
            //empty values
            hashMap.put("u", "");
            hashMap.put("f", "");
            hashMap.put("p", "");
            hashMap.put("n", "");
            hashMap.put("i", iuTmp);
            hashMap.put("phone", "");
            mAvatar=avatar;
            if (name == null || name.isEmpty())
                username = email;
            else
                username = name;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignInSignUp.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        public String doInBackground(String... args) {
            String link = Config.CMS_URL + "/act.php";
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        public void onPostExecute(String result) {
            mDialog.dismiss();
            if (result != null && !result.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state") || jsonObject.getString("error_description").toLowerCase().contains("exists")) {
                        String userId = jsonObject.getString("userid");
                        String phone = jsonObject.getString("phone");
                        String email = jsonObject.getString("email");
                        String fullName = jsonObject.getString("fullname");
                        String iuNumber = jsonObject.getString("iu");
//                        String avatar = jsonObject.getString("avatar");
//                        if(avatar!=null&&!avatar.isEmpty())
//                            mAvatar = avatar;
                            new RegisterToken(FirebaseInstanceId.getInstance().getToken(), userId, phone, username, iuNumber,email,fullName,mAvatar).execute();


                    } else {
                        Toast.makeText(DialogSignInSignUp.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(DialogSignInSignUp.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DialogSignInSignUp.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
        }
    }

    class RegisterToken extends AsyncTask<Void, Void, String> {
        private String url;
        private HashMap hashMap;
        private ProgressDialog mDialog;
        private String mUserId;
        private String mPhone;
        private String mUsername;
        private String mIUNumber;
        private String mEmail;
        private String mFullName;
        private String mAvatar;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogSignInSignUp.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        public RegisterToken(String token, String userId, String phone, String username, String iuNumber, String email, String fullName,String avatar) {
            url = Config.CMS_URL + "/act.php";
            hashMap = new HashMap();
            hashMap.put("gid", token);
            hashMap.put("act", "register");
//            String user= UserUtil.getUserId(getBaseContext());
            hashMap.put("u", userId);

            mUserId = userId;
            mPhone = phone;
            mUsername = username;
            mEmail = email;
            mFullName = fullName;
            mIUNumber = iuNumber;
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
                        UserUtil.setDataLogin(DialogSignInSignUp.this, mUsername, "", mUserId, mPhone, mIUNumber, mEmail, mFullName,mAvatar);
                        if (Global.activityMain != null)
                            Global.activityMain.updateSignInMenu(mUsername);
                        Toast.makeText(DialogSignInSignUp.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                        UserUtil.setLoginSocial(DialogSignInSignUp.this,true);
                        requestCode = RESULT_OK;
                        finish();
                    } else {
                        Toast.makeText(DialogSignInSignUp.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(DialogSignInSignUp.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }

            }
            super.onPostExecute(s);
        }
    }


}