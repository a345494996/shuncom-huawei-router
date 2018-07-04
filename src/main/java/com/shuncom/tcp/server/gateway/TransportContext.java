package com.shuncom.tcp.server.gateway;

import com.shuncom.tcp.server.ChannelContext;

import io.netty.channel.Channel;


public class TransportContext extends ChannelContext {

	private static final String AES_SECRET_KEY = TransportContext.class.getName() + ".AES_SECRET_KEY";
	private static final String AES_SECRET_IV = TransportContext.class.getName() + ".AES_SECRET_IV";
	private static final String AUTHENTICATED = TransportContext.class.getName() + ".AUTHENTICATED";
	public static final String CHANNELMARK = TransportContext.class.getName() + ".CHANNELMARK";
	
	public TransportContext(Channel channel) {
		super(channel);
	}

	public byte[] getAESKey() {
		return getTypedValue(AES_SECRET_KEY, byte[].class);
	}
	
	public void setAESKey(byte[] value) {
		nullSafeAbsentPut(AES_SECRET_KEY, value);
	}
	
	public byte[] getAESIv() {
		return getTypedValue(AES_SECRET_IV, byte[].class);
	}
	
	public void setAESIv(byte[] value) {
		 nullSafeAbsentPut(AES_SECRET_IV, value);
	}
	
	public boolean isAuthenticated() {
		Boolean authenticated = getTypedValue(AUTHENTICATED, Boolean.class);
		return (authenticated == null ? false : authenticated);
	}
	
	public void setAuthenticated(boolean value) {
		nullSafeAbsentPut(AUTHENTICATED, value);
	}
}
