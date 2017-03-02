package neublick.locatemylot.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neublick.locatemylot.R;
import neublick.locatemylot.activity.ParkingHistoryActivity;
import neublick.locatemylot.model.ParkingHistory;
import neublick.locatemylot.ui.RoundedImageView;
import neublick.locatemylot.util.Utils;

public class ParkingHistoryAdapter extends BaseAdapter {

    ParkingHistoryActivity activity;
    ArrayList<ParkingHistory> dataItems;
    LayoutInflater inflater;

    public ParkingHistoryAdapter(ParkingHistoryActivity activity) {
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
        dataItems = new ArrayList<ParkingHistory>();
    }

    public void replaceWith(List<ParkingHistory> newData) {
        dataItems = new ArrayList<ParkingHistory>(newData);
    }

    @Override
    public int getCount() {
        return dataItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dataItems.get(position);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.parking_history_item, parent, false);
        }

        final ParkingHistory item = (ParkingHistory) getItem(position);

        CircleImageView photo = (CircleImageView) view.findViewById(R.id.parking_photo);
        ImageView deleteCmd = (ImageView) view.findViewById(R.id.lml_action_history_delete);
        ImageView editCmd = (ImageView) view.findViewById(R.id.lml_action_history_edit);

        deleteCmd.setOnClickListener(activity);
        deleteCmd.setTag(position);
        editCmd.setOnClickListener(activity);
        editCmd.setTag(position);

        final TextView carparkName = (TextView) view.findViewById(R.id.carpark_name);
        final TextView carparkFloor = (TextView) view.findViewById(R.id.carpark_floor);
        final TextView carparkZone = (TextView) view.findViewById(R.id.carpark_zone);

        final TextView checkinDate = (TextView) view.findViewById(R.id.checkin_date);
        final TextView checkinTime = (TextView) view.findViewById(R.id.checkin_time);

        final TextView checkoutDate = (TextView) view.findViewById(R.id.checkout_date);
        final TextView checkoutTime = (TextView) view.findViewById(R.id.checkout_time);
        final TextView tvRates = (TextView) view.findViewById(R.id.tvRates);

        // hien thi anh
        String rates =Utils.formatRates(item.rates);
        tvRates.setText( rates);

//        new DisplayImageViewTask(photo).execute(item.photoName);
        if(item.photoName.isEmpty()){
            Picasso.with(activity).load(R.drawable.no_image).into(photo);
        }else {
            File file = Utils.getImageFile(item.photoName);
            Picasso.with(activity).load(file).error(R.drawable.no_image).into(photo);
        }
        carparkName.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.loadFont(activity, carparkName, "RobotoBold");
                carparkName.setText(item.carparkName);
            }
        }, 10);

        if (item.floor.isEmpty()) {
            carparkFloor.setVisibility(View.GONE);
        } else {
            carparkFloor.setVisibility(View.VISIBLE);
            carparkFloor.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.loadFont(activity, carparkFloor, "RobotoMedium");
                    carparkFloor.setText("Floor " + item.floor);
                }
            }, 10);
        }
        if (item.zone.isEmpty()) {
            carparkZone.setVisibility(View.GONE);
        } else {
            carparkZone.setVisibility(View.VISIBLE);

            carparkZone.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.loadFont(activity, carparkZone, "RobotoMedium");
                    carparkZone.setText("Zone " + item.zone);
                }
            }, 10);
        }

        checkinDate.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.loadFont(activity, checkinDate, "RobotoRegular");
                checkinDate.setText(Utils.getDate(item.timeCheckIn));
            }
        }, 10);

        checkinTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.loadFont(activity, checkinTime, "RobotoRegular");
                checkinTime.setText(Utils.getTime(item.timeCheckIn));
            }
        }, 10);

        checkoutDate.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.loadFont(activity, checkoutDate, "RobotoRegular");
                checkoutDate.setText(Utils.getDate(item.timeCheckOut));
            }
        }, 10);

        checkoutTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.loadFont(activity, checkoutTime, "RobotoRegular");
                checkoutTime.setText(Utils.getTime(item.timeCheckOut));
            }
        }, 10);

        return view;
    }

    // thuc hien cac thao tac
    // lay photoName roi lay ve bitmap da scaled
    class DisplayImageViewTask extends AsyncTask<String, Void, Bitmap> {
        public RoundedImageView imgv;

        public DisplayImageViewTask(RoundedImageView imgv) {
            this.imgv = imgv;
        }

        @Override
        public Bitmap doInBackground(String... photoNames) {
            // lay ve bitmap tu anh goc, thuong la bitmap rat nang

            // file anh
            File file = Utils.getImageFile(photoNames[0]);

            // bitmap ban dau
            Bitmap bitmapOrigin;

            boolean photoExists = file.isFile();

            if (photoExists) {
                bitmapOrigin = BitmapFactory.decodeFile(Utils.getImageFile(photoNames[0]).getAbsolutePath());
            } else {
                bitmapOrigin = BitmapFactory.decodeStream(activity.getResources().openRawResource(R.raw.no_image));
            }

            // tao bitmap thu nho tu bitmap ban dau
            try {
                return Bitmap.createScaledBitmap(bitmapOrigin, 300, 300, false);
            } finally {
                // thu hoi bo nho tu bitmap ban dau
                bitmapOrigin.recycle();
            }
        }

        @Override
        public void onPostExecute(Bitmap result) {
            imgv.setImageBitmap(result);
        }
    }

    void le(String s) {
        final String TAG = ParkingHistoryAdapter.class.getSimpleName();
        android.util.Log.e(TAG, s);
    }
}