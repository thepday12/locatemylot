package neublick.locatemylot.util;

import java.util.List;

import neublick.locatemylot.model.BeaconPoint;

// helper for Beacon
public class BeaconUtil {

	// foundBeaconList phai la danh sach cac BeaconPoint da sap xep
	// lay beacon giao nhau giua 2 tap beacon
	public static BeaconPoint intersect(List<BeaconPoint> welcomeBeaconList,
										List<BeaconPoint> foundBeaconList,
										int sizeOfFoundBeaconList)
	{
		final int sizeOfFoundList = Math.min(foundBeaconList.size(), sizeOfFoundBeaconList);
		for(int i = 0; i < sizeOfFoundList; ++i) {
			if (welcomeBeaconList.contains(foundBeaconList.get(i))) {
				return foundBeaconList.get(i);
			}
		}
		return null;
	}
}