package neublick.locatemylot.util;

public class LightweightTimerExt extends LightweightTimer {

	public long timeStart;

	// constructor
	// timeStart in milliseconds
	public LightweightTimerExt(long timeStart, Runnable r, long mills) {
		super(r, mills);
		this.timeStart = timeStart;
	}
}