package com.safziy.comom.service.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.safziy.commom.service.log.LogUtil;

public class ThreadPoolControlCenter implements ControlCenter{
	
	private ThreadPoolExecutor workPool = null;

	private String threadName;

	private String flag = "";

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	private int corePoolSize;

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public long getRecycleMilliseconds() {
		return recycleMilliseconds;
	}

	public void setRecycleMilliseconds(long recycleSeconds) {
		this.recycleMilliseconds = recycleSeconds;
	}

	private int maxPoolSize;
	private long recycleMilliseconds;

	private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

	public void handle(Runnable r) {
		if (null != workPool) {
			try {
				workPool.execute(r);
			} catch (Throwable e) {
				LogUtil.error(e);
			}
		}
	}

	private boolean isStop() {
		if (null != workPool)
			return workPool.isTerminated();
		else
			return true;
	}

	private void stop() {
		if (null != workPool)
			workPool.shutdown();
	}

	public void waitToStop(int sleepTime) {
		LogUtil.info("正在停止" + this.getClass().getCanonicalName() + "  " + flag + "  " + hashCode());
		stop();
		while (true) {
			if (isStop()) {
				break;
			} else {
				try {
					Thread.sleep(sleepTime);
					LogUtil.info("正在停止" + this.getClass().getCanonicalName() + "  " + flag + "  " + hashCode());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		LogUtil.info("成功停止" + this.getClass().getCanonicalName() + "  " + flag + "  " + hashCode());
	}

	public void start() {
		workPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, recycleMilliseconds, TimeUnit.MILLISECONDS,
				workQueue, new DefaultThreadFactory(threadName));
	}

	public void waitToStop() {
		waitToStop(1000);
	}

	public <T> Future<T> submit(Callable<T> callable) {
		if (null != workPool) {
			try {
				return workPool.submit(callable);
			} catch (Throwable e) {
				LogUtil.error(e);
			}
		}
		return null;
	}

	public Future<?> submit(Runnable runnable) {
		if (null != workPool) {
			try {
				return workPool.submit(runnable);
			} catch (Exception e) {
				LogUtil.error(e);
			}
		}
		return null;
	}

	public int size() {
		return workQueue.size();
	}

}
