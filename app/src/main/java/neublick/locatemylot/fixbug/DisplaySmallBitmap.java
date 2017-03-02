package neublick.locatemylot.fixbug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;

import neublick.locatemylot.R;
import neublick.locatemylot.util.Utils;

// chi dung de hien thi bitmap nho
public class DisplaySmallBitmap<T extends ImageView> extends AsyncTask<String, Void, Bitmap> {

	Context mContext;
	T mTarget;

	public DisplaySmallBitmap(Context context, T target) {
		mContext = context;
		mTarget = target;
	}

	@Override protected Bitmap doInBackground(String... photoNames) {
		// tra ve file anh can hien thi
		File file = Utils.getImageFile(photoNames[0]);

		boolean photoExists = file.isFile();

		if (photoExists) {
			return BitmapFactory.decodeFile(file.getAbsolutePath());
		} else {
			return BitmapFactory.decodeStream(mContext.getResources().openRawResource(R.raw.no_image));
		}
	}

	@Override public void onPostExecute(Bitmap result) {
		mTarget.setImageBitmap(result);
		System.gc();
	}
}