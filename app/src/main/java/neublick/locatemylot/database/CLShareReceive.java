package neublick.locatemylot.database;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.BeaconPoint;
import neublick.locatemylot.model.ShareReceiveObject;

/*
ID INTEGER PRIMARY KEY, TIME_SHOW REAL DEFAULT 0
*/

public class CLShareReceive {

    private static String[] columns = {
            "ID", "DATA", "TYPE", "TIME_RECEIVE"
    };


    public static void addItem(String data,int type) {
        String query = "INSERT OR REPLACE INTO TABLE_SHARE_RECEIVE (DATA,TYPE,TIME_RECEIVE)VALUES ('" + data + "'," + type +","+System.currentTimeMillis()+ ")";
        Database.getDatabase().execSQL(query);
    }

    /***
     * Tra ve danh sach share khac type dua vao
     * @param type
     * @return
     */
    public static List<ShareReceiveObject> getShareNotLikeType(int type) {

        String whereClause = "TYPE <> ?";
        String[] whereArgs = {String.valueOf(type)};
        Cursor c = Database.getDatabase().query(Database.TABLE_SHARE_RECEIVE, columns, whereClause, whereArgs, null, null, "TIME_RECEIVE DESC","1000");

        List<ShareReceiveObject> result = new ArrayList<ShareReceiveObject>();
        if (c.moveToFirst()) {
            do {
                ShareReceiveObject shareReceiveObject = new ShareReceiveObject(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getLong(3)
                );
                result.add(shareReceiveObject);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    public static int deleteAllItem() {
        return Database.getDatabase().delete(Database.TABLE_SHARE_RECEIVE, null, null);
    }

    public static int deleteItem(int id) {
        String whereClause = "ID = ?";
        String[] whereArgs = {String.valueOf(id)};
        return Database.getDatabase().delete(Database.TABLE_SHARE_RECEIVE, whereClause, whereArgs);
    }

    static String TAG = CLShareReceive.class.getSimpleName();


}