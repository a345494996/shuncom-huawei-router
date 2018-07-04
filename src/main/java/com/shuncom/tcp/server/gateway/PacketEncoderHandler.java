package com.shuncom.tcp.server.gateway;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.util.Hex;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class PacketEncoderHandler extends ChannelOutboundHandlerAdapter implements Encoder {

	private static final Logger logger = LoggerFactory.getLogger("Gateway-PacketCodecHandler");
	
	@Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		Packet packet = (Packet)msg;
		logger.debug("{} Send packet {}", ctx.channel().id(), packet.toString());
		
		
		ByteBuf buf = encode(packet);
		byte[] payload = new byte[buf.readableBytes()];
		buf.getBytes(0, payload);
		logger.debug("{} Send byte datagram {}", ctx.channel().id(), Hex.encodeUpperHexString(payload));
		
		packet = null;
        ctx.write(buf, promise);
    }

	@Override
	public ByteBuf encode(Packet packet) throws Exception {
		ByteBuf buf = Unpooled.buffer(10 + packet.getPayload().length);
		CodecUtil.writeFixedField(buf, 2, packet.getMagic());
		CodecUtil.writeFixedField(buf, 2, packet.getPayloadLen());
		CodecUtil.writeFixedField(buf, 1, packet.getVersion());
		CodecUtil.writeFixedField(buf, 1, packet.getEnctype());
		CodecUtil.writeFixedField(buf, 1, packet.getType());
		CodecUtil.writeFixedField(buf, 1, packet.getReserved());
		CodecUtil.writeFixedField(buf, 2, packet.getCrc());
		buf.writeBytes(packet.getPayload());
		return buf;
	}
}
