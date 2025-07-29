package com.zdd.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String YYYYMM = "yyyyMM";

    public static String getYYYYMMFormat() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMM);
        return currentDate.format(formatter);
    }
}
