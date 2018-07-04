package com.shuncom.tcp.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

public class ChannelContext {
	
	private final Channel channel;

	private Map<String, Object> backingMap = new ConcurrentHashMap<>();
	
	public ChannelContext(Channel channel) {
		if (channel == null) {
			throw new IllegalArgumentException();
		}
		this.channel = channel;
	}
	
	public Channel channel() {
		return channel;
	}

	@SuppressWarnings("unchecked")
	protected <E> E getTypedValue(String key, Class<E> type) {
        E found = null;
        Object o = backingMap.get(key);
        if (o != null) {
            if (!type.isAssignableFrom(o.getClass())) {
                String msg = "Invalid object found in ChannelContext Map under key [" + key + "].  Expected type " +
                        "was [" + type.getName() + "], but the object under that key is of type " +
                        "[" + o.getClass().getName() + "].";
                throw new IllegalArgumentException(msg);
            }
            found = (E) o;
        }
        return found;
    }
	
    protected void nullSafePut(String key, Object value) {
        if (value != null) {
            put(key, value);
        }
    }
    
    protected void nullSafeAbsentPut(String key, Object value) {
        if (value != null) {
        	backingMap.putIfAbsent(key, value);
        }
    }

    public int size() {
        return backingMap.size();
    }

    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    public boolean containsKey(Object o) {
        return backingMap.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return backingMap.containsValue(o);
    }

    public Object get(Object o) {
        return backingMap.get(o);
    }

    public Object put(String s, Object o) {
        return backingMap.put(s, o);
    }

    public Object remove(Object o) {
        return backingMap.remove(o);
    }

    public void putAll(Map<? extends String, ?> map) {
        backingMap.putAll(map);
    }

    public void clear() {
        backingMap.clear();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(backingMap.keySet());
    }

    public Collection<Object> values() {
        return Collections.unmodifiableCollection(backingMap.values());
    }

    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(backingMap.entrySet());
    }
}
