package neublick.locatemylot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import neublick.locatemylot.R;
import neublick.locatemylot.model.Carpark;

/**
 * Created by Thep on 9/15/2015.
 */
public class CarParkListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Carpark> mListItem;
    private ViewHoler mHoler;

    public CarParkListAdapter(Context context, List<Carpark> lstMenu) {
        this.mContext = context;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListItem = lstMenu;
    }

    @Override
    public int getCount() {
        return mListItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        if (view == null) {
            view = mInflater.inflate(R.layout.row_car_park_list, null);
            mHoler = new ViewHoler();
            mHoler.tvCarparkName = (TextView) view.findViewById(R.id.tvCarparkName);
            mHoler.tvSlot = (TextView) view.findViewById(R.id.tvSlot);
            view.setTag(mHoler);
        } else {
            mHoler = (ViewHoler) view.getTag();
        }
        final Carpark carpark = mListItem.get(position);
        mHoler.tvCarparkName.setText(carpark.name);
        int lot = carpark.lot;
        if (lot == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mHoler.tvSlot.setTextColor(mContext.getColor(R.color.red_dark));
            } else {
                mHoler.tvSlot.setTextColor(Color.parseColor("#cc0000"));
            }
            mHoler.tvSlot.setText("FULL");

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mHoler.tvSlot.setTextColor(mContext.getColor(R.color.blue));
            } else {
                mHoler.tvSlot.setTextColor(Color.parseColor("#3498db"));
            }
            if (lot > 0) {
                mHoler.tvSlot.setText("" + lot);
            } else {
                mHoler.tvSlot.setText("-");
            }
        }

        return view;
    }

    private class ViewHoler {
        TextView tvCarparkName, tvSlot;
    }

}
