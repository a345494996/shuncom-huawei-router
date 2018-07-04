package com.shuncom.tcp.server.gateway;

import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.util.ValidationException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundVerifier extends ChannelInboundHandlerAdapter {
     
	 private boolean verfyVersion = false;
	 private boolean verfyCrc = true;
	 
	 @Override
	 public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 Packet packet = (Packet)msg;
		 if (verfyVersion && 2 != packet.getVersion()) {
			 throw new ValidationException("Version is wrong " + packet.getVersion());
		 }
		 if (verfyCrc) {
			 boolean res = verfy(packet);
			 if (!res) {
				 throw new ValidationException("Crc is wrong");
			 }
		 }
		 if (1 == packet.getEnctype()) {
			 if (2 == packet.getType()) {
				 throw new ValidationException("Authentication packet can not be encrypted");
			 }
			 TransportContext transportContext = ChannelUtil.getChannelMapAttribute(ctx.channel(),  
					 Server.ATTRIBUTE_KEY_CHANNEL_CONTEXT, TransportContext.class).get();
			 decrypt(packet, transportContext);
		 }
	     ctx.fireChannelRead(packet);
	 }
	 
     private void decrypt(Packet packet, TransportContext transportContext) throws Exception {
    	 if (transportContext == null) {
    		 throw new NullPointerException("transportContext");
    	 }
		 packet.setPayload(CodecUtil.decryptAES(transportContext.getAESKey(), transportContext.getAESIv(), packet.getPayload()));
	 }
	 
	 private boolean verfy(Packet packet) {
		 return packet.getCrc() == CodecUtil.crc16(packet.getPayload());
	 }
}
