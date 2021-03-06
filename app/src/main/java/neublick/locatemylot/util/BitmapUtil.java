package neublick.locatemylot.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import neublick.locatemylot.app.Global;

// http://stackoverflow.com/questions/4349075/bitmapfactory-decoderesource-returns-a-mutable-bitmap-in-android-2-2-and-an-immu
// make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
public class BitmapUtil {
    public static String convertUri2FileUri(String uri) {
        return uri.replace("file://", "");
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

	public static Bitmap convertToMutable(Bitmap imgIn) {
		try {
			// this is the file going to use temporally to save the bytes.
			// this file will not be a image, it will store the raw image data.
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

			//Open an RandomAccessFile
			//Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
			//into AndroidManifest.xml file
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			// get the width and height of the source bitmap.
			int width = imgIn.getWidth();
			int height = imgIn.getHeight();
			Bitmap.Config type = imgIn.getConfig();

			//Copy the byte to the file
			//Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
			FileChannel channel = randomAccessFile.getChannel();
			MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
			imgIn.copyPixelsToBuffer(map);

			//recycle the source bitmap, this will be no longer used.
			imgIn.recycle();
			System.gc();// try to force the bytes from the imgIn to be released

			//Create a new bitmap to load the bitmap again. Probably the memory will be available.
			imgIn = Bitmap.createBitmap(width, height, type);
			map.position(0);

			//load it back from temporary
			imgIn.copyPixelsFromBuffer(map);

			//close the temporary file and channel , then delete that also
			channel.close();
			randomAccessFile.close();

			// delete the temp file
			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgIn;
	}

	private static String getFileName(String url){
        try {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            return fileName;
        }catch (Exception e){
            return  "";
        }
    }

    public static boolean imageAdvExist(String fileName){
        File myDir = new File(Global.MY_ADV_DIR);//LocateMyLot
        myDir = new File(myDir, fileName);
        if(myDir.exists()){
            return true;
        }
        return false;
    }
    public static File imageAdvFile(String fileName){
        File myDir = new File(Global.MY_ADV_DIR);//LocateMyLot
        myDir = new File(myDir, fileName);
        return myDir;
    }

    public static boolean imageAdvDelete(String fileName){
        File myDir = new File(Global.MY_ADV_DIR);//LocateMyLot
        myDir = new File(myDir, fileName);
        return myDir.delete();
    }
    public static boolean saveImage(String imageUrl) {
        String fileName = getFileName(imageUrl);
        // Store image to default external storage directory
        try {

            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            File myDir = new File(Global.MY_ADV_DIR);//LocateMyLot
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            myDir = new File(myDir, fileName);
            if (!myDir.exists()) {

                FileOutputStream out = new FileOutputStream(myDir);
                if(fileName.toLowerCase().endsWith(".png")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }else{
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }
                out.flush();
                out.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static Bitmap viewToBitmap(View view,int width,int height) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            //Scale bitmap
            return Bitmap.createScaledBitmap(bitmap,width,height, true);
//            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap resizeImage(File file, int maxWidth, int maxHeight){
// create the options
        BitmapFactory.Options opts = new BitmapFactory.Options();

//just decode the file
        opts.inJustDecodeBounds = false;

        Bitmap bp = null;
        try {
            bp = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
            if(bp==null)
                bp = BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
//        Bitmap bp = BitmapFactory.decodeFile(path, opts);

//get the original size
        int orignalHeight = opts.outHeight;
        int orignalWidth = opts.outWidth;
//initialization of the scale
        int resizeScale = 1;
//get the good scale
        if ( orignalWidth > maxWidth || orignalHeight > maxHeight ) {
            final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
            final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
            resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;

            maxHeight = maxHeight*maxWidth/orignalWidth;
            bp=Bitmap.createScaledBitmap(bp,
                    maxWidth, maxHeight, false);
        }
//put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale;
        opts.inJustDecodeBounds = false;
//get the futur size of the bitmap
        int bmSize = (orignalWidth / resizeScale) * (orignalHeight / resizeScale) * 4;
//check if it's possible to store into the vm java the picture
        if ( Runtime.getRuntime().freeMemory() > bmSize ) {
//decode the file
            try {
                bp = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
            } catch (FileNotFoundException e) {
            }
        }
        return bp;
    }

    public static Bitmap resizeImage(Bitmap bp, int maxWidth, int maxHeight){
        int orignalHeight = bp.getHeight();
        int orignalWidth = bp.getWidth();
//initialization of the scale
//        int resizeScale = 1;
//get the good scale
        if ( orignalWidth > maxWidth || orignalHeight > maxHeight ) {
            final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
            final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
//            resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;

            maxHeight = maxHeight*maxWidth/orignalWidth;
            bp=Bitmap.createScaledBitmap(bp,
                    maxWidth, maxHeight, false);
        }

        return bp;
    }
}