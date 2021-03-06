package com.shuncom.tcp.server.gateway;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

import com.huawei.hilink.device.status.HiLinkDeviceStatus;
import com.huawei.hilink.device.status.HiLinkDeviceStatusDetail;
import com.huawei.hilink.device.status.HiLinkDeviceStatusType;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.DeviceReportProxy;
import com.shuncom.tcp.server.ChannelGroupHolder;
import com.shuncom.tcp.server.ChannelUtil;
import com.shuncom.tcp.server.CustomizedChannelGroup;
import com.shuncom.tcp.server.gateway.action.Action;
import com.shuncom.tcp.server.gateway.action.ControlNotify;
import com.shuncom.tcp.server.gateway.action.Heartbeat;
import com.shuncom.tcp.server.gateway.action.Notify;
import com.shuncom.tcp.server.gateway.action.Register;
import com.shuncom.tcp.server.gateway.action.RemoveNotify;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;
import com.shuncom.tcp.server.gateway.cache.DeviceCache.Device;
import com.shuncom.util.Hex;
import com.shuncom.util.ValidationException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelMatchers;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;

public class RequestHandler extends ChannelInboundHandlerAdapter {
    
	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private Map<Integer, Action> actions = new HashMap<>();
	
	public RequestHandler() {
		actions.put(201, new Register());
	    actions.put(101, new Heartbeat());	
	    actions.put(102, new ControlNotify());	
	    actions.put(103, new RemoveNotify());	
	    actions.put(104, new Notify());	

	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	Packet packet = (Packet)msg;
    	Channel channel = ctx.channel();
    	ChannelRequestContext requestContext = new ChannelRequestContext(channel, packet);
    	int packetType = packet.getType();
    	switch(packetType) {
    	 case 1 : {
    		      authenticate(requestContext);
    		      handleRequest(requestContext);
    	 }; break;
    	 case 2 : establish(requestContext); break;
    	 default :
    		 throw new ValidationException("Unsupported packet type " + packetType);
    	}
    }

    @SuppressWarnings("unlikely-arg-type")
	private void establish(ChannelRequestContext requestContext) throws Exception {
    	Channel channel = requestContext.channel();
    	Packet packet = (Packet) requestContext.request();
    	JSONObject request = (JSONObject) packet.getConvert();
    	//logger.info("{} Authentication request {}", channel.id(), request);
    	
    	String id = request.getString("id");
    	final String mac = request.getString("mac");
    	Attribute<TransportContext> attribute = ChannelUtil.getChannelMapAttribute(requestContext.channel(),  
				 Server.ATTRIBUTE_KEY_CHANNEL_CONTEXT, TransportContext.class);
     	TransportContext transportContext = new TransportContext(channel);
     	transportContext.setAuthenticated(true);
     	attribute.setIfAbsent(transportContext);
     	transportContext.put(TransportContext.CHANNELMARK, mac);
     	
    	CustomizedChannelGroup channelGroup = ChannelGroupHolder.channelGroup(Server.GROUP);
    	if (channelGroup.contains(mac) && !channelGroup.contains(channel)) {
    		Channel previous = channelGroup.findChannel(mac);
    		channelGroup.disconnect(ChannelMatchers.is(previous));
    		channelGroup.remove(previous);
    	}
    	channelGroup.add(mac, channel);
    	
    	long timestamp = System.currentTimeMillis() / 1000;
    	String idSalt = id.substring(4);
    	byte[] salt = CodecUtil.encryptKey(idSalt, mac, timestamp);
        byte[] key = new byte[salt.length >> 1];
		System.arraycopy(salt, 0, key, 0, key.length);
        byte[] iv = new byte[salt.length >> 1];
		System.arraycopy(salt, iv.length, iv, 0, iv.length);
        transportContext.setAESKey(key);
        transportContext.setAESIv(iv);
        //logger.info("{} AES key {} iv {}" , channel.id(), Hex.encodeUpperHexString(key), Hex.encodeUpperHexString(iv));
        
        //channel关闭，1- 移除设备缓存记录，2 - 上报设备离线
        channel.closeFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				 Set<String> ids = DeviceCache.ids();
				 Iterator<String> itor = ids.iterator();
				 while (itor.hasNext()) {
					 String id = itor.next();
					 Device device = DeviceCache.get(id);
					 if (device != null && mac.equals(device.getGateway())) {
						 DeviceCache.remove(id);
						 //device.setOffline();
						 DeviceReportProxy.reportDeviceStatus(device.getDeviceUID(), new HiLinkDeviceStatus(HiLinkDeviceStatusType.OFFLINE,
								 HiLinkDeviceStatusDetail.NONE, "offline"));
					 }
				 }
			}
        });
        JSONObject response = new JSONObject();
        response.put("id", id);
        response.put("timestamp", timestamp);
        //logger.info("{} Authentication response {}", channel.id(), response);
    	Output output = new Output(0, 2, response.toString().getBytes("UTF-8"));
    	channel.writeAndFlush(output);
    }
    
    private void authenticate(ChannelRequestContext requestContext) throws Exception {
    	Channel channel = requestContext.channel();
    	TransportContext transportContext = ChannelUtil.getChannelMapAttribute(channel,  
				 Server.ATTRIBUTE_KEY_CHANNEL_CONTEXT, TransportContext.class).get();
    	if (transportContext == null || !transportContext.isAuthenticated()) {
    		logger.info("Close illegal channel {}", channel.id());
    		channel.close();
    		throw new ValidationException("Channel is not authencated {}" + channel.id());
    	}
    	String mac = (String) transportContext.get(TransportContext.CHANNELMARK);
    	CustomizedChannelGroup channelGroup = ChannelGroupHolder.channelGroup(Server.GROUP);
    	if(!channelGroup.containsValue(channel)) {
    		channelGroup.add(mac, channel);
    	}
    }
    
    private void handleRequest(ChannelRequestContext requestContext) throws Exception {
         Packet packet = (Packet) requestContext.request();
         JSONObject convert = (JSONObject) packet.getConvert();
         int code = convert.getInt("code");
         Action action = actions.get(code);
         if (action == null) {
        	 throw new ValidationException("Can not find action for request " + convert);
         }
         Object response = action.execute(requestContext);
         if (response != null) {
        	 requestContext.channel().writeAndFlush(response);
         }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    	if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
            	logger.info("Disconnect idle channel {}", ctx.channel().id());
                ctx.close();
            } 
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
    	logger.error("{} : {} ;at {}", cause.getClass().getName(), cause.getMessage(), cause.getStackTrace()[0]);
    }
    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    	logger.info("Channel registered {} {}", ctx.channel().id(), ctx.channel().remoteAddress());
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    	logger.info("Channel unregistered {} {}", ctx.channel().id(), ctx.channel().remoteAddress());
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	logger.info("Channel active {} {}", ctx.channel().id(), ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	logger.info("Channel inactive {} {}", ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
        ctx.fireChannelInactive();
    }
    
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }
    
}
