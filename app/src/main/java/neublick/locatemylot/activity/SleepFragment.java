package neublick.locatemylot.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Global;


/**
 * Created by Thep on 1/28/2016.
 */
public class SleepFragment extends Fragment {

    public static final SleepFragment newInstance(int position) {
        SleepFragment helpFragment = new SleepFragment();
        Bundle args = new Bundle();
        args.putInt(Global.EXTRA_POSITION_FRAGMENT, position);
        helpFragment.setArguments(args);
        return helpFragment;
    }


    private Context mContext;
    private int position;
    private ImageView ivSleep;



    private void showLayout() {
        switch (position) {
            case 0: {
                ivSleep.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Picasso.with(mContext).load(R.drawable.sleep_0).into(ivSleep);
            }
            break;
            case 1: {
                ivSleep.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Picasso.with(mContext).load(R.drawable.sleep_2).into(ivSleep);
            }
            break;
            case 2: {
                ivSleep.setScaleType(ImageView.ScaleType.FIT_START);
//                Picasso.with(mContext).load(R.drawable.sleep_3).into(ivSleep);
                Picasso.with(mContext).load(R.drawable.sleep_4).into(ivSleep);


            }
            break;
            case 3:{
                ivSleep.setScaleType(ImageView.ScaleType.FIT_END);
                Picasso.with(mContext).load(R.drawable.sleep_4).into(ivSleep);


            }
                break;
        }
    }

    public static void showHelp(final View view) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", .0f, 1f);
        fadeIn.setDuration(300);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeIn);
        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.VISIBLE);
            }
        });
        mAnimationSet.start();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.row_sleep, container, false);
        ivSleep = (ImageView) rootView.findViewById(R.id.ivSleep);

        mContext = rootView.getContext();


        position = getArguments().getInt(Global.EXTRA_POSITION_FRAGMENT, 0);
            showLayout();
        return rootView;
    }





}
