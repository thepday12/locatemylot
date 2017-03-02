package neublick.locatemylot.promotion;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;

import neublick.locatemylot.R;

public class ImageViewHolder extends ViewHolder {
	ImageView promotionImage;
	public ImageViewHolder(View v) {
		super(v);
		promotionImage = (ImageView)v.findViewById(R.id.promotion_image);
	}
}