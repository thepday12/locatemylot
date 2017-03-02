package neublick.locatemylot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.util.PromotionUtil;

public class PromotionListAdapter extends BaseAdapter {

	Context context;
	LayoutInflater inflater;
	List<Promotion> dataItems;

	public PromotionListAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		dataItems = new ArrayList<Promotion>();
	}

	public void replaceWith(List<Promotion> newData) {
		dataItems = newData;
		notifyDataSetChanged();
	}

	@Override public int getCount() {
		return dataItems.size();
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public Object getItem(int position) {
		return dataItems.get(position);
	}

	@Override public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			view = inflater.inflate(R.layout.promotion_list_item, parent, false);
		}
		Promotion item = (Promotion)getItem(position);
		ImageView promotionImage = (ImageView)view.findViewById(R.id.promotion_list_item);
		Picasso.with(context).load(PromotionUtil.getImageFileFromFileName(item.promotionThumb)).placeholder(R.drawable.loading).error(R.drawable.download_error).into(promotionImage);
		return view;
	}
}