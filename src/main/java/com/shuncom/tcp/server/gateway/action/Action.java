package com.shuncom.tcp.server.gateway.action;

import com.shuncom.tcp.server.gateway.ChannelRequestContext;

public interface Action {

	Object execute(ChannelRequestContext requestContext) throws Exception;
}
