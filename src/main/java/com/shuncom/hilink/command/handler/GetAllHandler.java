package com.shuncom.hilink.command.handler;

import org.json.JSONObject;
import com.huawei.hilink.device.cmd.HiLinkDeviceCmdUtil;
import com.huawei.hilink.device.cmd.HiLinkDeviceCommand;
import com.huawei.hilink.device.data.HiLinkDeviceData;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.DeviceReportProxy;
import com.shuncom.hilink.ErrorConstants;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;


public class GetAllHandler implements Handler {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public HiLinkDeviceCommand handle(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand) {
		
		return new HiLinkDeviceCommand(HiLinkDeviceCmdUtil.ACTION_RESPONSE, handleInner(deviceUID, deviceCommand), null);
	}
	
	

	@Override
	public String handleInner(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand) {
		String id = deviceUID.getSn();
		Device cached = DeviceCache.get(id);
		//不在线
		if (cached == null || !cached.isOnline()) {
			logger.error("Device is offline : {}", deviceUID);
			return String.valueOf(ErrorConstants.DEVICE_OFFLINE);
		}
		//没有Register cache 缓存 ，不能上报数据
		if(!RegisterCache.contains(id)) {
			logger.error("Device has no register cache : {}", deviceUID);
			return String.valueOf(ErrorConstants.DEVICE_OFFLINE);
		}
		//上报数据
		reportDeviceData(cached);
		//设备为注册状态
		cached.setRegister(true);
		//删除注册缓存
		RegisterCache.remove(id);
		//HiLinkUtil.deleteUnRegisterDevice();
		return String.valueOf(ErrorConstants.SUCCESS);
	}
	
	public void reportDeviceData(Device cached) {
		JSONObject upData = new JSONObject();
		HiLinkDeviceData deviceData;
		if(cached.isGateway()) {
			upData.put("enable", 1);
			//logger.info("Report gateway data : {} {}", cached.getId(), cached.getLocalModel());
			deviceData= HiLinkUtil.buildDeviceData(cached.getDeviceUID(), upData);
		}else {
			deviceData = HiLinkUtil.buildDefaultDeviceData(cached.getDeviceUID());
		}
		DeviceReportProxy.reportDeviceData(deviceData);
		
	}
}
