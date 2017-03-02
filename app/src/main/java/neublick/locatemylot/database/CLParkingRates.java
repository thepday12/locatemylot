package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.BeaconPoint;
import neublick.locatemylot.model.ParkingRates;

/*
"ID INTEGER PRIMARY KEY, " +
                "CARPARK_ID INTEGER, "+
                "DAY_TYPE INTEGER, "+
                "BEGIN_TIME VARCHAR, "+
                "END_TIME VARCHAR, "+
                "FIRST_MINS INTEGER, "+
                "FIRST_RATE REAL, " +
                "SUB_MINS INTEGER, "+
                "SUB_RATES REAL)"
*/

public class CLParkingRates {

	static final String[] ALL_COLUMNS = {
			"ID",
			"CARPARK_ID",
			"DAY_TYPE",
			"BEGIN_TIME",
			"END_TIME",
			"FIRST_MINS",
			"FIRST_RATE",
			"SUB_MINS",
			"SUB_RATES",
			"STATUS"
	};

	// delete a beacon by its id
	public static int deleteParkingRates(int parkingRatesId) {
		final String[] args = { String.valueOf(parkingRatesId)};
		return Database.getDatabase().delete(Database.TABLE_PARKING_RATES, "ID=?", args);
	}

	// insert a new Beacon into database ;)
	public static long addParkingRates(ParkingRates parkingRates) {
		ContentValues cv = new ContentValues();
		cv.put("ID", 			parkingRates.getId());
		cv.put("CARPARK_ID", 			parkingRates.getCarparkId());
		cv.put("DAY_TYPE", 			parkingRates.getDayType());
		cv.put("BEGIN_TIME", 			parkingRates.getBeginTime());
		cv.put("END_TIME", 			parkingRates.getEndTime());
		cv.put("FIRST_MINS", 		parkingRates.getFirstMins());
		cv.put("FIRST_RATE", 	parkingRates.getFirstRates());
		cv.put("SUB_MINS", 	parkingRates.getSubMins());
		cv.put("SUB_RATES", 	parkingRates.getSubRates());
		return Database.getDatabase().insert(Database.TABLE_PARKING_RATES, null, cv);
	}

	// return a Beacon object by its ID
	public static ParkingRates getParkingRatesByCarparkId(int carparkId) {

		String[] whereArgs = { String.valueOf(carparkId) };
		Cursor c = Database.getDatabase().query(Database.TABLE_PARKING_RATES, ALL_COLUMNS, "CARPARK_ID = ?", whereArgs, null, null, null);
		if (c.moveToFirst()) {
			try {
				ParkingRates rs = new ParkingRates(
						c.getInt(0),
						c.getInt(1),
						c.getInt(2),
						c.getString(3),
						c.getString(4),
						c.getInt(5),
						c.getFloat(6),
						c.getInt(7),
						c.getFloat(8),
						c.getInt(9)
				);
				return rs;
			} finally {
				c.close();
			}
		}
		return null;
	}

	public static List<ParkingRates> getListParkingRatesByCarparkId(int carparkId) {

		String whereClause = "CARPARK_ID = ?";
		String[] whereArgs = { String.valueOf(carparkId) };
		Cursor c = Database.getDatabase().query(Database.TABLE_PARKING_RATES, ALL_COLUMNS, whereClause, whereArgs, null, null, null);
		List<ParkingRates> parkingRatesList = new ArrayList<ParkingRates>();
		if (c.moveToFirst()) {
			do {
				ParkingRates parkingRates = new ParkingRates(
						c.getInt(0),
						c.getInt(1),
						c.getInt(2),
						c.getString(3),
						c.getString(4),
						c.getInt(5),
						c.getFloat(6),
						c.getInt(7),
						c.getFloat(8),
						c.getInt(9)
				);
				parkingRatesList.add(parkingRates);
			} while (c.moveToNext());
		}
		c.close();
		return parkingRatesList;
	}


	public static List<ParkingRates> getAllParkingRates() {

		Cursor c = Database.getDatabase().query(Database.TABLE_PARKING_RATES, ALL_COLUMNS, null, null, null, null, null);
		List<ParkingRates> parkingRatesList = new ArrayList<ParkingRates>();
		if (c.moveToFirst()) {
			do {
				ParkingRates parkingRates = new ParkingRates(
						c.getInt(0),
						c.getInt(1),
						c.getInt(2),
						c.getString(3),
						c.getString(4),
						c.getInt(5),
						c.getFloat(6),
						c.getInt(7),
						c.getFloat(8),
						c.getInt(9)
				);
				parkingRatesList.add(parkingRates);
			} while (c.moveToNext());
		}
		c.close();
		return parkingRatesList;
	}



    


}