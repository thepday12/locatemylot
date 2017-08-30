package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.ParkingRates;
import neublick.locatemylot.model.ParkingSurcharge;

/*

	"ID INTEGER PRIMARY KEY, " +
	"CARPARK_ID INTEGER, "+
	"DAY_TYPE INTEGER, "+
	"BEGIN_TIME VARCHAR, "+
	"END_TIME VARCHAR, "+
	"SURCHARGE  REAL)"
*/

public class CLParkingSurcharge {

	static final String[] ALL_COLUMNS = {
			"ID",
			"CARPARK_ID",
			"DAY_TYPE",
			"BEGIN_TIME",
			"END_TIME",
			"SURCHARGE",
			"STATUS"
	};

	// delete a beacon by its id
	public static int deleteParkingSurcharge(int parkingRatesId) {
		final String[] args = { String.valueOf(parkingRatesId)};
		return Database.getDatabase().delete(Database.TABLE_PARKING_SURCHARGE, "ID=?", args);
	}

	// insert a new Beacon into database ;)
	public static long addParkingSurcharge(ParkingSurcharge parkingSurcharge) {
		ContentValues cv = new ContentValues();
		cv.put("ID", 			parkingSurcharge.getId());
		cv.put("CARPARK_ID", 			parkingSurcharge.getCarparkId());
		cv.put("DAY_TYPE", 			parkingSurcharge.getDataType());
		cv.put("BEGIN_TIME", 			parkingSurcharge.getBeginTime());
		cv.put("END_TIME", 			parkingSurcharge.getEndTime());
		cv.put("SURCHARGE", 		parkingSurcharge.getSurcharge());

		return Database.getDatabase().insert(Database.TABLE_PARKING_SURCHARGE, null, cv);
	}


	public static List<ParkingSurcharge> getListParkingSurchargeByCarparkId(int carparkId) {

		String whereClause = "CARPARK_ID = ?";
		String[] whereArgs = { String.valueOf(carparkId) };
		Cursor c = Database.getDatabase().query(Database.TABLE_PARKING_SURCHARGE, ALL_COLUMNS, whereClause, whereArgs, null, null, null);
		List<ParkingSurcharge> parkingSurcharges = new ArrayList<ParkingSurcharge>();
		if (c.moveToFirst()) {
			do {
				ParkingSurcharge parkingSurcharge = new ParkingSurcharge(
						c.getInt(0),
						c.getInt(1),
						c.getInt(2),
						c.getString(3),
						c.getString(4),
						c.getFloat(5),
						c.getInt(6)
				);
				parkingSurcharges.add(parkingSurcharge);
			} while (c.moveToNext());
		}
		c.close();
		return parkingSurcharges;
	}


	public static List<ParkingSurcharge> getAllParkingSurcharge() {

		Cursor c = Database.getDatabase().query(Database.TABLE_PARKING_SURCHARGE, ALL_COLUMNS, null, null, null, null, null);
		List<ParkingSurcharge> parkingSurcharges = new ArrayList<ParkingSurcharge>();
		if (c.moveToFirst()) {
			do {
				ParkingSurcharge parkingSurcharge = new ParkingSurcharge(
						c.getInt(0),
						c.getInt(1),
						c.getInt(2),
						c.getString(3),
						c.getString(4),
						c.getFloat(5),
						c.getInt(6)
				);
				parkingSurcharges.add(parkingSurcharge);
			} while (c.moveToNext());
		}
		c.close();
		return parkingSurcharges;
	}






}