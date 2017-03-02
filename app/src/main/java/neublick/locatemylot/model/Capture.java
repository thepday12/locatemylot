package neublick.locatemylot.model;

import android.graphics.Bitmap;
import java.util.Calendar;

public class Capture {

	public int mId;
	public Bitmap mBitmap;
	public Calendar mTime;

	public Capture(Bitmap pBitmap, Calendar pTime) {
		mBitmap = pBitmap;
		mTime = pTime;
	}
}