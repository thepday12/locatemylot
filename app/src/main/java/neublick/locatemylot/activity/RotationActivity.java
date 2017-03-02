package neublick.locatemylot.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import neublick.locatemylot.R;
import neublick.locatemylot.fixbug.TaskShowImage;

public class RotationActivity extends BaseActivity {

	private ImageView viewAttached;
	private Button btnRemove;
	private String photoName;
	private FrameLayout root;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_rotation2);

		root = (FrameLayout)findViewById(R.id.frame_layout);
		ViewTreeObserver vto = root.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override public void onGlobalLayout() {
				root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				viewAttached = (ImageView) findViewById(R.id.view_attached);
				btnRemove = (Button) findViewById(R.id.remove);

				SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
				photoName = parkingSession.getString("PHOTO_NAME", "");

				if (!photoName.equals("")) {
					new TaskShowImage<ImageView>(RotationActivity.this, viewAttached).execute(photoName);
				}

				btnRemove.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) {
						// remove photo attached to the map
						SharedPreferences parkingSession = getSharedPreferences("PARKING_SESSION", MODE_PRIVATE);
						parkingSession.edit().putString("PHOTO_NAME", "").apply();
						finish();
					}
				});
			}
		});
	}
}