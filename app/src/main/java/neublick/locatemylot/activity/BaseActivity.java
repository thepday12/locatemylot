package neublick.locatemylot.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neublick.locatemylot.R;
import neublick.locatemylot.adapter.PaperCarParkTypeAdapter;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.dialog.DialogFeedback;
import neublick.locatemylot.dialog.DialogInformation;
import neublick.locatemylot.dialog.DialogPromotionList;
import neublick.locatemylot.dialog.DialogSetting;
import neublick.locatemylot.dialog.DialogShareLocation;
import neublick.locatemylot.dialog.DialogSignInSignUp;
import neublick.locatemylot.model.CarParkType;
import neublick.locatemylot.ui.SquareImageButton;
import neublick.locatemylot.ui.ToggleSquareImageButton;
import neublick.locatemylot.util.BitmapUtil;
import neublick.locatemylot.util.ParkingSession;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

import static neublick.locatemylot.app.Global.isUpdateInfo;

public class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    DrawerLayout mDrawerLayout;
    Toolbar mToolbar;
    ImageButton mDrawerButton;
    NavigationView mNavigation;
    RelativeLayout mContentMain, rlUpdate;
//    ImageButton btEdit;
    CircleImageView ivAvatar;
    EditText etDisplayName;
    private Dialog dialogNotice;
    private GoogleApiClient mGoogleApiClient;
    public static String CURRENT_MAP = "";
    private Dialog dialogEnterPhone;
    private ParkingSession mParkingSession;
    public String currentDisplayName = "";
    public ViewPager vpCarParkType;
    public List<CarParkType> carParkTypes;
    public LinearLayout llCarParkType;
    public ToggleSquareImageButton btCarParkList, btCarParkNear, btCarParkMap;
    private Uri selectedImage;
    private Dialog dialogChangePassword;
    private EditText etOldPassword, etNewPassword, etConfirmNewPassword;

    @Override
    protected void onPostCreate(Bundle savedState) {
        super.onPostCreate(savedState);

        FacebookSdk.sdkInitialize(BaseActivity.this);
        // cai dat toolbar
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        llCarParkType = (LinearLayout) findViewById(R.id.llCarParkType);
        vpCarParkType = (ViewPager) findViewById(R.id.vpCarParkType);
        carParkTypes = new ArrayList<>();
        carParkTypes.add(new CarParkType(0, R.id.btCarParkList, ""));
        carParkTypes.add(new CarParkType(1, R.id.btCarParkNear, ""));
        carParkTypes.add(new CarParkType(2, R.id.btCarParkMap, ""));

        btCarParkList = (ToggleSquareImageButton) findViewById(R.id.btCarParkList);
        btCarParkNear = (ToggleSquareImageButton) findViewById(R.id.btCarParkNear);
        btCarParkMap = (ToggleSquareImageButton) findViewById(R.id.btCarParkMap);
        vpCarParkType.setAdapter(new PaperCarParkTypeAdapter(getSupportFragmentManager(), carParkTypes));

        dialogChangePassword = new Dialog(BaseActivity.this);
        dialogChangePassword.setCanceledOnTouchOutside(true);
        dialogChangePassword.requestWindowFeature(Window.FEATURE_NO_TITLE);

        vpCarParkType.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateStatusCarParkType(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btCarParkList.setMyOnClickListener(new SquareImageButton.MyOnClickListener() {
            @Override
            public void onClick(View v) {
                vpCarParkType.setCurrentItem(0);
                updateStatusCarParkTypeWithId(v.getId());
            }
        });
        btCarParkNear.setMyOnClickListener(new SquareImageButton.MyOnClickListener() {
            @Override
            public void onClick(View v) {
                vpCarParkType.setCurrentItem(1);
                updateStatusCarParkTypeWithId(v.getId());
            }
        });
        btCarParkMap.setMyOnClickListener(new SquareImageButton.MyOnClickListener() {
            @Override
            public void onClick(View v) {
                vpCarParkType.setCurrentItem(2);
                updateStatusCarParkTypeWithId(v.getId());
            }
        });

        vpCarParkType.setVisibility(View.GONE);
        llCarParkType.setVisibility(View.GONE);
        setSupportActionBar(mToolbar);
        mParkingSession = ParkingSession.getInstanceSharedPreferences(BaseActivity.this);
        mContentMain = (RelativeLayout) findViewById(R.id.content_main);
        dialogEnterPhone = new Dialog(BaseActivity.this);
        dialogEnterPhone.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialogNotice = new Dialog(BaseActivity.this);
        dialogNotice.setCanceledOnTouchOutside(true);
        dialogNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(BaseActivity.this)
                .enableAutoManage(BaseActivity.this /* FragmentActivity */, BaseActivity.this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
        // don't want faded background, set transparent color in this method
//        mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));

//        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                float moveFactor = mNavigation.getWidth() * slideOffset;
//                mContentMain.setTranslationX(-moveFactor);
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
//
//            }
//        });

        mDrawerButton = (ImageButton) findViewById(R.id.drawer_button);
        mDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });


        mNavigation = (NavigationView) findViewById(R.id.navigation);
        // mNavigation.setItemIconTintList();

        mNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_sign_in: {
                        closeDrawer();
                        showSignInSignUp();
                    }
                    break;
                    case R.id.action_sign_out: {
                        closeDrawer();
                        new android.support.v7.app.AlertDialog.Builder(BaseActivity.this, R.style.AppTheme_AlertDialog)
                                .setTitle("Confirm")
                                .setMessage("Do you really want to sign out?")
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        UserUtil.signOut(BaseActivity.this);
                                        if (Global.activityMain != null)
                                            Global.activityMain.updateSignInMenu();
                                        //signOut social
                                        try {
                                            LoginManager.getInstance().logOut();
                                        } catch (Exception ex) {

                                        }
                                        if (mGoogleApiClient.isConnected()) {
                                            try {


                                                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                                                        .setResultCallback(
                                                                new ResultCallback<Status>() {
                                                                    @Override
                                                                    public void onResult(Status status) {
                                                                    }
                                                                });

                                            } catch (Exception ex) {

                                            }
                                        }
                                        startActivity(new Intent(BaseActivity.this, LoadingScreenActivity.class));
                                        finish();
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                    break;
                    case R.id.action_share_location: {
                        if (Global.getCurrentUser().isEmpty()) {
                            Utils.showMessage(dialogNotice, "Please sign in to use this function", "Notice", BaseActivity.this, false);
                        } else if (mParkingSession.isCheckIn() && (mParkingSession.isCarCheckLocation() || !mParkingSession.isNormalCheckIn())) {
                            String phone = UserUtil.getUserPhone(BaseActivity.this);
                            String iuNumber = UserUtil.getIUNumber(BaseActivity.this);
                            if (phone.isEmpty() && !UserUtil.getUserId(BaseActivity.this).isEmpty()) {
                                showDialogEnterPhone(false);
                            } else {
                                showDialogShareLocation();
                            }
                        } else {
                            Utils.showMessage(dialogNotice, "Please check in first to use this function", "Notice", BaseActivity.this, false);
                        }
                    }
                    break;
                    case R.id.action_promotion: {
                        closeDrawer();
//                        version cu
//                        Intent intent = new Intent(BaseActivity.this, DialogPromotionList.class);
//                        startActivity(intent);
                        Intent advIntent = new Intent(BaseActivity.this, ADVActivity.class);
                        advIntent.putExtra(Global.IS_ADV_LOCAL, true);
                        startActivity(advIntent);
                    }
                    break;
                    case R.id.action_setting: {
                        closeDrawer();
                        Intent intent = new Intent(BaseActivity.this, DialogSetting.class);
                        startActivity(intent);
                    }
                    break;
                    case R.id.action_info: {
                        closeDrawer();
                        Intent intent = new Intent(BaseActivity.this, DialogInformation.class);
                        startActivity(intent);
                    }
                    break;
                    case R.id.action_feedback: {
                        closeDrawer();
                        Intent intent = new Intent(BaseActivity.this, DialogFeedback.class);
                        startActivity(intent);
                    }
                    break;
                    case R.id.action_help: {
                        closeDrawer();
//                        getFuncTime().modifyState();
                        ToggleSquareImageButton funcTime = (ToggleSquareImageButton) findViewById(R.id.func_time);
                        if(funcTime!=null){
                            if(funcTime.statePressed) {
                                funcTime.modifyState();
                                vpCarParkType.setVisibility(View.GONE);
                                llCarParkType.setVisibility(View.GONE);
                            }
                        }

                        startActivity(new Intent(BaseActivity.this, HelpActivity.class));
                    }
                    break;
                    case R.id.action_history: {

                        Intent parkingHistoryIntent = new Intent(BaseActivity.this, ParkingHistoryActivity.class);
                        startActivity(parkingHistoryIntent);
                    }
                    break;
                    case R.id.action_change_pass: {
                        showDialogChangePassword();
                    }
                    break;
                }
                return false;
            }
        });

        ivAvatar = (CircleImageView) mNavigation.getHeaderView(0).findViewById(R.id.ivAvatar);
        etDisplayName = (EditText) mNavigation.getHeaderView(0).findViewById(R.id.etDisplayName);
        rlUpdate = (RelativeLayout) mNavigation.getHeaderView(0).findViewById(R.id.rlUpdate);
        loadAvatar();

