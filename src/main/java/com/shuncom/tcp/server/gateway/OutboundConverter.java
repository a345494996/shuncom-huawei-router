package com.shuncom.tcp.server.gateway;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class OutboundConverter extends ChannelOutboundHandlerAdapter {

	 private static final Logger logger = LoggerFactory.getLogger("Gateway-OutboundHandler");
	 
	@Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof Output || msg instanceof Packet) {
			ctx.write(msg);
			return;
		}
		logger.debug("Send content {}", msg.toString());
		
		byte[] payload = msg.toString().getBytes("UTF-8");
		Output output = new Output(payload);
        ctx.write(output, promise);
    }
}
