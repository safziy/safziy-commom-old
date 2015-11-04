package com.safziy.commom.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static String COMMON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String COMMON_DATE_TO_HOUR_RANK = "yyyy-MM-dd HH";

	public static String DEFAULT_ONLY_DATE_FORMAT = "yyyy-MM-dd";

	public static String RANK_UPDATE_TIME_FORMAT = "yyyy年MM月dd日HH:mm";

	public static String RANK_UPDATE_TIME_FORMAT_2 = "yyyy年MM月dd日HH:mm:ss";

	public static String RANK_UPDATE_TIME_FORMAT_3 = "yyyy年MM月dd日HH";

	public static String RANK_UPDATE_TIME_FORMAT_4 = "yyyy年MM月dd日";

	public static String SEQUENCE_UPDATE_TIME_FORMAT = "yyyyMMddHHmmss";

	public static final long ONE_DAY_MILLS = 1000 * 60 * 60 * 24;

	public static final int FIVE_HOUR = 5;

	/**
	 * 
	 * @param 要转换的毫秒数
	 * @return 该毫秒数转换为 *天*小时*分* 后的格式
	 */
	public static String formatDuring(long mss) {
		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		// long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		// long seconds = (mss % (1000 * 60)) / 1000;
		return days + "天" + hours + "小时";
	}

	private static boolean _isSameDay(Date date1, Date date2) {
		return org.apache.commons.lang.time.DateUtils.isSameDay(date1, date2);
	}

	public static boolean isSameDayByWeehour(long time1, long time2) {
		Date newDate1 = new Date(time1);
		Date newDate2 = new Date(time2);
		return _isSameDay(newDate1, newDate2);
	}

	/**
	 * 已某一个整点为标准 判断是否是同一天
	 */
	public static boolean isSameDay(long date1, long date2, int hour) {
		Date newDate1 = new Date(date1 - hour * 1000 * 60 * 60);
		Date newDate2 = new Date(date2 - hour * 1000 * 60 * 60);
		return _isSameDay(newDate1, newDate2);
	}

	@Deprecated
	public static boolean isSameDay(int date1, int date2) {
		Date newDate1 = new Date(date1 * 1000L);
		Date newDate2 = new Date(date2 * 1000L);
		return _isSameDay(newDate1, newDate2);
	}

	/**
	 * 已某一个整点为标准 判断是否是同一天
	 */
	public static boolean isSameDay(int date1, int date2, int hour) {
		Date newDate1 = new Date(date1 * 1000L - hour * 1000 * 60 * 60);
		Date newDate2 = new Date(date2 * 1000L - hour * 1000 * 60 * 60);
		return _isSameDay(newDate1, newDate2);
	}

	public static boolean isSameHour(long date1, long date2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTimeInMillis(date1);
		c2.setTimeInMillis(date2);

		return ValidateUtils.isEqual(c1.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.HOUR_OF_DAY));
	}

	public static Date truncateToday() {
		return org.apache.commons.lang.time.DateUtils.truncate(new Date(), Calendar.DATE);
	}

	/**
	 * 比较date1是否比date2多了millsTime这么长的时间(date1>=date2)
	 * 
	 * @param date1
	 *            更新的时间
	 * @param date2
	 *            更老的时间
	 * @param millsTime
	 *            期望多出来的时间长度
	 */
	public static boolean compareMoreThan(Date date1, Date date2, long millsTime) {

		if ((date2.getTime() + millsTime) >= date1.getTime()) {
			return true;
		} else {
			return false;
		}
	}

	public static String formatDate(long time, String format) {
		DateFormat df;
		if (ValidateUtils.isNotNull(format)) {
			df = new SimpleDateFormat(format);
		} else {
			df = new SimpleDateFormat(DEFAULT_ONLY_DATE_FORMAT);
		}
		Date date = new Date(time);
		return df.format(date);
	}

	public static String commomFormatDate(int time) {
		DateFormat df = new SimpleDateFormat(COMMON_DATE_FORMAT);
		Date date = new Date(TimeUtils.sec2Mic(time));
		return df.format(date);
	}

	public static String commomFormatDate(long time) {
		DateFormat df = new SimpleDateFormat(COMMON_DATE_FORMAT);
		Date date = new Date(time);
		return df.format(date);
	}

	public static String formatOnlyDate(long time) {
		return formatOnlyDate(time, null);
	}

	public static String formatOnlyDate(long time, String format) {
		DateFormat df;
		if (ValidateUtils.isNotNull(format)) {
			df = new SimpleDateFormat(format);
		} else {
			df = new SimpleDateFormat(DEFAULT_ONLY_DATE_FORMAT);
		}
		Date date = new Date(time);
		return df.format(date);
	}

	public static String formatDate(long time) {
		return formatDate(time, null);
	}

	public static String now() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}

	public static String today() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(new Date());
	}

	public static long compareSecond(long oldTime, long newTime) {
		long between = (newTime - oldTime) / 1000;
		return between;
	}

	public static long compareMinute(long oldTime, long newTime) {
		long between = (newTime - oldTime) / 1000 / 60;
		return between;
	}

	public static int compareYear(long oldTime, long newTime) {
		if (oldTime > newTime) {
			return -1;
		}

		if (oldTime == newTime) {
			return 0;
		}

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(oldTime);
		int oldYear = c.get(Calendar.YEAR);
		int oldMonth = c.get(Calendar.MONTH) + 1;
		int oldDate = c.get(Calendar.DAY_OF_MONTH);

		c.setTimeInMillis(newTime);
		int newYear = c.get(Calendar.YEAR);
		int newMonth = c.get(Calendar.MONTH) + 1;
		int newDate = c.get(Calendar.DAY_OF_MONTH);

		int resultYear = newYear - oldYear;
		if (oldYear < newYear) {
			if (oldMonth > newMonth) {
				resultYear--;
			} else if (oldMonth == newMonth && oldDate > newDate) {
				resultYear--;
			}
		}

		return resultYear;
	}

	public static boolean isAdult(long birthday) {
		int year = DateUtils.compareYear(birthday, System.currentTimeMillis());
		if (year >= 18) {
			return true;
		} else {
			return false;
		}
	}

	public static String compareDate(long oldTime, long newTime) {

		long range = (newTime - oldTime);

		long days = range / 1000 / 60 / 60 / 24;

		range = (range - days * 1000 * 60 * 60 * 24);

		long hours = range / 1000 / 60 / 60;

		range = (range - hours * 1000 * 60 * 60);

		long minutes = range / 1000 / 60;

		range = (range - minutes * 1000 * 60);

		long seconds = range / 1000;

		return days + "天" + hours + "小时" + minutes + "分钟" + seconds + "秒";
	}

	/**
	 * 将日期字符串转为Date
	 * 
	 * @param pattern
	 *            指定strDate的匹配格式,如果为null则认为strDate匹配默认的pattern
	 */
	public static Date parse(String strDate, String pattern) throws ParseException {
		Date returnValue = null;

		if (null == pattern) {
			SimpleDateFormat df = new SimpleDateFormat(DEFAULT_ONLY_DATE_FORMAT);
			returnValue = df.parse(strDate);
		} else {
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			returnValue = df.parse(strDate);
		}

		return returnValue;
	}

	public static int parseCommomFormat(String strDate) throws ParseException {
		if (ValidateUtils.isNotNullAndMoreThanZero(strDate)) {
			SimpleDateFormat df = new SimpleDateFormat(COMMON_DATE_FORMAT);
			Date returnValue = df.parse(strDate);
			return TimeUtils.mic2Sec(returnValue.getTime());
		}
		return 0;
	}

	public static Date parse(String strDate) throws ParseException {
		return parse(strDate, null);
	}

	public static long getWeeHoursAfter20MinsForTodayRank() {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 20);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	public static long getTodayWeeHours() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	public static long getWeeHours(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	public static long getBeforeDay(int day) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -day);

		return c.getTimeInMillis();
	}

	public static String getTimeForDadian(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c.get(Calendar.HOUR_OF_DAY) + "" + c.get(Calendar.MINUTE) + "" + c.get(Calendar.SECOND);
	}

	public static String currentTime() {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return df.format(new Date());
	}

}
