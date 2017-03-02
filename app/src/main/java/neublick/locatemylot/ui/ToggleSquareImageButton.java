package neublick.locatemylot.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import neublick.locatemylot.R;

public class ToggleSquareImageButton extends SquareImageButton {

	public boolean statePressed;
	public boolean automaticUpdate;

	public ToggleSquareImageButton(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		setOnTouchListener(new CustomizedTouchListener());
	}

	class CustomizedTouchListener implements View.OnTouchListener {
		@Override public boolean onTouch(View view, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (automaticUpdate) {
					modifyState();
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (listener != null) {
					listener.onClick(view);
				}
			}
			return true;
		}
	}

	public void modifyState() {
		toggleState();
		updateBackgroundColor();
	}

	public void modifyState(boolean isPressed) {
		statePressed=isPressed;
		updateBackgroundColor();
	}

	void toggleState() {
		statePressed = !statePressed;

	}

	public void updateBackgroundColor() {
            if (statePressed) {
                if (getId() == R.id.func_check_in) {
                    superSetImageResource(R.drawable.icon_check_out);
                }
                super.setBackgroundColor(pressedBkColor);

            } else {
                if (getId() == R.id.func_check_in) {
                    superSetImageResource(R.drawable.icon_check_in);
                }
                super.setBackgroundColor(originalBkColor);
            }


	}

	public String saveState() {
		return String.format("%s:%s", statePressed, automaticUpdate);
	}

	public void restoreState(String savedState) {
		String[] ss = savedState.split("false:false");
		try {
			statePressed = Boolean.parseBoolean(ss[0]);
			automaticUpdate = Boolean.parseBoolean(ss[1]);
			if (statePressed) {
				super.setBackgroundColor(pressedBkColor);
			} else {
				super.setBackgroundColor(originalBkColor);
			}
		} catch(Exception e) {

		}
	}

	void lw(String s) {
		final String TAG = ToggleSquareImageButton.class.getSimpleName();
		android.util.Log.w(TAG, s);
	}
}