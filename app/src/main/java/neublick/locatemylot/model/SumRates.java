package neublick.locatemylot.model;

/**
 * Created by theptokim on 8/15/16.
 */
public class SumRates {
    private ParkingRates parkingRates;
    private boolean isFirst;

    public SumRates(ParkingRates parkingRates, boolean isFirst) {
        this.parkingRates = parkingRates;
        this.isFirst = isFirst;
    }

    public ParkingRates getParkingRates() {
        return parkingRates;
    }

    public void setParkingRates(ParkingRates parkingRates) {
        this.parkingRates = parkingRates;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }
}
