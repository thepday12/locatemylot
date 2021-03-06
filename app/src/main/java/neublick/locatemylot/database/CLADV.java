package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.ADVObject;

/*
ID INTEGER PRIMARY KEY
NAME INTEGER
X REAL
Y REAL
ZONE VARCHAR
FLOOR VARCHAR
CARPARK_ID INTEGER
*/

public class CLADV {

    private static String[] columns = {
            "ID",
            "IMAGE",
            "STATUS"
    };



    public static int deleteAllADV() {
        return Database.getDatabase().delete(Database.TABLE_ADV, null, null);
    }

    public static int deleteADV(String id) {
        final String[] args = {String.valueOf(id)};
        ContentValues cv = new ContentValues();
        cv.put(columns[2], 0);
        return Database.getDatabase().update(Database.TABLE_ADV, cv, "ID=?",args);
    }


    public static long addItem(ADVObject advObject) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(columns[0], advObject.getId());
            cv.put(columns[1], advObject.getImage());
            return Database.getDatabase().insert(Database.TABLE_ADV, null, cv);
        }catch (Exception e){
            return 0;
        }
    }

    public static List<ADVObject> getAllADV() {
        String whereClause =  columns[2]+" > 0 ";
        Cursor c = Database.getDatabase().query(Database.TABLE_ADV, columns, whereClause, null, null, null, null);
        List<ADVObject> result = new ArrayList<ADVObject>();
        if (c.moveToFirst()) {
            do {
                ADVObject advObject = new ADVObject(
                        c.getString(0),
                        c.getString(1)
                );
                result.add(advObject);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }
    public static List<ADVObject> getAllADVDelete() {
        String whereClause =  columns[2]+" = 0";
        Cursor c = Database.getDatabase().query(Database.TABLE_ADV, columns, whereClause, null, null, null, null);
        List<ADVObject> result = new ArrayList<ADVObject>();
        if (c.moveToFirst()) {
            do {
                ADVObject advObject = new ADVObject(
                        c.getString(0),
                        c.getString(1)
                );
                result.add(advObject);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }
    public static ADVObject getADVById(String id) {
        String whereClause = "ID = '" + id + "'";
        Cursor c = Database.getDatabase().query(Database.TABLE_ADV, columns, whereClause, null, null, null, null);
        ADVObject result = new ADVObject();
        if (c.moveToFirst()) {
                result = new ADVObject(
                        c.getString(0),
                        c.getString(1)
                );
        }
        c.close();
        return result;
    }

    static String TAG = CLADV.class.getSimpleName();


    
}