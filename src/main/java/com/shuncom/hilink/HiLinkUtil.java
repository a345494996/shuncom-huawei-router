package com.shuncom.hilink;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.huawei.hilink.device.cmd.HiLinkDeviceCommand;
import com.huawei.hilink.device.data.HiLinkDeviceData;
import com.huawei.hilink.device.data.HiLinkServiceData;
import com.huawei.hilink.device.info.HiLinkDevice;
import com.huawei.hilink.device.status.HiLinkDeviceStatus;
import com.huawei.hilink.device.status.HiLinkDeviceStatusDetail;
import com.huawei.hilink.device.status.HiLinkDeviceStatusType;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.tcp.server.ChannelGroupHolder;
import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.tcp.server.gateway.Server;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.DeviceConfig;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository.ServiceInfo;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;
import com.shuncom.tcp.server.gateway.cache.RegisterCache.PortProperties;
import com.shuncom.tcp.server.gateway.cache.RequestCache;
import com.shuncom.tcp.server.gateway.cache.RequestKey;
import com.shuncom.util.Constants;

import io.netty.channel.Channel;


public class HiLinkUtil {
	
    private static final Logger logger = LoggerFactory.getLogger(HiLinkUtil.class);
	
	public static final InetAddress inetAddress = getLocalHostLANAddress();
    /**
     * 下行构建本地命令
     * @param deviceUID
     * @param deviceCommand
     * @return
     */
	public static JSONObject buildCommand(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand) {
		JSONObject command = new JSONObject();
		//Device Config
		String devType = deviceUID.getDeviceTypeUID().toString();
		DeviceConfig deviceConfig = HiLinkDeviceRepository.getDeviceConfig(HiLinkDeviceRepository.getLocalModel(devType));
		if (deviceConfig == null) {
			logger.error("Cannot find device config : {}", devType);
            return command;	
		}
		
		Map<String, ServiceInfo> serviceInfos = deviceConfig.getServiceInfos();
		List<HiLinkServiceData> serviceDatas = deviceCommand.getData().getServiceDatas();
		for (HiLinkServiceData serviceData : serviceDatas) {
			String serviceId = serviceData.getSid();
			Map<String, Object> data = serviceData.getCharacteristics();
			ServiceInfo serviceInfo = serviceInfos.get(serviceId);
			if (serviceInfo == null) {
				logger.error("Cannot find device service config : {}, {}", devType, serviceId);
				continue;
			}
			Map<String, Object> serviceProfile = serviceInfo.getServiceProfile();
			Map<String, String> propsMapping = serviceInfo.getPropsMapping();
			Iterator<Entry<String, Object>> itor = data.entrySet().iterator();
			while (itor.hasNext()) {
				Entry<String, Object> entry = itor.next();
				String prop = entry.getKey();
				String localProp = propsMapping.get(prop);
				if (localProp == null) {
					logger.error("Local unsupported  property : {}, {}", devType, prop);
					continue;
				}
				if(((String)serviceProfile.get(prop)).contains("PUT")) {
					command.put(localProp, entry.getValue());
				}
				
			}
		}
		return deviceConfig.getConvert().convertHuaweiData(command);
	}
	
	/**
	 * 上行构建HiLink数据
	 * @param deviceUID
	 * @param data
	 * @return
	 */
	public static HiLinkDeviceData buildDeviceData(HiLinkDeviceUID deviceUID, JSONObject data) {
		HiLinkDeviceData deviceData = new HiLinkDeviceData(deviceUID);
		String devType = deviceUID.getDeviceTypeUID().toString();
		DeviceConfig deviceConfig = HiLinkDeviceRepository.getDeviceConfig(HiLinkDeviceRepository.getLocalModel(devType));
		if (deviceConfig == null) {
			logger.error("Cannot find device config : {}", devType);
			return deviceData;
		}
		data = deviceConfig.getConvert().convertShuncomData(data);
		logger.debug("Convert data :{}", data);
		Map<String, ServiceInfo> serviceInfos = deviceConfig.getServiceInfos();
		Iterator<ServiceInfo> itor = serviceInfos.values().iterator();
		while (itor.hasNext()) {
			ServiceInfo serviceInfo = itor.next();
			HiLinkServiceData serviceData = new HiLinkServiceData(serviceInfo.getServiceId());
			Iterator<Entry<String, String>> _itor = serviceInfo.getPropsMapping().entrySet().iterator();
			while (_itor.hasNext()) {
				Entry<String, String> entry = _itor.next();
				String prop = entry.getKey();
				String localProp = entry.getValue();
				if (data.has(localProp)) {
					serviceData.addCharacteristic(prop, data.get(localProp));
				} 
				else {
					//logger.error("HiLink unsupported property : {}, {}", devType, localProp);
				}
			}
			if (serviceData.getCharacteristics().size() > 0) {
				deviceData.add(serviceData);
			}
		}
		
		return deviceData;
	}
	
	
	
