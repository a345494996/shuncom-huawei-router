package com.shuncom.tcp.server.gateway.action;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.huawei.hilink.device.data.HiLinkDeviceData;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.tcp.server.ChannelGroupHolder;
import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.tcp.server.gateway.ChannelRequestContext;
import com.shuncom.tcp.server.gateway.Packet;
import com.shuncom.tcp.server.gateway.Server;
import com.shuncom.tcp.server.gateway.TransportContext;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.tcp.server.gateway.cache.HiLinkDeviceRepository;
import com.shuncom.tcp.server.gateway.cache.RegisterCache;
import com.shuncom.tcp.server.gateway.cache.RegisterCache.PortProperties;
import com.shuncom.tcp.server.gateway.cache.RequestCache;
import com.shuncom.tcp.server.gateway.cache.RequestKey;
import com.shuncom.util.Constants;
import io.netty.channel.Channel;
import io.netty.util.Attribute;

public class DeviceGroup implements Action{

	private static final Logger logger = LoggerFactory.getLogger(DeviceGroup.class);
	@Override
	public Object execute(ChannelRequestContext requestContext) throws Exception {
		 Packet packet = (Packet) requestContext.request();
		 
		 JSONObject request = (JSONObject) packet.getConvert();
		 int method = request.getInt("method");
		 Channel[] channels  = ChannelGroupHolder.channelGroup(Server.GROUP).toArray(new Channel[] {});
		 JSONObject response = new JSONObject();
		 switch (method) {
			case 1:{//查看当前channel
				if(channels == null) {
					logger.info("Channel's size is 0");
				}else {
					for(Channel channel : channels) {
						Attribute<TransportContext> attribute = ChannelUtil.getChannelMapAttribute(channel,  
								 Server.ATTRIBUTE_KEY_CHANNEL_CONTEXT, TransportContext.class);
				     	TransportContext transportContext = attribute.get();
				     	String gateway = (String) transportContext.get(TransportContext.CHANNELMARK);
						logger.info("channel id : {}, gateway : {}", channel.id(), gateway);
					}
				}
				
				return null;
			}
			
			case 2:{//测试上行数据
				String localModel = request.getString("localModel");
				String id = request.getString("id");
				JSONObject st = request.getJSONObject("st");
				HiLinkDeviceData uplinkData = HiLinkUtil.buildDeviceData(
						new HiLinkDeviceUID(HiLinkDeviceRepository.getDeviceConfig(localModel).getUIDType()+":"+id), st);
				logger.info("HiLinkDeviceData :{}", uplinkData);
				return null;
			}
			
			case 3:{//清除网关缓存
				String id = request.getString("id");
				DeviceCache.remove(id);
				logger.info("Clear gateway cache id:{}", id);
				return null;
			}
			
			case 4:{
				
			}break;
			
			case 5:{//测试控制同步
				Channel currChannel = requestContext.channel();
				RequestKey key = new RequestKey();
				long serial = key.getSerial();
				response.put("code", 1002);
				response.put("serial", serial);
				if(request.has("use")) {
					String id = request.getString("id");
					int ep = request.getInt("ep");
					int pmtjn = request.getInt("pmtjn");
					int enwtlst = request.getInt("enwtlst");
					response.put("id", id);
					response.put("ep", ep);
					JSONObject control = new JSONObject();
					control.put("pmtjn", pmtjn);
					control.put("enwtlst", enwtlst);
					response.put("control", control);
				}else {
					response.put("id", "00ffffffffffffffffff");
					response.put("ep", 1);
					JSONObject control = new JSONObject();
					control.put("pmtjn", 120);
					control.put("enwtlst", 0);
					response.put("control", control);
				}
				for(Channel channel :channels) {
					if(!channel.equals(currChannel)) {
						channel.writeAndFlush(response);
					}
				}
				RequestCache.put(key);
				SynchronousQueue<Object> getter = RequestCache.get(key);
				Object result = RequestCache.poll(getter, Constants.requestTimeout, TimeUnit.MILLISECONDS);
				if(result == null) {
					logger.info("Recevice synchronousQueue message failure");
				}
				JSONObject newResponse = (JSONObject)result;
				currChannel.writeAndFlush(newResponse);
				logger.info("Success to command device : {}", newResponse);
				return null;
				
			}
			
			case 6:{//查看DeviceCache
				Set<String> ids= DeviceCache.ids();
				if(ids.size() <= 0) {
					logger.info("DeviceCache is null");
				}
				Iterator<String> itor = ids.iterator();
				for(;itor.hasNext();) {
					String id = itor.next();
					Device device = DeviceCache.get(id);
					logger.info("DeviceCache id : {}, ep:{}, localModel:{}, DeviceInfo id:{}, isGateway:{}, gateway:{}, HiLinkDeviceUID:{}, isOnline: {}, isRegister: {} "
							,id , device.getEp(), device.getLocalModel(), id, device.isGateway(), device.getGateway(), device.getDeviceUID(), device.isOnline(), device.isRegister());
				}
				return null;
			}
			
			case 7:{//查看RegisterCache
				Set<String> ids= RegisterCache.ids();
				if(ids.size() <= 0) {
					logger.info("RegisterCache is null");
				}
				Iterator<String> itor = ids.iterator();
				for(;itor.hasNext();) {
					String id = itor.next();
					if(RegisterCache.get(id).getClass().equals(PortProperties.class)) {
						PortProperties portProperties= (PortProperties) RegisterCache.get(id);
						Iterator<Entry<String, JSONObject>> itorPort= portProperties.allProperties().entrySet().iterator();
						for(;itorPort.hasNext();) {
							Entry<String, JSONObject> entry = itorPort.next();
							String ep = entry.getKey();
							JSONObject st = entry.getValue();
							logger.info("RegisterCache id : {}, ep : {}, portProperty : {}"
									, id, ep, st);
						}
					}
				}
				return null;
			}
			
			default:
				break;
			}
		for(Channel channel :channels) {
			Channel currChannel = requestContext.channel();
			if(!channel.equals(currChannel)) {
				channel.writeAndFlush(response);
			}
		}
		 
		return null;
	}

	
}
