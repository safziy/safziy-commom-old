package com.safziy.comom.service.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 任务控制中心
 * 
 * @author PeterLee
 * 
 */
public interface ControlCenter {
	

	/**
	 * 提交一个任务
	 * 
	 */
	public <T> Future<T> submit(Callable<T> callable);

	public Future<?> submit(Runnable runnable);

	public void handle(Runnable r);

	/**
	 * stop the multi-thread-pool
	 * 
	 */

	/**
	 * wait the multi-thread-pool stop
	 */
	public void waitToStop(int sleepTime);

	/**
	 * 默认1秒钟
	 */
	public void waitToStop();

	/**
	 * start a multi-thread-pool
	 */
	public void start();

	public void setFlag(String flag);
	
	public int size();

}
