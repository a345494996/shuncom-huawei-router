package com.shuncom.tcp.server.gateway;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.util.Hex;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PacketDecoderHandler extends ChannelInboundHandlerAdapter implements Decoder {

	private static final Logger logger = LoggerFactory.getLogger("Gateway-PacketCodecHandler");
	
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf)msg;
		byte[] payload = new byte[buf.readableBytes()];
		buf.getBytes(0, payload);
		logger.debug("{} Recieved byte datagram {}", ctx.channel().id(), Hex.encodeUpperHexString(payload));
		
		Packet packet = decode(buf);
		logger.debug("{} Recieved packet {}", ctx.channel().id(), packet.toString());
		
		buf.release();
        ctx.fireChannelRead(packet);
    }

	@Override
	public Packet decode(ByteBuf buf) throws Exception {
		Packet packet = new Packet();
		packet.setMagic(buf.readShort());
		packet.setPayloadLen(buf.readShort());
		packet.setVersion(buf.readByte());
		packet.setEnctype(buf.readByte());
		packet.setType(buf.readByte());
		packet.setReserved(buf.readByte());
		packet.setCrc(buf.readShort());
		byte[] payload = new byte[packet.getPayloadLen()]; 
		buf.readBytes(payload);
		packet.setPayload(payload);
		return packet;
	}
}
