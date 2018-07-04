package com.shuncom.tcp.server.gateway.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.huawei.hilink.device.uid.HiLinkDeviceUID;

public class DeviceCache {
	private static Map<String, Device> devices = new ConcurrentHashMap<>();
	private static List<String> gwList = new ArrayList<>(16);
	
	public static Device put(String id, Device device) {
		if(device.isGateway()) {
			gwList.add(device.getGateway());
		}
		return devices.put(id, device);
	}
	
	public static Device get(String id) {
		return devices.get(id);
	}
	
	public static String[] gwMacs() {
		return gwList.toArray(new String[gwList.size()]);
	}
	
	public static boolean containsGwMac(String mac) {
		String[] values = gwList.toArray(new String[gwList.size()]);
		for(String value : values) {
			if(value.equals(mac)) {
				return true;
			}
		}
		return false;
	}
	
	public static Device remove(String id) {
		Device device = devices.get(id);
		if(device != null && device.isGateway()) {
			gwList.remove(device.getGateway());
		}
		return devices.remove(id);
	}
	
	public static boolean contains(String id) {
		return devices.containsKey(id);
	}
	
	public static Set<String> ids() {
		return new HashSet<>(devices.keySet());
	}
	
	public static Device getGateway(String gateway) {
		
		Iterator<Entry<String, Device>> itor = devices.entrySet().iterator();
		for(;itor.hasNext();) {
			Entry<String, Device> entry = itor.next();
			String id = entry.getKey();
			Device device = devices.get(id);
			if(!device.isGateway()) {
				continue;
			}
			if(device.getGateway().equals(gateway)) {
				return device;
			}
		}
		return null;
	}
	public static List<Device> getDevicesByGateway(String gateway){
		List<Device> list = new ArrayList<>();
		if(gateway == null) {
			return list;
		}
		Iterator<Entry<String, Device>> itor = devices.entrySet().iterator();
		for(;itor.hasNext();) {
			Entry<String, Device> entry = itor.next();
			Device device = entry.getValue();
			if(gateway.equals(device.getGateway()) && device.isRegister() && !device.isGateway()) {
				list.add(device);
			}
		}
		return list;
	}
	
	public static List<Device> getDevsByGateway(String gateway){
		List<Device> list = new ArrayList<>();
		if(gateway == null) {
			return list;
		}
		Iterator<Entry<String, Device>> itor = devices.entrySet().iterator();
		for(;itor.hasNext();) {
			Entry<String, Device> entry = itor.next();
			Device device = entry.getValue();
			if(gateway.equals(device.getGateway()) && !device.isGateway()) {
				list.add(device);
			}
		}
		return list;
	}
	
	public static List<Device> findUnRegisterDevicesByProdid(String prodid, String gateway){
		List<Device> list = new ArrayList<Device>();
		String localModel = HiLinkDeviceRepository.getLocalModelByProdid(prodid);
		Iterator<Entry<String, Device>> itor = devices.entrySet().iterator();
		for(;itor.hasNext();) {
			Entry<String, Device> entry = itor.next();
			Device device = entry.getValue();
			if(device.getGateway() == null)
				continue;
			if(device.isRegister() || !device.isOnline() 
					|| device.getDeviceUID() == null || device.getEp() == -1
					|| !device.getLocalModel().equals(localModel) || !device.getGateway().equals(gateway)) {
				continue;
			}
			if(!RegisterCache.contains(device.getId())) {
				continue;
			}
			list.add(device);
		}
		return list;
	}
	
	public static final class Device {
		private final String id;
		private final String gateway;
		private final boolean isGateway;
		private boolean online = false;
		private boolean register = false;
		private final HiLinkDeviceUID deviceUID;
		private int ep;
		private final String localModel;
		private final long beginTime;
		
		
		public Device(String id, String gateway, long beginTime) {
			this(id, gateway, false, null, -1, null, beginTime);
		}
		
		public Device(String id, String gateway, HiLinkDeviceUID deviceUID) {
			this(id, gateway, false, deviceUID, -1, null);
		}
		
		public Device(String id, String gateway, HiLinkDeviceUID deviceUID, int ep, String localModel) {
			this(id, gateway, false, deviceUID, ep, localModel);
		}
		
		public String getLocalModel() {
			return localModel;
		}

		public Device(String id, String gateway, boolean isGateway, HiLinkDeviceUID deviceUID) {
			this(id, gateway, isGateway, deviceUID, -1, null);
		}
		
		public Device(String id, String gateway, boolean isGateway, HiLinkDeviceUID deviceUID, int ep, String localModel) {
			this(id, gateway, isGateway, deviceUID, ep, localModel, 0);
		}
		
		public Device(String id, String gateway, boolean isGateway, HiLinkDeviceUID deviceUID, int ep, String localModel, long beginTime) {
			this.id = id;
			this.gateway = gateway;
			this.isGateway = isGateway;
			this.deviceUID = deviceUID;
			this.ep = ep;
			this.localModel = localModel;
			this.beginTime = beginTime;
		}

		public String getId() {
			return id;
		}
		
		
		public int getEp() {
			return ep;
		}
		
		public void setEp(int ep) {
			if(ep >= 0) 
				this.ep = ep;
		}

		public String getGateway() {
			return gateway;
		}
		
		public boolean isGateway() {
			return isGateway;
		}
		
		
		public synchronized void setOnline() {
			this.online = true;
		}
		
		public synchronized void setOffline() {
			this.online = false;
		}
		
		public synchronized boolean isOnline() {
			return online;
		}

		public synchronized boolean isRegister() {
			return register;
		}

		public synchronized void setRegister(boolean register) {
			this.register = register;
		}
		
		public long getBeginTime() {
			return beginTime;
		}

		public HiLinkDeviceUID getDeviceUID() {
			return deviceUID;
		}

		@Override
		public String toString() {
			return "Device [id=" + id + ", gateway=" + gateway + ", isGateway=" + isGateway + ", online=" + online
					+ ", register=" + register + ", deviceUID=" + deviceUID + ", ep=" + ep + ", localModel="
					+ localModel + "]";
		}
		
	}
}
