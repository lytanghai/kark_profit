package com.money.kark_profit.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtils {

    public static Date formatPhnomPenhTime(Date date) {
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("Asia/Phnom_Penh"));
        return Date.from(zdt.toInstant());
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
