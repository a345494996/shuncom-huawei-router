package com.shuncom.tcp.server.gateway.action;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.tcp.server.gateway.ChannelRequestContext;
import com.shuncom.tcp.server.gateway.Packet;
import com.shuncom.tcp.server.gateway.cache.RequestCache;
import com.shuncom.tcp.server.gateway.cache.RequestKey;

/**
 * code-102
 *
 */
public class ControlNotify implements Action {

	private static final Logger logger = LoggerFactory.getLogger(ControlNotify.class);
	@Override
	public Object execute(ChannelRequestContext requestContext) throws Exception {
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		logger.info("Control device back info :{}", request);
		int serial = request.getInt("serial");
		SynchronousQueue<Object> setter = RequestCache.get(new RequestKey(serial));
		if (setter == null) {
			logger.info("Control device result timeout : {}", request);
		} 
		else {
			boolean offered = RequestCache.offer(setter, request, 2000, TimeUnit.MILLISECONDS);
			if (!offered) {
				logger.info("Duplicate result : {}", request);
			}
		}
		return null;
	}

}
