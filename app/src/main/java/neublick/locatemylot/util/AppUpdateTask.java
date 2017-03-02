package neublick.locatemylot.util;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.database.Database;

public class AppUpdateTask extends AsyncTask<String, Void, List<String>> {
	@Override public List<String> doInBackground(String... urls) {
		URL url = getURL(urls[0]);
		if (url == null) return null;
		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line;
			List<String> result = new ArrayList<String>();
			while((line = reader.readLine()) != null) {
				result.add(line);
			}
			le(result.toString());
			return result;
		} catch(IOException e) {
			return null;
		}
	}

	@Override public void onPostExecute(List<String> result) {
		if (result == null) {
			le("CAN NOT UPDATE");
			return;
		}
		for(String s: result) {
			String[] ss=s.split("~");
			if(ss[0].equalsIgnoreCase("D") && ss.length>=8) {
				Database.getDatabase().execSQL("insert or replace into CL_BEACONS (ID, NAME, X, Y, ZONE, FLOOR, CARPARK_ID, IS_WELCOME) values" +
						"(" + ss[1] + "," + ss[2] + "," + ss[3] + "," + ss[4] + ",'" + ss[5] + "','" + ss[6] + "','" + ss[7] + "','" + ss[8] + "')");
			}
			else if(ss[0].equalsIgnoreCase("C") && ss.length>=3) {
				Database.getDatabase().execSQL("insert or replace into CL_CARPARKS (ID, NAME) values" +
						"(" + ss[1] + ",'" + ss[2] + "')");
			} else if(ss[0].equalsIgnoreCase("P") && ss.length>=7) {
				Database.getDatabase().execSQL("insert or replace into CL_PATH (ID, X, Y, LABEL, ADJ, CARPARK_ID) values" +
						"(" + ss[1] + "," + ss[2] + "," + ss[3] + ",'" + ss[4] + "','" + ss[5] + "'," + ss[6] + ")");
			}
		}
	}

	private URL getURL(String s) {
		try {
			return new URL(s);
		} catch(MalformedURLException e) {
			return null;
		}
	}

	void le(String s) {
		final String TAG = AppUpdateTask.class.getSimpleName();
		android.util.Log.e(TAG, s);
	}
}