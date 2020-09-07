package io.mgmeet.sdk;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SdkUtil {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static Date dateAfterMinutes(int duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, duration);
        return calendar.getTime();
    }

    public static Date parseJsonDate(String s) {
        try {
            Date date = dateFormat.parse(s);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toDateString(Date date) {
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String s = dateFormat.format(date);
        return s;
    }
}
