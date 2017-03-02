package neublick.locatemylot.adapter;

/**
 * Created by Thep on 10/18/2015.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import neublick.locatemylot.R;


public class RecyclerSleepAdapter extends RecyclerView
        .Adapter<RecyclerSleepAdapter
        .DataObjectHolder> {
    private List<Integer> mDataset;
    private Context mContext;
    private int screenWidth;

    public RecyclerSleepAdapter(List<Integer> dataset) {
        mDataset = dataset;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        ImageView ivSleep;
        private View view;

        public DataObjectHolder(View itemView) {
            super(itemView);
            view = itemView;
            ivSleep = (ImageView) itemView.findViewById(R.id.ivSleep);
        }

    }


    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_sleep, parent, false);
        mContext = view.getContext();
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {

        Picasso.with(mContext).load(mDataset.get(position)).into(holder.ivSleep, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
        if(position<2){
            holder.ivSleep.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }else if(position==2){
            holder.ivSleep.setScaleType(ImageView.ScaleType.FIT_START);
        }else {
            holder.ivSleep.setScaleType(ImageView.ScaleType.FIT_END);

        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}