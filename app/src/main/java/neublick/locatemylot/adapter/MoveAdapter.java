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

import neublick.locatemylot.R;
import neublick.locatemylot.model.Carpark;


public class MoveAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mListItem;
    private ViewHoler mHoler;

    public MoveAdapter(Context context, List<String> lstMenu) {
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
            view = mInflater.inflate(R.layout.row_move, null);
            mHoler = new ViewHoler();
            mHoler.tvMove = (TextView) view.findViewById(R.id.tvMove);
            view.setTag(mHoler);
        } else {
            mHoler = (ViewHoler) view.getTag();
        }
        final String supportMove = mListItem.get(position);
        mHoler.tvMove.setText(supportMove);
        return view;
    }

    private class ViewHoler {
        TextView tvMove, tvSlot;
    }

}
