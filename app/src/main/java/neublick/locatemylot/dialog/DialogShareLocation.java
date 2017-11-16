package neublick.locatemylot.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.LocateMyLotActivity;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLHintShareLocation;
import neublick.locatemylot.util.ParkingSession;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

public class DialogShareLocation extends Activity {
    private ParkingSession mParkingSession;
    private Dialog dialogNotice;
    private final int REQUEST_PERMISSION_READ_CONTACT = 1;
    private final int REQUEST_CODE_PICK_CONTACTS = 2;
    private AutoCompleteTextView etShareId;
    private String contactID;
    private String phone="";
    private int mType;
    private Intent mIntent;
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.85);

        setContentView(R.layout.dialog_share_location);

        mIntent = getIntent();
        mType = mIntent.getIntExtra("TYPE",Global.TYPE_SHARE_LOCATION);

        dialogNotice = new Dialog(DialogShareLocation.this);
        dialogNotice.setCanceledOnTouchOutside(true);
        dialogNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mParkingSession = ParkingSession.getInstanceSharedPreferences(DialogShareLocation.this);
        // set below the setContentView()
        getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, CLHintShareLocation.getAllStringShareLocationHint());
        etShareId = (AutoCompleteTextView) findViewById(R.id.etShareId);
        etShareId.setAdapter(adapter);
        ImageButton btChooseContact = (ImageButton) findViewById(R.id.btChooseContact);
        Button share = (Button) findViewById(R.id.action_share_location);
        Button cancel = (Button) findViewById(R.id.action_cancel);

        btChooseContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPermissionReadContact(DialogShareLocation.this);
            }
        });

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneGetShare=phone;
                if(phoneGetShare.isEmpty()) {
                    phoneGetShare = etShareId.getText().toString();
                }
                phoneGetShare.replace(" ", "").trim();

                /***
                 * Neu phoneGetShare khac rong va khac so dien thoai kiem tra tiep
                 * Voi type = TYPE_SHARE_LOCATION thi user can phai check in truoc do
                 * Voi
                 */
                if (!phoneGetShare.isEmpty()) {
                    if (phoneGetShare.equals(UserUtil.getUserId(DialogShareLocation.this)) || phoneGetShare.equals(UserUtil.getUserName(DialogShareLocation.this)) || UserUtil.formatPhone(phoneGetShare).equals(UserUtil.getUserPhone(DialogShareLocation.this))) {
                        Toast.makeText(DialogShareLocation.this, "You can't share location  with yourself", Toast.LENGTH_SHORT).show();
                    } else {
                        switch (mType) {
                            case Global.TYPE_SHARE_LOCATION:
                                if (!mParkingSession.isCheckIn() || (mParkingSession.isNormalCheckIn() && !mParkingSession.isCarCheckLocation())) {
                                    Utils.showMessage(dialogNotice, "Please check in first to use this function", "Information", DialogShareLocation.this, true);
                                } else {
                                    new ShareLocationTask(phoneGetShare).execute();
                                }
                                break;
                            case Global.TYPE_SHARE_SCREEN:
                                Bitmap bitmap = LocateMyLotActivity.BITMAP_SHARE_SCREEN;

                                if(bitmap!=null) {
                                    new ShareScreen(phoneGetShare).execute();
                                }else {
                                    Toast.makeText(DialogShareLocation.this, "Photo invalid", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case Global.TYPE_SHARE_CAR_PHOTO:
                                Uri imageUri = null;
                                try {
                                    imageUri = Uri.parse(mIntent.getStringExtra("IMAGE_URI"));
                                }catch (Exception e){

                                }
                                if(imageUri!=null){
                                    new ShareCarPhoto(phoneGetShare,imageUri).execute();
                                }else {
                                    Toast.makeText(DialogShareLocation.this, "Photo invalid", Toast.LENGTH_SHORT).show();
                                }
                                break;

                        }
                    }
                }


            }
        });
    }

    private void verifyPermissionReadContact(Activity activity) {
        // Here, thisActivity is the current activity
        int permissionCamera = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_CONTACTS);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_READ_CONTACT);
        } else {
            startContactActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int result = 0;
        if (grantResults.length > 0) {
            for (int grant : grantResults) {
                result += grant;
            }
        } else {
            result = -1;
        }
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_CONTACT: {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    startContactActivity();
                } else {
                    Toast.makeText(this, R.string.denied_read_contact_permission, Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }

    private void startContactActivity() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }

    private String retrieveContactName(Uri uriContact) {
        String contactName = "";
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();
        return contactName;
    }

    private String retrieveContactNumber(Uri uriContact) {

        String contactNumber = "";

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();


        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        return contactNumber;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Uri uriContact = data.getData();
            String name = retrieveContactName(uriContact);
            phone = retrieveContactNumber(uriContact);
//            Bitmap photo = retrieveContactPhoto();
            etShareId.setText(phone);
//            Picasso.with(AddNewActivity.this).load(contactPhotoUri).error(R.drawable.ic_supervisor_account_white_24dp).transform(new CircleTransform()).into(ivWithUser);
//            ivWithUser.setImageBitmap(photo);
        }
    }

    // tham so dau vao la username can share
    // dau ra luon luon la string :))
    class ShareLocationTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private String phoneGetShare;

        public ShareLocationTask(String phoneGetShare) {
            this.phoneGetShare = phoneGetShare;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogShareLocation.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        // chi co 1 tham so dau vao, la username can share-location :))
        // args[0] la username can chia se location
        @Override
        public String doInBackground(Void... args) {

            String usr = UserUtil.getUserId(DialogShareLocation.this);
//            if(usr.isEmpty())
//                usr = UserUtil.getUserName(DialogShareLocation.this);
//			// user chua dang nhap
//			if (usr == "") {
//				return "";
//			}

            // neu user da dang nhap
            int carparkId = mParkingSession.getCarParkCheckIn();//parkingSession.getInt("CARPARK_ID", 1);
            float x = 0;
            float y = 0;
            String zone = "";
            String floor = "";
            if (mParkingSession.isNormalCheckIn()) {
                x = mParkingSession.getX();//parkingSession.getFloat("ORIGINAL_X", 0);
                y = mParkingSession.getY();//parkingSession.getFloat("ORIGINAL_Y", 0);
                zone = mParkingSession.getZone();//parkingSession.getString("ZONE", "");
                floor = mParkingSession.getFloor();//parkingSession.getString("FLOOR", "");
            }
            String link = Config.CMS_URL + "/act.php";
//			 link = String.format(
//					Config.CMS_URL + "/act.php?act=sharelocation"
//				+ "&from=%s&to=%s&carpark_id=%s&x=%s&y=%s&zone=%s&floor=%s&checkintime=%s",
//				usr,
//				args[0],
//				carparkId,
//				x,
//				y,
//				zone,
//				floor,
//					Global.entryTime
//			);
            HashMap hashMap = new HashMap();
            hashMap.put("act", "sharelocation");
            hashMap.put("from", usr);
            hashMap.put("to", phoneGetShare);
            hashMap.put("carpark_id", String.valueOf(carparkId));
            hashMap.put("x", String.valueOf(x));
            hashMap.put("y", String.valueOf(y));
            hashMap.put("zone", zone);
            hashMap.put("floor", floor);
            hashMap.put("checkintime", String.valueOf(Global.entryTime));
            return Utils.getResponseFromUrl(link, hashMap);
        }

        @Override
        public void onPostExecute(String result) {
            mDialog.dismiss();
            if (result == null || result.isEmpty()) {
                Utils.showMessage(dialogNotice, "Cannot share location", "Notice", DialogShareLocation.this, false);
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        CLHintShareLocation.addItem(phoneGetShare);
                        Utils.showMessage(dialogNotice, "Share location successfully", "", DialogShareLocation.this, true);
                    } else {
                        Utils.showMessage(dialogNotice, jsonObject.getString("error_description"), "Notice", DialogShareLocation.this, false);
                    }
                } catch (JSONException e) {
                    Utils.showMessage(dialogNotice, "Can not share location", "Notice", DialogShareLocation.this, false);
                }


            }
        }
    }

    public class ShareScreen extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private String phoneGetShare;

        public ShareScreen(String phoneGetShare) {
            this.phoneGetShare = phoneGetShare;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogShareLocation.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String userId =  UserUtil.getUserId(DialogShareLocation.this);
            HashMap hashMap = new HashMap();
            hashMap.put("act", "sharescreen");
            hashMap.put("to", phoneGetShare);
            hashMap.put("from",userId);
            return Utils.shareScreenBitmap(hashMap,LocateMyLotActivity.BITMAP_SHARE_SCREEN,userId);
        }

        @Override
        protected void onPostExecute(String result) {
            LocateMyLotActivity.BITMAP_SHARE_SCREEN = null;
            mDialog.dismiss();
            if (result == null || result.isEmpty()) {
                Utils.showMessage(dialogNotice, "Cannot share location", "Notice", DialogShareLocation.this, false);
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        CLHintShareLocation.addItem(phoneGetShare);
                        Utils.showMessage(dialogNotice, "Share location successfully", "", DialogShareLocation.this, true);
                    } else {
                        Utils.showMessage(dialogNotice, jsonObject.getString("error_description"), "Notice", DialogShareLocation.this, false);
                    }
                } catch (JSONException e) {
                    Utils.showMessage(dialogNotice, "Can not share location", "Notice", DialogShareLocation.this, false);
                }
            }
            super.onPostExecute(result);
        }
    }
    public class ShareCarPhoto extends AsyncTask<Void, Void, String> {
        private ProgressDialog mDialog;
        private String phoneGetShare;
        private Uri imageUri;

        public ShareCarPhoto(String phoneGetShare,Uri imageUri) {
            this.phoneGetShare = phoneGetShare;
            this.imageUri = imageUri;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(DialogShareLocation.this, null,
                    "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            HashMap hashMap = new HashMap();
            hashMap.put("act", "sharePhotoCar");
            hashMap.put("to", phoneGetShare);
            hashMap.put("from", UserUtil.getUserId(DialogShareLocation.this));
            return Utils.shareCarPhoto(DialogShareLocation.this,hashMap,imageUri);
        }

        @Override
        protected void onPostExecute(String result) {
            LocateMyLotActivity.BITMAP_SHARE_SCREEN = null;
            mDialog.dismiss();
            if (result == null || result.isEmpty()) {
                Utils.showMessage(dialogNotice, "Cannot share location", "Notice", DialogShareLocation.this, false);
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        CLHintShareLocation.addItem(phoneGetShare);
                        Utils.showMessage(dialogNotice, "Share location successfully", "", DialogShareLocation.this, true);
                    } else {
                        Utils.showMessage(dialogNotice, jsonObject.getString("error_description"), "Notice", DialogShareLocation.this, false);
                    }
                } catch (JSONException e) {
                    Utils.showMessage(dialogNotice, "Can not share location", "Notice", DialogShareLocation.this, false);
                }
            }
            super.onPostExecute(result);
        }
    }
}