	public static HiLinkDeviceData buildDefaultDeviceData(HiLinkDeviceUID deviceUID) {
		HiLinkDeviceData deviceData = new HiLinkDeviceData(deviceUID);
		Map<String ,Object> map = new HashMap<>();
		map.put("lowBattery", 0);
		HiLinkServiceData serviceData = new HiLinkServiceData("battery",map);
		deviceData.add(serviceData);
		return deviceData;
	}
	
	public static String getRegistedDeviceUID(String deviceId) {
		//1) 请求路由获取
		List<HiLinkDevice> hiLinkDevices = DeviceMngrProxy.getDeviceMngr().getAllDevices();
		if(hiLinkDevices == null) {
			return null;
		}
    	for (HiLinkDevice hiLinkDevice : hiLinkDevices) {
    		if(hiLinkDevice.getDevUID().getSn().equals(deviceId)) {
    			return hiLinkDevice.getDevUID().toString();
    		}
    	}
    	
    	return null;
	}
	
	public static HiLinkDevice getHiLinkDevice(String deviceId) {
		//1) 请求路由获取
		List<HiLinkDevice> hiLinkDevices = DeviceMngrProxy.getDeviceMngr().getAllDevices();
		if(hiLinkDevices == null) {
			return null;
		}
    	for (HiLinkDevice hiLinkDevice : hiLinkDevices) {
    		if(hiLinkDevice.getDevUID().getSn().equals(deviceId)) {
    			return hiLinkDevice;
    		}
    	}
    	
    	return null;
	}
	
	public static HiLinkDevice getHiLinkDevice(String deviceId, String gateway) {
		//1) 请求路由获取
		List<HiLinkDevice> hiLinkDevices = DeviceMngrProxy.getDeviceMngr().getAllDevices();
		if(hiLinkDevices == null) {
			return null;
		}
    	for (HiLinkDevice hiLinkDevice : hiLinkDevices) {
    		if(hiLinkDevice.getDevUID().getSn().equals(deviceId) && hiLinkDevice.getDeviceInfo().getMac().equals(gateway)) {
    			return hiLinkDevice;
    		}
    	}
    	
    	return null;
	}
	
	
	public static String getRegistedDeviceUID(List<HiLinkDevice> hiLinkDevices, String deviceId) {
		//1) 请求路由获取
		if(hiLinkDevices == null) {
			return null;
		}
    	for (HiLinkDevice hiLinkDevice : hiLinkDevices) {
    		if(hiLinkDevice.getDevUID().getSn().equals(deviceId)) {
    			return hiLinkDevice.getDevUID().toString();
    		}
    	}
    	
    	return null;
	}
	
	public static JSONObject jsonObjectCopy(JSONObject source, JSONObject target) {
		String[] keys = JSONObject.getNames(source);
		if (keys != null) {
			for (String key : keys) {
				target.put(key, source.get(key));
			}
		}
		return target;
	}
	
	public static JSONObject jsonPropsCopy(JSONObject source, JSONObject target, String...keys) {
		if (keys != null) {
			for (String key : keys) {
				if (source.has(key)) {
					target.put(key, source.get(key));
				}
			}
		}
		return target;
	}
	
