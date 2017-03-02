package neublick.locatemylot.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import neublick.locatemylot.util.Utils;

/**
 * Created by theptokim on 8/11/16.
 */
public class Holiday {
    public Holiday(int id, String holidayDate) {
        this.id = id;
        this.holidayDate = holidayDate;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHolidayDate() {
        return holidayDate;
    }


    public boolean isHoliday(Date date){
        Date holiday = Utils.convertStringToDate(holidayDate);
        return (holiday.getYear()==date.getYear()&&holiday.getMonth()==date.getMonth()&&holiday.getDay()==date.getDay());
    }

    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate;
    }

    private int id;
    private String holidayDate;

}
