package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.Holiday;
import neublick.locatemylot.model.ParkingRates;

/*
"ID INTEGER PRIMARY KEY, " +
                "HOLIDAY VARCHAR)"
*/

public class CLHoliday {

	static final String[] ALL_COLUMNS = {
			"ID",
			"HOLIDAY"
	};

	// delete a beacon by its id
	public static int deleteHoliday(int id) {
		final String[] args = { String.valueOf(id)};
		return Database.getDatabase().delete(Database.TABLE_HOLIDAY, "ID=?", args);
	}

	// insert a new Beacon into database ;)
	public static long addHoliday(Holiday holiday) {
		ContentValues cv = new ContentValues();
		cv.put("ID", 			holiday.getId());
		cv.put("HOLIDAY", 			holiday.getHolidayDate());
		return Database.getDatabase().insert(Database.TABLE_HOLIDAY, null, cv);
	}

	public static List<Holiday> getAllHoliday() {

		Cursor c = Database.getDatabase().query(Database.TABLE_HOLIDAY, ALL_COLUMNS, null, null, null, null, null);
		List<Holiday> holidays = new ArrayList<Holiday>();
		if (c.moveToFirst()) {
			do {
				Holiday parkingRates = new Holiday(
						c.getInt(0),
						c.getString(1)
				);
				holidays.add(parkingRates);
			} while (c.moveToNext());
		}
		c.close();
		return holidays;
	}






}