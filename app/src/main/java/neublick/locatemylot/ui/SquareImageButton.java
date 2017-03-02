package neublick.locatemylot.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class SquareImageButton extends ImageButton {

	// original background color
	public int originalBkColor;

	public int pressedBkColor = Color.parseColor("#3498db");

	public MyOnClickListener listener;

	public SquareImageButton(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		setScaleType(ScaleType.FIT_CENTER);

		// save the original background color
		originalBkColor = Color.TRANSPARENT;
		Drawable background = this.getBackground();
		if (background instanceof ColorDrawable) {
			originalBkColor = ((ColorDrawable) background).getColor();
		}

		setOnTouchListener(new CustomizedTouchListener());
	}

	/*
	@Override protected void onMeasure(int specW, int specH) {
		int itemWidth = MeasureSpec.getSize(specW);
		super.setMeasuredDimension(itemWidth, itemWidth- Config.TOOLBAR_GIAM_HEIGHT);
	}
	*/

	@Override protected void onMeasure(int specW, int specH) {
		super.onMeasure(specW, specH);
		lw("SquareImageButton::onMeasure()");
	}

	@Override public void setImageResource(int resId) {
		new AsyncSetImageResourceToImageButton().execute(resId);
	}

	public void superSetImageResource(int resId) {
		super.setImageResource(resId);
	}

	class AsyncSetImageResourceToImageButton extends AsyncTask<Integer, Void, Void> {
		@Override public Void doInBackground(Integer... resIds) {
			superSetImageResource(resIds[0]);
			return null;
		}
	}

	class CustomizedTouchListener implements View.OnTouchListener {
		@Override public boolean onTouch(View view, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				setBackgroundColor(pressedBkColor);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				setBackgroundColor(originalBkColor);
				if (listener != null) {
					listener.onClick(view);
				}
			}
			return true;
		}
	}

	public void setMyOnClickListener(MyOnClickListener listener) {
		this.listener = listener;
	}

	public interface MyOnClickListener {
		void onClick(View view);
	}

	void lw(String s) {
		final String TAG = SquareImageButton.class.getSimpleName();
		android.util.Log.w(TAG, s);
	}
}