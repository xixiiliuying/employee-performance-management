package com.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtils {

    /** 时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String MONTH_PATTERN = "yyyy-MM";
//    public static final String YEAR_PATTERN = "yyyy";
//
//    /** 中文时间格式 */
//    public static final String CN_DATE_TIME_PATTERN = "yyyy年MM月dd日 HH时mm分ss秒";
//    public static final String CN_DATE_PATTERN = "yyyy年MM月dd日";
//    public static final String CN_MONTH_PATTERN = "yyyy年MM月";

    /**
     * 获取当前日期时间
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取当前日期时间字符串
     */
    public static String nowDateTime() {
        return format(new Date(), DATE_TIME_PATTERN);
    }

    /**
     * 获取当前日期字符串
     */
    public static String nowDate() {
        return format(new Date(), DATE_PATTERN);
    }

    /**
     * 获取当前时间字符串
     */
    public static String nowTime() {
        return format(new Date(), TIME_PATTERN);
    }

    /**
     * 获取当前月份字符串
     */
    public static String nowMonth() {
        return format(new Date(), MONTH_PATTERN);
    }

    /**
     * 日期格式化
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 日期格式化（默认格式）
     */
    public static String format(Date date) {
        return format(date, DATE_TIME_PATTERN);
    }

    /**
     * 字符串转日期
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("日期解析失败: " + dateStr, e);
        }
    }

    /**
     * 字符串转日期（自动识别格式）
     */
    public static Date parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        String pattern;
        if (dateStr.contains(":")) {
            pattern = DATE_TIME_PATTERN;
        } else if (dateStr.contains("-")) {
            if (dateStr.length() == 10) {
                pattern = DATE_PATTERN;
            } else {
                pattern = MONTH_PATTERN;
            }
        } else {
            pattern = DATE_PATTERN;
        }

        return parse(dateStr, pattern);
    }

    /**
     * LocalDate 转 Date
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date 转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalTime 转 Date（使用当前日期）
     */
    public static Date toDate(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
        return toDate(localDateTime);
    }

    /**
     * 获取月份的第一天
     */
    public static Date getFirstDayOfMonth(String month) {
        LocalDate firstDay = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return toDate(firstDay);
    }

    /**
     * 获取月份的最后一天
     */
    public static Date getLastDayOfMonth(String month) {
        LocalDate firstDay = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        return toDate(lastDay);
    }

    /**
     * 获取指定日期的开始时间（00:00:00）
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime startOfDay = toLocalDate(date).atStartOfDay();
        return toDate(startOfDay);
    }

    /**
     * 获取指定日期的结束时间（23:59:59）
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime endOfDay = toLocalDate(date).atTime(23, 59, 59);
        return toDate(endOfDay);
    }

    /**
     * 日期加减
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * 月份加减
     */
    public static Date addMonths(Date date, int months) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * 计算两个日期之间的天数差
     */
    public static int daysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        long diff = getStartOfDay(endDate).getTime() - getStartOfDay(startDate).getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * 计算两个时间之间的小时差
     */
    public static double hoursBetween(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long diff = endTime.getTime() - startTime.getTime();
        return diff / (1000.0 * 60 * 60);
    }

    /**
     * 计算两个时间之间的分钟差
     */
    public static int minutesBetween(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long diff = endTime.getTime() - startTime.getTime();
        return (int) (diff / (1000 * 60));
    }

    /**
     * 判断是否为工作日（周一至周五）
     */
    public static boolean isWorkday(Date date) {
        if (date == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    /**
     * 判断是否为周末
     */
    public static boolean isWeekend(Date date) {
        return !isWorkday(date);
    }

    /**
     * 获取当前季度
     */
    public static int getCurrentQuarter() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        return (month - 1) / 3 + 1;
    }

    /**
     * 获取季度开始日期
     */
    public static Date getQuarterStartDate(int quarter) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int startMonth = (quarter - 1) * 3;
        calendar.set(year, startMonth, 1, 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 获取季度结束日期
     */
    public static Date getQuarterEndDate(int quarter) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int endMonth = quarter * 3;
        calendar.set(year, endMonth, 1, 23, 59, 59);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    /**
     * 获取年龄（根据生日计算）
     */
    public static int getAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    /**
     * 时间戳转日期
     */
    public static Date timestampToDate(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp);
    }

    /**
     * 日期转时间戳
     */
    public static Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }

    /**
     * 格式化持续时间（分钟转小时分钟）
     */
    public static String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "0分钟";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "分钟";
        } else if (remainingMinutes == 0) {
            return hours + "小时";
        } else {
            return hours + "小时" + remainingMinutes + "分钟";
        }
    }

    /**
     * 检查日期是否在范围内
     */
    public static boolean isDateInRange(Date date, Date startDate, Date endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.before(startDate) && !date.after(endDate);
    }

    /**
     * 获取本周第一天（周一）
     */
    public static Date getFirstDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return getStartOfDay(calendar.getTime());
    }

    /**
     * 获取本周最后一天（周日）
     */
    public static Date getLastDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return getEndOfDay(calendar.getTime());
    }

    /**
     * 比较两个日期（忽略时间）
     */
    public static int compareDateIgnoreTime(Date date1, Date date2) {
        if (date1 == null && date2 == null) return 0;
        if (date1 == null) return -1;
        if (date2 == null) return 1;

        Date d1 = getStartOfDay(date1);
        Date d2 = getStartOfDay(date2);
        return d1.compareTo(d2);
    }

    /**
     * 获取当月工作日天数
     */
    public static int getWorkdaysInMonth(String month) {
        Date startDate = getFirstDayOfMonth(month);
        Date endDate = getLastDayOfMonth(month);
        return getWorkdaysBetween(startDate, endDate);
    }

    /**
     * 获取两个日期之间的工作日天数
     */
    public static int getWorkdaysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        int workdays = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (!calendar.getTime().after(endDate)) {
            if (isWorkday(calendar.getTime())) {
                workdays++;
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return workdays;
    }
}