package neublick.locatemylot.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import static android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import static android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.PromotionListAdapter;
import neublick.locatemylot.database.CLPromotion;
import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.ui.MyGridView;
import neublick.locatemylot.util.UserUtil;

public class DialogPromotionList extends Activity {

	MyGridView promotionGridView;
	PromotionListAdapter promotionListAdapter;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = (int) (metrics.widthPixels * 0.9);

		setContentView(R.layout.dialog_promotion_list);
		// set below the setContentView()
		getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

		promotionGridView = (MyGridView)findViewById(R.id.promotion_list);
		promotionListAdapter = new PromotionListAdapter(this);
		promotionGridView.setAdapter(promotionListAdapter);
		promotionGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent promotionDetailIntent = new Intent(DialogPromotionList.this, DialogPromotionDetail.class);
				Promotion item = (Promotion) promotionListAdapter.getItem(position);
				promotionDetailIntent.putExtra("promotion_item", item);
				startActivity(promotionDetailIntent);
			}
		});

		findViewById(R.id.action_close).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				finish();
			}
		});
	}

	@Override protected void onResume() {
		super.onResume();
		updatePromotionListView();
	}

	void updatePromotionListView() {
		if (UserUtil.isLoggedIn(this)) {
			final String userName = UserUtil.getUserName(this);
			le("user_name_lay_duoc_la="+userName);
			final List<Promotion> items = CLPromotion.getAllByUserName(userName);
			le("List<Promotion> items.size() = " + items.size());
			runOnUiThread(new Runnable() {
				@Override public void run() {
					promotionListAdapter.replaceWith(items);
					promotionListAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	static void le(String fmt) {
		final String TAG = DialogPromotionList.class.getSimpleName();
		android.util.Log.e(TAG, fmt);
	}
}