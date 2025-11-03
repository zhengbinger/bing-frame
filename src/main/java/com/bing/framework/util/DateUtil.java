package com.bing.framework.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * 提供日期格式化、解析、获取当前时间等静态方法
 * 封装SimpleDateFormat操作，避免重复创建实例
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
public class DateUtil {

    /**
     * 日期时间格式：yyyy-MM-dd HH:mm:ss。
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式：yyyy-MM-dd。
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式：HH:mm:ss。
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 日期格式化。
     * 
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    public static String formatDate(final Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
        return sdf.format(date);
    }

    /**
     * 日期格式化（自定义格式）。
     * 
     * @param date 日期对象
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
     */
    public static String formatDate(final Date date, final String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 解析日期字符串。
     * 
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    public static Date parseDate(final String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            throw new RuntimeException("日期解析失败: " + dateStr, e);
        }
    }

    /**
     * 日期解析。
     * 
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return 日期对象
     */
    public static Date parseDate(final String dateStr, final String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty() || pattern == null) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            throw new RuntimeException("日期解析失败: " + dateStr, e);
        }
    }

    /**
     * 获取当前时间。
     * 
     * @return 当前时间字符串
     */
    public static String getCurrentTime() {
        return formatDate(new Date());
    }

    /**
     * 获取当前日期。
     * 
     * @return 当前日期字符串
     */
    public static String getCurrentDate() {
        return formatDate(new Date(), DATE_FORMAT);
    }
}