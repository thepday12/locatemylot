package neublick.locatemylot.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import static android.view.View.OnClickListener;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import neublick.locatemylot.R;
import neublick.locatemylot.database.CLPromotion;
import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.util.PromotionUtil;
import neublick.locatemylot.util.UserUtil;

public class DialogPromotionDetail extends Activity {

	Promotion promotionDetail;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = (int) (metrics.widthPixels * 0.80);

		setContentView(R.layout.dialog_promotion_detail);

		promotionDetail = (Promotion)getIntent().getSerializableExtra("promotion_item");
		Picasso.with(DialogPromotionDetail.this).load(PromotionUtil.getImageFileFromFileName(promotionDetail.promotionImage)).placeholder(R.drawable.loading).into(
				(ImageView) findViewById(R.id.promotion_image)
		);

		// set below the setContentView()
		getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

		Button close = (Button)findViewById(R.id.action_close);
		Button delete = (Button)findViewById(R.id.action_delete);

		close.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				finish();
			}
		});

		delete.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (UserUtil.isLoggedIn(DialogPromotionDetail.this)) {
					CLPromotion.deleteEntryWithUser(promotionDetail, UserUtil.getUserName(DialogPromotionDetail.this));
				}
				finish();
			}
		});
	}
}