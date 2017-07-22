package neublick.locatemylot.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;


import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLADV;
import neublick.locatemylot.model.ADVObject;
import neublick.locatemylot.util.BitmapUtil;
import neublick.locatemylot.util.OnSingleClickListener;


/**
 * Created by Thep on 1/28/2016.
 */
public class ADVFragment extends Fragment {

    public static ADVFragment newInstance(String id, String image, boolean isLocal) {
        ADVFragment fragment = new ADVFragment();
        Bundle args = new Bundle();
        args.putString("ID", id);
        args.putString("IMAGE", image);
        args.putBoolean("IS_LOCAL", isLocal);
        fragment.setArguments(args);
        return fragment;
    }

    private String advId, advImageName, imageUrl;
    private Context mContext;
    private ImageView ivADV;
    private ImageButton btDownload;
    private RelativeLayout rlDownload;
    private AVLoadingIndicatorView pbLoading, pbLoadingSave;
    private boolean isDownloading = false;
    private Dialog dialog;
    /***
     * Tham so xac dinh xem adv local hay ko
     */
    private boolean isLocal = false;

    private GridLayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_adv, container, false);
        mContext = rootView.getContext();
        Bundle bundle = getArguments();
        advId = bundle.getString("ID");
        advImageName = bundle.getString("IMAGE");
        isLocal = bundle.getBoolean("IS_LOCAL", false);
        imageUrl = Config.CMS_URL + "/cms/upload_files/" + advImageName;

        TextView tvVisitWebsite = (TextView) rootView.findViewById(R.id.tvVisitWebsite);
        rlDownload = (RelativeLayout) rootView.findViewById(R.id.rlDownload);
        ivADV = (ImageView) rootView.findViewById(R.id.ivADV);
        btDownload = (ImageButton) rootView.findViewById(R.id.btDownload);
        pbLoading = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoading);
        pbLoadingSave = (AVLoadingIndicatorView) rootView.findViewById(R.id.pbLoadingSave);

        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        tvVisitWebsite.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(mContext, ADVWebActivity.class);
                intent.putExtra(Global.ADV_DATA, advId);
                startActivity(intent);
            }
        });


        if (advImageName.isEmpty()) {
            ivADV.setImageResource(R.drawable.ic_image_error);
            pbLoading.hide();
        } else {
            Picasso.with(mContext).load(imageUrl).error(R.drawable.ic_image_error).into(ivADV, new Callback() {
                @Override
                public void onSuccess() {
                    pbLoading.hide();
                }

                @Override
                public void onError() {
                    pbLoading.hide();
                }
            });
        }

        if (isLocal) {
            btDownload.setImageResource(R.drawable.ic_delete);
            btDownload.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    showDialogDelete();
                }
            });
        } else {
            if (advImageName.isEmpty() || BitmapUtil.imageAdvExist(advImageName)) {
                rlDownload.setVisibility(View.INVISIBLE);
            } else {
                rlDownload.setVisibility(View.VISIBLE);
                btDownload.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (!isDownloading) {
                            new DownloadImageAdv().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                });
            }
        }

        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    public void showDialogDelete() {

        if (dialog != null && dialog.isShowing()) return;

        dialog.setContentView(R.layout.confirm_delete_adv);
//        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        TextView tvTitleModeSaving = (TextView) dialog.findViewById(R.id.tvTitleModeSaving);

        Button btOk = (Button) dialog.findViewById(R.id.btOk);
        Button btCancel = (Button) dialog.findViewById(R.id.btCancel);
        tvTitleModeSaving.setText(Html.fromHtml("Are you sure you want to delete this promotion? <br/><b>Warning: You can't restore deleted</b> "));
        // if button is clicked, close the custom dialog
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapUtil.imageAdvDelete(advImageName);
                CLADV.deleteADV(advId);
                Intent intent = new Intent(Global.UPDATE_VIEW_ADV_KEY);
                mContext.sendBroadcast(intent);
                dialog.dismiss();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    class DownloadImageAdv extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            isDownloading = true;
            btDownload.setEnabled(false);
            btDownload.setVisibility(View.INVISIBLE);
            pbLoadingSave.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pbLoadingSave.hide();
            btDownload.setVisibility(View.VISIBLE);

            if (result) {
                CLADV.addItem(new ADVObject(advId, advImageName));
                btDownload.setImageResource(R.drawable.ic_check);
            } else {
                btDownload.setImageResource(R.drawable.ic_error);
            }
            fadeOutBtDownload();
            isDownloading = false;
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return BitmapUtil.saveImage(imageUrl);
        }
    }

    private void fadeOutBtDownload() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(1000);
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlDownload.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btDownload.startAnimation(fadeOut);
    }


}