	/**
     * 获得localModel
     * @param deviceCommand
     * @return
     */
	public static String getLocalModel(JSONObject param) {
		String dsp = null;
		//1.先查看有没有dsp,是否在配置中有;
		if(param.has("dsp")) {
			dsp = param.getString("dsp");
			dsp = dsp.split(" ")[0];
			if(HiLinkDeviceRepository.containLocalModel(dsp)) {
				return dsp;
			}
			String[] values = dsp.split("-");
			if(values.length >= 2) {
				String result = values[0].trim() + "-" + values[1].trim();
				if(HiLinkDeviceRepository.containLocalModel(result)) {
					return result;
				}
			}
		}
		//2.再查看是否有ztype,根据ztype获得dsp;
		if(param.has("ztype")) {
			int ztype = param.getInt("ztype");
			if(HiLinkDeviceRepository.containZtype(ztype)) {
				dsp = HiLinkDeviceRepository.getLocalModelByZtype(ztype);
				return dsp;
			}
		}
		//3.根据swid判断
		if(param.has("swid")) {
			
			String swid = param.getString("swid");
			//logger.info("swid:{}", swid);
			
			String[] values = swid.split("-");
			if(values.length >= 2) {
				String result = values[0].trim() + "-" + values[1].trim();
				//logger.info("result:{}", result);
				if(HiLinkDeviceRepository.containSwid(result)) {
					dsp = HiLinkDeviceRepository.getLocalModelBySwid(result);
					//logger.info("dsp:{}", dsp);
					return dsp;
				}
			}
			if(HiLinkDeviceRepository.containSwid(swid)) {
				dsp = HiLinkDeviceRepository.getLocalModelBySwid(swid);
				return dsp;
			}
		}
		
		//根据数据属性获得dsp;
		if(param.has("pt")) {
			return "Shuncom-curtain-motor";
		}
		
		
		return null;
	}
	
	
	/**
     * 获得localModel
     * @param deviceCommand
     * @return
     */
	public static String getLocalModel(JSONObject param, String deviceId) {
		String result = getLocalModel(param);
		
		if(result != null)
			return result;
		
		if(!param.has("dsp") || !DeviceCache.contains(deviceId) || !RegisterCache.contains(deviceId))
			return null;
		
		PortProperties portProps = (PortProperties)RegisterCache.get(deviceId);;
		String dsp = param.getString("dsp");
		Iterator<Entry<String, JSONObject>> itor = portProps.allProperties().entrySet().iterator();
		int max = 0;
		for(;itor.hasNext();) {
			Entry<String, JSONObject> entry = itor.next();
			String key = entry.getKey();
			Integer intKey = Integer.valueOf(key);
			if(intKey > max)
				max = intKey;
		}
		dsp = dsp + String.valueOf(max);
		if(HiLinkDeviceRepository.containLocalModel(dsp))
			return dsp;
		return null;
	}
	
