package it.univaq.mwt.ifame.utility;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String getDate(String ts, String format) {
        Long tstamp = Long.valueOf(ts);
        Date d = new Date(tstamp);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(d);
    }

    public static String getDateJsonFormat(String date) {
        String[] dateSplit = date.split("/");
        return dateSplit[2] + "-" + dateSplit[1] + "-" + dateSplit[0];
    }

    public static String hourFormatter(String hour, Boolean offset) {
        if (hour.split(":")[1].equals("0")) return (offset ? (Integer.valueOf(hour.split(":")[0])-1) : hour.split(":")[0]) + ":00";
        return hour;
    }

}
