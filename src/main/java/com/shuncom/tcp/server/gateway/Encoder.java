package com.shuncom.tcp.server.gateway;

import io.netty.buffer.ByteBuf;

public interface Encoder {

	ByteBuf encode(Packet packet) throws Exception;
}
