package com.money.kark_profit.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("dd/MM/yyyy");

    public static Date formatPhnomPenhTime(Date date) {
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("Asia/Phnom_Penh"));
        return Date.from(zdt.toInstant());
    }

    public static Date parseDateWithCurrentTime(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(input, formatter);
        LocalDateTime localDateTime = localDate.atTime(LocalTime.now());
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String localTimeConverter(String date) {
        OffsetDateTime apiTime = OffsetDateTime.parse(date);

        ZonedDateTime phnomPenhTime = apiTime.atZoneSameInstant(
                ZoneId.of("Asia/Phnom_Penh")
        );

        return phnomPenhTime.format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        );
    }
    public static int getMonth(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate.getMonthValue(); // 1 = January, 12 = December
    }

    public static int getYear(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate.getYear();
    }

    public static Date[] getFullDay(String date) throws ParseException {
        Date inputDate = DATE_FORMAT_1.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDate);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfDay = cal.getTime();

        return new Date[]{startOfDay, endOfDay};
    }

    public static Date[] getPreviousMonthRange() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);

        // Start of month (00:00:00)
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // End of month (23:59:59)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        return new Date[]{startDate, endDate};
    }

}
