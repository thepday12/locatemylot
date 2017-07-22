package neublick.locatemylot.util;

import android.os.SystemClock;
import android.view.View;

/***
 * Ham cho phep moi lan click tren view cach nhau toi thieu 0.6 giay
 * Trong cung 1 Activity cac view khong bi anh huong lan nhau
 */
public abstract class OnSingleClickListener implements View.OnClickListener {
    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;
    /**
     * Override onOneClick() instead.
     */
    @Override
    public final void onClick(View v) {
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL)
            return;
        mLastClickTime=currentClickTime;
        onSingleClick(v);
    }

    /***
     * Cho phep click moi 0.6 giay
     * @param v
     */
    public abstract void onSingleClick(View v);


}