package neublick.locatemylot.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.util.BitmapUtil;
import neublick.locatemylot.util.ImagePicker;
import neublick.locatemylot.util.UserUtil;
import neublick.locatemylot.util.Utils;

import static neublick.locatemylot.app.Global.isUpdateInfo;

public class UpdateInfoActivity extends AppCompatActivity {

    private final int RESULT_SELECT_IMAGE = 21;
    private CircleImageView ivAvatar;
    private EditText etName, etPhone, etIU;
    private Uri selectedImage;
    private String userId;
    private String userAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        ivAvatar= (CircleImageView) findViewById(R.id.ivAvatar);
        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etIU = (EditText) findViewById(R.id.etIU);
        Button btUpdate = (Button) findViewById(R.id.btUpdate);
        Button btClose = (Button) findViewById(R.id.btClose);

        init();


        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isInternetConnected(UpdateInfoActivity.this)) {
                    String displayName = etName.getText().toString();
                    String phone = etPhone.getText().toString();
                    String iuNumber = etIU.getText().toString();
                    if(displayName.isEmpty()){
                        etName.setError(getString(R.string.edit_text_null_value));
                    }else if(phone.isEmpty()){
                        etPhone.setError(getString(R.string.edit_text_null_value));
                    }else if(iuNumber.isEmpty()){
                        etIU.setError(getString(R.string.edit_text_null_value));
                    }else {
                        new UpdateInfo(userId, displayName, iuNumber, phone).execute();
                    }
                }else{
                    Toast.makeText(UpdateInfoActivity.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(UpdateInfoActivity.this);
                startActivityForResult(chooseImageIntent, RESULT_SELECT_IMAGE);
            }
        });
    }

    private void init(){
        userId = UserUtil.getUserId(UpdateInfoActivity.this);
        String fullName = UserUtil.getUserFullName(UpdateInfoActivity.this);

        String  phone = UserUtil.getUserPhone(UpdateInfoActivity.this);
        String  iuNumber = UserUtil.getIUNumber(UpdateInfoActivity.this);


            if (fullName.isEmpty()) {
                fullName = getString(R.string.anonymous_user);
            }

            ivAvatar.setVisibility(View.VISIBLE);

            loadAvatar();
            etName.setText(fullName);
            etPhone.setText(phone);
            etIU.setText(iuNumber);
    }

    public void loadAvatar() {
        userAvatar = UserUtil.getAvatar(UpdateInfoActivity.this);
        Utils.loadAvatar(UpdateInfoActivity.this,ivAvatar,userAvatar);
//        if (userAvatar.isEmpty()) {
//            Picasso.with(UpdateInfoActivity.this).load(R.drawable.default_avatar).memoryPolicy(MemoryPolicy.NO_CACHE).into(ivAvatar, new Callback() {
//                @Override
//                public void onSuccess() {
//                    ivAvatar.setBackgroundColor(Color.parseColor("#2dcc70"));
//                }
//
//                @Override
//                public void onError() {
//                      ivAvatar.setBackgroundColor(Color.parseColor("#2dcc70"));
//                }
//            });
//        } else {
//            Picasso.with(UpdateInfoActivity.this).load(userAvatar).error(R.drawable.default_avatar).memoryPolicy(MemoryPolicy.NO_CACHE).into(ivAvatar, new Callback() {
//                @Override
//                public void onSuccess() {
//                      ivAvatar.setBackgroundColor(Color.parseColor("#2dcc70"));
//                }
//
//                @Override
//                public void onError() {
//                      ivAvatar.setBackgroundColor(Color.parseColor("#2dcc70"));
//                }
//            });
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SELECT_IMAGE:

                if (resultCode == Activity.RESULT_OK) {
                    try {
                        File imageFile = ImagePicker.getTempFile(UpdateInfoActivity.this);
                        boolean isCamera = (data == null ||
                                data.getData() == null  ||
                                data.getData().toString().contains(imageFile.toString()));
                        if (isCamera) {     /** CAMERA **/
                            selectedImage = Uri.fromFile(imageFile);
                        } else {            /** ALBUM **/
                            selectedImage = data.getData();
                        }
                        Picasso.with(UpdateInfoActivity.this).load(selectedImage.toString()).error(R.drawable.default_avatar).into(ivAvatar, new Callback() {
                            @Override
                            public void onSuccess() {
                                  ivAvatar.setBackgroundColor(Color.parseColor("#2dcc70"));
                            }

                            @Override
                            public void onError() {
                                  ivAvatar.setBackgroundColor(Color.parseColor("#2dcc70"));
                            }
                        });
                        userAvatar = selectedImage.toString();

                    } catch (Exception e) {

                    }
                }
                break;
        }

    }

    class UpdateInfo extends AsyncTask<Void, Void, String> {
        private String mUserId;
        private String mIUNumber;
        private String mDisplayName;
        private String mPhone;
        private int reqWidth, reqHeight, MAX_IMAGE_SIZE;
        private ProgressDialog mDialog;
        public UpdateInfo(String userId, String displayName, String iuNumber, String phone) {
            mUserId = userId;
            mIUNumber = iuNumber;
            mDisplayName = displayName;
            mPhone= phone;
            reqWidth = ivAvatar.getWidth()*2;
            reqHeight = ivAvatar.getHeight()*2;
            MAX_IMAGE_SIZE = reqWidth;
            if (reqHeight > reqWidth)
                MAX_IMAGE_SIZE = reqHeight;
        }

        @Override
        protected void onPreExecute() {
            isUpdateInfo = true;
            mDialog = ProgressDialog.show(UpdateInfoActivity.this, null,
                    "Loading..", true);
            super.onPreExecute();
        }

        // chi co 1 tham so dau vao, la username can share-location :))
        // args[0] la username can chia se location
        @Override
        public String doInBackground(Void... args) {
            String avatar = "";
            try {

                if (!userAvatar.isEmpty() && !avatar.startsWith("http")) {
                    selectedImage = Uri.parse(userAvatar);
                    if (selectedImage != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inDither = true;
                        BitmapFactory.decodeFile(BitmapUtil.convertUri2FileUri(userAvatar), options);
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
            hashMap.put("p", mPhone);
            hashMap.put("iu", mIUNumber);
            hashMap.put("fn", mDisplayName);
            if(!avatar.isEmpty())
                hashMap.put("a", avatar);

            return Utils.getResponseFromUrlNoEncode(link, hashMap);
        }

        @Override
        public void onPostExecute(String result) {
            isUpdateInfo = false;
            mDialog.dismiss();
            if (result == null || result.isEmpty()) {
                return;
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("state")) {
                        UserUtil.setIUNumber(UpdateInfoActivity.this, mIUNumber);
                        UserUtil.setIUNumberTMP(UpdateInfoActivity.this, "");
                        UserUtil.setUserFullName(UpdateInfoActivity.this, mDisplayName);
                        UserUtil.setUserFullNameNew(UpdateInfoActivity.this, "");
                        UserUtil.setUserPhone(UpdateInfoActivity.this, mPhone);
                        String avatar = jsonObject.getString("avatar");
                        if(!avatar.isEmpty()) {
                            UserUtil.setAvatar(UpdateInfoActivity.this, avatar);
                            Utils.saveAvatar(avatar,UpdateInfoActivity.this);
//                            Picasso.with(UpdateInfoActivity.this).invalidate(avatar);
//                            Picasso.with(UpdateInfoActivity.this).load(avatar).error(R.drawable.default_avatar).into(ivAvatar);
                        }
//                        Intent intent = new Intent(Global.UPDATE_INFO_BROADCAST_KEY);
//                        sendBroadcast(intent);
                        finish();
                    } else {
                        Toast.makeText(UpdateInfoActivity.this, jsonObject.getString("error_description"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                }


            }
        }
    }

}
