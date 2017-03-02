package neublick.locatemylot.fixbug;


/*
1. Neu Bitmap cua anh qua lon -> Scale bitmap voi scaledWidth = 512;
2. Neu anh bi nguoc -> Xoay anh
3. Luu lai anh
4. Hien thi anh da luu
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import neublick.locatemylot.R;
import neublick.locatemylot.util.Utils;

/*
Tham so vao: fileName
 */
public class TaskShowImage<T extends ImageView> extends AsyncTask<String, Void, Bitmap> {
	Context context;
	T target;

	public TaskShowImage(Context context, T target) {
		this.context = context;
		this.target = target;
	}

	@Override public Bitmap doInBackground(String... photoName) {
		// file anh ban dau co the la rat nang
		// tra ve file anh can hien thi
		File file = Utils.getImageFile(photoName[0]);

		if (!file.isFile()) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
		}

		String absoluteFilePath = file.getAbsolutePath();

		Bitmap bitmapOrigin = BitmapFactory.decodeFile(absoluteFilePath);

		boolean needToSave = false;
		int widthScaled = 512;

		if (bitmapOrigin.getWidth() > widthScaled) {
			bitmapOrigin = Bitmap.createScaledBitmap(bitmapOrigin, widthScaled, widthScaled * bitmapOrigin.getHeight() / bitmapOrigin.getWidth(), false);
			needToSave = true;
		}

		// chinh rotation cho photo
		int rotation = ImageUtil.getImageOrientation(absoluteFilePath);
		if (rotation != 0) {

			Matrix m = new Matrix();
			m.postRotate(rotation);
			bitmapOrigin = Bitmap.createBitmap(bitmapOrigin, 0, 0, bitmapOrigin.getWidth(), bitmapOrigin.getHeight(), m, false);

			needToSave = true;
		}

		if (needToSave) {
			// luu bitmap da scaled va rotated vao file
			File destFile = new File(absoluteFilePath);
			try {
				FileOutputStream fOut = new FileOutputStream(destFile);
				bitmapOrigin.compress(Bitmap.CompressFormat.PNG, 85, fOut);
				fOut.flush();
				fOut.close();
			} catch (IOException ignored) {

			}
		}
		return bitmapOrigin;
	}

	@Override public void onPostExecute(Bitmap result) {
		target.setImageBitmap(result);
	}
}