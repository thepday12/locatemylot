package neublick.locatemylot.model;

public class ParkingHistory {
	public int id=0;
	public long timeCheckIn=-1;
	public long timeCheckOut=-1;
	public float x;
	public float y;
	public String photoName;
	public String zone;
	public String floor;
	public int carpackId=-1;
	public String carparkName;
    public String liftData;
    public int isNormal;
    public float rates;

	@Override public String toString() {
		return String.format("[id=%d, timeCheckIn=%d, timeCheckOut=%d, carparkId=%d]",
			id,
			(timeCheckIn==-1)? "null": timeCheckIn,
			(timeCheckOut==-1)? "null": timeCheckOut,
			(carpackId==-1)? "null": carpackId
		);
	}
}