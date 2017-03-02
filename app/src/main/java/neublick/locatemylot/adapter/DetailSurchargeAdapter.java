package neublick.locatemylot.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.model.DetailCharge;

/**
 * Created by Thep on 9/15/2015.
 */
public class DetailSurchargeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<DetailCharge> mListItem;
    private ViewHoler mHoler;

    public DetailSurchargeAdapter(Context context, List<DetailCharge> lstMenu) {
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
            view = mInflater.inflate(R.layout.row_detail_surcharge, null);
            mHoler = new ViewHoler();
            mHoler.tvDate = (TextView) view.findViewById(R.id.tvDate);
            mHoler.tvBeginEnd = (TextView) view.findViewById(R.id.tvBeginEnd);
            mHoler.tvRates = (TextView) view.findViewById(R.id.tvRates);
            view.setTag(mHoler);
        } else {
            mHoler = (ViewHoler) view.getTag();
        }
        final DetailCharge detailCharge = mListItem.get(position);
        mHoler.tvDate.setText(detailCharge.getStringDate());
        mHoler.tvRates.setText("$ "+detailCharge.getRates());
        mHoler.tvBeginEnd.setText(detailCharge.getRangeTime());


        return view;
    }

    private class ViewHoler {
        TextView  tvDate, tvRates, tvBeginEnd;
    }

}
