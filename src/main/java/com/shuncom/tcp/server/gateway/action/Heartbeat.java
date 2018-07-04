package com.shuncom.tcp.server.gateway.action;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.huawei.hilink.device.info.HiLinkDevice;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.DeviceMngrProxy;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.tcp.server.gateway.ChannelRequestContext;
import com.shuncom.tcp.server.gateway.Packet;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;
import com.shuncom.tcp.server.gateway.cache.RegisterCache.PortProperties;
import static com.shuncom.hilink.HiLinkUtil.*;
import static com.shuncom.tcp.server.gateway.action.ActionUtil.*;



/**
 * code-101
 *
 */
public class Heartbeat implements Action {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public Object execute(ChannelRequestContext requestContext) throws Exception {
		Packet packet = (Packet) requestContext.request();
		JSONObject request = (JSONObject) packet.getConvert();
		if(!request.has("gw")) {
			return defaultResult();
		}
		JSONObject gw = request.getJSONObject("gw");
		String gateway = gw.getString("mac");
		//澶勭悊缃戝叧闂
		handleGateway(gateway);
		JSONArray devices = request.getJSONArray("device");
		handleDevice(devices, gateway);
		return defaultResult();
	} 
	
	private JSONObject defaultResult() {
		JSONObject response = new JSONObject();
		response.put("code", 1001);
		response.put("result", 0);
		response.put("timestamp", System.currentTimeMillis() / 1000);
		return response;
	}
	private void handleGateway(String mac) {
		String localModel = "SHUNCOM-GATAWAY";
		String gateway = mac;
		String gwId = HiLinkUtil.getGWId(gateway);
		Device gwDevice = DeviceCache.get(gwId);
		HiLinkDevice hiDevice = DeviceMngrProxy.getDeviceMngr().getDevice(getHiLinkUID(localModel, gwId, logger));
		doHandleGateway(gwDevice, hiDevice, gateway, gwId, localModel);
	}
	private void doHandleGateway(Device gwDevice, HiLinkDevice hiDevice, String gateway, String gwId, String localModel) {
		
		//APP涓婃病鏈夋敞鍐岋紝闇�瑕佷笂鎶ccess鐨勬潯浠�(1.鏈韩灏辨病鏈変笂鎶ヨ繃access. 2.涓婃姤杩嘺ccess,浣嗘槸浠ラ槻娌℃湁涓婃姤鎴愬姛锛屽啀娆′笂鎶�)
		if((gwDevice == null && hiDevice == null) || (gwDevice != null && !gwDevice.isRegister() && hiDevice == null)) {
			logger.debug("Gateway {} is not register", getSimpleId(gwId));
			//鍒濆鍖栦笂鎶CCESS闇�瑕佺殑鏉′欢锛涘苟缁檊wDevice璧嬪�硷紱
			initGwDevice(gwDevice, gateway, gwId, localModel, false);
			//涓婃姤access
			RegisterCache.put(gwId, new PortProperties(gwId));
			logger.debug("Gateway {} report device access", getSimpleId(gwId));
			reportDeviceAccess(DeviceCache.get(gwId), logger);
			return;
		}
		
		//APP宸茬粡娉ㄥ唽浜嗚澶�(鏈夌紦瀛樺拰娌℃湁缂撳瓨锛屽鏋滄病鏈夌紦瀛樺氨瀛樺叆缂撳瓨)
		if((hiDevice != null)) {
			logger.debug("Gateway {} has registered", getSimpleId(gwId));
			//鍒濆鍖栫紦瀛橈紱骞剁粰gwDevice璧嬪�硷紱
			initGwDevice(gwDevice, gateway, gwId, localModel, true);
			//濡傛灉涓嶅湪绾匡紝灏变笂绾�
			ifGwNotOnline(hiDevice, DeviceCache.get(gwId));
			return;
		}
		//缂撳瓨宸茬粡鏈夛紝浣嗘槸浜戠娌℃湁锛孉pp涓婃樉绀猴紙鍦ㄩ噸鍚彃浠剁殑鎯呭喌涓嬶級
		if(gwDevice != null && gwDevice.isRegister() && hiDevice == null) {
			//鍒濆鍖栫紦瀛橈紱骞剁粰gwDevice璧嬪�硷紱
			logger.debug("Gateway {} has registered", getSimpleId(gwId));
			RegisterCache.put(gwId, new PortProperties(gwId));
			gwDevice.setOnline();
			logger.debug("Gateway {} is not exsit in huawei store, report device access and status online", getSimpleId(gwDevice.getId()));
			reportDeviceAccess(gwDevice, logger);
			//reportDeviceStatusOnline(gwDevice.getDeviceUID());
			return;
		}
	}
	
