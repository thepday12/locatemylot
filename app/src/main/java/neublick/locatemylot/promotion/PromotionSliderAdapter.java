package neublick.locatemylot.promotion;

import neublick.locatemylot.R;
import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.util.LightweightTimer;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.util.ParkingSession;
import neublick.locatemylot.util.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PromotionSliderAdapter extends RecyclerView.Adapter<ImageViewHolder> implements Runnable {

    Context context;
    LightweightTimer timer;
    FrameLayout parent;
    ImageView loading;

    private int mCarParkId = ParkingSession.DEFAULT_CARPARK_NULL;
    private long lastTimeGetPromotion = 0;
    boolean isStopped = true;
    private Handler mHandler = new Handler();

    //LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(2*1024*1024);

    public PromotionSliderAdapter(Context context) {
        this.context = context;
        dataItems = new ArrayList<Promotion>();
        timer = new LightweightTimer(this, Config.PROMOTION_UPDATE_INTERVAL);
    }

    public PromotionSliderAdapter(Context context, FrameLayout parent) {
        this(context);
        this.parent = parent;
        loading = (ImageView) LayoutInflater.from(context).inflate(R.layout.ic_loading, null);
    }

    public List<Promotion> dataItems;

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.promotion_item_thumb, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        if (dataItems.isEmpty()) {
            return;
        }
        Promotion item = dataItems.get(position % dataItems.size());
        if (holder.promotionImage != null)
            Picasso.with(context).load(item.promotionThumb).placeholder(R.drawable.loading).into(holder.promotionImage);
    }

    public Promotion getItem(int position) {
        if (dataItems.isEmpty()) {
            return new Promotion();
        }
        return dataItems.get(position % dataItems.size());
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void run() {
        android.util.Log.e("PromotionSliderAdapter", "Start PromoUpdateTask " + Calendar.getInstance().SECOND);
        int currentId = Global.currentCarparkID;
        long currentTime = System.currentTimeMillis();
        if (Utils.isInternetConnected(context) && currentId > 0 && mCarParkId != currentId && currentTime - lastTimeGetPromotion > 10000) {
            lastTimeGetPromotion = currentTime;
            /*
			try {
				parent.addView(loading);
			} catch (Exception ignored) {}
			*/
            PromoUpdateTask pTask = new PromoUpdateTask();
            pTask.execute(currentId);
        } else if (!isStopped)
            mHandler.postDelayed(this, Config.PROMOTION_UPDATE_INTERVAL);
    }

    public void onUpdatePromotionTaskFinished(List<Promotion> result) {
        android.util.Log.e("PromotionSliderAdapter", "Stop PromoUpdateTask " + Calendar.getInstance().SECOND);
		/*
		mHandler.postDelayed(new Runnable() {
			@Override public void run() {
				try {
					parent.removeView(loading);
				} catch(Exception ignored) {

				}
			}
		}, 500);
		*/
        if (result != null) {
            dataItems = result;
            notifyDataSetChanged();
        }
        if (!isStopped)
            mHandler.postDelayed(this, Config.PROMOTION_UPDATE_INTERVAL);
    }

    // WRAPPER cho LightweightTimer :-)
    public void start() {
        isStopped = false;
        run();
        //timer.start();
    }

    public void stop() {
        isStopped = true;
        mHandler.removeCallbacks(this);
        //timer.stop();
    }

    // DEBUG
    public void toastMessage(String fmt) {
        Toast.makeText(context.getApplicationContext(), fmt, Toast.LENGTH_SHORT).show();
    }

    private class PromoUpdateTask extends AsyncTask<Integer, Void, List<Promotion>> {
        String promotionContent = "";

        @Override
        public List<Promotion> doInBackground(Integer... carparkIDs) {
            //SystemClock.sleep(3000);
            HashMap hashMap = new HashMap();
//            hashMap.put("carpark_id", String.valueOf(carparkIDs[0]));
//            String tmp = Utils.getResponseFromUrl(Config.CMS_URL + "/h_promotion.php",hashMap);
            String tmp = Utils.getResponseFromUrl(Config.CMS_URL + "/h_promotion.php?carpark_id=" + carparkIDs[0], hashMap);
//			String tmp = Utils.getResponseFromUrl(Config.CMS_URL + "/h_promotion.php?carpark_id=" + carparkIDs[0]);
            if (!tmp.isEmpty())
                promotionContent = tmp;
            else
                return null;
            mCarParkId = carparkIDs[0];
            List<Promotion> result = new ArrayList<Promotion>();
            String lines[] = promotionContent.split("[\\r\\n]+");
            for (int i = 0; i < lines.length; i++)
                if (!lines[i].isEmpty()) {
                    result.add(Promotion.Factory.fromThumb(lines[i]));
                }
            return result;
        }

        @Override
        public void onPostExecute(List<Promotion> result) {
            onUpdatePromotionTaskFinished(result);
        }
    }
}