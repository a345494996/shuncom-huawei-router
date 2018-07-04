package com.shuncom.tcp.server.gateway.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.huawei.hilink.device.info.HiLinkDeviceInfo;
import com.huawei.hilink.device.info.HiLinkDeviceInfoUtil;
import com.huawei.hilink.device.info.HiLinkServiceInfo;
import com.huawei.hilink.device.uid.HiLinkDeviceTypeUID;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.DeviceConfig;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.DeviceInfo;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.ServiceInfo;

public class DeviceInfoBuilder {
	
	

	//build deviceInfo
	public static HiLinkDeviceInfo deviceInfo(DeviceInfo devInfo, String id, String mac, String status) {
		
		HiLinkDeviceInfo deviceInfo = new HiLinkDeviceInfo();
		if(devInfo == null) {
			return deviceInfo;
		}
		Device device = DeviceCache.get(id);
		deviceInfo.setDevType(devInfo.getDevType());
		deviceInfo.setManu(devInfo.getManu());
		deviceInfo.setModel(devInfo.getModel());
		deviceInfo.setName(devInfo.getName());
		deviceInfo.setSn(id);
		deviceInfo.setMac(mac);
		deviceInfo.setDescription(devInfo.getDescription());
		deviceInfo.setFwv(devInfo.getFwv());
		deviceInfo.setHiv(devInfo.getHiv());
		deviceInfo.setHwv(devInfo.getHwv());
		deviceInfo.setStatus(String.valueOf(status));
		deviceInfo.setPkgName(devInfo.getPkgName());
		deviceInfo.setProdId(devInfo.getProdId());
		deviceInfo.setSwv(devInfo.getSwv());
		deviceInfo.setProtType(HiLinkDeviceInfoUtil.DEV_PROTTYPE_ZIGBEE);
		deviceInfo.setIpAddr("192.168.1.4");
		if(device.isGateway()) {
			deviceInfo.setIsSubDevice(false);
			deviceInfo.setBridgeUID(device.getDeviceUID());
		}else {
			Device gwDevice = DeviceCache.getGateway(device.getGateway());
			deviceInfo.setBridgeUID(gwDevice.getDeviceUID());
			deviceInfo.setIsSubDevice(true);
		}
		
		return deviceInfo;
	}
	
	public static HiLinkDeviceInfo deviceInfo(HiLinkDeviceTypeUID devTypeUID, String id, String mac, String status) {
		
		DeviceConfig devConfig = HiLinkDeviceRepository.getDeviceConfig(HiLinkDeviceRepository.getLocalModel(devTypeUID.toString()));
		HiLinkDeviceInfo deviceInfo = deviceInfo(devConfig.getDeviceInfo(), id, mac, status);
		return deviceInfo;
		
	}
	
	public static HiLinkDeviceInfo deviceInfo(String localModel, String id, String mac, String status) {
		
		DeviceConfig devConfig = HiLinkDeviceRepository.getDeviceConfig(localModel);
		HiLinkDeviceInfo deviceInfo = deviceInfo(devConfig.getDeviceInfo(), id, mac, status);
		return deviceInfo;
		
	}
	
	public static List<HiLinkServiceInfo> serviceInfoList(Map<String, ServiceInfo> svInfoMap){
		
		List<HiLinkServiceInfo> serviceInfos = new ArrayList<>(); 
		if(svInfoMap == null) {
			return serviceInfos;
		}
		Iterator<ServiceInfo> itor = svInfoMap.values().iterator();
		for(;itor.hasNext();) {
			ServiceInfo serviceInfo = itor.next();
			serviceInfos.add(new HiLinkServiceInfo(serviceInfo.getServiceId(),serviceInfo.getServiceType()));
		}
		
		return serviceInfos;
	}
	
	public static List<HiLinkServiceInfo> serviceInfoList(String localModel){
		
		Map<String, ServiceInfo> svInfoMap = HiLinkDeviceRepository.getDeviceConfig(localModel).getServiceInfos();
		return serviceInfoList(svInfoMap);
	}
	
}
