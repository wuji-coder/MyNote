package com.example.android.mynote.util;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * 类型转换类，需要在database中使用@TypeConverters({Converters.class})生效
 * @author 98578
 * @create 2020-05-29 16:49
 */
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
