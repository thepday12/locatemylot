package neublick.locatemylot.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.model.DetailCharge;

/**
 * Created by Thep on 9/15/2015.
 */
public class DetailChargeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<DetailCharge> mListItem;
    private ViewHoler mHoler;

    public DetailChargeAdapter(Context context, List<DetailCharge> lstMenu) {
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
            view = mInflater.inflate(R.layout.row_detail_charge, null);
            mHoler = new ViewHoler();
            mHoler.tvType = (TextView) view.findViewById(R.id.tvType);
            mHoler.tvDate = (TextView) view.findViewById(R.id.tvDate);
            mHoler.tvTime = (TextView) view.findViewById(R.id.tvTime);
            mHoler.tvRates = (TextView) view.findViewById(R.id.tvRates);
            view.setTag(mHoler);
        } else {
            mHoler = (ViewHoler) view.getTag();
        }
        final DetailCharge detailCharge = mListItem.get(position);
        mHoler.tvDate.setText(Html.fromHtml("<b>" + detailCharge.getStringDate() + "</b><br/>(" + detailCharge.getRangeTime() + ")"));
        mHoler.tvRates.setText("$ " + detailCharge.getRates());
        mHoler.tvTime.setText(convertTime(detailCharge.getTime()));
        int colorStatus = R.color.first_time_color;
        switch (detailCharge.getType()) {
            case 1:
                colorStatus = R.color.second_time_color;
                break;
            case 2:
                colorStatus = R.color.zone_color;
        }
        mHoler.tvType.setBackgroundResource(colorStatus);

        return view;
    }

    private class ViewHoler {
        TextView tvType, tvDate, tvRates, tvTime;
    }

    private String convertTime(int time) {
        String timeConvert = "";
        if (time < 60) {
            timeConvert = time + " min";
            if (time > 1)
                timeConvert += "s";
        } else {
            int hours = time / 60;
            int mins = time - hours * 60;
            timeConvert = hours + " hour";
            if (hours > 1) {
                timeConvert += "s";
            }
            if (mins > 0) {
                timeConvert += " " + mins + " min";
                if (mins > 1)
                    timeConvert += "s";
            }
        }
        return timeConvert;
    }

}
