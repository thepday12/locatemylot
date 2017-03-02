package neublick.locatemylot.model;

/**
 * Created by theptokim on 8/11/16.
 */
public class ParkingSurcharge {

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

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
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

    public float getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(float surcharge) {
        this.surcharge = surcharge;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int id;
    private int carparkId;
    private int dataType;
    private String beginTime;
    private String endTime;
    private float surcharge;
    private int status;

    public ParkingSurcharge(int id, int carparkId, int dataType, String beginTime, String endTime, float surcharge, int status) {
        this.id = id;
        this.carparkId = carparkId;
        this.dataType = dataType;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.surcharge = surcharge;
        this.status = status;
    }
}
