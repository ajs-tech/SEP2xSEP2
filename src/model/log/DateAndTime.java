package model.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateAndTime {
    private int year;
    private int month;
    private int day;
    LocalDateTime now;

    public DateAndTime(){
        now = LocalDateTime.now();
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();
    }

    public String getTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

    public String getDateAndTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        return now.format(formatter);
    }

    public String toString(){
        return getDateAndTime();
    }
}
