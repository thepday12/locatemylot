package neublick.locatemylot.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    // TAG for debug
    static String TAG = Database.class.getSimpleName();

    private static Database INSTANCE;
    private static SQLiteDatabase sqlite;
    private static String DB_NAME = "new_db_x_y";
    private static int DB_VERSION = 6;
    public static final String TABLE_BEACON = "CL_BEACONS";
    public static final String TABLE_PATH = "CL_PATH";
    public static final String TABLE_PARKING_HISTORY = "CL_PARKING_HISTORY";
    public static final String TABLE_PARKING_RATES = "PARKING_RATE";
    public static final String TABLE_PARKING_SURCHARGE = "PARKING_SURCHARGE";
    public static final String TABLE_HOLIDAY = "HOLIDAY";
    public static final String TABLE_CARPARKS = "CL_CARPARKS";
    public static final String TABLE_PROMOTION = "CL_PROMOTION";
    public static final String TABLE_HINT_SHARE_LOCATION = "CL_HINT_SHARE_LOCATION";

    public static void initialize(Context context) {
        if (null == INSTANCE) {
            INSTANCE = new Database(context);
        }
    }

    public static SQLiteDatabase getDatabase() {
        if (null == sqlite) {
            sqlite = INSTANCE.getWritableDatabase();
        }
        return sqlite;
    }

    protected Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_BEACON+"(" +
                "ID INTEGER PRIMARY KEY, " +
                "NAME VARCHAR, " +
                "MAJOR INTEGER DEFAULT 0, " +
                "MINOR INTEGER DEFAULT 0, " +
                "X REAL, " +
                "Y REAL, " +
                "ZONE VARCHAR, " +
                "FLOOR VARCHAR, " +
                "CARPARK_ID INTEGER, " +
                //0: normal, 1: beacon welcome carpark, 2: thang máy (lift) 3: my beacon 4: check out
                "BEACON_TYPE INTEGER DEFAULT 0)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_PATH +"(" +
                "ID INTEGER, " +
                "X REAL, " +
                "Y REAL, " +
                "LABEL VARCHAR, " +
                "ADJ VARCHAR, " +
                "CARPARK_ID INTEGER , PRIMARY KEY (ID, CARPARK_ID) )"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_PROMOTION+"(" +
                "ID INTEGER, " +
                "NAME VARCHAR, " +
                "USER_NAME VARCHAR, " +
                "PROMOTION_THUMB VARCHAR, " +
                "PROMOTION_IMAGE VARCHAR, " +
                "TIME_GET INT, " +
                "TIME_REDEEM INT, " +
                "PROMOTION_CONTENT VARCHAR, " +
                "PRIMARY KEY (ID, USER_NAME) )"
        );
//thep update 2016/08/23  -- version 3
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_CARPARKS +"(" +
                "ID INTEGER PRIMARY KEY, " +
                "NAME VARCHAR, " +
                "FLOORS VARCHAR, " +
                "CP_TYPE INTEGER DEFAULT 1, " +//1- lap beacon -2 khong lap
                "LAT REAL DEFAULT 0, " +
                "LON REAL DEFAULT 0," +
                "RATES_INFO VARCHAR)"
        );//end

        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_PARKING_HISTORY+"(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TIME_CHECKIN INT, " +
                "TIME_CHECKOUT INT, " +
                "X REAL, " +
                "Y REAL, " +
                "PHOTO_NAME VARCHAR, " +
                "ZONE VARCHAR, " +
                "FLOOR VARCHAR, " +
                "CARPARK_ID INTEGER," +
                "LIFT_DATA VARCHAR," +
                "RATES REAL DEFAULT 0, " +
                "IS_NORMAL INTEGER)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS CL_SETTING(" +
                "ALERT_BEFORE INT DEFAULT 10" +
                ")"
        );

        //thep update 2016/08/11  -- version 2
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_PARKING_RATES+"(" +
                "ID INTEGER PRIMARY KEY, " +
                "CARPARK_ID INTEGER, " +
                "DAY_TYPE INTEGER, " +
                "BEGIN_TIME VARCHAR, " +
                "END_TIME VARCHAR, " +
                "FIRST_MINS INTEGER, " +
                "FIRST_RATE REAL, " +
                "SUB_MINS INTEGER, " +
                "SUB_RATES REAL, " +
                "STATUS INTEGER DEFAULT 1)"//1: On 2: For tenants only 3: Closed 4: Season parking Only 5: HDB coupon parking
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_PARKING_SURCHARGE+"(" +
                "ID INTEGER PRIMARY KEY, " +
                "CARPARK_ID INTEGER, " +
                "DAY_TYPE INTEGER, " +
                "BEGIN_TIME VARCHAR, " +
                "END_TIME VARCHAR, " +
                "SURCHARGE  REAL, " +
                "STATUS INTEGER DEFAULT 1)"//1: On 2: For tenants only 3: Closed 4: Season parking Only 5: HDB coupon parking
        );
        //end
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_HOLIDAY+"(" +
                "ID INTEGER PRIMARY KEY, " +
                "HOLIDAY VARCHAR)"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_HINT_SHARE_LOCATION +"(" +
                "ID TEXT PRIMARY KEY, " +
                "COUNT INTEGER DEFAULT 1)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateVersion5(db, oldVersion);
        updateVersion6(db, oldVersion);
    }

    private void updateVersion5(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 5) {
            String sqlString ="ALTER TABLE " + TABLE_PARKING_HISTORY + " ADD COLUMN " +
                    "LIFT_DATA VARCHAR";
            String sqlString2 ="ALTER TABLE " + TABLE_PARKING_HISTORY + " ADD COLUMN " +
                    "IS_NORMAL INTEGER";
            db.execSQL(sqlString);
            db.execSQL(sqlString2);
        }
    }

    private void updateVersion6(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 6) {
            String sqlString ="ALTER TABLE " + TABLE_PARKING_HISTORY + " ADD COLUMN " +
                    "RATES REAL DEFAULT 0";
            db.execSQL(sqlString);

        }
    }

    public static int deleteAll(String tableName) {
        return Database.getDatabase().delete(tableName, null, null);
    }
}