	private void initGwDevice(Device gwDevice, String gateway, String gwId, String localModel, boolean isRegister) {
		if(gwDevice == null) {
			logger.debug("Gateway {} device cache is null, store gateway cache!", getSimpleId(gwId));
			gwDevice = new Device(gwId, gateway, true, getHiLinkUID(localModel, gwId, logger), -1, "SHUNCOM-GATAWAY");
     		DeviceCache.put(gwId, gwDevice);
		}
		gwDevice.setRegister(isRegister);
		gwDevice.setOnline();
		
	}
	
	
	private void ifGwNotOnline(HiLinkDevice hiDevice, Device gwDevice) {
		String status = hiDevice.getDeviceInfo().getStatus();
		//logger.info("Gateway {} status : {}", getSimpleId(gwDevice.getId()), status);
		if(!"online".equals(status)) {
			String gwId = gwDevice.getId();
			RegisterCache.put(gwId, new PortProperties(gwId));
			//logger.info("Gateway {} is not online, report device access and status online", getSimpleId(gwDevice.getId()));
			reportDeviceAccess(gwDevice, logger);
			reportDeviceStatusOnline(gwDevice.getDeviceUID(), logger);
		}
	}
	
	private void handleDevice(JSONArray devices, String gateway) {
		List<HiLinkDevice> hiLinkDevices = DeviceMngrProxy.getDeviceMngr().getAllDevices();
		for (int i = 0; i < devices.length(); i++) {
			JSONObject device = devices.getJSONObject(i);
			JSONObject st = device.getJSONObject("st");
			String deviceId = device.getString("id");
			boolean ol = device.getBoolean("ol");
			int ep = device.getInt("ep");
			Device cached = DeviceCache.get(deviceId);
			if(!ol) {
				logger.error("Offline device: {}", getSimpleId(deviceId));
				continue;
			}
			doHandleDevice(hiLinkDevices, cached, deviceId, gateway, ep, st, ol);
		}
	}
	
	private void doHandleDevice(List<HiLinkDevice> hiLinkDevices, Device cached, String deviceId, String gateway, int ep, JSONObject st ,boolean ol) {
		HiLinkDevice hiDevice = HiLinkUtil.getHiLinkDevice(deviceId);
     	String deviceUID = null;
     	if(hiDevice != null) {
     		deviceUID = hiDevice.getDevUID().toString();
     	}
		String gwId = HiLinkUtil.getGWId(gateway);
		
		
		
		if(deviceUID == null && cached == null) {
			initUnRegisterDevice(cached, gateway, deviceId, logger);
			logger.info("{} deviceCache :{}", getSimpleId(deviceId), cached);
			
			registerDeviceData(cached, ep, deviceId, st, gateway, logger);
			return;
		}
		
		
		if(deviceUID != null && cached != null && !cached.isRegister()) {
			
			registerDeviceData(cached, ep, deviceId, st, gateway, logger);
			return;
		}
		
		if(deviceUID == null && cached != null && cached.isRegister()) {
			//logger.info("Huawei platform not exsit {} {},so delete cache which has Registered ",HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()), getSimpleId(cached.getId()));
			deleteDeviceById(deviceId, getServerChannel(cached.getGateway()));
			DeviceCache.remove(cached.getId());
			return;
		}
		
		
		if(deviceUID == null && cached != null && !cached.isRegister()) {
			cached = HiLinkUtil.copyNewDevice(cached, gateway);
			DeviceCache.remove(cached.getId());
			DeviceCache.put(cached.getId(), cached);
			registerDeviceData(cached, ep, deviceId, st, gateway, logger);
		}
			
		
		
		if(deviceUID != null) {
			//logger.info("{} {} is register", HiLinkDeviceRepository.getModelToCategory(HiLinkDeviceRepository.getLocalModel(hiDevice.getDevUID().getDeviceTypeUID().toString())), getSimpleId(deviceId));
			
			String hiGwId = hiDevice.getDeviceInfo().getMac();
			if(!hiGwId.equals(gateway)) {
				logger.info("{} {} gateway {},however it is changed to {}!", HiLinkDeviceRepository.getModelToCategory(HiLinkDeviceRepository.getLocalModel(hiDevice.getDevUID().getDeviceTypeUID().toString())), getSimpleId(deviceId), hiGwId, gwId);
				return;
			}
			
			
			
			initRegisterDevice(deviceUID, cached, gateway, deviceId, ep, logger);
			String status = hiDevice.getDeviceInfo().getStatus();
			cached = DeviceCache.get(deviceId);
			//logger.info("{} {} status {}", HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()), getSimpleId(deviceId), status);
			if(!"online".equals(status)  &&  ol && !isRecord(hiDevice.getDevUID().getDeviceTypeUID().toString(), logger)) {
				//logger.info("{} {} is not online, report device discover and status online", getSimpleId(deviceId), HiLinkDeviceRepository.getModelToCategory(cached.getLocalModel()));
				PortProperties portProps = new PortProperties(deviceId);
				portProps.addPortProperties(ep, HiLinkUtil.jsonObjectCopy(st, new JSONObject()));
				RegisterCache.put(deviceId, portProps);
				//reportDeviceDiscover(cached, gateway);
				reportDeviceStatusOnline(cached.getDeviceUID(), logger);
				addWhiteList(gateway, deviceId, logger);
			}
			return;
		}
	}
		
}
