package neublick.locatemylot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.model.Carpark;

/**
 * Created by Thep on 9/15/2015.
 */
public class CarparkAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Carpark> mListItem;
    private ViewHoler mHoler;

    public CarparkAdapter(Context context, List<Carpark> lstMenu) {
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
            view = mInflater.inflate(R.layout.row_car_park, null);
            mHoler = new ViewHoler();
            mHoler.tvCarparkName = (TextView) view.findViewById(R.id.tvCarparkName);
            view.setTag(mHoler);
        } else {
            mHoler = (ViewHoler) view.getTag();
        }
        final Carpark carpark = mListItem.get(position);
        mHoler.tvCarparkName.setText(carpark.name);
        return view;
    }

    private class ViewHoler {
        TextView tvCarparkName;
    }

}
