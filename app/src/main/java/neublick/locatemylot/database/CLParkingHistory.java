package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.ParkingHistory;

/*
db.execSQL("CREATE TABLE IF NOT EXISTS CL_PARKING_HISTORY(" +
		"ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
		"TIME_CHECKIN INT, "+
		"TIME_CHECKOUT INT, "+
		"X REAL, "+
		"Y REAL, "+
		"PHOTO_NAME VARCHAR, "+
		"ZONE VARCHAR, "+
		"FLOOR VARCHAR, "+
		"CARPARK_ID INTEGER)"
		);
*/

public class CLParkingHistory {


    public static long addEntry(ParkingHistory newEntry) {
        ContentValues values = new ContentValues();
        values.put("TIME_CHECKIN", newEntry.timeCheckIn);
        values.put("TIME_CHECKOUT", newEntry.timeCheckOut);
        values.put("X", newEntry.x);
        values.put("Y", newEntry.y);

        values.put("PHOTO_NAME", newEntry.photoName);
        values.put("ZONE", newEntry.zone);
        values.put("FLOOR", newEntry.floor);
        values.put("CARPARK_ID", newEntry.carpackId);
        values.put("LIFT_DATA", newEntry.liftData);
        values.put("IS_NORMAL", newEntry.isNormal);
        values.put("RATES", newEntry.rates);
        values.put("LIFT", newEntry.beaconLiftId);
        values.put("CAR", newEntry.beaconCarId);

        return Database.getDatabase().insert(Database.TABLE_PARKING_HISTORY, null, values);
    }


    public static int deleteEntry(int id) {
        final String[] args = {String.valueOf(id)};
        return Database.getDatabase().delete(Database.TABLE_PARKING_HISTORY, "ID=?", args);
    }


    public static List<ParkingHistory> getParkingHistoryLimit(int limit) {
        List<ParkingHistory> result = new ArrayList<ParkingHistory>();
        final String query = "SELECT CL_PARKING_HISTORY.ID, TIME_CHECKIN, TIME_CHECKOUT, X, Y, PHOTO_NAME, ZONE, FLOOR, CARPARK_ID, NAME,LIFT_DATA,IS_NORMAL, RATES," +
                "CL_PARKING_HISTORY.LIFT,CL_PARKING_HISTORY.CAR  "
                + "FROM CL_CARPARKS, CL_PARKING_HISTORY WHERE CL_CARPARKS.ID = CL_PARKING_HISTORY.CARPARK_ID ORDER BY TIME_CHECKIN DESC LIMIT " + limit;

        Cursor c = Database.getDatabase().rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                ParkingHistory item = new ParkingHistory();
                item.id = c.getInt(0);
                item.timeCheckIn = c.getLong(1);
                item.timeCheckOut = c.getLong(2);
                item.x = c.getFloat(3);
                item.y = c.getFloat(4);
                item.photoName = c.getString(5);
                item.zone = c.getString(6);
                item.floor = c.getString(7);
                item.carpackId = c.getInt(8);
                item.carparkName = c.getString(9);
                item.liftData = c.getString(10);
                item.isNormal = c.getInt(11);
                item.rates = c.getInt(12);
                item.beaconLiftId = c.getInt(13);
                item.beaconCarId = c.getInt(14);
                result.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }


}