package neublick.locatemylot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.R;
import neublick.locatemylot.adapter.CarParkListAdapter;
import neublick.locatemylot.app.Global;
import neublick.locatemylot.database.CLCarpark;
import neublick.locatemylot.model.Carpark;
import neublick.locatemylot.util.GPSHelper;

import static android.R.attr.data;


/**
 * Created by Thep on 1/28/2016.
 */
public class CarParkListFragment extends Fragment {

    public static final CarParkListFragment newInstance(int position) {
        CarParkListFragment helpFragment = new CarParkListFragment();
        Bundle args = new Bundle();
        args.putInt(Global.EXTRA_POSITION_FRAGMENT, position);
        helpFragment.setArguments(args);
        return helpFragment;
    }


    private Context mContext;
    private int position;
    private ListView lvCarPark;
    private List<Carpark> carparks;
    private TextView tvNoData;
    private ProgressBar pbLoading;
    private AutoCompleteTextView etCarParkSearch;
    private ImageButton btSearch, btClear;
    private BroadcastReceiver broadCastReload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_carpark_list, container, false);
        lvCarPark = (ListView) rootView.findViewById(R.id.lvCarPark);
        tvNoData = (TextView) rootView.findViewById(R.id.tvNoData);
        pbLoading = (ProgressBar) rootView.findViewById(R.id.pbLoading);
        etCarParkSearch = (AutoCompleteTextView) rootView.findViewById(R.id.etCarParkSearch);
        btSearch = (ImageButton) rootView.findViewById(R.id.btSearch);
        btClear = (ImageButton) rootView.findViewById(R.id.btClear);
        rootView.getContext().registerReceiver(broadCastReload, new IntentFilter(LocateMyLotActivity.BROADCAST_UPDATE_LOT));
        mContext = rootView.getContext();

        loadData();

        etCarParkSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                searchCarPark(input);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text != null && !text.isEmpty()) {
                    btClear.setVisibility(View.VISIBLE);
                } else {
                    btClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCarPark(etCarParkSearch.getText().toString());
            }
        });
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCarParkSearch.setText("");
            }
        });

        lvCarPark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
//                intent.putExtra(Global.SHOW_DIALOG_START, true);
//                mContext.sendBroadcast(intent);
                Carpark carpark = (Carpark) parent.getAdapter().getItem(position);
                Intent intent = new Intent(Global.DETECT_LIFT_LOBBY_BEACON);
                intent.putExtra(Global.SELECT_BEACON_ID_KEY, carpark.id);
                intent.putExtra(GPSHelper.CARPARK_ID_LIST_KEY, carpark.id);

                mContext.sendBroadcast(intent);
            }
        });
        return rootView;
    }

    private void loadData() {
        new UpdateData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void searchCarPark(String input) {
        if (input.length() > 0) {
            List<Carpark> carParkSearch = new ArrayList<Carpark>();
            List<String> hintList = new ArrayList<String>();
            for (Carpark carpark : carparks) {
                String name = carpark.name.toLowerCase();
                if (name.contains(input.toLowerCase())) {
                    carParkSearch.add(carpark);
                    hintList.add(name);

                }
            }
            if (carParkSearch.size() > 0) {
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
//                        android.R.layout.simple_dropdown_item_1line, hintList);
//                etCarParkSearch.setAdapter(adapter);
                lvCarPark.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                lvCarPark.setAdapter(new CarParkListAdapter(getContext(),carParkSearch));
            }else{
                tvNoData.setVisibility(View.VISIBLE);
                lvCarPark.setVisibility(View.INVISIBLE);
            }
        }else{
            lvCarPark.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
            lvCarPark.setAdapter(new CarParkListAdapter(getContext(), carparks));
        }
    }


    @Override
    public void onDestroy() {
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


    class UpdateData extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            carparks = LocateMyLotActivity.carParkAndLots;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            lvCarPark.setAdapter(new CarParkListAdapter(mContext, carparks));
            pbLoading.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
        }
    }
}