	/**
     * 包装uplinkData，如果数据不变就不上报不改变的数据
     * @param uplinkData
     * @return
     */
	public static HiLinkDeviceData wrapperHeartBeatUplinkData(HiLinkDeviceData uplinkData) {
		HiLinkDeviceUID deviceUID = uplinkData.getDevUID();
		HiLinkDeviceData resultData = new HiLinkDeviceData(deviceUID);
		List<HiLinkServiceData> serviceDataList = uplinkData.getServiceDatas();
		for(int i = 0; i < serviceDataList.size(); i++) {
			HiLinkServiceData serviceData = serviceDataList.get(i);
			Map<String,Object> serMap = serviceData.getCharacteristics();
			String serviceId = serviceData.getSid();
			HiLinkServiceData   oldSerData = DeviceMngrProxy.getDeviceMngr().getDeviceDataBySerID(deviceUID, serviceId);
			logger.debug("Old service data : {}, service data :{}",oldSerData, serviceData);
			Map<String,Object> oldSerMap = oldSerData == null ? new HashMap<String,Object>():oldSerData.getCharacteristics() != null ?oldSerData.getCharacteristics() :new HashMap<String,Object>() ;
			Iterator<Entry<String, Object>> itor = serMap.entrySet().iterator();
			Map<String, Object> resultMap = new HashMap<>();
			for(;itor.hasNext();) {
				Entry<String, Object> entry = itor.next();
				String key = entry.getKey();
				Object value = entry.getValue();
				if(oldSerMap.containsKey(key)) {

					if(Integer.class.equals(value.getClass())) {
						value = Double.valueOf(String.valueOf(value));
					}
					if(!value.equals(oldSerMap.get(key))) {
						if(Double.class.equals(value.getClass())) {
							value = ((Double)value).intValue();
						}
						resultMap.put(key, value);
					}
				}else {
					resultMap.put(key, value);
				}
			}
			if(resultMap.size() > 0) {
				resultData.add(new HiLinkServiceData(serviceId, resultMap));
				logger.debug("New service data :{}", new HiLinkServiceData(serviceId, resultMap));
			}
		}
		return resultData;
	}
	/**
     * 包装uplinkData，如果数据不变就不上报不改变的数据
     * @param uplinkData
     * @return
     */
	public static HiLinkDeviceData wrapperNotifyUplinkData(HiLinkDeviceData uplinkData) {
		HiLinkDeviceUID deviceUID = uplinkData.getDevUID();
		HiLinkDeviceData resultData = new HiLinkDeviceData(deviceUID);
		List<HiLinkServiceData> serviceDataList = uplinkData.getServiceDatas();
		for(int i = 0; i < serviceDataList.size(); i++) {
			HiLinkServiceData serviceData = serviceDataList.get(i);
			Map<String,Object> serMap = serviceData.getCharacteristics();
			String serviceId = serviceData.getSid().trim();
			HiLinkServiceData   oldSerData = DeviceMngrProxy.getDeviceMngr().getDeviceDataBySerID(deviceUID, serviceId);
			logger.debug("Old service data : {}, service data :{}",oldSerData, serviceData);
			Map<String,Object> oldSerMap = oldSerData == null ? new HashMap<String,Object>():oldSerData.getCharacteristics() != null ?oldSerData.getCharacteristics() :new HashMap<String,Object>() ;
			Iterator<Entry<String, Object>> itor = serMap.entrySet().iterator();
			Map<String, Object> resultMap = new HashMap<>();
			if("motionSensor".equals(serviceId) || "lockAlarm".equals(serviceId) || "event".equals(serviceId)
					|| "account".equals(serviceId) || "smartLock".equals(serviceId) || "doorSensor".equals(serviceId)) {
				for(;itor.hasNext();) {
					Entry<String, Object> entry = itor.next();
					String key = entry.getKey();
					Object value = entry.getValue();
					resultMap.put(key, value);
				}
			} else {
				for(;itor.hasNext();) {
					Entry<String, Object> entry = itor.next();
					String key = entry.getKey();
					Object value = entry.getValue();
					if(oldSerMap.containsKey(key)) {

						if(Integer.class.equals(value.getClass())) {
							//value = Double.valueOf(String.valueOf(value));
							int r = (int) value;
							double v = r;
							value = v;
						}
						if(!value.equals(oldSerMap.get(key))) {
							if(Double.class.equals(value.getClass())) {
								value = ((Double)value).intValue();
							}
							resultMap.put(key, value);
						}
					}else {
						resultMap.put(key, value);
					}
				}
			}
			if(resultMap.size() > 0) {
				resultData.add(new HiLinkServiceData(serviceId, resultMap));
				logger.debug("New service data :{}", new HiLinkServiceData(serviceId, resultMap));
			}
		}
		return resultData;
	}
	
	public static JSONObject deleteDeviceById(String deviceId, Channel channel) {
		RequestKey requestKey = new RequestKey();
		JSONObject request = new JSONObject();
		request.put("code", 1003);
		request.put("id", deviceId);
		request.put("serial", requestKey.getSerial());
		RequestCache.put(requestKey);
		SynchronousQueue<Object> getter = RequestCache.get(requestKey);
		ChannelUtil.simpleWriteAndFlush(channel, request);
		JSONObject response = (JSONObject)RequestCache.poll(getter, Constants.requestTimeout, TimeUnit.MILLISECONDS);
		return response;
	}
	
	
	
