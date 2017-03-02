package neublick.locatemylot.model;

import java.util.StringTokenizer;

public class MapViewState {
	// map state
	public float SCALE 		= 1f;
	public float TRANS_X 	= 1f;
	public float TRANS_Y 	= 1f;

	// car object state
	public float CAR_LOCATION_X;
	public float CAR_LOCATION_Y;

	protected MapViewState() {

	}

	public static MapViewState from(String s) {
		MapViewState result = new MapViewState();
		StringTokenizer tokenizer = new StringTokenizer(s, "|");
		if (tokenizer.hasMoreTokens()) {
			StringTokenizer otherTokenizer = new StringTokenizer(tokenizer.nextToken(), ":");
			if (otherTokenizer.hasMoreTokens()) {
				result.SCALE 	= Float.valueOf(otherTokenizer.nextToken());
				result.TRANS_X 	= Float.valueOf(otherTokenizer.nextToken());
				result.TRANS_Y 	= Float.valueOf(otherTokenizer.nextToken());
			}
			otherTokenizer = new StringTokenizer(tokenizer.nextToken(), ":");
			if (otherTokenizer.hasMoreTokens()) {
				result.CAR_LOCATION_X = Float.valueOf(otherTokenizer.nextToken());
				result.CAR_LOCATION_Y = Float.valueOf(otherTokenizer.nextToken());
			}
		}
		return result;
	}
}