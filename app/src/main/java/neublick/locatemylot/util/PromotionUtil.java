package neublick.locatemylot.util;

import android.os.AsyncTask;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import neublick.locatemylot.app.Config;

public class PromotionUtil {

	// PHOTO functions
	public static File getImageFileFromFileName(String fileName) {
		File parentDir = new File(Environment.getExternalStorageDirectory(), Config.PROMOTION_IMAGE_DIR);
		parentDir.mkdirs();
		return new File(parentDir, fileName);
	}

	public static String getImageFileNameFromLink(String link) {
		if (link == null) {
			return "";
		}
		int index = link.lastIndexOf("/");
		if (index != -1) {
			return link.substring(index + 1, link.length());
		}
		return link;
	}

	// copy file tu internet sang thu muc lmlPromotion/
	// tra ve NULL neu I/O reading error
	public static class CopyFileFromInternetTask extends AsyncTask<String, Void, String> {

		String outputName;

		public CopyFileFromInternetTask(String outputName) {
			this.outputName = outputName;
			le("output_name = " + outputName);
		}

		@Override public String doInBackground(String... urlString) {
			try {
				URL url = new URL(urlString[0]);
				InputStream source= url.openConnection().getInputStream();
				le("source = " + source);
				le("get_image_file_from_file_name = " + PromotionUtil.getImageFileFromFileName(outputName));
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(PromotionUtil.getImageFileFromFileName(outputName));
					byte[] buffer = new byte[1024];
					int byteRead;
					while((byteRead = source.read(buffer)) != -1) {
						fos.write(buffer, 0, byteRead);
					}
					fos.flush();
				} catch(IOException e) {

				} finally {
					if (source != null) {
						try {
							source.close();
						} catch(IOException ignored) {

						}
					}
					if (fos != null) {
						try {
							fos.close();
						} catch(IOException ignored) {

						}
					}
				}
			} catch(Exception e) {
				return e.getMessage();
			}
			return "";
		}

		@Override public void onPostExecute(String result) {
		}
	}

	public static void le(String fmt) {
		final String TAG = PromotionUtil.class.getSimpleName();
		android.util.Log.e(TAG, fmt);
	}
}