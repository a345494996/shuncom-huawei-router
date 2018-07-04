package com.shuncom.tcp.server.gateway.action;

import org.json.JSONArray;
import org.json.JSONObject;
import com.huawei.hilink.device.data.HiLinkDeviceData;
import com.huawei.hilink.device.info.HiLinkDevice;
import com.huawei.hilink.device.info.HiLinkDeviceInfo;
import com.huawei.hilink.device.status.HiLinkDeviceStatus;
import com.huawei.hilink.device.status.HiLinkDeviceStatusDetail;
import com.huawei.hilink.device.status.HiLinkDeviceStatusType;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.DeviceReportProxy;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.tcp.server.gateway.ChannelRequestContext;
import com.shuncom.tcp.server.gateway.Packet;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.DeviceInfoBuilder;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.DeviceConfig;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;
import com.shuncom.tcp.server.gateway.cache.RegisterCache.PortProperties;
import com.shuncom.util.ValidationException;
import io.netty.channel.Channel;
import static com.shuncom.tcp.server.gateway.action.ActionUtil.*;

/**
 * code-104
 *
 */
public class Notify implements Action {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Override
	public Object execute(ChannelRequestContext requestContext) throws Exception {
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		int control = request.getInt("control");
		//0 - 上报网关信息
		//1 - 上报设备删除（退网）
		//3 - 上报设备状态改变
		switch(control) {
		   case 0 : return control0(requestContext);
		   case 1 : return control1(requestContext);
		   case 2 : return control2(requestContext);
		   default : {
			   throw new ValidationException("Unsupported control type " + control);
		   }
		}
	}

	private Object control0(ChannelRequestContext requestContext) {
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		String mac = request.getString("mac");
		String deviceId = HiLinkUtil.getGWId(mac);
		Device device = DeviceCache.get(deviceId);
		if (device == null) {
	     	//设备注册信息
	     	String devUID = HiLinkUtil.getRegistedDeviceUID(deviceId);
	     	//1) 未注册
	     	if (devUID == null) {
	     		DeviceConfig devConfig = HiLinkDeviceRepository.getDeviceConfig("SHUNCOM-GATAWAY");
	     		device = new Device(deviceId, mac, true, new HiLinkDeviceUID(devConfig.getModel() + ":" + deviceId));
	     		device.setRegister(false);
	     		HiLinkDeviceInfo deviceInfo = DeviceInfoBuilder.deviceInfo(devConfig.getDeviceInfo(), deviceId, mac, "online");
				if(deviceInfo != null) {
					DeviceReportProxy.reportDeviceAccess("com.shuncom.huawei.router", deviceInfo, DeviceInfoBuilder.serviceInfoList("SHUNCOM-GATAWAY"));
					//logger.info("Report gateway access , gatewayInfo : {}", deviceInfo);
					logger.info("Report gateway {} access", device.getId());
				}
	     	}
	     	//2) 已注册
	     	else {
	     		device = new Device(deviceId, mac, true, new HiLinkDeviceUID(devUID));
	     		device.setRegister(true);
	     	}
	     	device.setOnline();
	     	DeviceCache.put(deviceId, device);
		}
		
		JSONObject response = new JSONObject();
		response.put("code", 1004);
		response.put("control", request.get("control"));
		response.put("id", request.get("id"));
		response.put("result", 0);
		return response;
	}