//        ivAvatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////                startActivityForResult(i, RESULT_SELECT_IMAGE);
//                Intent chooseImageIntent = ImagePicker.getPickImageIntent(BaseActivity.this);
//                startActivityForResult(chooseImageIntent, RESULT_SELECT_IMAGE);
//            }
//        });
        String fullName = UserUtil.getUserFullName(BaseActivity.this);
        if (fullName.isEmpty()) {
            fullName = getString(R.string.anonymous_user);
        }
        currentDisplayName = fullName;

//        btEdit = (ImageButton) mNavigation.getHeaderView(0).findViewById(R.id.btEdit);
//        if (btEdit != null)
//            btEdit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    editDisplayName();
//                }
//            });

    }


    public void loadAvatar() {
        String avatar = UserUtil.getAvatar(BaseActivity.this);
//        if (avatar.isEmpty()) {
//            Picasso.with(BaseActivity.this).load(R.drawable.default_avatar).into(ivAvatar);
//        } else {
//            Picasso.with(BaseActivity.this).load(avatar).error(R.drawable.default_avatar).into(ivAvatar);
//        }
        Utils.loadAvatar(BaseActivity.this,ivAvatar,avatar);
    }



    public void updateStatusCarParkType(int position) {
//        for (CarParkType carParkType : carParkTypes) {
//            ToggleSquareImageButton button = (ToggleSquareImageButton) findViewById(carParkType.getButtonId());
//            if (carParkType.getId() == position) {
//                button.modifyState(true);
//            } else {
//                button.modifyState(false);
//            }
//        }
        updateStatusCarParkTypeWithId(carParkTypes.get(position).getButtonId());
    }

    private void updateStatusCarParkTypeWithId(int buttonId) {
        for (CarParkType carParkType : carParkTypes) {
            ToggleSquareImageButton button = (ToggleSquareImageButton) findViewById(carParkType.getButtonId());
            if (carParkType.getButtonId() == buttonId) {
                button.modifyState(true);
            } else {
                button.modifyState(false);
            }
        }
    }

    public void editDisplayName() {
        if (!etDisplayName.isEnabled()) {
            etDisplayName.setEnabled(true);
            focusEditText(etDisplayName);
        } else {
            String name = etDisplayName.getText().toString();
            if (name.isEmpty()) {
                String fullName = UserUtil.getUserFullName(BaseActivity.this);
                if (fullName.isEmpty()) {
                    fullName = getString(R.string.anonymous_user);
                }
                currentDisplayName = fullName;
                etDisplayName.setText(currentDisplayName);
            } else {
                if (!currentDisplayName.equals(name)) {
                    currentDisplayName = name;
                    UserUtil.setUserFullName(BaseActivity.this, currentDisplayName);
                    UserUtil.setUserFullNameNew(BaseActivity.this, currentDisplayName);
                    updateInfo();
                }
            }
            etDisplayName.setEnabled(false);
        }
    }

    public void updateInfo() {
        if (!isUpdateInfo) {
            String uiNumber = UserUtil.getIUNumber(BaseActivity.this);
            String uiNumberTmp = UserUtil.getIUNumberTMP(BaseActivity.this);
            String fullNameNew = UserUtil.getUserFullNameNew(BaseActivity.this);
            String avatar = UserUtil.getAvatar(BaseActivity.this);
            if (Utils.isInternetConnected(BaseActivity.this) && ((!uiNumberTmp.isEmpty() && uiNumber.isEmpty() || !fullNameNew.isEmpty()) || (!avatar.isEmpty() && !avatar.startsWith("http")))) {
                new UpdateInfo(uiNumberTmp, fullNameNew).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void focusEditText(EditText editText) {
        if (editText.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void showSignInSignUp() {
        Intent intent = new Intent(BaseActivity.this, DialogSignInSignUp.class);
        startActivity(intent);
    }

    public void showDialogEnterPhone(final Boolean isFirstTime) {
        if (dialogEnterPhone.isShowing())
            return;
        dialogEnterPhone.setContentView(R.layout.dialog_enter_phone);
        dialogEnterPhone.setCanceledOnTouchOutside(false);
        TextView tvTitle = (TextView) dialogEnterPhone.findViewById(R.id.tvTitle);
        TextView tvContent = (TextView) dialogEnterPhone.findViewById(R.id.tvContent);
        Button btOk = (Button) dialogEnterPhone.findViewById(R.id.btOk);
        Button btCancel = (Button) dialogEnterPhone.findViewById(R.id.btCancel);

        if (isFirstTime) {
//            tvTitle.setText("");
//            tvContent.setText("");
//            btCancel.setVisibility(View.VISIBLE);
            btCancel.setVisibility(View.GONE);
        } else {
//            tvTitle.setText("Share Your Location");
//            tvContent.setText("Please enter your handphone number to use this function");
            btCancel.setVisibility(View.GONE);
        }

        final EditText etPhone = (EditText) dialogEnterPhone.findViewById(R.id.etPhone);
        // if button is clicked, close the custom dialogEnterPhone
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etPhone.getText().toString();
                if (phone != null && !phone.isEmpty()) {
                    new UpdatePhone(isFirstTime, phone, dialogEnterPhone).execute();
                } else {
                    etPhone.setError(getString(R.string.edit_text_null_value));
                }
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserUtil.setNotShowEnterPhoneAgain(BaseActivity.this, true);
                dialogEnterPhone.dismiss();
            }
        });
        dialogEnterPhone.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    finish();
//                    dialogEnterPhone.dismiss();
                } else {
                    return false;
                }
                return true;
            }
        });
        dialogEnterPhone.show();
    }


    private void showDialogShareLocation() {
        closeDrawer();
        Intent intent = new Intent(BaseActivity.this, DialogShareLocation.class);
        startActivity(intent);
    }
    private void showDialogChangePassword() {

        if (!dialogChangePassword.isShowing()) {
            dialogChangePassword.setContentView(R.layout.dialog_change_password);

            Button btCancel = (Button) dialogChangePassword.findViewById(R.id.btCancel);
            Button btSubmit = (Button) dialogChangePassword.findViewById(R.id.btSubmit);
            etOldPassword = (EditText) dialogChangePassword.findViewById(R.id.etOldPassword);
            etNewPassword = (EditText) dialogChangePassword.findViewById(R.id.etNewPassword);
            etConfirmNewPassword = (EditText) dialogChangePassword.findViewById(R.id.etConfirmNewPassword);


            etConfirmNewPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.submitNewPassword || id == EditorInfo.IME_NULL) {
                        changePassword();
                        return true;
                    }
                    return false;
                }
            });

            btSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePassword();
                }
            });
            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogChangePassword.dismiss();
                }
            });
            dialogChangePassword.show();
        }

    }

    private void hiddenKeyboard(){
//        View view = this.getCurrentFocus();
//        if (view != null) {
            View view = new View(BaseActivity.this);
//        }
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
    }
    private void changePassword() {
        hiddenKeyboard();
        String oldPassword = etOldPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();


        if (oldPassword.length() > 4) {
            if (newPassword.length() > 4) {
                if (newPassword.equals(etConfirmNewPassword.getText().toString())) {
                    if (oldPassword.equals(newPassword)) {
                        etNewPassword.setError(getString(R.string.edit_text_password_is_used));
                        etNewPassword.requestFocus();
                    } else {
                        new ChangePassword(BaseActivity.this, oldPassword, newPassword).execute();
                    }
                } else {
                    etConfirmNewPassword.setError(getString(R.string.edit_text_password_not_match));
                    etConfirmNewPassword.requestFocus();
                }
            } else {
                etNewPassword.setError(getString(R.string.edit_text_password_short));
                etNewPassword.requestFocus();
            }
        } else {
            etOldPassword.setError(getString(R.string.edit_text_password_short));
            etOldPassword.requestFocus();
        }

    }
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }

    void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }
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


    public class UpdatePhone extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private Dialog mDialogEnterPhone;
        private HashMap hashMap;
        private String mPhone;
        private boolean mIsFirstTime;

        public UpdatePhone(boolean isFirstTime, String phone, Dialog dialogEnterPhone) {
            mPhone = UserUtil.formatPhone(phone);
            hashMap = new HashMap();
            hashMap.put("act", "updateinfo");
            hashMap.put("p", mPhone);
            hashMap.put("i", UserUtil.getUserId(BaseActivity.this));
            mIsFirstTime = isFirstTime;
            mDialogEnterPhone = dialogEnterPhone;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(BaseActivity.this, null,
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
                        UserUtil.setUserPhone(BaseActivity.this, mPhone);
                        mDialogEnterPhone.dismiss();
                        if (!mIsFirstTime)
                            showDialogShareLocation();
                    } else {
                        Toast.makeText(BaseActivity.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(BaseActivity.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BaseActivity.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }


    class UpdateInfo extends AsyncTask<Void, Void, String> {
        private String mUserId;
        private String mIUNumber;
        private String mFullName;
        private int reqWidth, reqHeight, MAX_IMAGE_SIZE;

        public UpdateInfo(String iuNumber, String fullName) {
            mUserId = UserUtil.getUserId(BaseActivity.this);
            mIUNumber = iuNumber;
            mFullName = fullName;
            reqWidth = ivAvatar.getWidth();
            reqHeight = ivAvatar.getHeight();
            MAX_IMAGE_SIZE = reqWidth;
            if (reqHeight > reqWidth)
                MAX_IMAGE_SIZE = reqHeight;
        }

        @Override
        protected void onPreExecute() {
            isUpdateInfo = true;
            if (rlUpdate == null) {
                mNavigation = (NavigationView) findViewById(R.id.navigation);
                rlUpdate = (RelativeLayout) mNavigation.getHeaderView(0).findViewById(R.id.rlUpdate);
            }
            rlUpdate.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        // chi co 1 tham so dau vao, la username can share-location :))
        // args[0] la username can chia se location
        @Override
        public String doInBackground(Void... args) {
            String avatar = "";
            try {
                String avat = UserUtil.getAvatar(BaseActivity.this);
                if (!avat.isEmpty() && !avatar.startsWith("http")) {
                    selectedImage = Uri.parse(avat);
                    if (selectedImage != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inDither = true;
                        BitmapFactory.decodeFile(BitmapUtil.convertUri2FileUri(avat), options);
                        options.inSampleSize = BitmapUtil.calculateInSampleSize(options, reqWidth, reqHeight);
                        options.inJustDecodeBounds = false;
                        Bitmap bm = null;

                        bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, options);
                        bm = BitmapUtil.scaleDown(bm, MAX_IMAGE_SIZE, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
                        byte[] byteArrayImage = baos.toByteArray();
                        avatar = "data:image/jpeg;base64," + neublick.locatemylot.util.Base64.encodeBytes(byteArrayImage, Base64.DEFAULT);
                    }
                }
            } catch (Exception e) {
            }
            String link = Config.CMS_URL + "/act.php";
            HashMap hashMap = new HashMap();
            hashMap.put("act", "updateinfo");
            hashMap.put("i", mUserId);
            hashMap.put("p", UserUtil.getUserPhone(BaseActivity.this));
            hashMap.put("iu", mIUNumber);
            hashMap.put("fn", mFullName);
            hashMap.put("a", avatar);

            return Utils.getResponseFromUrlNoEncode(link, hashMap);
        }

        @Override
        public void onPostExecute(String result) {
            isUpdateInfo = false;
            rlUpdate.setVisibility(View.INVISIBLE);
            if (result == null || result.isEmpty()) {
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        if (!mIUNumber.isEmpty()) {
                            UserUtil.setIUNumber(BaseActivity.this, mIUNumber);
                            UserUtil.setIUNumberTMP(BaseActivity.this, "");
                        }
                        UserUtil.setUserFullNameNew(BaseActivity.this, "");
                        UserUtil.setAvatar(BaseActivity.this,jsonObject.getString("avatar"));
                    } else {
                    }
                } catch (JSONException e) {
                }


            }
        }
    }
    class UpdateAvatar extends AsyncTask<Void, Void, String> {
        private String mUserId;
        private int reqWidth, reqHeight, MAX_IMAGE_SIZE;

        public UpdateAvatar() {
            mUserId = UserUtil.getUserId(BaseActivity.this);
            reqWidth = ivAvatar.getWidth();
            reqHeight = ivAvatar.getHeight();
            MAX_IMAGE_SIZE = reqWidth;
            if (reqHeight > reqWidth)
                MAX_IMAGE_SIZE = reqHeight;
        }

        @Override
        protected void onPreExecute() {
            isUpdateInfo = true;
            if (rlUpdate == null) {
                mNavigation = (NavigationView) findViewById(R.id.navigation);
                rlUpdate = (RelativeLayout) mNavigation.getHeaderView(0).findViewById(R.id.rlUpdate);
            }
            rlUpdate.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        // chi co 1 tham so dau vao, la username can share-location :))
        // args[0] la username can chia se location
        @Override
        public String doInBackground(Void... args) {
            String avatar = "";
            try {
                    if (selectedImage != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inDither = true;
                        BitmapFactory.decodeFile(BitmapUtil.convertUri2FileUri(selectedImage.toString()), options);
                        options.inSampleSize = BitmapUtil.calculateInSampleSize(options, reqWidth, reqHeight);
                        options.inJustDecodeBounds = false;
                        Bitmap bm = null;

                        bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, options);
                        bm = BitmapUtil.scaleDown(bm, MAX_IMAGE_SIZE, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
                        byte[] byteArrayImage = baos.toByteArray();
                        avatar = "data:image/jpeg;base64," + neublick.locatemylot.util.Base64.encodeBytes(byteArrayImage, Base64.DEFAULT);
                    }
            } catch (Exception e) {
            }
            String link = Config.CMS_URL + "/act.php";
            HashMap hashMap = new HashMap();
            hashMap.put("act", "updateinfo");
            hashMap.put("i", mUserId);
            hashMap.put("a", avatar);

            return Utils.getResponseFromUrlNoEncode(link, hashMap);
        }

        @Override
        public void onPostExecute(String result) {
            isUpdateInfo = false;
            rlUpdate.setVisibility(View.INVISIBLE);
            if (result == null || result.isEmpty()) {
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        String avatar = jsonObject.getString("avatar");
                        UserUtil.setAvatar(BaseActivity.this,avatar);
                        Picasso.with(BaseActivity.this).invalidate(avatar);
                        Picasso.with(BaseActivity.this).load(avatar).into(ivAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(BaseActivity.this).load( selectedImage.toString()).into(ivAvatar);
                            }
                        });

                    } else {
                    }
                } catch (JSONException e) {
                }


            }
        }
    }

    class ChangePassword extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private String mOldPassword;
        private String mNewPassword;
        private ProgressDialog mDialog;

        public ChangePassword(Context context, String oldPassword, String newPassword) {
            mContext = context;
            mOldPassword = oldPassword;
            mNewPassword = newPassword;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(mContext, null,
                    "Loading..", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String link = Config.CMS_URL + "/act.php";
            HashMap hashMap = new HashMap();
            http://neublick.com/demo/carlocation/act.php?act=changepass&e=talentcat@gmail.com&o=123456&n=1212121
            hashMap.put("act", "changepass");
            hashMap.put("e", UserUtil.getUserEmail(BaseActivity.this));
            hashMap.put("o", mOldPassword);
            hashMap.put("n", mNewPassword);
            return Utils.getResponseFromUrlNoEncode(link, hashMap);
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            if (!s.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getBoolean("state")) {
                        dialogChangePassword.dismiss();
                        hiddenKeyboard();
                        Toast.makeText(BaseActivity.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BaseActivity.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(BaseActivity.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BaseActivity.this, getString(R.string.text_empty_json), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }
}