package com.shuncom.tcp.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.util.concurrent.DefaultEventExecutor;

public class ChannelGroupHolder {

	private static ConcurrentMap<String, CustomizedChannelGroup> groups = new ConcurrentHashMap<>();
    private static final String DEFAULT_NAME = ChannelGroupHolder.class.getName() + ".DEFAULT_NAME";
	
	public static CustomizedChannelGroup channelGroup() {
		return channelGroup(DEFAULT_NAME);
	}
	
	public static boolean addChannelGroup(String name, CustomizedChannelGroup channelGroup) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		return groups.putIfAbsent(name, channelGroup) != null;
	}
	
	public static CustomizedChannelGroup channelGroup(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (!groups.containsKey(name)) {
			groups.put(name, new CustomizedChannelGroup(name, new DefaultEventExecutor()));
		}
		return groups.get(name);
	}
	
	public static boolean contains(String name) {
	    return groups.containsKey(name);	
	}
	
	public static String[] names() {
		return groups.keySet().toArray(new String[groups.size()]);
	}
	
	public static boolean remove() {
	    return remove(DEFAULT_NAME);	
	}
	
	public static boolean remove(String name) {
		return groups.remove(name) != null;
	}
	
	public static void clear() {
		groups.clear();
	}
}
