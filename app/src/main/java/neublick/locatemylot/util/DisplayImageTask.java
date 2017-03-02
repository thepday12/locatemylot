package neublick.locatemylot.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import neublick.locatemylot.R;

public abstract class DisplayImageTask<T extends ImageView> extends AsyncTask<String, Void, Bitmap> {

	Context context;
	T target;
	public int scaledWidth = 300;
	public int scaledHeight = 300;

	public DisplayImageTask(Context context, T target) {
		this.context = context;
		this.target = target;
	}

	public DisplayImageTask<T> setScaledBitmap(int scaledW, int scaledH) {
		scaledWidth = scaledW;
		scaledHeight = scaledH;
		return this;
	}

	@Override public Bitmap doInBackground(String... photoNames) {
		// file anh ban dau thuong rat nang
		// tra ve file anh can hien thi
		File file = Utils.getImageFile(photoNames[0]);

		// bitmap ban dau
		Bitmap bitmapOrigin;

		boolean photoExists = file.isFile();

		if (photoExists) {
			bitmapOrigin = BitmapFactory.decodeFile(Utils.getImageFile(photoNames[0]).getAbsolutePath());
		} else {
			bitmapOrigin = BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.no_image));
		}

		// tao bitmap thu nho tu bitmap ban dau
		try {
			return Bitmap.createScaledBitmap(bitmapOrigin, scaledWidth, scaledHeight, false);
		} finally {
			// thu hoi bo nho tu bitmap ban dau
			bitmapOrigin.recycle();
		}
	}

	@Override public void onPostExecute(Bitmap result) {
		target.setImageBitmap(result);
		System.gc();
	}
}