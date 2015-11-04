package com.safziy.service.log;

import org.apache.log4j.Logger;

public class LogUtil {
	private static Logger commonLogger = Logger.getLogger("commonLogger");
	private static Logger databaseLogger = Logger.getLogger("databaseLogger");
	private static Logger servletLogger = Logger.getLogger("servletLogger");

	public static void debug(String message) {
		commonLogger.debug(message);
	}

	public static void info(String message) {
		commonLogger.info(message);
	}

	public static void error(String message) {
		commonLogger.error(message);
	}
	
	public static void error(Throwable e) {
		commonLogger.error(e);
	}

	public static void databaseDebug(String message) {
		databaseLogger.debug(message);
	}

	public static void databaseInfo(String message) {
		databaseLogger.info(message);
	}

	public static void databaseError(String message) {
		databaseLogger.error(message);
	}

	public static void servletDebug(String message) {
		servletLogger.debug(message);
	}

	public static void servletInfo(String message) {
		servletLogger.info(message);
	}

	public static void servletError(String message) {
		servletLogger.error(message);
	}

}