	public static void updateDeviceData(HiLinkDeviceData uplinkData) {
		List<HiLinkServiceData> list = uplinkData.getServiceDatas();
		HiLinkDeviceUID UID = uplinkData.getDevUID();
		for(int i = 0; i <list.size(); i++) {
			HiLinkServiceData serData = list.get(i);
			HiLinkDeviceData devData =  new HiLinkDeviceData(UID);
			devData.add(serData);
			DeviceReportProxy.reportDeviceData(devData);
		}
	}
	
	
	
	public static String getGWId(String mac) {
		StringBuffer sb = new StringBuffer();
		
		String head = "00ff2c2c";
		sb.append(head);
		
		String[] macs = mac.split(":");
		for(String s : macs) {
			sb.append(s.trim());
		}
		return sb.toString();
	}
	public static void deviceOffline(HiLinkDeviceUID deviceUID) {
		DeviceReportProxy.reportDeviceStatus(deviceUID, new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE, 
				HiLinkDeviceStatusDetail.NONE,"OFFLINE"));
	}
	public static void deviceOnline(HiLinkDeviceUID deviceUID) {
		DeviceReportProxy.reportDeviceStatus(deviceUID, new HiLinkDeviceStatus(HiLinkDeviceStatusType.ONLINE, 
				HiLinkDeviceStatusDetail.NONE,"ONLINE"));
	}
	
	public static Channel getChannel(String name, String key) {
		
		return ChannelGroupHolder.channelGroup(name).findChannel(key);
	}
	public static Channel getServerChannel(String key) {
		
		return ChannelGroupHolder.channelGroup(Server.GROUP).findChannel(key);
	}
	
	public static Device copyNewDevice(Device device, String gateway) {
		if(device.getGateway().equals(gateway)) {
			return device;
		}
		Device newDevice = new Device(device.getId(), gateway, false, device.getDeviceUID(), device.getEp(), device.getLocalModel());
		newDevice.setRegister(device.isRegister());
		if(device.isOnline()) {
			newDevice.setOnline();
		}else {
			newDevice.setOffline();
		}
		return newDevice;
	}
	
	public static void sendGWControl(Channel channel, int pmtjn) {
		JSONObject request = new JSONObject();
		JSONObject control = new JSONObject();
		RequestKey key = new RequestKey();
		control.put("pmtjn", pmtjn);
		control.put("enwtlst", 0);
		request.put("code", 1002);
		request.put("id", "00ffffffffffffffffff");
		request.put("ep", 1);
		request.put("serial", key.getSerial());
		request.put("control", control);
		
		ChannelUtil.writeAndFlush(channel, request);
	}
	
	public static String getSimpleId(String id) {
		int length = id.length();
		
		return id.substring(length - 4 , length);
	}
	
	public static void deleteUnRegisterDevice() {
		Iterator<String> itor = DeviceCache.ids().iterator();
		for(;itor.hasNext();) {
			String id = itor.next();
			Device device = DeviceCache.get(id);
			if(device != null && !device.isRegister()) {
				Channel channel = getServerChannel(device.getGateway());
				deleteDeviceById(id, channel);
			}
		}
	}
	
	
	public static InetAddress getLocalHostLANAddress() {
	    try {
	        InetAddress candidateAddress = null;
	        // 遍历所有的网络接口
	        for (Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            // 在所有的接口下再遍历IP
	            for (Enumeration<?> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
	                    if (inetAddr.isSiteLocalAddress()) {
	                        // 如果是site-local地址，就是它了
	                        return inetAddr;
	                    } else if (candidateAddress == null) {
	                        // site-local类型的地址未被发现，先记录候选地址
	                        candidateAddress = inetAddr;
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            return candidateAddress;
	        }
	        // 如果没有发现 non-loopback地址.只能用最次选的方案
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        return jdkSuppliedAddress;
	    } catch (Exception e) {
	    }
	    return null;
	}
	
	public static boolean isJSON(String value) {
		if(value == null) {
			return false;
		}
		try {
			new JSONObject(value);
		}catch (JSONException e) {
			try {
				new JSONArray(value);
			}catch (JSONException e1) {
				return false;
			}
		}
		return true;
	}
	
}
	
