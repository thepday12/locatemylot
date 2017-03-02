package neublick.locatemylot.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import neublick.locatemylot.R;
import neublick.locatemylot.app.Config;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLPromotion;
import neublick.locatemylot.io.IOResult;
import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.util.Utils;

public class DialogSavePromotionOrSeePoster extends Activity {

	TextView promotionContent;
	Promotion promotionDetail;
	Promotion promotionThumb;
	Button buttonSave;
	Button buttonClose;

    private Dialog dialogNotice;
	// activityType = 0 => khong hien thi duoc promotion
	// activityType = 1 => save promotion
	// activityType = 2 => see promotion
	int activityType;

	public TextView getPromotionContent() {
		if (promotionContent == null) {
			promotionContent = (TextView)findViewById(R.id.promotion_content);
		}
		return promotionContent;
	}

	public Button getButtonSave() {
		if (buttonSave == null) {
			buttonSave = (Button)findViewById(R.id.action_save_promotion);
		}
		return buttonSave;
	}

	public Button getButtonClose() {
		if (buttonClose == null) {
			buttonClose = (Button)findViewById(R.id.action_close);
		}
		return buttonClose;
	}

	void onGetPromotionDetailTaskFinished(PromotionResult result)
	{

			if (result != null) {
				promotionDetail = result.promotion;
				if (promotionDetail != null) {
					if (promotionDetail.id != Promotion.DEFAULT_ID) {
						if (promotionDetail.isPromotion == 1) {
							if (promotionThumb != null)
								promotionDetail.mergeWithPromotionThumb(promotionThumb);
							activityType = 1;
						} else if (promotionDetail.isPromotion == 0) {
							activityType = 2;
						}
					}

					if (activityType > 0) {
						Picasso.with(this).load(promotionDetail.promotionImage).placeholder(R.drawable.loading).error(R.drawable.download_error).into((ImageView) findViewById(R.id.promotion_image));
						getPromotionContent().postDelayed(new Runnable() {
							@Override
							public void run() {
								getPromotionContent().setText(promotionDetail.promotionContent);
							}
						}, 0);
					}

					if (activityType == 1) {
						getButtonSave().setVisibility(View.VISIBLE);
						getButtonSave().setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
                                dialogNotice = new Dialog(DialogSavePromotionOrSeePoster.this);
                                dialogNotice.setCanceledOnTouchOutside(true);
                                dialogNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);
								String userName=Global.getCurrentUser();
								if (userName.isEmpty()) {
									Utils.showMessage(dialogNotice,"You must log in first to use this function", "Notice", DialogSavePromotionOrSeePoster.this,false);
									return;
								}

								le("KYH.userName= " + userName);

								// NEU DA DANG NHAP
								CLPromotion.addEntryWithUser(promotionDetail, userName);
								Utils.showMessage(dialogNotice,"Promotion saved successfully", "", Global.activityMain,true);
							}
						});
					}
				}
			}

	}

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = (int) (metrics.widthPixels * 0.85);
		promotionThumb = (Promotion)getIntent().getSerializableExtra("promotion_item");
		final int promotionId = getIntent().getIntExtra("promotion_id", -1);
		if (Utils.isInternetConnected(DialogSavePromotionOrSeePoster.this)) {
			setContentView(R.layout.dialog_promotion_save);
			getButtonSave().setVisibility(View.GONE);
			getButtonClose().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});
			if (promotionId != -1) {
				try {
					new GetPromotionDetailTask().execute(promotionId);
				} catch (Exception e) {
				}
			}
		}
		else {
			setContentView(R.layout.dialog_promotion_error);
			getButtonClose().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});
		}

		// set below the setContentView()
		getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
	}

	class PromotionResult {
		public Promotion promotion;
		public IOResult ioResult;

		public PromotionResult(Promotion promotion, IOResult ioResult) {
			this.promotion = promotion;
			this.ioResult = ioResult;
		}
	}

	class GetPromotionDetailTask extends AsyncTask<Integer, Void, PromotionResult> {
		@Override public PromotionResult doInBackground(Integer... promotion_id) {
			PromotionResult result=new PromotionResult(null, IOResult.NO_INTERNET_CONNECTION);

			if (Utils.isInternetConnected(DialogSavePromotionOrSeePoster.this)) {
                HashMap hashMap = new HashMap();
//                hashMap.put("promotion_id",  String.valueOf(promotion_id[0]));
                String tmp = Utils.getResponseFromUrl(Config.CMS_URL + "/h_get_promotion.php?promotion_id=" + promotion_id[0],hashMap);
//				String tmp = Utils.getResponseFromUrl(Config.CMS_URL + "/h_get_promotion.php?promotion_id=" + promotion_id[0]);
				if (!tmp.isEmpty()) {
					result = new PromotionResult(Promotion.Factory.fromDetail(tmp.replace("\n","")), IOResult.OK);
				}
			}

			return result;
		}
		@Override
		public void onPostExecute(PromotionResult result) {
			onGetPromotionDetailTaskFinished(result);
		}
	}

	static void le(String fmt) {
		final String TAG = DialogSavePromotionOrSeePoster.class.getSimpleName();
		android.util.Log.e(TAG, fmt);
	}
}