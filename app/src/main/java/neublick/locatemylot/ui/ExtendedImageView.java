package neublick.locatemylot.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

// ImageView luu lai tham so no quay 1 goc bao nhieu khi dung ham setRotation()
public class ExtendedImageView extends ImageView {
	public float alpha;
	public ExtendedImageView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	@Override public void setRotation(float alpha) {
		super.setRotation(alpha);
		this.alpha = alpha;
	}
}