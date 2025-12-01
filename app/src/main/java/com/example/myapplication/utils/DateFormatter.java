package com.example.myapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期格式化工具（优化版）
 * 性能优化：使用 Calendar 复用，避免频繁创建对象
 */
public class DateFormatter {

    // 复用 Calendar 和 SimpleDateFormat（线程不安全，仅在单线程环境使用）
    private static final ThreadLocal<Calendar> CALENDAR = ThreadLocal.withInitial(Calendar::getInstance);
    private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm", Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("MM-dd", Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> DAY_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));

    /**
     * 格式化时间戳为友好的时间文本
     * 规则：
     * - 24h内（今天）：HH:mm
     * - 24h内（昨天）：昨天 HH:mm
     * - 7天内：x天前
     * - 其余：MM-dd
     *
     * @param timestamp 时间戳（秒）
     * @return 格式化后的文本
     */
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

    /**
     * 判断是否是今天
     * 优化：使用 Calendar 比较日期，更准确
     */
    private static boolean isToday(long timestampMillis) {
        Calendar target = CALENDAR.get();
        Calendar now = Calendar.getInstance();

        target.setTimeInMillis(timestampMillis);

        return target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 判断是否是昨天
     * 修复：原代码没有正确判断昨天的情况
     */
    private static boolean isYesterday(long timestampMillis) {
        Calendar target = CALENDAR.get();
        Calendar now = Calendar.getInstance();

        target.setTimeInMillis(timestampMillis);

        // 昨天 = 今天的日期 - 1
        return target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1;
    }

    /**
     * 格式化为完整日期时间（用于调试）
     * @param timestamp 时间戳（秒）
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatFullDateTime(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(timestamp * 1000));
    }
}