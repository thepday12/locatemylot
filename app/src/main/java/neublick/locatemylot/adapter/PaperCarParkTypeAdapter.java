package neublick.locatemylot.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import neublick.locatemylot.activity.CarParkListFragment;
import neublick.locatemylot.activity.CarParkMapFragment;
import neublick.locatemylot.activity.CarParkNearFragment;
import neublick.locatemylot.model.CarParkType;


/**
 * Created by Thep on 1/28/2016.
 */
public class PaperCarParkTypeAdapter extends FragmentStatePagerAdapter {
    private List<CarParkType> mSectionObjects;

    public PaperCarParkTypeAdapter(FragmentManager fm, List<CarParkType> sectionObjects) {
        super(fm);
        mSectionObjects = sectionObjects;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return CarParkNearFragment.newInstance(mSectionObjects.get(position).getId());
            case 2:
                return CarParkMapFragment.newInstance(mSectionObjects.get(position).getId());
            default:
                return CarParkListFragment.newInstance(mSectionObjects.get(position).getId());
        }
    }

    @Override
    public int getCount() {
        return mSectionObjects.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mSectionObjects.get(position).getName();
    }
}
