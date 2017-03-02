package neublick.locatemylot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class SettingAdapter extends BaseAdapter {

	Context context;
	LayoutInflater inflater;
	ArrayList<SettingPreference> dataItems = new ArrayList<SettingPreference>();

	public SettingAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	@Override public int getCount() {
		return dataItems.size();
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public Object getItem(int position) {
		return dataItems.get(position);
	}

	@Override public View getView(int position, View view, ViewGroup container) {

		return view;
	}
}