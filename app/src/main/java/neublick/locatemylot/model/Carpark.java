package neublick.locatemylot.model;

import neublick.locatemylot.util.GPSHelper;

public class Carpark {
    public int id=0;
    public String name="";
    public String floor="";
    public String ratesInfo="";
    public int cpType;
    public double lat;
    public double lon;
    public int lot =-1;
    @Override public String toString() {
        return String.format("[%d:%s]",
                id,
                (name.isEmpty())? "null": name
        );
    }
    public double getRange(double currentLat, double currentLon){
//		double rangeLat =lat-currentLat;
//		double rangeLon =lon-currentLon;
//		return  Math.sqrt(rangeLat*rangeLat+rangeLon/rangeLon);
        return GPSHelper.meterDistanceBetweenPoints(lat,lon,currentLat,currentLon);
    }
}