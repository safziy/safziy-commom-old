package com.safziy.commom.utils;

import java.util.Collection;
import java.util.Map;

import org.springframework.util.Assert;

public class ValidateUtils {

	public static final String NOT_SAME_HANDCARD_COUNT_BETWEEN_CLIENT_AND_SERVER_CODE = "90000";

	/**
	 * 与 between 相反
	 * 
	 * @param source
	 * @param small
	 * @param big
	 * @return
	 */
	public static boolean notBetween(int source, int small, int big) {
		return !ValidateUtils.between(source, small, big);

	}

	/**
	 * 在small和big之间，就返回true，其中包含small和big
	 * 
	 * @param source
	 * @param small
	 * @param big
	 * @return
	 */
	public static boolean between(int source, int small, int big) {
		boolean returnValue = false;

		if (source >= small && source <= big)
			returnValue = true;
		return returnValue;

	}

	public static boolean isNull(Object o) {
		if (null != o)
			return false;
		else
			return true;
	}

	public static boolean isNotNull(Object o) {
		return !isNull(o);
	}

	public static boolean isFalse(Boolean b) {
		if (b.equals(Boolean.FALSE))
			return true;
		else
			return false;
	}

	public static boolean isEqual(boolean i, boolean j) {
		return i == j;
	}

	public static boolean isNotEqual(boolean i, boolean j) {
		return !ValidateUtils.isEqual(i, j);
	}

	public static boolean isNotEqual(Integer i, Integer j) {
		return !ValidateUtils.isEqual(i, j);
	}

	public static boolean isNotEqual(int i, int j) {
		return i != j;
	}

	public static boolean isNotEqual(byte i, byte j) {
		return i != j;
	}

	public static boolean isEqual(byte i, byte j) {
		return i == j;
	}

	public static boolean isEqual(Integer i, Integer j) {
		if (isNull(i))
			return false;
		if (isNull(j))
			return false;
		return i.equals(j);
	}

	public static boolean isEqualZero(int i) {
		return isEqual(i, 0);
	}

	public static boolean isEqual(String i, String j) {
		if (isNull(i) && isNull(j))
			return true;
		if (isNull(i))
			return false;
		if (isNull(j))
			return false;
		return i.equals(j);
	}

	public static boolean isNotEqual(String i, String j) {
		return !isEqual(i, j);
	}

	public static boolean isEqual(int i, int j) {
		return i == j;
	}

	public static boolean isEqual(long i, long j) {
		return i == j;
	}

	public static boolean isNotEqual(long i, long j) {
		return !ValidateUtils.isEqual(i, j);
	}

	public static void notNumberThrowException(String n) {
		try {
			Integer.parseInt(n);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void ifTrueThrowException(boolean b) {
		ValidateUtils.ifTrueThrowException(b, " it's true ?? ");
	}

	public static void ifTrueThrowException(boolean b, String n) {
		if (b)
			throw new IllegalArgumentException(n);
	}

	public static void ifNullThrowException(Object o) {
		Assert.notNull(o);
	}

	public static void ifNullThrowException(Object o, String message) {
		Assert.notNull(o, message);
	}

	public static void notBetweenThrowException(Integer source, Integer small, Integer big, String message) {
		if (source < small)
			throw new IllegalArgumentException(message);
		if (source > big)
			throw new IllegalArgumentException(message);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean isInstanceOf(Object o, Class z) {
		return z.isAssignableFrom(o.getClass());
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNotInstanceOf(Object o, Class z) {
		return !isInstanceOf(o, z);
	}

	public static boolean isNumber(String s) {
		if (ValidateUtils.isNotNull(s)) {
			try {
				Integer.parseInt(s);
				return true;
			} catch (Exception e) {

			}
		}
		return false;
	}

	public static boolean isFloat(String s) {
		if (ValidateUtils.isNotNull(s)) {
			try {
				Float.parseFloat(s);
				return true;
			} catch (Exception e) {

			}
		}
		return false;
	}

	public static boolean isNotNullAndMoreThanZero(String s) {
		if (isNotNull(s) && s.length() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNotNullAndMoreThanZero(Collection s) {
		if (isNotNull(s) && s.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean isNotNullAndMoreThanZero(Map map) {
		if (isNotNull(map) && map.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 包含空格
	 * 
	 * @param content
	 * @return
	 */
	public static boolean containSpaceCharacter(String content) {
		if (content.indexOf(" ") != -1) {
			return true;
		}
		return false;
	}

}
