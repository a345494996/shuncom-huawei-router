package com.shuncom.tcp.server;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;

public class CustomizedChannelGroup extends DefaultChannelGroup {

	private Map<ChannelMark, Channel> channels = new ConcurrentHashMap<>();
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public CustomizedChannelGroup(EventExecutor executor) {
        this(executor, false);
    }
	
	public CustomizedChannelGroup(String name, EventExecutor executor) {
	    this(name, executor, false);
	}
	
	public CustomizedChannelGroup(EventExecutor executor, boolean stayClosed) {
		 super(executor, stayClosed);
	}
	
	public CustomizedChannelGroup(String name, EventExecutor executor, boolean stayClosed) {
		super(name, executor, stayClosed);
	}
	
	
	public boolean containsValue(Channel channel) {
		return this.channels.containsValue(channel);
	}
	
    @Override
    public boolean contains(Object o) {
    	if (o == null) {
    		return false;
    	}
    	lock.readLock().lock();
    	try {
    		if (super.contains(o)) {
        		return true;
        	}
        	if (channels.containsKey(o) || channels.containsKey(mark(o))) {
        		return true;
        	}
		} catch (Exception e) {
			logger.info("Catch exception :{}", e.getClass().getName());
			return false;
		} finally {
			lock.readLock().unlock();
		}
    	return false;
    }

    private ChannelMark mark(Object o) {
    	return new ChannelMark(o);
    }
    
    @Override
    public boolean add(Channel channel) {
       if (channel == null) {
    	   throw new NullPointerException("channel");
       }
       return add(mark(channel.id()), channel);
    }

    public boolean add(Object object, Channel channel) {
       return add(mark(object), channel);	
    }
    
    public boolean add(ChannelMark mark, Channel channel) {
    	if (mark == null) {
    		throw new NullPointerException("mark");
    	}
    	if (channel == null) {
    		throw new NullPointerException("channel");
    	}
    	lock.writeLock().lock();
    	try {
    		if (channels.containsKey(mark) || super.contains(channel)) {
        		return false;
        	}
        	if (super.add(channel)) {
        		channels.put(mark, channel);
        		return true;
        	}
		} catch (Exception e) {
			logger.info("Catch exception :{}", e.getClass().getName());
			return false;
		} finally {
	        lock.writeLock().unlock();
		}
        return false;
    }
    
    public Channel findChannel(Object obj) {
    	if (obj == null) {
    		throw new NullPointerException("object");
    	}
    	if (obj instanceof ChannelId) {
    		return find((ChannelId)obj);
    	}
    	ChannelMark mark = (obj instanceof ChannelMark) ? (ChannelMark)obj : mark(obj);
    	return channels.get(mark);
    }
    
    public ChannelMark findMark(ChannelId id) {
        return findMark(find(id));	
    }
    
    public ChannelMark findMark(Channel channel) {
    	if (channel == null) {
    		return null;
    	}
    	Iterator<Entry<ChannelMark, Channel>> itor = channels.entrySet().iterator();
		for (;itor.hasNext();) {
			Entry<ChannelMark, Channel> mark = itor.next();
			if (channel.equals(mark.getValue())) {
				return mark.getKey();
			}
		}
		return null;
    }
    
    @Override
    public boolean remove(Object obj) {
    	if (obj == null) {
    		return false;
    	}
    	
        lock.writeLock().lock();
        try {
        	  if (obj instanceof Channel) {
              	Channel c = (Channel)obj;
              	if (removeByChannel(c)) {
              		return true;
              	}
              }
              if (obj instanceof ChannelId) {
                  Channel c = find((ChannelId)obj);
                  if (c != null) {
                  	if (removeByChannel(c)) {
                  		return true;
                  	}
                  }
              }
              ChannelMark mark = (obj instanceof ChannelMark) ? (ChannelMark)obj : mark(obj);
              if (removeByMark(mark)) {
              	return true;
              }
		} catch (Exception e) {
			logger.info("Catch exception :{}", e.getClass().getName());
			return false;
		} finally {
			lock.writeLock().unlock();	
		}
      return false;
    }

    private boolean removeByChannel(Channel channel) {
    	if (super.remove(channel)) {
    		Iterator<Entry<ChannelMark, Channel>> itor = channels.entrySet().iterator();
    		for (;itor.hasNext();) {
    			Entry<ChannelMark, Channel> mark = itor.next();
    			if (channel.equals(mark.getValue())) {
    				channels.remove(mark.getKey());
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private boolean removeByMark(ChannelMark mark) {
    	 if (channels.containsKey(mark)) {
         	Channel channel = channels.get(mark);
         	if(super.remove(channel)) {
         		channels.remove(mark);
         		return true;
         	}
         }
    	 return false;
    }
    
    @Override
    public void clear() {
       lock.writeLock().lock();
       try {
    	   super.clear();
           channels.clear();
       } catch (Exception e) {
    	   logger.info("Catch exception :{}", e.getClass().getName());
       } finally {
    	   lock.writeLock().unlock();
       }
    }

    public Set<ChannelMark> channelMarks() {
    	return Collections.unmodifiableSet(channels.keySet());
    }
    
}
