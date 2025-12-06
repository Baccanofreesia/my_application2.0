package com.example.myapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateFormatter {

    // 复用 Calendar 和 SimpleDateFormat
    private static final ThreadLocal<Calendar> CALENDAR = ThreadLocal.withInitial(Calendar::getInstance);
    private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm", Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("MM-dd", Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> DAY_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));

    public static String formatDate(long timestamp) {
        // 转换为毫秒
        long timestampMillis = timestamp * 1000;
        long currentMillis = System.currentTimeMillis();
        long diffMillis = currentMillis - timestampMillis;

        // 转换为天数
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);

        Date date = new Date(timestampMillis);

        // 今天
        if (isToday(timestampMillis)) {
            return TIME_FORMAT.get().format(date);
        }
        // 昨天
        else if (isYesterday(timestampMillis)) {
            return "昨天 " + TIME_FORMAT.get().format(date);
        }
        // 7天内
        else if (diffDays < 7) {
            return diffDays + "天前";
        }
        // 其余显示具体日期
        else {
            return DATE_FORMAT.get().format(date);
        }
    }

    private static boolean isToday(long timestampMillis) {
        Calendar target = CALENDAR.get();
        Calendar now = Calendar.getInstance();

        target.setTimeInMillis(timestampMillis);

        return target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isYesterday(long timestampMillis) {
        Calendar target = CALENDAR.get();
        Calendar now = Calendar.getInstance();

        target.setTimeInMillis(timestampMillis);

        // 昨天 = 今天的日期 - 1
        return target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1;
    }

    public static String formatFullDateTime(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(timestamp * 1000));
    }
}