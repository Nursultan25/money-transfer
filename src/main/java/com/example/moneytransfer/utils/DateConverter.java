package com.example.moneytransfer.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateConverter {
    public static Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault())
                        .toInstant());
    }
}
