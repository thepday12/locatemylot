package neublick.locatemylot.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class RectangleImageButton extends ImageButton {

	// original background color
	public int originalBkColor;

	public int pressedBkColor = Color.BLUE;

	public RectangleImageButton(Context context, AttributeSet attrSet) {
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

	class CustomizedTouchListener implements View.OnTouchListener {
		@Override public boolean onTouch(View view, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				setBackgroundColor(pressedBkColor);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				setBackgroundColor(originalBkColor);
			}
			return true;
		}
	}
}