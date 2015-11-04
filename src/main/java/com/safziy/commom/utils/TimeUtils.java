package com.safziy.commom.utils;

import java.util.Calendar;

public class TimeUtils {

	/**
	 * 一秒的毫秒数
	 */
	public static final long ONE_SECONDS_MICROSECONDS = 1000;

	/**
	 * 一小时秒数
	 */
	public static final int ONE_HOUR_SECONDS = 60 * 60;

	/**
	 * 一小时毫秒数
	 */
	public static final int ONE_HOUR_MICROSECONDS = 60 * 60 * 1000;

	/**
	 * 一小时的分钟数
	 */
	public static final int ONE_HOUR_MINUTES = 60;

	/**
	 * 一分钟的秒数
	 */
	public static final int ONE_MINUTE_SECONDS = 60;

	/**
	 * 一分钟的毫秒数
	 */
	public static final long ONE_MINUTE_MICROSECONDS = 60 * 1000;

	/**
	 * 一天的秒数
	 */
	public static int ONE_DAY_SECONDS = 60 * 24 * 60;

	/**
	 * 一天的毫秒
	 */
	public static long ONE_DAY_MICROSECONDS = 60 * 24 * 60 * 1000;

	public static final int FIVE_HOUR = 5;

	public static long getIntervalMilliSecond(long time1, long time2) {
		return time1 - time2;
	}

	/**
	 * 得到某两个时间的时间差（以秒为单位）
	 * 
	 * @param time2
	 * @param time1
	 * @return
	 */
	public static int getIntervalSeconds(long time1, long time2) {
		return (int) ((time1 - time2) / 1000);
	}

	public static int getIntervalMinutes(long time1, long time2) {
		return (int) ((time1 - time2) / TimeUtils.ONE_MINUTE_MICROSECONDS);
	}

	public static int getCurrentTimeSeconds() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	public static long getmicSeconds(int time) {
		return (time * 1000);
	}

	/**
	 * 得到第一个时间和地二个时间的时间差（以秒为单位），如果第一个时间time1<=time2返回0
	 * 
	 * @param time2
	 * @param time1
	 * @return
	 */
	public static int getIntervalMoreAndEqualZeroSeconds(long time1, long time2) {
		int remainSeconds = 0;
		if (time1 > time2) {
			remainSeconds = (int) ((time1 - time2) / 1000);
		}
		return remainSeconds;
	}

	/**
	 * 得到第一个时间和地二个时间的时间差（以秒为单位），如果第一个时间time1<=time2返回0
	 * 
	 * @param time2
	 * @param time1
	 * @return
	 */
	public static int getIntervalMoreAndEqualZeroSeconds(int time1, int time2) {
		int remainSeconds = 0;
		if (time1 > time2) {
			remainSeconds = time1 - time2;
		}
		return remainSeconds;
	}

	/**
	 * 得到加速后剩余的完成时间
	 * 
	 * @return
	 */
	public static int getAccelerateMoreThanZeroRemainSeconds(long finishTime, int accelerateSeconds) {
		int remainSeconds = TimeUtils.getIntervalSeconds(finishTime, System.currentTimeMillis()) - accelerateSeconds;
		if (remainSeconds < 0) {
			remainSeconds = 0;
		}
		return remainSeconds;
	}

	/**
	 * 得到结束时间 单位是毫秒
	 */
	public static long getFinishTime(long beginTime, int seconds) {
		return beginTime + seconds * 1000;
	}

	/**
	 * 得到结束时间，单位是秒
	 * 
	 * @param beginTime
	 * @param seconds
	 * @return
	 */
	public static int getFinishTime(int beginTime, int seconds) {
		return beginTime + seconds;
	}

