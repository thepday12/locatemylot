package neublick.locatemylot.model;

/**
 * Created by theptokim on 8/11/16.
 */
public class ParkingRates {
    private int id;
    private int carparkId;
    private int dayType;
    private String beginTime;
    private String endTime;
    private int firstMins;
    private float firstRates;
    private int subMins;
    private float subRates;
    private int status;

    public ParkingRates(int id, int carparkId, int dayType, String beginTime, String endTime, int firstMins, float firstRates, int subMins, float subRates, int status) {
        this.id = id;
        this.carparkId = carparkId;
        this.dayType = dayType;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.firstMins = firstMins;
        this.firstRates = firstRates;
        this.subMins = subMins;
        this.subRates = subRates;
        this.status = status;
    }

    public ParkingRates(){
        this.id = -100;
        this.carparkId = -100;
        this.dayType = 0;
        this.beginTime = "";
        this.endTime = "";
        this.firstMins = 0;
        this.firstRates = 0;
        this.subMins = 0;
        this.subRates = 0;
        this.status=0;
    }


    public int getDayType() {

        return dayType;
    }

    public void setDayType(int dataType) {
        this.dayType = dataType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarparkId() {
        return carparkId;
    }

    public void setCarparkId(int carparkId) {
        this.carparkId = carparkId;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getFirstMins() {
        return firstMins;
    }

    public void setFirstMins(int firstMins) {
        this.firstMins = firstMins;
    }

    public float getFirstRates() {
        return firstRates;
    }

    public void setFirstRates(float firstRates) {
        this.firstRates = firstRates;
    }

    public int getSubMins() {
        return subMins;
    }

    public void setSubMins(int subMins) {
        this.subMins = subMins;
    }

    public float getSubRates() {
        return subRates;
    }

    public void setSubRates(float subRates) {
        this.subRates = subRates;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
