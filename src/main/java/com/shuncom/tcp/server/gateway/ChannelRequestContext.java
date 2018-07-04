package com.shuncom.tcp.server.gateway;

import com.shuncom.tcp.server.ChannelContext;

import io.netty.channel.Channel;

public class ChannelRequestContext extends ChannelContext {

	private final Object request;
	
	public ChannelRequestContext(Channel channel, Object request) {
		super(channel);
		this.request = request;
	}
	
	public Object request() {
		return request;
	}

}