	/**
	 * 得到按天离传入时间最近的一次凌晨时间(即上一个凌晨)
	 * 
	 * @return
	 */
	public static long getLastWeeHoursByDay(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	public static long getLastSpecialHoursByDay(long time, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		if (c.get(Calendar.HOUR_OF_DAY) < hour) {
			c.add(Calendar.DAY_OF_YEAR, -1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTimeInMillis();
	}

	/**
	 * 得到按周离传入时间最近的一次凌晨时间(即上一个周日的凌晨)
	 */
	public static long getLastWeeHoursByWeek(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		if (c.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else {
			c.add(Calendar.WEEK_OF_YEAR, -1);
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static long getLastSpecialHoursByWeek(long time, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		if (c.get(Calendar.DAY_OF_WEEK) > Calendar.MONDAY
				|| (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && c.get(Calendar.HOUR_OF_DAY) >= hour)) {
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else {
			c.add(Calendar.WEEK_OF_YEAR, -1);
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	/**
	 * 得到按月离传入时间最近的一次凌晨时间(即上一个周日的凌晨)
	 */
	public static long getLastWeeHoursByMonth(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);

		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static long getLastSpecialHoursByMonth(long time, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		if (c.get(Calendar.DAY_OF_MONTH) == 1 && c.get(Calendar.HOUR_OF_DAY) < hour) {
			c.add(Calendar.MONTH, -1);
		}
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	/**
	 * 得到 当天的 某小时时间
	 * 
	 * @param hour
	 * @return
	 */
	public static long getTimeOfHour(int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public static long getTimeOfHour(long currentTime, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	/**
	 * 得到 当天某小时的 秒
	 * 
	 * @param hour
	 * @return
	 */
	public static int getSecondOfOneDay(int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return (int) (c.getTimeInMillis() / 1000);
	}

	/**
	 * 得到当天的 某个时间点对应的long类型时间
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public static long getTimeOfOneDay(int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	/**
	 * 当前时间是否在 start和end之间(包含)
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean isBetween(int start, int end) {
		long currentTime = System.currentTimeMillis();
		return checkTime(currentTime, start, end);
	}

	/**
	 * currentTime 是否在 start和end之间(包含)
	 * 
	 * @param currentTime
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean isBetweenOfHour(long currentTime, int start, int end) {
		return checkTime(currentTime, start, end);
	}

	private static boolean checkTime(long currentTime, int start, int end) {
		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(currentTime);
		c1.set(Calendar.HOUR_OF_DAY, start);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);

		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(currentTime);
		c2.set(Calendar.HOUR_OF_DAY, end);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);

		if (currentTime >= c1.getTimeInMillis() && currentTime <= c2.getTimeInMillis()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 按天返回下一个凌晨的时间（今天晚上12点）
	 * 
	 * @param date
	 * @return
	 */
	public static long getNextWeeHours(long date) {
		return getLastWeeHoursByDay(date + TimeUtils.ONE_DAY_SECONDS * 1000);
	}

	public static long getNextSpecialHours(long date, int hour) {
		return getLastSpecialHoursByDay(date + TimeUtils.ONE_DAY_SECONDS * 1000, hour);
	}

	/**
	 * time1和time2的间隔天数 如果time1<time2则算出的天数为0
	 * 
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getIntervalMoreAndEqualZeroDay(long time1, long time2) {
		long weeHour1 = getLastWeeHoursByDay(time1);
		long weeHour2 = getLastWeeHoursByDay(time2);
		int returnValue = (int) ((weeHour1 - weeHour2) / (ONE_DAY_SECONDS * 1000));
		if (returnValue < 0) {
			returnValue = 0;
		}
		return returnValue;
	}

	public static int getIntervalMoreAndEqualZeroDay(long time1, long time2, int hour) {
		long specialHour1 = getLastSpecialHoursByDay(time1, hour);
		long specialHour2 = getLastSpecialHoursByDay(time2, hour);
		int returnValue = (int) ((specialHour1 - specialHour2) / (ONE_DAY_SECONDS * 1000));
		if (returnValue < 0) {
			returnValue = 0;
		}
		return returnValue;
	}

	/**
	 * 将秒转换成N时N分N秒
	 * 
	 * @param seconds
	 * @return
	 */
	public static final String convertSecondsToShow(int seconds) {
		int hour = seconds / ONE_HOUR_SECONDS;
		seconds = seconds % ONE_DAY_SECONDS;
		int minute = seconds / ONE_MINUTE_SECONDS;
		seconds = seconds % ONE_MINUTE_SECONDS;
		return trim(hour) + ":" + trim(minute) + ":" + trim(seconds);
	}

	private static String trim(int number) {
		String value = number + "";
		if (value.length() == 1) {
			return "0" + value;
		} else {
			return value;
		}
	}

	/**
	 * 返回距离未来某个时间的秒数
	 * 
	 * @param currentTime
	 * @return
	 */
	public static int getIntervalSecond(long currentTime, int dayOfWeek, int hour, int minutes) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		c.set(Calendar.DAY_OF_WEEK, (dayOfWeek + 1) % 7);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (c.getTimeInMillis() < currentTime) {
			c.add(Calendar.DAY_OF_MONTH, +7);
		}
		return getIntervalMoreAndEqualZeroSeconds(c.getTimeInMillis(), currentTime);
	}

	/**
	 * 返回距离过去最近的某个时间的秒数
	 * 
	 * @param currentTime
	 * @param dayOfWeek
	 * @param hour
	 * @param minutes
	 * @return
	 */
	public static int getIntervalSecondsByLastTime(long currentTime, int dayOfWeek, int hour, int minutes) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		c.set(Calendar.DAY_OF_WEEK, (dayOfWeek + 1) % 7);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (c.getTimeInMillis() > currentTime) {
			c.add(Calendar.DAY_OF_MONTH, -7);
		}
		return getIntervalMoreAndEqualZeroSeconds(currentTime, c.getTimeInMillis());
	}

	/**
	 * 获得过去某天的时间(long)
	 * 
	 * @param dayOfWeek
	 * @param hour
	 * @param minutes
	 * @return
	 */
	public static long getTimeOfThePassDay(int dayOfWeek, int hour, int minutes) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK, (dayOfWeek + 1) % 7);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (c.getTimeInMillis() > System.currentTimeMillis()) {
			c.add(Calendar.DAY_OF_MONTH, -7);
		}
		return c.getTimeInMillis();
	}

	public static int getHourOfTime(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 秒转化为分钟
	 * 
	 * @param seconds
	 * @return
	 */
	public static int sec2Min(long seconds) {
		return (int) (seconds / TimeUtils.ONE_MINUTE_SECONDS);
	}

	/**
	 * 秒转化为分钟(不足一分钟的部分算一分钟)
	 * 
	 * @param seconds
	 * @return
	 */
	public static int sec2MinCeil(long seconds) {
		int min = (int) (seconds / TimeUtils.ONE_MINUTE_SECONDS);
		if (seconds % TimeUtils.ONE_MINUTE_SECONDS != 0) {
			min++;
		}
		return min;
	}

	/**
	 * 分钟转化为秒
	 * 
	 * @param seconds
	 * @return
	 */
	public static int min2Sec(int min) {
		return (int) (min * TimeUtils.ONE_MINUTE_SECONDS);
	}

	/**
	 * 秒转化为小时
	 * 
	 * @param seconds
	 * @return
	 */
	public static int sec2Hour(int seconds) {
		return (int) (seconds / TimeUtils.ONE_HOUR_SECONDS);
	}

	/**
	 * 小时转化为秒
	 */
	public static int hour2Sec(int hour) {
		return hour * TimeUtils.ONE_HOUR_SECONDS;
	}

	/**
	 * 毫秒转化为秒
	 * 
	 * @param seconds
	 * @return
	 */
	public static int mic2Sec(long mic) {
		return (int) (mic / TimeUtils.ONE_SECONDS_MICROSECONDS);
	}

	public static long sec2Mic(int seconds) {
		return seconds * TimeUtils.ONE_SECONDS_MICROSECONDS;
	}

	/**
	 * 获得时间属于星期几(东八区)
	 */
	public static int getDayOfWeek(long currentTime) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		if (ValidateUtils.isEqual(dayOfWeek, Calendar.SUNDAY)) {
			return 7;
		} else {
			return --dayOfWeek;
		}
	}

	public static int getDayOfWeek(long currentTime, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime - hour * ONE_HOUR_MICROSECONDS);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		if (ValidateUtils.isEqual(dayOfWeek, Calendar.SUNDAY)) {
			return 7;
		} else {
			return --dayOfWeek;
		}
	}

	/**
	 * 获得时间属于几月(东八区)
	 */
	public static int getMouth(long currentTime) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime);
		return c.get(Calendar.MONTH) + 1;
	}

	public static int getMouth(long currentTime, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(currentTime - hour * ONE_HOUR_MICROSECONDS);
		return c.get(Calendar.MONTH) + 1;
	}

	public static void main(String[] args) {
		System.out.println(getDayOfWeek(System.currentTimeMillis()));
	}

	/**
	 * 秒转换成毫秒
	 * 
	 * @param seconds
	 * @return
	 */
	public static long int2Long(int seconds) {
		return ((long) seconds) * 1000l;
	}

	/**
	 * 毫秒转换成秒
	 * 
	 * @param time
	 * @return
	 */
	public static int long2Int(long time) {
		return (int) (time / 1000);
	}

}
