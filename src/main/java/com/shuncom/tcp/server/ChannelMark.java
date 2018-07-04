package com.shuncom.tcp.server;

public class ChannelMark {

    private final Object id;
	
	public ChannelMark(Object id) {
		if (id == null) {
			throw new NullPointerException("id");
		}
		this.id = id;
	}
	
	public Object getId() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj || 
		(obj instanceof ChannelMark && id.equals(((ChannelMark)obj).getId()));
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override 
	public String toString() {
		return id.toString();
	}
}
