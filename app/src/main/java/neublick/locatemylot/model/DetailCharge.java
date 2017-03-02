package neublick.locatemylot.model;

import java.util.Date;

import neublick.locatemylot.util.Utils;

/**
 * Created by theptokim on 8/25/16.
 */
public class DetailCharge {
    public Date getDate() {
        return date;
    }

    public String getStringDate() {
        return Utils.convertDateTimeToString(date).substring(0,16);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public float getRates() {
        return rates;
    }

    public void setRates(float rates) {
        this.rates = rates;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRangeTime() {
        return rangeTime;
    }

    public void setRangeTime(String rangeTime) {
        this.rangeTime = rangeTime;
    }

    public DetailCharge(Date date, int time, float rates, int type, String rangeTime) {
        this.date = date;
        this.time = time;
        this.rates = rates;
        this.type = type;
        this.rangeTime = rangeTime;
    }

    public DetailCharge(Date date, float rates, String rangeTime) {
        this.date = date;
        this.rates = rates;
        this.rangeTime = rangeTime;
    }

    private Date date;
    private int time;
    private float rates;
    private int type;//0- firstTime  1-secondTime 2-Zone
    private String rangeTime;

}
