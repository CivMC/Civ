package xyz.huskydog.banstickCore.cmc.utils;

import java.text.SimpleDateFormat;

public interface DateUtils {
    static SimpleDateFormat getDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
