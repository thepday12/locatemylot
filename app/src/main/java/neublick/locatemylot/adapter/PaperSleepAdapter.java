package neublick.locatemylot.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import neublick.locatemylot.activity.HelpFragment;
import neublick.locatemylot.activity.SleepFragment;


/**
 * Created by Thep on 1/28/2016.
 */
public class PaperSleepAdapter extends FragmentStatePagerAdapter {

    private int size;
    public PaperSleepAdapter(FragmentManager fm, int size) {
        super(fm);
        this.size=size;
    }

    @Override
    public Fragment getItem(int position) {
        return SleepFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
