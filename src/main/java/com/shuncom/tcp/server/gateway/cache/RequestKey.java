package com.shuncom.tcp.server.gateway.cache;

import java.security.SecureRandom;
import java.util.Random;

public class RequestKey {
	private static final Random random = new SecureRandom();
	private final int serial;
	private final long time;
	
	public RequestKey() {
		this(random.nextInt());
	}
	
	public RequestKey(int serial) {
		this.serial = serial;
	    this.time = System.currentTimeMillis();
	}
	
	public int getSerial() {
		return serial;
	}
	
	public long getTime() {
		return time;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof RequestKey)
				&& ((RequestKey)obj).getSerial() == serial;
	}
	
	@Override
	public int hashCode() {
		return serial;
	}
	
	@Override
	public String toString() {
		return "serial :" + serial + ", time:" + time;
	}
}
