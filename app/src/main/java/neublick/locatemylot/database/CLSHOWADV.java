package neublick.locatemylot.database;

import android.database.Cursor;

/*
ID INTEGER PRIMARY KEY, TIME_SHOW REAL DEFAULT 0
*/

public class CLSHOWADV {

    private static String[] columns = {
            "ID",
            "TIME_SHOW"
    };



    public static void addItem(String beaconId, long timeShow) {
        String query = "INSERT OR REPLACE INTO TABLE_SHOW_ADV (ID,TIME_SHOW)VALUES ("+beaconId+","+timeShow+")";
        Database.getDatabase().execSQL(query);
    }


    public static boolean isValidShowAdv(String beaconId,long currentTime) {
        boolean result = true;
        String whereClause = "ID = " + beaconId + "";
        Cursor c = Database.getDatabase().query(Database.TABLE_SHOW_ADV, columns, whereClause, null, null, null, null);
        if (c.moveToFirst()) {
                result = currentTime - c.getLong(1) > 300000;
        }
        c.close();
        return result;
    }

    static String TAG = CLSHOWADV.class.getSimpleName();


    
}