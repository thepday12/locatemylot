package neublick.locatemylot.database;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.Carpark;

public class CLCarpark {
	private static final String[] columns = {
			"ID",
			"NAME",
			"FLOORS",
			"CP_TYPE, "+//1- lap beacon -2 khong lap
            "LAT, " +
            "LON,"+
            "RATES_INFO"
	};


	public static List<Carpark> getAllEntries() {
		List<Carpark> result =  new ArrayList<Carpark>();

		Cursor c = Database.getDatabase().query(Database.TABLE_CARPARKS, columns, null, null, null, null, "NAME DESC");
		if (c.moveToFirst()) {
			do {
				Carpark item = new Carpark();
				item.id 	= c.getInt(0);
				item.name 	= c.getString(1);
				item.floor 	= c.getString(2);
				item.cpType 	= c.getInt(3);
				item.lat 	= c.getDouble(4);
				item.lon 	= c.getDouble(5);
                item.ratesInfo 	= c.getString(6);
				result.add(item);
			} while(c.moveToNext());
		}
		c.close();
		return result;
	}

	public static String getCarparkNameByCarparkId(int carparkId) {
		Cursor c = Database.getDatabase().rawQuery(
			"SELECT NAME FROM "+Database.TABLE_CARPARKS +" WHERE ID = ?",
			new String[] {
				String.valueOf(carparkId)
			}
		);
		c.moveToFirst();
		try {
			int carparkNameIndex = c.getColumnIndex("NAME");
			return c.getString(carparkNameIndex);
		}catch (Exception ex){
			return "";
		}finally {
			c.close();
		}
	}
    public static Carpark getCarparkByCarparkId(int carparkId) {
        String[] whereArgs = {String.valueOf(carparkId)};
        Cursor c = Database.getDatabase().query(Database.TABLE_CARPARKS, columns, "ID = ?", whereArgs, null, null, null);
        if (c.moveToFirst()) {
            try {
                Carpark item = new Carpark();
                item.id = c.getInt(0);
                item.name = c.getString(1);
                item.floor = c.getString(2);
                item.cpType = c.getInt(3);
                item.lat = c.getDouble(4);
                item.lon = c.getDouble(5);
                item.ratesInfo 	= c.getString(6);
                return item;
            } finally {
                c.close();
            }
        }
        return null;
	}
	public static List<Carpark> getListCarParksWithType(int cpType) {

		String whereClause = "CP_TYPE = ?";
		String[] whereArgs = { String.valueOf(cpType) };
		Cursor c = Database.getDatabase().query(Database.TABLE_CARPARKS, columns, whereClause, whereArgs, null, null, null);
		List<Carpark> result =  new ArrayList<Carpark>();
		if (c.moveToFirst()) {
			do {
				Carpark item = new Carpark();
				item.id 	= c.getInt(0);
				item.name 	= c.getString(1);
				item.floor 	= c.getString(2);
				item.cpType 	= c.getInt(3);
				item.lat 	= c.getDouble(4);
				item.lon 	= c.getDouble(5);
                item.ratesInfo 	= c.getString(6);

				result.add(item);
			} while(c.moveToNext());
		}
		c.close();
		return result;
	}
    public static List<Carpark> getListCarParksWithListID(String data) {

		String whereClause = "ID IN ("+data+")";
//		String[] whereArgs = { data };
		Cursor c = Database.getDatabase().query(Database.TABLE_CARPARKS, columns, whereClause, null, null, null, null);
		List<Carpark> result =  new ArrayList<Carpark>();
		if (c.moveToFirst()) {
			do {
				Carpark item = new Carpark();
				item.id 	= c.getInt(0);
				item.name 	= c.getString(1);
				item.floor 	= c.getString(2);
				item.cpType 	= c.getInt(3);
				item.lat 	= c.getDouble(4);
				item.lon 	= c.getDouble(5);
				item.ratesInfo 	= c.getString(6);

				result.add(item);
			} while(c.moveToNext());
		}
		c.close();
		return result;
	}
}