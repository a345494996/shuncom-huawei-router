package com.shuncom.tcp.server.gateway;


import com.shuncom.tcp.server.ChannelUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class OutboundVerifier extends ChannelOutboundHandlerAdapter {

	private static final short magic = (short) 0xAA55;
	private static final int version = 2;
	private static final int reserved = 0;
	private boolean verfyCrc = true;
	
	@Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof Packet) {
			ctx.write(msg);
			return;
		}
		Output output = (Output)msg;
		Packet packet = new Packet();
		packet.setMagic(magic);
		byte[] payload = output.getPayload();
		packet.setVersion(version);
		packet.setEnctype(output.getEnctype());
		packet.setType(output.getType());
		packet.setReserved(reserved);
		packet.setPayload(payload);
		if (1 == packet.getEnctype()) {
			TransportContext transportContext = ChannelUtil.getChannelMapAttribute(ctx.channel(),  
					 Server.ATTRIBUTE_KEY_CHANNEL_CONTEXT, TransportContext.class).get();
			encrypt(packet, transportContext);
		}
		packet.setPayloadLen((short) packet.getPayload().length);
		
		if (verfyCrc) {
			crc(packet);
		}
        ctx.write(packet, promise);
    }
	
	private void crc(Packet packet) {
		packet.setCrc(CodecUtil.crc16(packet.getPayload()));
	}
	
	private void encrypt(Packet packet, TransportContext transportContext) throws Exception {
		if (transportContext == null) {
   		   throw new NullPointerException("transportContext");
   	    }
		packet.setPayload(CodecUtil.encryptAES(transportContext.getAESKey(), transportContext.getAESIv(), packet.getPayload()));
	}
}
