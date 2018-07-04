package com.shuncom.tcp.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class ChannelUtil {

	public static Channel findChannel(String groupName, Object obj) {
		CustomizedChannelGroup group = ChannelGroupHolder.channelGroup(groupName);
		return (group == null) ? null : group.findChannel(obj);
	}
	
	public static Channel findChannelWithCheck(String groupName, Object obj) {
		Channel channel = findChannel(groupName, obj);
		if (channel == null) {
			throw new ClientException("Can not find client channel " + obj.toString());
		}
		return channel;
	}
	
	public static <T> Attribute<T> getChannelMapAttribute(Channel channel, String name, Class<T> type) {
		AttributeKey<T> key =  AttributeKey.valueOf(name);
		return channel.attr(key);
	}
	
	public static ChannelFuture writeAndFlush(Channel channel, Object msg) {
		if (channel.isActive() && channel.isWritable()) {
			return channel.writeAndFlush(msg);
		}
		throw new ClientException("channel is not avaliable " + channel);
	}
	
	public static ChannelFuture simpleWriteAndFlush(Channel channel, Object msg) {
		if (channel.isActive() && channel.isWritable()) {
			return channel.writeAndFlush(msg);
		}
		return null;
	}
}
