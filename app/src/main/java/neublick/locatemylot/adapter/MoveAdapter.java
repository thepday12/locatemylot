package neublick.locatemylot.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.model.DetailMoveObject;


public class MoveAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<DetailMoveObject> mListItem;
    private ViewHoler mHoler;

    public MoveAdapter(Context context, List<DetailMoveObject> lstMenu) {
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
        final DetailMoveObject object = mListItem.get(position);
        mHoler.tvMove.setText(object.getText());
        if(position > 0) {
            mHoler.tvMove.setTextColor(Color.CYAN);
        }else{
            mHoler.tvMove.setTextColor(Color.MAGENTA);
        }
//        view.setOnClickListener(new OnSingleClickListener() {
//            @Override
//            public void onSingleClick(View v) {
//                String dir = Global.MY_DIR + object.getMapName();
//                File fileMap = new File(dir);
//                if(fileMap.exists()){
//                    Intent intent = new Intent(mContext, DetailMoveActivity.class);
//                    intent.putExtra(Global.EXTRA_DATA, new Gson().toJson(object));
//                    mContext.startActivity(intent);
//                }else{
//                    Toast.makeText(mContext, "Map not avaible", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        return view;
    }

    private class ViewHoler {
        TextView tvMove;
        RelativeLayout rlMain;

    }
    public void changeNewData( List<DetailMoveObject> lstMenu){
        mListItem =lstMenu;
        notifyDataSetChanged();
    }

}
