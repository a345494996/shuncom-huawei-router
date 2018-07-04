package com.shuncom.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomizedThreadFactory implements ThreadFactory {

	private String threadNamePrefix;
	private int threadPriority = Thread.NORM_PRIORITY;
	private boolean daemon = false;
	private ThreadGroup threadGroup;
	private final AtomicInteger threadCount = new AtomicInteger(0);

	public CustomizedThreadFactory(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
	}

	public String getThreadNamePrefix() {
		return this.threadNamePrefix;
	}

	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	public int getThreadPriority() {
		return this.threadPriority;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public boolean isDaemon() {
		return this.daemon;
	}

	public void setThreadGroupName(String name) {
		this.threadGroup = new ThreadGroup(name);
	}

	public void setThreadGroup(ThreadGroup threadGroup) {
		this.threadGroup = threadGroup;
	}

	public ThreadGroup getThreadGroup() {
		return this.threadGroup;
	}

	public Thread createThread(Runnable runnable) {
		Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(isDaemon());
		return thread;
	}
	
	private String nextThreadName() {
		return getThreadNamePrefix() + this.threadCount.incrementAndGet();
	}

	private String getDefaultThreadNamePrefix() {
		return getClass().getSimpleName() + "-";
	}
	
	@Override
	public Thread newThread(Runnable runnable) {
		return createThread(runnable);
	}
}
