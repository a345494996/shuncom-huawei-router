package com.shuncom.tcp.server.gateway.action;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.huawei.hilink.device.status.HiLinkDeviceStatus;
import com.huawei.hilink.device.status.HiLinkDeviceStatusDetail;
import com.huawei.hilink.device.status.HiLinkDeviceStatusType;
import com.huawei.hilink.device.uid.HiLinkDeviceTypeUID;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.shuncom.hilink.DeviceReportProxy;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.hilink.PluginMonitorProxy;
import com.shuncom.hilink.command.handler.PostHandler;
import com.shuncom.tcp.server.ChannelGroupHolder;
import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.tcp.server.gateway.Server;
import com.shuncom.tcp.server.gateway.TransportContext;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.DeviceInfoBuilder;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;
import com.shuncom.tcp.server.gateway.cache.RegisterCache.PortProperties;

import io.netty.channel.Channel;
import io.netty.util.Attribute;

public class ActionUtil {
	
	public static void registerDeviceData(Device cached, int port, String deviceId, JSONObject st, String gateway, Logger logger) {
		if(cached == null || port <= 0 || deviceId == null || st == null || gateway == null) {
			if(logger != null)
				logger.error("Register cache error, param is null");
			return;
		}
		if (RegisterCache.contains(deviceId)) {
			PortProperties portProps = (PortProperties) RegisterCache.get(deviceId);
			if (portProps.containsPort(port)) {
				JSONObject cache = portProps.getPortProperties(port);
				HiLinkUtil.jsonObjectCopy(st, cache);
			} 
			else {
				portProps.addPortProperties(port, st);
			}
		} 
		//2) 未注册缓存
		else {
			PortProperties portProps = new PortProperties(deviceId);
			portProps.addPortProperties(port, HiLinkUtil.jsonObjectCopy(st, new JSONObject()));
			RegisterCache.put(deviceId, portProps);
		}
		
		//判断是否可以上报deviceAccess
		PortProperties portProps = (PortProperties) RegisterCache.get(deviceId);
		Iterator<JSONObject> itor = portProps.allProperties().values().iterator();
		for (;itor.hasNext();) {
			JSONObject it = itor.next();
			String dsp = HiLinkUtil.getLocalModel(it);
			if (dsp != null) {
				HiLinkDeviceUID deviceUID = new HiLinkDeviceUID(
						new HiLinkDeviceTypeUID(HiLinkDeviceRepository.getDeviceConfig(dsp).getUIDType()),deviceId);
				
				if(cached.getDeviceUID() == null || cached.getEp() == -1 || cached.getGateway() == null) {
					cached = new Device(deviceId, gateway, deviceUID, port, dsp);
					cached.setOnline();
					cached.setRegister(false);
					DeviceCache.put(deviceId, cached);
				}
				break;
			}
		}
	}
	
	
	
	public static void initUnRegisterDevice(Device cached, String gateway, String deviceId, Logger logger) {
		
		if(cached == null) {
			cached = new Device(deviceId, gateway, System.currentTimeMillis());
     		DeviceCache.put(deviceId, cached);
		}
		cached.setRegister(false);
		cached.setOnline();
	}
	
	
	public static void initRegisterDevice(String devUID, Device cached, String gateway, String deviceId ,int ep, Logger logger) {
		
		if(cached == null) {
			
			cached = new Device(deviceId, gateway, new HiLinkDeviceUID(devUID), ep, 
					HiLinkDeviceRepository.getLocalModel(new HiLinkDeviceUID(devUID).getDeviceTypeUID().toString()));
     		DeviceCache.put(deviceId, cached);
		}
		cached.setRegister(true);
		cached.setOnline();
		
	}
	
	
	public static boolean isRecord(String devType, Logger logger) {
		if(PostHandler.containKey("prodid")) {
			String prodid = (String) PostHandler.getRecord("prodid");
			boolean end = (boolean) PostHandler.getRecord("end");
			String postLocalModel = HiLinkDeviceRepository.getLocalModelByProdid(prodid);
			String localModel = HiLinkDeviceRepository.getLocalModel(devType);
			if(!end && localModel.equals(postLocalModel)) {
				return true;
			}
		}
		return false;
	}
	
	public static void addWhiteList(String gateway, String id, Logger logger) {
		Channel channel = ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(gateway);
		
		JSONObject response = new JSONObject();
		response.put("code", 2001);
		JSONArray checkList = new JSONArray();
		JSONObject entry = new JSONObject();
		
		entry.put("id", id);
		entry.put("control", 0);
		checkList.put(entry);
		response.put("check_list", checkList);
		ChannelUtil.simpleWriteAndFlush(channel, response);
		
	}
	
	public static void reportDeviceStatusOnline(HiLinkDeviceUID UID, Logger logger) {
		DeviceReportProxy.reportDeviceStatus(UID, new HiLinkDeviceStatus(HiLinkDeviceStatusType.ONLINE, HiLinkDeviceStatusDetail.NONE, "ONLINE"));
	}
	
	public static void reportDeviceStatusOffline(HiLinkDeviceUID UID, Logger logger) {
		DeviceReportProxy.reportDeviceStatus(UID, new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE, HiLinkDeviceStatusDetail.NONE, "OFFLINE"));
	}
	
	public static HiLinkDeviceUID getHiLinkUID(String localModel, String id, Logger logger) {
		return new HiLinkDeviceUID(HiLinkDeviceRepository.getDeviceConfig(localModel).getUIDType().toString() + ":" + id);
	}
	
	public static void reportDeviceAccess(Device device, Logger logger) {
		reportDeviceAccess(device.getGateway(), device.getId(), device.getLocalModel(), logger);
	}
	
	
	public static void reportDeviceAccess(String gateway, String id, String localModel, Logger logger) {
		DeviceReportProxy.reportDeviceAccess(PluginMonitorProxy.pkgName, 
				DeviceInfoBuilder.deviceInfo(localModel, id, gateway, "online"), DeviceInfoBuilder.serviceInfoList(localModel));
	}
	
	public static String getGatewayByChannel(Channel channel) {
		Attribute<TransportContext> attribute = ChannelUtil.getChannelMapAttribute(channel,  
				 Server.ATTRIBUTE_KEY_CHANNEL_CONTEXT, TransportContext.class);
	   	TransportContext transportContext = attribute.get();
	   	String gateway = (String) transportContext.get(TransportContext.CHANNELMARK);
	   	return gateway;
	}
}
