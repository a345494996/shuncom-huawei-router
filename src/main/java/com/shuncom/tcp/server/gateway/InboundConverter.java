package com.shuncom.tcp.server.gateway;


import org.json.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundConverter extends ChannelInboundHandlerAdapter {

	 //private static final Logger logger = LoggerFactory.getLogger("Gateway-InboundHandler");
	 
	 @Override
	 public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 Packet packet = (Packet)msg;
		 String message = new String(packet.getPayload(), "UTF-8");
		 JSONObject convert = CodecUtil.toJSON(message);
		 packet.setConvert(convert);
		 //logger.debug("Recieved content {}", packet.getConvert());
		 ctx.fireChannelRead(packet);
	 }
	 
}
