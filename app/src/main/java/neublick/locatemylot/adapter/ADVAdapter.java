package neublick.locatemylot.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import neublick.locatemylot.activity.ADVFragment;
import neublick.locatemylot.model.ADVObject;


/**
 * Created by Thep on 1/28/2016.
 */
public class ADVAdapter extends FragmentStatePagerAdapter {
    private  List<ADVObject> advObjects;
    private Context mContext;
    private boolean isAdvLocal;

    public ADVAdapter(FragmentManager fm, Context context, List<ADVObject> advObjects,boolean isAdvLocal) {
        super(fm);
        mContext = context;
        this.advObjects = advObjects;
        this.isAdvLocal = isAdvLocal;
    }

    @Override
    public Fragment getItem(int position) {
        ADVObject advObject = advObjects.get(position);
        return ADVFragment.newInstance(advObject.getId(),advObject.getImage(),isAdvLocal);
    }

    @Override
    public int getCount() {
        return advObjects.size();
    }


}
