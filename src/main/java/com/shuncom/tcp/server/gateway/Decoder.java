package com.shuncom.tcp.server.gateway;

import io.netty.buffer.ByteBuf;

public interface Decoder {

	Packet decode(ByteBuf buf) throws Exception;
}
