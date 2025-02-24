package com.devotedmc.ExilePearl.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class PearlDateFormatFactory {
    public static DateFormat buildPearlDateFormat() {
        var dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }
}
