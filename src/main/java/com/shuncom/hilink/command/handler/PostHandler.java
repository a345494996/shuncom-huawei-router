package com.shuncom.hilink.command.handler;


import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import com.huawei.hilink.device.cmd.HiLinkDeviceCmdUtil;
import com.huawei.hilink.device.cmd.HiLinkDeviceCommand;
import com.huawei.hilink.device.data.HiLinkDeviceData;
import com.huawei.hilink.device.data.HiLinkServiceData;
import com.huawei.hilink.device.info.HiLinkDeviceInfo;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.DeviceReportProxy;
import com.shuncom.hilink.ErrorConstants;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.tcp.server.ChannelGroupHolder;
import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.tcp.server.gateway.Server;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.DeviceInfoBuilder;
import com.shuncom.tcp.server.gateway.cache.RequestCache;
import com.shuncom.tcp.server.gateway.cache.RequestKey;
import com.shuncom.util.Constants;

import io.netty.channel.Channel;


public class PostHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final Map<String,Object> record = new ConcurrentHashMap<>();
	private static final Random random = new SecureRandom();

	public static Object getRecord(String key) {
		return record.get(key);
	}
	public static boolean containKey(String key) {
		return record.containsKey(key);
	}
	
	@Override
	public HiLinkDeviceCommand handle(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand) {
		String id = deviceUID.getSn();
		Device device = DeviceCache.get(id);
		if (device == null || !device.isOnline()) {
			logger.error("Device is offline : {}", deviceUID);
			HiLinkUtil.deviceOffline(deviceUID);
			logger.debug("Report device offline : {}", deviceUID);
			HiLinkDeviceCommand responseCommand = new HiLinkDeviceCommand(HiLinkDeviceCmdUtil.ACTION_RESPONSE, ErrorConstants.valueOfString(ErrorConstants.DEVICE_OFFLINE), null);
			logger.debug("Command execute sucess response info :{}", responseCommand);
			return responseCommand;
		}
		JSONObject control = new JSONObject();
		int ep = 1;
		//网关专门做处理
		if(device.isGateway()) {
			Map<String, Object> gwCommand = deviceCommand.getData().getServiceDatas().get(0).getCharacteristics();
			double enable = (double) gwCommand.get("enable");
			final String productid = (String) gwCommand.get("productid");
			
			final String gateway = device.getGateway();
			final Map<String, Boolean> hasRegister = new HashMap<>();
			final int numId = random.nextInt();
			record.put("record", numId);
			record.put("prodid", productid);
			record.put("end", false);
			hasRegister.put("hasRegister", false);
			id = "00ffffffffffffffffff";
			control.put("enwtlst", 0);
			if(enable == 0.0) {
				logger.info("Gateway {} stop to add device", device.getId());
				record.put("end", true);
				//发送关闭操作
				control.put("pmtjn", 0);
			}else {
				logger.info("Gateway {} add device", device.getId());
				control.put("pmtjn", 60);
			}
			
			if(enable != 0) {
				for(int i = 0; i < 10; i++) {
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							if(!record.get("record").equals(numId)) {
								hasRegister.put("hasRegister", true);
								return;
							}
							boolean result = hasRegister.get("hasRegister");
							if(result) {
								return;
							}
							List<Device> deviceList = DeviceCache.findUnRegisterDevicesByProdid(productid, gateway);
							for(int i = 0; i < deviceList.size(); i++) {
								Device unRegDevice = deviceList.get(i);
								
								if(unRegDevice != null) {
									String dsp = unRegDevice.getLocalModel();
									Device gw = DeviceCache.getGateway(unRegDevice.getGateway());
									Map<String, Object> map = new HashMap<>();
									map.put("productid", productid);
									map.put("sn", unRegDevice.getId());
									HiLinkServiceData gwData = new HiLinkServiceData("discovery",map);
									HiLinkDeviceData hiLinkDeviceData = new HiLinkDeviceData(gw.getDeviceUID(), Arrays.asList(gwData));
									DeviceReportProxy.reportDeviceData(hiLinkDeviceData);
									
									Channel channel = ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(unRegDevice.getGateway());
									HiLinkDeviceInfo deviceInfo = DeviceInfoBuilder.deviceInfo(dsp, unRegDevice.getId(), unRegDevice.getGateway(), "online");
									logger.info("Report device discover id:{}", unRegDevice.getId());
									DeviceReportProxy.reportDeviceDiscovered(unRegDevice.getDeviceUID(), deviceInfo, DeviceInfoBuilder.serviceInfoList(dsp));
									
									unRegDevice.setRegister(true);
									
									JSONObject response = new JSONObject();
									response.put("code", 2001);
									JSONArray checkList = new JSONArray();
									JSONObject entry = new JSONObject();
									
									entry.put("id", unRegDevice.getId());
									entry.put("control", 0);
									checkList.put(entry);
									response.put("check_list", checkList);
									ChannelUtil.simpleWriteAndFlush(channel, response);
									hasRegister.put("hasRegister", true);
									record.put("end", true);
									HiLinkUtil.sendGWControl(channel, 0);
									return;
								}
							}
						}
					}, 5000 * (i+1));
				}
				for(int i = 0; i < 10; i++) {
					final int count =  i;
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							if(!record.get("record").equals(numId)) {
								hasRegister.put("hasRegister", true);
								return;
							}
							boolean result = hasRegister.get("hasRegister");
							if(result) {
								return;
							}
							List<Device> deviceList = DeviceCache.findUnRegisterDevicesByProdid(productid, gateway);
							for(int i = 0; i < deviceList.size(); i++) {
								Device unRegDevice = deviceList.get(i);
								
								if(unRegDevice != null) {
									String dsp = unRegDevice.getLocalModel();
									Device gw = DeviceCache.getGateway(unRegDevice.getGateway());
									Map<String, Object> map = new HashMap<>();
									map.put("productid", productid);
									map.put("sn", unRegDevice.getId());
									HiLinkServiceData gwData = new HiLinkServiceData("discovery",map);
									HiLinkDeviceData hiLinkDeviceData = new HiLinkDeviceData(gw.getDeviceUID(), Arrays.asList(gwData));
									DeviceReportProxy.reportDeviceData(hiLinkDeviceData);
									
									Channel channel = ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(unRegDevice.getGateway());
									HiLinkDeviceInfo deviceInfo = DeviceInfoBuilder.deviceInfo(dsp, unRegDevice.getId(), unRegDevice.getGateway(), "online");
									logger.info("Report device discover id:{}", unRegDevice.getId());
									DeviceReportProxy.reportDeviceDiscovered(unRegDevice.getDeviceUID(), deviceInfo, DeviceInfoBuilder.serviceInfoList(dsp));					
									unRegDevice.setRegister(true);
									JSONObject response = new JSONObject();
									response.put("code", 2001);
									JSONArray checkList = new JSONArray();
									JSONObject entry = new JSONObject();
									
									entry.put("id", unRegDevice.getId());
									entry.put("control", 0);
									checkList.put(entry);
									response.put("check_list", checkList);
									ChannelUtil.simpleWriteAndFlush(channel, response);
									hasRegister.put("hasRegister", true);
									record.put("end", true);
									HiLinkUtil.sendGWControl(channel, 0);
									return;
								}
							}
							if(count == 9) {
								record.put("end", true);
							}
						}
					}, 50000 + (i+1)*1000);
				}
			}
			
		}else {
			logger.info("Do device command :{}", deviceUID);
			control = HiLinkUtil.buildCommand(deviceUID, deviceCommand);
			ep = device.getEp();
		}
		JSONObject request = new JSONObject();
		RequestKey requestKey = new RequestKey();
		request.put("code", 1002);
		request.put("id", id);
		request.put("ep", ep);
		request.put("serial", requestKey.getSerial());
		request.put("control", control);
		//logger.info("Command info :{}", request);
		Channel channel =  ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(device.getGateway());
		if(channel == null) {
			HiLinkDeviceCommand responseCommand = new HiLinkDeviceCommand(HiLinkDeviceCmdUtil.ACTION_RESPONSE, ErrorConstants.valueOfString(ErrorConstants.INTERNAL_ERROR), null);
			//logger.info("Command execute sucess response info :{}", responseCommand);
			return responseCommand;
		}
		ChannelUtil.simpleWriteAndFlush(channel, request);
		//通知控制设备
		
		RequestCache.put(requestKey);
		SynchronousQueue<Object> getter = RequestCache.get(requestKey);
		JSONObject response = (JSONObject)RequestCache.poll(getter, Constants.requestTimeout, TimeUnit.MILLISECONDS);
		if (response == null) {
			logger.info("Command timeout :{}", request);
		}else {
			if(response.has("result")) {
				if (0 == response.getInt("result")) {
					logger.info("Success send command to device : {}", response);
				}else if(3 == response.getInt("result")) {
					logger.error("Duplicate send command to device : {}", response);
				}
			}
		}
		
		HiLinkDeviceCommand responseCommand = new HiLinkDeviceCommand(HiLinkDeviceCmdUtil.ACTION_RESPONSE, ErrorConstants.valueOfString(ErrorConstants.SUCCESS), deviceCommand.getData());
		return responseCommand;
	}
	

	
	public String handleInner(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand) {
		
		return null;
	}
}
