package com.shuncom.tcp.server.gateway.action;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.huawei.hilink.device.info.HiLinkDevice;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.DeviceMngrProxy;
import com.shuncom.tcp.server.gateway.ChannelRequestContext;
import com.shuncom.tcp.server.gateway.Packet;

/**
 *code-201 
 *
 */
public class Register implements Action {
	private static final Logger logger = LoggerFactory.getLogger(Register.class);
	private Map<String, RegisterRequest> regs = new ConcurrentHashMap<>();
	
	@Override
	public Object execute(ChannelRequestContext requestContext) throws Exception {
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		clear();  //娓呯悊缂撳瓨璁板綍
		String id = request.getString("id");
		if (regs.containsKey(id)) {  //蹇界暐閲嶅璇锋眰
			logger.info("Duplicate register request : {}", request);
		}
		else {
			RegisterRequest regRequest = new RegisterRequest(id, System.currentTimeMillis());
			regs.put(id, regRequest);
			JSONObject response = new JSONObject();
			response.put("code", 2001);
			JSONArray checkList = new JSONArray();
			JSONObject entry = new JSONObject();
			entry.put("id", id);
			entry.put("control", 0);
			checkList.put(entry);
			response.put("check_list", checkList);
			return response;
		}
		
		return null;
		
		
	}
	
	private void clear() {
	    Set<String> set = new HashSet<>(regs.keySet());
	    Iterator<String> itor = set.iterator();
	    long current = System.currentTimeMillis();
	    while (itor.hasNext()) {
	    	String key = itor.next();
	    	RegisterRequest regRequest = regs.get(key);
	    	if (regRequest != null && ((current - regRequest.getTime()) > 600000)) {
	    		regs.remove(key);
	    	}
	    }
	}
	
	private static class RegisterRequest {
		private final String id;
		private final long time;
		
		public RegisterRequest(String id, long time) {
			this.id = id;
			this.time = time;
		}

		public String getId() {
			return id;
		}

		public long getTime() {
			return time;
		}
	}
	
}
