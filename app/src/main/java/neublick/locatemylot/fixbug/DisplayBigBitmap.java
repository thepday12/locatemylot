package neublick.locatemylot.fixbug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;

import neublick.locatemylot.R;
import neublick.locatemylot.util.Utils;

// Dung de hien thi viewAttached trong LocateMyLotActivity
// Anh hien thi ko bi meo, bien dang
public class DisplayBigBitmap<T extends ImageView> extends AsyncTask<String, Void, Bitmap> {

	Context context;
	T target;

	public static int SCALED_WIDTH = 1024;
	public int scaledHeight = 0;

	public DisplayBigBitmap(Context context, T target) {
		this(context, target, 1024);
	}

	public DisplayBigBitmap(Context context, T target, int scaledW) {
		this.context = context;
		this.target = target;
		SCALED_WIDTH = scaledW;
	}

	@Override protected Bitmap doInBackground(String... photoNames) {
		// file anh ban dau thuong rat nang
		// tra ve file anh can hien thi
		File file = Utils.getImageFile(photoNames[0]);

		// bitmap ban dau
		Bitmap bitmapOrigin;
		Bitmap rotatedBitmap = null;

		boolean photoExists = file.isFile();

		if (photoExists) {
			bitmapOrigin = BitmapFactory.decodeFile(Utils.getImageFile(photoNames[0]).getAbsolutePath());

			// phan tu dong xoay
			Matrix matrix = new Matrix();
			matrix.postRotate(ImageUtil.getImageOrientation(file.getAbsolutePath()));

			rotatedBitmap = Bitmap.createBitmap(bitmapOrigin, 0, 0, bitmapOrigin.getWidth(), bitmapOrigin.getHeight(), matrix, false);

			scaledHeight = SCALED_WIDTH*bitmapOrigin.getHeight()/bitmapOrigin.getWidth();
		} else {
			bitmapOrigin = BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.no_image));
		}

		// tao bitmap thu nho tu bitmap ban dau
		try {

			return Bitmap.createScaledBitmap(rotatedBitmap, SCALED_WIDTH, scaledHeight, false);
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