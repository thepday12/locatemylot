package neublick.locatemylot.model;

public class BeaconPoint {

	public int mId; // MAJOR cua BEACON
	public String mName; // Maybe BEACON's MAC ADDR
    public int mMajor;
    public int mMinor;

	public float mX;
	public float mY;

	public String mZone;
	public String mFloor;
	public int mCarparkId; // MINOR cua BEACON

	public double mDistance;
	public double mRSSI;
    /***
     * //0: normal, 1: beacon welcome carpark, 2: thang máy (lift) 3: my beacon 4: check out 5:Thang bo
     */
	public int mBeaconType=0;
	public boolean isPromotion=false;

	/*
	db.execSQL("CREATE TABLE IF NOT EXISTS CL_BEACONS(" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"NAME INTEGER, "+
			"X INTEGER, "+
			"Y INTEGER, "+
			"ZONE VARCHAR, "+
			"FLOOR VARCHAR, "+
			"CARPARK_ID VARCHAR)"
			);
	*/



	public BeaconPoint(int pId, String pName,int major, int minor, float pX, float pY, String pZone, String pFloor, int pCarparkId, int beaconType,int isPromotion) {
        mId = pId;
        mName = pName;
        mMajor=major;
        mMinor = minor;
        mX = pX;
        mY = pY;
        mZone = pZone;
        mFloor = pFloor;
        mCarparkId = pCarparkId;
		mBeaconType=beaconType;
        this.isPromotion = (isPromotion>0);
	}

	public BeaconPoint() {

	}

	// 2 Beacon dc goi la bang nhau neu chung co cung ID
	@Override public boolean equals(Object o) {
		if (o instanceof BeaconPoint) {
			BeaconPoint pc = (BeaconPoint)o;
			return pc.mId == mId;
		}
		return false;
	}

	// for debug
	@Override public String toString() {
		return String.format(
			"[Beacon id=%d,name=%s,x=%f,y=%f,zone=%s,floor=%s,carpark=%s,is_welcome=%s]",
			mId,
			mName,
			mX,
			mY,
			mZone,
			mFloor,
			mCarparkId,
			mBeaconType
		);
	}
}