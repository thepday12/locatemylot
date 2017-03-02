package neublick.locatemylot.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.view.View.OnClickListener;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.ParkingHistoryAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLParkingHistory;
import neublick.locatemylot.model.ParkingHistory;
import neublick.locatemylot.util.ParkingSession;
import neublick.locatemylot.util.Utils;

public class ParkingHistoryActivity extends AppCompatActivity implements OnClickListener {
    private ListView historyListView;
    private TextView tvNoHistory;
    private ParkingHistoryAdapter historyAdapter;
    private Button btClose;
    private Toolbar mToolbar;


    private ParkingSession mParkingSession;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_parking_history);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mParkingSession = ParkingSession.getInstanceSharedPreferences(ParkingHistoryActivity.this);
        historyListView = (ListView) findViewById(R.id.historyListView);
        tvNoHistory = (TextView) findViewById(R.id.tvNoHistory);
        btClose = (Button) findViewById(R.id.btClose);
        historyAdapter = new ParkingHistoryAdapter(this);
        historyListView.setAdapter(historyAdapter);

        btClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
    @Override
    protected void onResume() {
        super.onResume();
        updateHistoryListView();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.lml_action_history_edit) {
            final ParkingHistory parkingHistoryItem = (ParkingHistory) historyAdapter.getItem((Integer) view.getTag());

            new AlertDialog.Builder(ParkingHistoryActivity.this, R.style.AppTheme_AlertDialog)
                    .setTitle("Confirm")
                    .setMessage("Would you like to set back your vehicle's location?\n"
                            + "Entry time: " + Utils.getCalendarReadable(Utils.retrieveCalendarFromMillis(parkingHistoryItem.timeCheckIn))
                    )
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // save the car location (x, y, zone, floor) && photoName
                            mParkingSession.setCheckIn(true);
                            mParkingSession.setCarParkCheckIn(parkingHistoryItem.carpackId);
                            mParkingSession.setCheckInFromHistory(true);
                            mParkingSession.setPhotoName(parkingHistoryItem.photoName);
                            mParkingSession.setTimeCheckIn(parkingHistoryItem.timeCheckIn);
                            Global.entryTime = -1;
                            if (parkingHistoryItem.isNormal > 0) {
                                mParkingSession.setNormalCheckIn(true);
                                mParkingSession.setLiftLobby(parkingHistoryItem.liftData);
                                if (parkingHistoryItem.floor.isEmpty()) {
                                    setNullDataLocation();
                                } else {
                                    mParkingSession.setCheckCarLocation(true);
                                    mParkingSession.setZone(parkingHistoryItem.zone);
                                    mParkingSession.setFloor(parkingHistoryItem.floor);
                                    mParkingSession.setX(parkingHistoryItem.x);
                                    mParkingSession.setY(parkingHistoryItem.y);
                                }
                            } else {
                                mParkingSession.setNormalCheckIn(false);
                                mParkingSession.setLiftLobby("");
                                setNullDataLocation();
                            }

                            finish();
                            if (Global.activityMain != null)
                                Global.activityMain.updateCheckInStatus();
                        }
                    })
                    .show();
            // display the car on map-view
            // chi can this.finish(), MainActivity se chay onResume() de hien thi CAR tren ban do
        } else if (id == R.id.lml_action_history_delete) {
            ParkingHistory item = (ParkingHistory) historyAdapter.getItem((Integer) view.getTag());
            CLParkingHistory.deleteEntry(item.id);
            updateHistoryListView();
        }
    }

    private void setNullDataLocation() {
        mParkingSession.setZone("");
        mParkingSession.setFloor("");
        mParkingSession.setX(0);
        mParkingSession.setY(0);
        mParkingSession.setCheckCarLocation(false);
    }

    void updateHistoryListView() {
        //final List<ParkingHistory> items = CLParkingHistory.getAllParkingHistory2();
        final List<ParkingHistory> items = CLParkingHistory.getParkingHistoryLimit(10);
        /*
		Collections.sort(items, new java.util.Comparator<ParkingHistory>() {
			@Override public int compare(ParkingHistory o1, ParkingHistory o2) {
				return (int)(o2.timeCheckIn - o1.timeCheckIn);
			}
		});
		*/
        if (items.size() > 0) {
            historyListView.setVisibility(View.VISIBLE);
            tvNoHistory.setVisibility(View.GONE);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    historyAdapter.replaceWith(items);
                    historyAdapter.notifyDataSetChanged();
                }
            });
        } else {
            historyListView.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        }
    }

    void toastM(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}