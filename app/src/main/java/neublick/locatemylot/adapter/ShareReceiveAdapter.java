package neublick.locatemylot.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.activity.DetailImageActivity;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLShareReceive;
import neublick.locatemylot.model.ShareReceiveObject;


public class ShareReceiveAdapter extends RecyclerView
        .Adapter<ShareReceiveAdapter
        .DataObjectHolder> {
    private List<ShareReceiveObject> mDataSet;
    private Context mContext;

    static class DataObjectHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ProgressBar pbLoading;
        Picasso picasso;
        TextView tvTimeReceived,tvFrom,tvType;
        ImageView ivDelete;
        View view;

        DataObjectHolder(View itemView) {
            super(itemView);
            view = itemView;
            picasso = Picasso.with(itemView.getContext());
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            pbLoading = (ProgressBar) itemView.findViewById(R.id.pbLoading);
            tvTimeReceived = (TextView) itemView.findViewById(R.id.tvTimeReceived);
            tvFrom = (TextView) itemView.findViewById(R.id.tvFrom);
            tvType = (TextView) itemView.findViewById(R.id.tvType);
            ivDelete = (ImageView) itemView.findViewById(R.id.ivDelete);
        }

        public Picasso getPicasso() {
            if (picasso == null) {
                picasso = Picasso.with(itemView.getContext());
            }
            return picasso;
        }
    }


    public ShareReceiveAdapter(List<ShareReceiveObject> myDataSet) {
        mDataSet = myDataSet;

    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_share_receive, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        mContext = view.getContext();
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        final ShareReceiveObject object = mDataSet.get(position);
        ViewGroup.LayoutParams layoutParams = holder.view.getLayoutParams();

        holder.view.setLayoutParams(layoutParams);

        final String imageUrl = object.getImageUrl();
        loadSelfie(holder,imageUrl);
        final String fromUserName = "From: "+object.getUserFromName();
        holder.tvFrom.setText(fromUserName);
        String dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(object.getTime()));

        holder.tvTimeReceived.setText("Received: "+dateString);
        switch (object.getType()){
            case Global.TYPE_SHARE_SCREEN:
                holder.tvType.setText(R.string.title_screen_share);
                break;
            case Global.TYPE_SHARE_CAR_PHOTO:
                holder.tvType.setText(R.string.title_car_photo_share);
                break;
        }
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(object);
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailImageActivity.class);
                intent.putExtra("IMAGE_URL",imageUrl);
                intent.putExtra("FROM",fromUserName);
                mContext.startActivity(intent);
            }
        });

    }

    private void loadSelfie(final DataObjectHolder holder, final String imageUrl) {
        holder.pbLoading.setVisibility(View.VISIBLE);
        if(imageUrl.isEmpty()){
            holder.ivPhoto.setImageResource(R.drawable.no_image);
        }else {
            holder.picasso.load(imageUrl).error(R.drawable.no_image).into(holder.ivPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    holder.pbLoading.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError() {
                    holder.pbLoading.setVisibility(View.INVISIBLE);
//                loadSelfie(holder,imageUrl);
                }
            });
        }
    }

    @Override
    public void onViewRecycled(DataObjectHolder holder) {
        holder.getPicasso().cancelRequest(holder.ivPhoto);
        super.onViewRecycled(holder);
    }



    public void removeItem(ShareReceiveObject shareReceiveObject) {
       int itemId = shareReceiveObject.getId();
        for(int i =0;i<mDataSet.size();i++){
            if(itemId == mDataSet.get(i).getId()){
                mDataSet.remove(i);
                notifyItemRemoved(i);
                CLShareReceive.deleteItem(itemId);
                break;
            }
        }

    }



    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}