	private Object control1(ChannelRequestContext requestContext) {
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		//logger.info("CodeRemove device info:{}", request);
		JSONArray ids = request.getJSONArray("id");
		Channel channel = requestContext.channel();
		for (int i = 0; i < ids.length(); i++) {
			String id = ids.getString(i);
			if(DeviceCache.contains(id)) {
				Device device = DeviceCache.get(id);
				//logger.info("Notify device offline :{}", id);
				if(!channel.equals(HiLinkUtil.getServerChannel(device.getGateway())))
					continue;
				//DeviceReportProxy.reportDeviceStatus(device.getDeviceUID(), new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE,
						     //HiLinkDeviceStatusDetail.NONE, "offline"));
				if(RegisterCache.contains(device.getId())) {
					RegisterCache.remove(device.getId());
				}
				if(device.isRegister()) {
					/*logger.info("Remove device :{}", device.getDeviceUID());
					DeviceCache.remove(device.getId());
					if(device.getDeviceUID() != null) {
						HiLinkDevice  hiDevice = DeviceMngrProxy.getDeviceMngr().getDevice(device.getDeviceUID());
						if(hiDevice != null) {
							logger.info("Remove hiDevice :{}", hiDevice);
							device.setRegister(false);
							device.setOffline();
						}else {
							DeviceCache.remove(device.getId());
						}
					}else {
						DeviceCache.remove(device.getId());
					}*/
					
					/*device.setRegister(false);
					device.setOffline();
					device.setGateway(null);*/
					//DeviceReportProxy.reportDeviceRemoved(device.getDeviceUID());
					DeviceReportProxy.reportDeviceStatus(device.getDeviceUID(), new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE, HiLinkDeviceStatusDetail.NONE, "OFFLINE"));
				}
				DeviceCache.remove(device.getId());
				logger.info("Remove device {}", device.getId());
			}
		}
		JSONObject response = new JSONObject();
		response.put("code", 1004);
		response.put("control", request.get("control"));
		JSONArray id = request.getJSONArray("id");
		response.put("id", id);
		response.put("result", 0);
		return response;
	}

	private Object control2(ChannelRequestContext requestContext) {
		
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		logger.debug("Notify device info :{}", request);
		String deviceId = request.getString("id");
		String gateway = getGatewayByChannel(requestContext.channel());
    	//没有ep就不保存
    	if(!request.has("ep")) {
    		logger.error("Notify doesn't have ep");
    		return defaultReult(request);
    	}
		int port = request.getInt("ep");  
		JSONObject st = request.getJSONObject("st");
		Device device = DeviceCache.get(deviceId);
		boolean online = request.getBoolean("ol");  //在线状态
		
		handleDevice(device, deviceId, gateway, port, st, online);
		return defaultReult(request);
	}
	
	public JSONObject defaultReult(JSONObject request) {
		JSONObject response = new JSONObject();
		response.put("code", 1004);
		response.put("id", request.get("id"));
		HiLinkUtil.jsonPropsCopy(request, response, "ep", "pid", "did");
		response.put("control", request.get("control"));
		response.put("result", 0);
		return response;
	}
	
	public void handleDevice(Device cached, String deviceId, String gateway, int ep, JSONObject st, boolean ol) {
		//设备注册信息
		HiLinkDevice hiDevice = HiLinkUtil.getHiLinkDevice(deviceId);
     	String devUID = null;
     	if(hiDevice != null) {
     		devUID = hiDevice.getDevUID().toString();
     	}
     	String gwId = HiLinkUtil.getGWId(gateway);
     	//List<HiLinkDevice> hiLinkDevices = DeviceMngrProxy.getDeviceMngr().getAllDevices();
     	
     	//如果缓存没有，云端也没有，设置缓存false,并且存储结果
     	if(cached == null && devUID == null) {
     		initUnRegisterDevice(cached, gateway, deviceId, logger);
			//logger.info("Regsiter device {}", deviceId);
			//注册register缓存
			registerDeviceData(cached, ep, deviceId, st, gateway, logger);
			return;
     	}
     	//缓存已经有了，但是没有注册，保存数据,等待注册。(云端有，没有注册，这个时候就是设备误删离线)
		if(devUID != null && cached != null && !cached.isRegister()) {
			//缓存中的gateway和当前的gateway不一致就不缓存register;
			cached.setOnline();
			if(!cached.getGateway().equals(gateway)) {
				logger.info("Gateway is different");
				return;
			}
			//logger.info("Regsiter device {}", deviceId);
			registerDeviceData(cached, ep, deviceId, st, gateway, logger);
			return;
		}
		
		//云端没有，缓存有，直接删除缓存结束；重新缓存；
		if(devUID == null && cached != null && cached.isRegister()) {
			logger.info("Platform not exist, delete device cache {} ", deviceId);
			HiLinkUtil.deleteDeviceById(deviceId, HiLinkUtil.getServerChannel(cached.getGateway()));
			DeviceCache.remove(cached.getId());
			return;
		}
		
		//云端里边没有，缓存有，而且是没有注册过的,看是否需要转换网关
		if(devUID == null && cached != null && !cached.isRegister()) {
			cached.setOnline();
			
			cached = HiLinkUtil.copyNewDevice(cached, gateway);
			DeviceCache.remove(cached.getId());
			DeviceCache.put(cached.getId(), cached);
			//logger.info("Register device {}", deviceId);
			registerDeviceData(cached, ep, deviceId, st, gateway, logger);
			return;
		}
		
		
		//设备有缓存，APP上已经拥有，但是没有缓存（更新插件时候会出现此问题，此时设备已经上线，如果对应的网关不对，直接返回，如果网关对应，上报信息）
		if(devUID != null && cached == null) {
			
			//如果此设备正在执行添加设备操作
			if(isRecord(hiDevice.getDevUID().getDeviceTypeUID().toString(), logger)) {
				//设置为未注册，并缓存
				initRegisterDevice(devUID, cached, gateway, deviceId, ep, logger);
				DeviceCache.get(deviceId).setRegister(false);
				logger.info("Adding device ,Register device {}", deviceId);
				registerDeviceData(DeviceCache.get(deviceId), ep, deviceId, st, gateway, logger);
			}else {
				//logger.info("Category {} device {} is register", HiLinkDeviceRepository.getModelToCategory(HiLinkDeviceRepository.getLocalModel(hiDevice.getDevUID().getDeviceTypeUID().toString())), deviceId);
				//如果设备云端存储的网关设备和当前的网关不一致
				String hiGwId = hiDevice.getDeviceInfo().getMac();
				//网关存储不一致不需要处理。
				if(!hiGwId.equals(gateway)) {
					logger.info("Category {} device {} gateway {}, however it is changed to {}!", HiLinkDeviceRepository.getModelToCategory(HiLinkDeviceRepository.getLocalModel(hiDevice.getDevUID().getDeviceTypeUID().toString())), deviceId, hiGwId, gwId);
					return;
				}
				
				//初始化device cache缓存
				initRegisterDevice(devUID, cached, gateway, deviceId, ep, logger);
				
				String status = hiDevice.getDeviceInfo().getStatus();
				cached = DeviceCache.get(deviceId);
				//logger.info("Category {} device {} status {}", deviceId, HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()), status);
				if(ol) {
					cached.setOnline();
					//logger.info("Category {} device {} status {}", deviceId, HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()), status);
					if(!"online".equals(status)) {
						//logger.info("Category {} device {} is not online, report device discover and status online", deviceId, HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()));
						PortProperties portProps = new PortProperties(deviceId);
						portProps.addPortProperties(ep, HiLinkUtil.jsonObjectCopy(st, new JSONObject()));
						RegisterCache.put(deviceId, portProps);
						//reportDeviceDiscover(cached, gateway);
						reportDeviceStatusOnline(cached.getDeviceUID(), logger);
						logger.info("Set device {} online", deviceId);
						//将设备加入白名单
						addWhiteList(gateway, deviceId, logger);
					}
					HiLinkDeviceData uplinkData = HiLinkUtil.buildDeviceData(hiDevice.getDevUID(), st);
					//如果有新的数据就上报
					//如果是红外感应设备直接上报
					uplinkData = HiLinkUtil.wrapperNotifyUplinkData(uplinkData);
					if (uplinkData.getServiceDatas().size() > 0) {
						logger.info("Report device data :{}", uplinkData);
						DeviceReportProxy.reportDeviceData(uplinkData);
					}
				}else {
					cached.setOffline();
					DeviceCache.remove(deviceId);
					if(RegisterCache.contains(deviceId)) {
						RegisterCache.remove(deviceId);
					}
					if(cached.isRegister()) {
						DeviceReportProxy.reportDeviceStatus(cached.getDeviceUID(), new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE,
							     HiLinkDeviceStatusDetail.NONE, "offline"));
					}
					logger.info("Set device {} offline", deviceId);
				}
			}
			return;
		}
		
		//设备有缓存，APP上已经拥有(上边已经处理了缓存有，没有注册的问题)
		if(devUID != null && cached != null && cached.isRegister()) {
			
			
			//logger.info("Category {} device {} is register", HiLinkDeviceRepository.getModelToCategory(HiLinkDeviceRepository.getLocalModel(hiDevice.getDevUID().getDeviceTypeUID().toString())), deviceId);
			//如果设备云端存储的网关设备和当前的网关不一致
			String hiGwId = hiDevice.getDeviceInfo().getMac();
			//网关存储不一致不需要处理。
			if(!hiGwId.equals(gateway)) {
				logger.info("Category {} device {} gateway {}, however it is changed to {}!", HiLinkDeviceRepository.getModelToCategory(HiLinkDeviceRepository.getLocalModel(hiDevice.getDevUID().getDeviceTypeUID().toString())), deviceId, hiGwId, gwId);
				return;
			}
			
			//初始化device cache缓存
			//initRegisterDevice(devUID, cached, gateway, deviceId, ep);
			if(ol) {
				cached.setOnline();
				String status = hiDevice.getDeviceInfo().getStatus();
				//logger.info("Category {} device {} status {}", deviceId, HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()), status);
				if(!"online".equals(status)) {
					//logger.info("Category {} device {} is not online, report device discover and status online", HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()), deviceId);
					PortProperties portProps = new PortProperties(deviceId);
					portProps.addPortProperties(ep, HiLinkUtil.jsonObjectCopy(st, new JSONObject()));
					RegisterCache.put(deviceId, portProps);
					//reportDeviceDiscover(cached, gateway);
					logger.info("Set device {} online", deviceId);
					reportDeviceStatusOnline(cached.getDeviceUID(), logger);
					//将设备加入白名单
					addWhiteList(gateway, deviceId, logger);
				}
				HiLinkDeviceData uplinkData = HiLinkUtil.buildDeviceData(hiDevice.getDevUID(), st);
				//如果有新的数据就上报
				//如果是红外感应设备直接上报
				uplinkData = HiLinkUtil.wrapperNotifyUplinkData(uplinkData);
				if (uplinkData.getServiceDatas().size() > 0) {
					logger.info("Report device data :{}", uplinkData);
					DeviceReportProxy.reportDeviceData(uplinkData);
				}
			}else {
				cached.setOffline();
				DeviceCache.remove(deviceId);
				if(RegisterCache.contains(deviceId)) {
					RegisterCache.remove(deviceId);
				}
				if(cached.isRegister()) {
					DeviceReportProxy.reportDeviceStatus(cached.getDeviceUID(), new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE,
						     HiLinkDeviceStatusDetail.NONE, "offline"));
				}
				logger.info("Set device {} offline", deviceId);
			}
			return;
		}
			
	}
	
	
}
