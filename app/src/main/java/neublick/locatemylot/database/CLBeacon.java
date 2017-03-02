package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.BeaconPoint;

/*
ID INTEGER PRIMARY KEY
NAME INTEGER
X REAL
Y REAL
ZONE VARCHAR
FLOOR VARCHAR
CARPARK_ID INTEGER
*/

public class CLBeacon {

    private static String[] columns = {
            "ID",
            "NAME",
            "MAJOR",
            "MINOR",
            "X",
            "Y",
            "ZONE",
            "FLOOR",
            "CARPARK_ID",
            "BEACON_TYPE"
    };

    // delete all beacons
    public static long deleteAll() {
        return Database.getDatabase().delete(Database.TABLE_BEACON, null, null);
    }

    // delete a beacon by its id
    public static int deleteBeacon(int beaconId) {
        final String[] args = {String.valueOf(beaconId)};
        return Database.getDatabase().delete(Database.TABLE_BEACON, "ID=?", args);
    }

    // insert a new Beacon into database ;)
    public static long addEntry(BeaconPoint beaconPoint) {
        ContentValues cv = new ContentValues();
        cv.put("ID", beaconPoint.mId);
        cv.put("NAME", beaconPoint.mName);
        cv.put("X", beaconPoint.mX);
        cv.put("Y", beaconPoint.mY);
        cv.put("ZONE", beaconPoint.mZone);
        cv.put("FLOOR", beaconPoint.mFloor);
        cv.put("CARPARK_ID", beaconPoint.mCarparkId);
        return Database.getDatabase().insert(Database.TABLE_BEACON, null, cv);
    }

    // return a Beacon object by its ID
    public static BeaconPoint getBeaconById(int beaconId) {

        String[] whereArgs = {String.valueOf(beaconId)};
        Cursor c = Database.getDatabase().query(Database.TABLE_BEACON, columns, "ID = ?", whereArgs, null, null, null);
        if (c.moveToFirst()) {
            try {
                BeaconPoint rs = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
                android.util.Log.d(TAG, "getBeaconById: " + rs.toString() + "rs!=null");
                return rs;
            } finally {
                c.close();
            }
        }
        return null;
    }

    public static BeaconPoint getBeaconByMajorAndMinor(int major, int minor) {
        String whereClause = "MAJOR = " + major + " AND MINOR = " + minor;
        Cursor c = null;
        try {

            c = Database.getDatabase().query(Database.TABLE_BEACON, columns, whereClause, null, null, null, null);
            if (c.moveToFirst()) {
                BeaconPoint rs = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
                android.util.Log.d(TAG, "getBeaconById: " + rs.toString() + "rs!=null");
                return rs;
            }
        } finally {
            if(c!=null)
            c.close();
        }
        return null;
    }

    public static List<BeaconPoint> getBeaconsByCarparkId(int carparkId) {

        String whereClause = "CARPARK_ID = ?";
        String[] whereArgs = {String.valueOf(carparkId)};
        Cursor c = Database.getDatabase().query(Database.TABLE_BEACON, columns, whereClause, whereArgs, null, null, null);
        List<BeaconPoint> listOfBeacons = new ArrayList<BeaconPoint>();
        if (c.moveToFirst()) {
            do {
                BeaconPoint beaconPoint = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
                listOfBeacons.add(beaconPoint);
            } while (c.moveToNext());
        }
        c.close();
        return listOfBeacons;
    }

    public static List<BeaconPoint> getAllBeacons(int carparkId, String floor) {
        String whereClause = "CARPARK_ID = ? AND FLOOR = ?";
        String[] whereArgs = {
                String.valueOf(carparkId),
                String.valueOf(floor)
        };
        Cursor c = Database.getDatabase().query(Database.TABLE_BEACON, columns, whereClause, whereArgs, null, null, null);
        List<BeaconPoint> listOfBeacons = new ArrayList<BeaconPoint>();
        if (c.moveToFirst()) {
            do {
                BeaconPoint beaconPoint = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
                android.util.Log.d(TAG, "BCZ:" + beaconPoint.toString());
            } while (c.moveToNext());
        }
        c.close();
        android.util.Log.d("Database", listOfBeacons.toString());
        return listOfBeacons;
    }

    public static List<BeaconPoint> getAllEntries() {

        Cursor c = Database.getDatabase().query(Database.TABLE_BEACON, columns, null, null, null, null, null);
        List<BeaconPoint> result = new ArrayList<BeaconPoint>();
        if (c.moveToFirst()) {
            do {
                BeaconPoint beaconItem = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
                result.add(beaconItem);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }


    // lay ve danh sach beacon co welcome_beacon = 1 va carpark_id = gia tri nao do
    public static List<BeaconPoint> getWelcomeBeaconsByCarparkId(int carparkId) {
        String whereClause = "BEACON_TYPE = ? AND CARPARK_ID = ?";
        String[] whereArgs = {
                "1",
                String.valueOf(carparkId)
        };
        Cursor c = Database.getDatabase().query(Database.TABLE_BEACON, columns, whereClause, whereArgs, null, null, null);
        List<BeaconPoint> welcomeBeaconsList = new ArrayList<BeaconPoint>();
        if (c.moveToFirst()) {
            do {
                BeaconPoint bpItem = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
            } while (c.moveToNext());
        }
        c.close();
        return welcomeBeaconsList;
    }

    // lay ve toan bo welcome beacons co trong CSDL
    // lay ve toan bo beacons co BEACON_TYPE = 1
    public static List<BeaconPoint> allWelcomeBeacons() {
        Cursor c = Database.getDatabase().rawQuery(
                "SELECT ID,NAME,MAJOR,MINOR,X, Y, ZONE,FLOOR,CARPARK_ID,BEACON_TYPE FROM CL_BEACONS WHERE BEACON_TYPE = 1",
                null
        );
        List<BeaconPoint> welcomeBeaconsList = new ArrayList<BeaconPoint>();
        le("cursor.getCount()=" + c.getCount());
        if (c.moveToFirst()) {
            do {
                BeaconPoint bpItem = new BeaconPoint(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        c.getFloat(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8),
                        c.getInt(9)
                );
                welcomeBeaconsList.add(bpItem);
            } while (c.moveToNext());
        }
        c.close();
        return welcomeBeaconsList;
    }


    static String TAG = CLBeacon.class.getSimpleName();

    static void le(String s) {
        final String TAG = CLBeacon.class.getSimpleName();
        android.util.Log.e(TAG, s);
    }

}