package com.shuncom.hilink;


import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

import org.json.JSONObject;

import com.huawei.hilink.device.cmd.HiLinkDeviceCmdUtil;
import com.huawei.hilink.device.cmd.HiLinkDeviceCommand;
import com.huawei.hilink.device.info.HiLinkDeviceInfo;
import com.huawei.hilink.device.info.HiLinkServiceInfo;
import com.huawei.hilink.device.status.HiLinkDeviceStatus;
import com.huawei.hilink.device.status.HiLinkDeviceStatusDetail;
import com.huawei.hilink.device.status.HiLinkDeviceStatusType;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.openapi.device.AbstractHiLinkDeviceCmdHandler;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.command.handler.GetAllHandler;
import com.shuncom.hilink.command.handler.Handler;
import com.shuncom.hilink.command.handler.PostHandler;
import com.shuncom.tcp.server.ChannelGroupHolder;
import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.tcp.server.gateway.Server;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.DeviceInfoBuilder;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.DeviceConfig;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;
import com.shuncom.tcp.server.gateway.cache.RequestCache;
import com.shuncom.tcp.server.gateway.cache.RequestKey;
import io.netty.channel.Channel;

public class CommandHandler implements AbstractHiLinkDeviceCmdHandler {
	
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private Handler getAllHandler = new GetAllHandler();
    private Handler postHandler = new PostHandler();
   
	public void activate() {
		logger.info("Command handler activate");
	}
	
	public void deactivate() {
    	logger.info("Command handler deactivate");
    }
	    
	@Override
	public HiLinkDeviceCommand handleCommand(HiLinkDeviceUID hiLinkDeviceUID, HiLinkDeviceCommand hiLinkDeviceCommand) {
		logger.debug("Handle command, devUID:{}, devCommand:{}", hiLinkDeviceUID, hiLinkDeviceCommand);
		switch(hiLinkDeviceCommand.getAction()) {
		   case HiLinkDeviceCmdUtil.ACTION_GETALL : return getAllHandler.handle(hiLinkDeviceUID, hiLinkDeviceCommand);
		   case HiLinkDeviceCmdUtil.ACTION_POST : return postHandler.handle(hiLinkDeviceUID, hiLinkDeviceCommand);
		   default : {
			   JSONObject response = new JSONObject();
			   response.put("errcode", ErrorConstants.PARAMS_ERROR); //5100 - 参数错误
			   String errdesc = response.toString();
			   return new HiLinkDeviceCommand(HiLinkDeviceCmdUtil.ACTION_RESPONSE, errdesc, null);
		   }
		}
	}

	@Override
	public String getManu() {
		return PluginMonitorProxy.pluginInfo.getPluginManu();
	}

	@Override
	public String getPkgName() {
		return PluginMonitorProxy.pluginInfo.getPkgName();
	}
	
	@Override
	public void startScan() {
		Set<String> set = DeviceCache.ids();
		String[] ids = set.toArray(new String[] {});
		for(String id : ids) {
			Device device = DeviceCache.get(id);
			if(device.getGateway() == null || device.isGateway() )
				continue;
			DeviceCache.remove(id);
			if(RegisterCache.contains(id))
				RegisterCache.remove(id);
			
			Channel channel = HiLinkUtil.getServerChannel(device.getGateway());
			HiLinkUtil.deleteDeviceById(id, channel);
			logger.info("Delete {}", id);
			
		}
		
		
	}

	@Override
	public String deviceRemove() {
		return null;
	}

	@Override
	public String deviceRemove(HiLinkDeviceUID hiLinkDeviceUID) {
		
		String deviceId = hiLinkDeviceUID.getSn();
		logger.debug("Remove device :{} ", deviceId);
		Device device = DeviceCache.get(deviceId);
		if (device == null || !device.isOnline()) {
			logger.error("Device {} is offline", deviceId);
			return ErrorConstants.valueOfString(ErrorConstants.DEVICE_OFFLINE);
		} 
		Channel channel = ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(device.getGateway());
		if (channel == null) {
			return ErrorConstants.valueOfString(ErrorConstants.DEVICE_OFFLINE);
		}
		if(device.isGateway()) {
			DeviceCache.remove(deviceId);
			String gateway = device.getGateway();
			List<Device> deviceList =DeviceCache.getDevsByGateway(gateway);
			for(int i = 0; i < deviceList.size(); i++) {
				Device dev = deviceList.get(i);
				logger.debug("Need to offline device :{}", dev);
				String devId = dev.getId();
				
				if(dev.isRegister())
					DeviceReportProxy.reportDeviceStatus(dev.getDeviceUID(), new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE, HiLinkDeviceStatusDetail.NONE, "OFFLINE"));
				DeviceCache.remove(devId);
				if(RegisterCache.contains(devId)) {
					RegisterCache.remove(devId);
				}
				JSONObject response = HiLinkUtil.deleteDeviceById(devId, channel);
				if (response == null) {
					logger.error("Delete device {} failed", devId);
					return ErrorConstants.valueOfString(ErrorConstants.REQUEST_TIMEOUT);
				}
				if (0 == response.getInt("result")) {
					//清除设备注册信息
					//DeviceReportProxy.reportDeviceRemoved(hiLinkDeviceUID);
					logger.info("Success to remove device : {}", devId);
				}
			}
			return ErrorConstants.valueOfString(ErrorConstants.SUCCESS);
			
		} else {
			
				DeviceCache.remove(deviceId);
				if(RegisterCache.contains(deviceId)) {
					RegisterCache.remove(deviceId);
				}
				JSONObject response = HiLinkUtil.deleteDeviceById(deviceId, channel);
				if (response == null) {
					logger.error("Delete device {} failed", deviceId);
					return ErrorConstants.valueOfString(ErrorConstants.REQUEST_TIMEOUT);
				}
				if (0 == response.getInt("result")) {
					//清除设备注册信息
					//DeviceReportProxy.reportDeviceRemoved(hiLinkDeviceUID);
					logger.info("Success to remove device : {}", deviceId);
					return ErrorConstants.valueOfString(ErrorConstants.SUCCESS);
				}	
		}
		return ErrorConstants.valueOfString(ErrorConstants.INTERNAL_ERROR);
	}

	@Override
	public String deviceRegister(String mac, String id) {
		//1.make sure device UID
		Device device = DeviceCache.getGateway(mac);
		if(device == null) {
			return ErrorConstants.valueOfString(ErrorConstants.DEVICE_OFFLINE);
		}
		HiLinkDeviceUID UID = device.getDeviceUID();
		Channel channel =  ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(device.getGateway());
		String online = "online";
		if(channel == null) {
			logger.error("Gateway {} channel is null", device.getId());
			return ErrorConstants.valueOfString(ErrorConstants.SUCCESS);
		}
		//2.device info
		DeviceConfig devConfig = HiLinkDeviceRepository.getDeviceConfig(device.getLocalModel());
		HiLinkDeviceInfo deviceInfo = DeviceInfoBuilder.deviceInfo(devConfig.getDeviceInfo(), device.getId(), mac, online);
		List<HiLinkServiceInfo> serviceInfoList = DeviceInfoBuilder.serviceInfoList(devConfig.getServiceInfos());
		DeviceReportProxy.reportDeviceDiscovered(UID, deviceInfo, serviceInfoList);
		logger.debug("Gateway device config :{}, device info :{}, serviceInfoList :{}", devConfig, deviceInfo, serviceInfoList);
		return ErrorConstants.valueOfString(ErrorConstants.SUCCESS);
	}


	
	
}

