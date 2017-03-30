package neublick.locatemylot.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.85);

        setContentView(R.layout.dialog_share_location);
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

                if (!mParkingSession.isCheckIn() || (mParkingSession.isNormalCheckIn() && !mParkingSession.isCarCheckLocation())) {
                    Utils.showMessage(dialogNotice, "Please check in first to use this function", "Information", DialogShareLocation.this, true);

                } else {

                    // neu user da check in thi cho phep no dung chuc nang nay =))))
                    // share thong tin checkin cai xe cua no =))
                    String userID=phone;
                    if(userID.isEmpty()) {
                        userID = etShareId.getText().toString();
                    }
                    userID.replace(" ", "").trim();
                    if (!userID.isEmpty()) {
                        Log.e("RESPONSE_SERVER", UserUtil.formatPhone(userID) + "_" + UserUtil.getUserPhone(DialogShareLocation.this));
                        if (userID.equals(UserUtil.getUserId(DialogShareLocation.this)) || userID.equals(UserUtil.getUserName(DialogShareLocation.this)) || UserUtil.formatPhone(userID).equals(UserUtil.getUserPhone(DialogShareLocation.this))) {
                            Toast.makeText(DialogShareLocation.this, "You can't share location  with yourself", Toast.LENGTH_SHORT).show();
                        } else {
                            new ShareLocationTask(userID).execute();
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
        private String mUserId;

        public ShareLocationTask(String userid) {
            mUserId = userid;
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
            hashMap.put("to", mUserId);
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
                        CLHintShareLocation.addItem(mUserId);
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
}