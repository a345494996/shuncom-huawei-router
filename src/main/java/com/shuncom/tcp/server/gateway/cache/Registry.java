package com.shuncom.tcp.server.gateway.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Registry implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, Record> registryInfo = new ConcurrentHashMap<String, Record>();
	
	public Map<String, Record> getRegistryInfo() {
		return registryInfo;
	}

	public void setRegistryInfo(Map<String, Record> registryInfo) {
		this.registryInfo = registryInfo;
	}
	
	public static class Record implements Serializable {
	   private static final long serialVersionUID = 1L;
	   private final String id;
	   private boolean localRegister = false;
	   private String deviceUID;
	   private boolean register = false;
	   
	   public Record(String id) {
		   this(id, null);
	   }

		public Record(String id, String deviceUID) {
		   this.id = id;
		   this.deviceUID = deviceUID;
	    }

		public String getId() {
			return id;
		}
	
		public String getDeviceUID() {
			return deviceUID;
		}

		public void setDeviceUID(String deviceUID) {
			this.deviceUID = deviceUID;
		}
		
		public boolean isRegister() {
			return register;
		}

		public void setRegister(boolean register) {
			this.register = register;
		}
		
		public boolean isLocalRegister() {
			return localRegister;
		}

		public void setLocalRegister(boolean localRegister) {
			this.localRegister = localRegister;
		}
		
		@Override
		public String toString() {
			return "id:" + id + ", DeviceUID:" + deviceUID + ", register:" + register;
		}
	}
}
