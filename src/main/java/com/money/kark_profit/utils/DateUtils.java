package com.money.kark_profit.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static Date formatPhnomPenhTime(Date date) {
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("Asia/Phnom_Penh"));
        return Date.from(zdt.toInstant());
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

}
