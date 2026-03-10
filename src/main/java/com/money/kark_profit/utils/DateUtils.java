package com.money.kark_profit.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtils {

    public static Date formatPhnomPenhTime(Date date) {
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("Asia/Phnom_Penh"));
        return Date.from(zdt.toInstant());
    }

}
