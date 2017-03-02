package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.BeaconPoint;
import neublick.locatemylot.model.ShareLocationHint;

/*
ID INTEGER PRIMARY KEY
NAME INTEGER
X REAL
Y REAL
ZONE VARCHAR
FLOOR VARCHAR
CARPARK_ID INTEGER
*/

public class CLHintShareLocation {

    private static String[] columns = {
            "ID",
            "COUNT"
    };

    // delete all beacons
    public static long deleteAll() {
        return Database.getDatabase().delete(Database.TABLE_HINT_SHARE_LOCATION, null, null);
    }

    // delete a beacon by its id
    public static int deleteBeacon(String beaconId) {
        final String[] args = {String.valueOf(beaconId)};
        return Database.getDatabase().delete(Database.TABLE_HINT_SHARE_LOCATION, "ID=?", args);
    }

    public static long updateHint(ShareLocationHint shareLocationHint) {//ghi file trc khi call
            ContentValues paymentContentValues = new ContentValues();
            paymentContentValues.put("count", shareLocationHint.getCount()+1);
            return Database.getDatabase().update(Database.TABLE_HINT_SHARE_LOCATION, paymentContentValues, "ID='" + shareLocationHint.getHint()+"'", null);
    }
    // insert a new Beacon into database ;)
    public static long addItem(String hint) {
        ShareLocationHint shareLocationHint =getShareLocationHintById(hint);
        if(shareLocationHint!=null){
            return updateHint(shareLocationHint);
        }else {
            ContentValues cv = new ContentValues();
            cv.put("ID", hint);
            return Database.getDatabase().insert(Database.TABLE_HINT_SHARE_LOCATION, null, cv);
        }
    }

    // return a Beacon object by its ID
    public static ShareLocationHint getShareLocationHintById(String id) {

        String[] whereArgs = {String.valueOf(id)};
        Cursor c = Database.getDatabase().query(Database.TABLE_HINT_SHARE_LOCATION, columns, "ID = ?", whereArgs, null, null, null);
        if (c.moveToFirst()) {
            try {
                ShareLocationHint rs = new ShareLocationHint(
                        c.getString(0),
                        c.getInt(1)
                );
                android.util.Log.d(TAG, "getBeaconById: " + rs.toString() + "rs!=null");
                return rs;
            } finally {
                c.close();
            }
        }
        return null;
    }

    public static List<ShareLocationHint> getAllShareLocationHint() {

        Cursor c = Database.getDatabase().query(Database.TABLE_HINT_SHARE_LOCATION, columns, null, null, null, null, null);
        List<ShareLocationHint> result = new ArrayList<ShareLocationHint>();
        if (c.moveToFirst()) {
            do {
                ShareLocationHint rs = new ShareLocationHint(
                        c.getString(0),
                        c.getInt(1)
                );
                result.add(rs);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }
    public static List<String> getAllStringShareLocationHint() {

        Cursor c = Database.getDatabase().query(Database.TABLE_HINT_SHARE_LOCATION, columns, null, null, null, null, "COUNT DESC");
      List<String> data= new ArrayList<>();
        if (c.moveToFirst()) {
            do {
//                ShareLocationHint rs = new ShareLocationHint(
//                        c.getString(0),
//                        c.getInt(1)
//                );
//                result.add(rs);
                data.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return data;
    }



    static String TAG = CLHintShareLocation.class.getSimpleName();


    
}