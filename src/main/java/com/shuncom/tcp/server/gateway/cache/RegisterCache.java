package com.shuncom.tcp.server.gateway.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import org.json.JSONObject;

public class RegisterCache {
	
	private static Map<String, Object> registerCache = new ConcurrentHashMap<>();
	
	public static Object put(String id, Object devInfo) {
		return registerCache.put(id, devInfo);
	}
	
	public static Object get(String id) {
		return registerCache.get(id);
	}
	
	public static Object remove(String id) {
		return registerCache.remove(id);
	}
	
	public static boolean contains(String id) {
		return registerCache.containsKey(id);
	}
	
	public static Set<String> ids() {
		return new HashSet<>(registerCache.keySet());
	}

	
	public static class PortProperties {
		private final String id;
		private final Map<String, JSONObject> properties = new ConcurrentHashMap<>();
		
		public PortProperties(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public JSONObject addPortProperties(int port, JSONObject props) {
			return properties.put(String.valueOf(port), props);
		}
		
		public JSONObject getPortProperties(int port) {
			return properties.get(String.valueOf(port));
		}
		
		public boolean containsPort(int port) {
			return properties.containsKey(String.valueOf(port));
		}
		
		public JSONObject remove(int port) {
			return properties.remove(String.valueOf(port));
		}
		
		public Map<String, JSONObject> allProperties() {
			return new HashMap<>(properties);
		}

		@Override
		public String toString() {
			return "PortProperties [id=" + id + ", properties=" + properties + "]";
		} 
		
	}

}
