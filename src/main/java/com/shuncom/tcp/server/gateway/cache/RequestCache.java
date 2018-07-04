package com.shuncom.tcp.server.gateway.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONObject;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;

public class RequestCache {
	private static final Logger logger = LoggerFactory.getLogger(RequestCache.class);
	private static final Map<RequestKey, SynchronousQueue<Object>> requests = new HashMap<>();
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static final long perid = 500;
	private static final long timeout = 8000;
	
	static {
		
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				lock.writeLock().lock();
				Set<RequestKey> keys = new HashSet<>(requests.keySet());
				Iterator<RequestKey> itor = keys.iterator();
				long current = System.currentTimeMillis();
				for (;itor.hasNext();) {
					RequestKey key = itor.next();
					if (current - key.getTime() >= timeout) {
						requests.remove(key);
						logger.info("Remove timeout request {}", key);
					}
				}
				lock.writeLock().unlock();
			}}, 0, perid);
	}
	
	public static SynchronousQueue<Object> put(RequestKey key) {
		return put(key, new SynchronousQueue<Object>());
	}
	
	public static SynchronousQueue<Object> put(RequestKey key, SynchronousQueue<Object> request) {
		lock.writeLock().lock();
		SynchronousQueue<Object> res = requests.put(key, request);
		lock.writeLock().unlock();
		return res;
	}
	
	public static SynchronousQueue<Object> get(RequestKey key) {
		lock.readLock().lock();
		SynchronousQueue<Object> res = requests.get(key);
		lock.readLock().unlock();
		return res;
	}
	
	public static SynchronousQueue<Object> remove(RequestKey key) {
		lock.writeLock().lock();
		SynchronousQueue<Object> res = requests.remove(key);
		lock.writeLock().unlock();
		return res;
	}

	public static Object poll(SynchronousQueue<Object> setter, long timeout, TimeUnit unit) {
		try {
		    return setter.poll(timeout, unit);
		} 
		catch(InterruptedException e) {
			logger.error("Poll response interrupted : {}", e.getMessage());
			JSONObject errorResponse = new JSONObject();
			errorResponse.put("result", -1);
			return errorResponse;
		}
	}
	
	public static boolean offer(SynchronousQueue<Object> getter, Object value, long timeout, TimeUnit unit) {
		try {
			return getter.offer(value, timeout, unit);
		} 
		catch(InterruptedException e) {
			logger.error("Offer response interrupted : {}", e.getMessage());
			return false;
		}
	}
}
