package neublick.locatemylot.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import neublick.locatemylot.R;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.util.ParkingSession;
import neublick.locatemylot.util.Utils;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Thep on 1/28/2016.
 */
public class HelpFragment extends Fragment {

    public static final HelpFragment newInstance(int position) {
        HelpFragment helpFragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(Global.EXTRA_POSITION_FRAGMENT, position);
        helpFragment.setArguments(args);
        return helpFragment;
    }


    private Context mContext;
    private int position;
    private RelativeLayout rlHelpMenu,rlHelpCamera;
    private LinearLayout llEntryTime, llTotalDuration, llParkingRates;
//    private RelativeLayout rlFuncWay, rlFuncCheck, rlFuncCamera, rlFuncTime, rlFuncHistory;
    private ImageView ivHelpWay;
    private  CircleImageView ivCamera;
    private ImageView func_way,func_check_in,func_camera,func_time,func_history;
    private ImageView ivEntry, ivDuration, ivParkingRates;
    private TextView textEntryTime, textDuration;
    private TextView entry_time, duration, tvParkingRates;
    private int screenWidth;

    private SharedPreferences toggleState;

    private BroadcastReceiver broadCastReload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showLayout();
        }
    };

    private void showLayout() {
        switch (position) {
            case 0: {
                if (Global.entryTime > 0) {
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
                        entry_time.setText(sdf.format(new Date(Global.entryTime)));
                    }
                } else {
                    entry_time.setText("--:--:--");
                }
                final Typeface typeFace = Typeface.createFromAsset(
                        mContext.getAssets(),
                        "RobotoRegular.ttf"
                );
                textEntryTime.setTypeface(typeFace);
                entry_time.setTypeface(typeFace);
                showHelp(llEntryTime);
                showHelp(ivEntry);
            }
            break;
            case 1: {
                if (Global.entryTime > 0) {
                    long t = System.currentTimeMillis();
                    String time = Utils.makeDurationReadable((int) (t - Global.entryTime) / 1000);
                    duration.setText(time);

                } else {
                    duration.setText("--:--:--");
                }
                final Typeface typeFace = Typeface.createFromAsset(
                        mContext.getAssets(),
                        "RobotoRegular.ttf"
                );
                textDuration.setTypeface(typeFace);
                duration.setTypeface(typeFace);
                showHelp(llTotalDuration);
                showHelp(ivDuration);
            }
            break;
            case 2: {
                if (Global.entryTime > 0) {
                    tvParkingRates.setText(LocateMyLotActivity.CURRENT_RATES_TEXT);
                } else {
                    tvParkingRates.setText("--");
                }
                final Typeface typeFace = Typeface.createFromAsset(
                        mContext.getAssets(),
                        "RobotoRegular.ttf"
                );
                textDuration.setTypeface(typeFace);
                duration.setTypeface(typeFace);
                showHelp(llParkingRates);
                showHelp(ivParkingRates);
            }
            break;
            case 3:
                showHelp(rlHelpMenu);
                break;
            case 5: {
                boolean isCheck = false;
                try {
                    isCheck = Boolean.parseBoolean(toggleState.getString("func_way", "").split(":")[0]);

                } catch (Exception e) {

                }
                if (isCheck)
                    func_way.setBackgroundColor(Color.parseColor("#3F92D7"));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivHelpWay.getLayoutParams();
//                layoutParams.leftMargin = screenWidth / 10*2;

                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                ivHelpWay.setLayoutParams(layoutParams);
                showHelp(func_way);
                showHelp(ivHelpWay);
            }
            break;
            case 4:
            {
                boolean isCheck = false;
                try {
                    isCheck = Boolean.parseBoolean(toggleState.getString("func_check_in", "").split(":")[0]);

                } catch (Exception e) {

                }
                if (isCheck)
                    func_check_in.setBackgroundColor(Color.parseColor("#3F92D7"));
                ivHelpWay.setImageResource(R.drawable.check_in_out_help);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivHelpWay.getLayoutParams();
//                layoutParams.leftMargin = screenWidth / 10;
                ivHelpWay.setLayoutParams(layoutParams);
                showHelp(func_check_in);
                showHelp(ivHelpWay);
            }

                break;
//            case 6:{
//                ivHelpWay.setBackgroundColor(Color.parseColor("#3F92D7"));
//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivHelpWay.getLayoutParams();
//                layoutParams.leftMargin = (int) (screenWidth / 10*2f);
//                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                ivHelpWay.setLayoutParams(layoutParams);
//                ivHelpWay.setScaleType(ImageView.ScaleType.FIT_CENTER);
////                showHelp(rlFuncCamera);
//                showHelp(ivHelpWay);
//            }
//                break;
            case 6:
            {
//                func_time.setBackgroundColor(Color.parseColor("#3F92D7"));
                ivHelpWay.setImageResource(R.drawable.select_carpark_help);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivHelpWay.getLayoutParams();
                layoutParams.rightMargin = (int) (screenWidth / 10);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                ivHelpWay.setLayoutParams(layoutParams);
                ivHelpWay.setScaleType(ImageView.ScaleType.FIT_END);
                showHelp(func_time);
                showHelp(ivHelpWay);
            }
                break;
            case 7:
            {
//                ivHelpWay.setImageResource(R.drawable.history_help);
//                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivHelpWay.getLayoutParams();
//                layoutParams.rightMargin = screenWidth / 10;
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                ivHelpWay.setLayoutParams(layoutParams);
//                ivHelpWay.setScaleType(ImageView.ScaleType.FIT_END);
                ParkingSession mParkingSession = ParkingSession.getInstanceSharedPreferences(getActivity());
                final String fileName = mParkingSession.getPhotoUri();//parkingSession.getString("PHOTO_NAME", "");

                // fileName chac chan khac "" ?!
                if (!fileName.equals("")) {
                    File file = Utils.getImageFile(fileName);
                    Picasso.with(getContext()).load(file).into(ivCamera);
                }
                showHelp(rlHelpCamera);
//                showHelp(ivHelpWay);
            }
                break;
            case 9:
                break;
            case 10:
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
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        rlHelpMenu = (RelativeLayout) rootView.findViewById(R.id.rlHelpMenu);
        rlHelpCamera = (RelativeLayout) rootView.findViewById(R.id.rlHelpCamera);
        llEntryTime = (LinearLayout) rootView.findViewById(R.id.llEntryTime);
        llTotalDuration = (LinearLayout) rootView.findViewById(R.id.llTotalDuration);
        llParkingRates = (LinearLayout) rootView.findViewById(R.id.llParkingRates);
//        rlFuncWay = (RelativeLayout) rootView.findViewById(R.id.rlFuncWay);
//        rlFuncCheck = (RelativeLayout) rootView.findViewById(R.id.rlFuncCheck);
//        rlFuncCamera = (RelativeLayout) rootView.findViewById(R.id.rlFuncCamera);
//        rlFuncTime = (RelativeLayout) rootView.findViewById(R.id.rlFuncTime);
//        rlFuncHistory = (RelativeLayout) rootView.findViewById(R.id.rlFuncHistory);
        textEntryTime = (TextView) rootView.findViewById(R.id.text_entry_time);
        tvParkingRates = (TextView) rootView.findViewById(R.id.tvParkingRates);
        entry_time = (TextView) rootView.findViewById(R.id.entry_time);
        textDuration = (TextView) rootView.findViewById(R.id.text_duration);
        duration = (TextView) rootView.findViewById(R.id.duration);
        ivCamera = (CircleImageView) rootView.findViewById(R.id.ivCamera);
        ivEntry = (ImageView) rootView.findViewById(R.id.ivEntry);
        ivDuration = (ImageView) rootView.findViewById(R.id.ivDuration);
        ivParkingRates = (ImageView) rootView.findViewById(R.id.ivParkingRates);
        ivHelpWay = (ImageView) rootView.findViewById(R.id.ivHelpWay);
        func_way = (ImageView) rootView.findViewById(R.id.func_way);
        func_check_in = (ImageView) rootView.findViewById(R.id.func_check_in);
//        func_camera = (ImageView) rootView.findViewById(R.id.func_camera);
        func_time = (ImageView) rootView.findViewById(R.id.func_time);
//        func_history = (ImageView) rootView.findViewById(R.id.func_history);


        mContext = rootView.getContext();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        toggleState = mContext.getSharedPreferences("toggle_state", MODE_PRIVATE);

        position = getArguments().getInt(Global.EXTRA_POSITION_FRAGMENT, 0);
        mContext.registerReceiver(broadCastReload, new IntentFilter(Global.TAG_HELP_FRAGMENT + position));
        if (position == 0) {
            showLayout();
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(broadCastReload);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


}
