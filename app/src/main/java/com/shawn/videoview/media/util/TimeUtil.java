package com.shawn.videoview.media.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间格式化工具类
 * <p/>
 * author: Shawn
 * time  : 2016/8/29 15:24
 */
public class TimeUtil {
    /**
     * 格式化时间文本
     *
     * @param currentHour
     * @param currentMinute
     * @return
     */
    public static String formatTime(int currentHour, int currentMinute) {
        String fixedHour;
        String fixedMinute = currentMinute + "";
        String formatTime;
        if (currentHour < 12) {
            fixedHour = currentHour + "";
            if (currentMinute < 10) {
                fixedMinute = "0" + fixedMinute;
            }
            formatTime = fixedHour + ":" + fixedMinute + " AM";
        } else if (currentHour == 12) {
            fixedHour = currentHour + "";
            if (currentMinute < 10) {
                fixedMinute = "0" + fixedMinute;
            }
            formatTime = fixedHour + ":" + fixedMinute + " PM";
        } else {
            fixedHour = (currentHour - 12) + "";
            if (currentMinute < 10) {
                fixedMinute = "0" + fixedMinute;
            }
            formatTime = fixedHour + ":" + fixedMinute + " PM";
        }
        return formatTime;
    }

    /**
     * 转换音视频播放时间
     *
     * @param milliseconds 传入毫秒值
     * @return 返回 hh:mm:ss或mm:ss格式的数据
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatTime(long milliseconds) {
        // 获取日历函数
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat dateFormat;
        // 判断是否大于60分钟，如果大于就显示小时。设置日期格式
        if (milliseconds / 60000 > 60) {
            dateFormat = new SimpleDateFormat("hh:mm:ss");
        } else {
            dateFormat = new SimpleDateFormat("mm:ss");
        }
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 将时间戳格式化为具体日期
     *
     * @param milliseconds  Java 中为 18位的毫秒值，如果不足18位需要补齐
     * @return
     */
    public static String formatTimeStampToDate(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(milliseconds);
        //获取当前时间
        return formatter.format(curDate);
    }
}
