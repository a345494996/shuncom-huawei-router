package com.shuncom.tcp.server.gateway;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.json.JSONObject;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.hilink.HiLinkUtil;
import com.shuncom.tcp.server.gateway.cache.DeviceCache;

public class SendPortUdpServer {
	
	private final static Logger logger = LoggerFactory.getLogger(SendPortUdpServer.class);
	private final int MAX_LENGTH = 1024; // 最大接收字节长度  // port号
    // 用以存放接收数据的字节数组
    private byte[] receMsgs = new byte[MAX_LENGTH];
	// 数据报套接字
    private DatagramSocket datagramSocket;
    private final int port;
    private boolean broken;
    
    public SendPortUdpServer(int port) {
    	this.port = port;
    	this.datagramSocket = getDataramSocket(port);
		logger.info("Listen on :{}", port);
		this.broken = false;
    }
    public SendPortUdpServer() {
    	this.port = 9999;
    	this.datagramSocket = getDataramSocket(port);
    	this.broken = false;
    }
    public void receive() {
    	if(this.broken) {
    		return;
    	}
    	try {
	    	DatagramPacket request = new DatagramPacket(receMsgs, receMsgs.length);
	    	datagramSocket.receive(request);
	        String receStr = new String(request.getData(), 0 , request.getLength());
	        receiveAndSendHandler(request, receStr);
	        close();
	        datagramSocket = getDataramSocket(port);
	        receive();
    	}
    	catch (Exception e) {
    		if(this.broken) {
        		return;
        	}
    		close();
        	datagramSocket = getDataramSocket(port);
        	receive();
		}
    }
    
    private void receiveAndSendHandler(DatagramPacket request, String receStr) throws IOException {
    	if(HiLinkUtil.isJSON(receStr)) {
    		JSONObject data = new JSONObject(receStr);
    		logger.info("data {}", data);
    		if(data.has("code") && data.has("msg") && data.has("ethmac")) {
    			int code = data.getInt("code");
    			//String msg = data.getString("msg");
    			String mac = data.getString("ethmac");
    			if(code == 0 && !DeviceCache.containsGwMac(mac)) {
    				//send(datagramSocket, back(request, getBack()));
    				datagramSocket.send(back(request, getBack()));
    			}
    		}
    	}
    }
    
    public void close() {
    	if(datagramSocket != null) {
    		
    		datagramSocket.close();
    		datagramSocket = null;
    	}
    }
    
    public void destroy() {
    	logger.info("Udp {} distroy", port);
    	this.broken = true;
    	close();
    }
    
    private static DatagramSocket getDataramSocket(int port) {
    	try {
			return new DatagramSocket(port);
		} catch (SocketException e) {
			return null;
		}
    }
 
    
    private DatagramPacket back(DatagramPacket request, String result) throws UnsupportedEncodingException {
    	DatagramPacket response = new DatagramPacket(receMsgs, receMsgs.length, request.getAddress(), request.getPort());
    	response.setData(result.getBytes("UTF-8"));
    	return response;
    }
    
    private String getBack() {
    	JSONObject result = new JSONObject();
    	result.put("code", 2);
    	result.put("key", "shuncom_device");
    	result.put("prevgwip", HiLinkUtil.inetAddress.getHostAddress());
    	//System.out.println(HiLinkUtil.inetAddress.getHostAddress());
    	result.put("prevgwport", 6011);
    	
    	return result.toString();
    }
    
    public boolean isBroken() {
    	return this.broken;
    }
    
    public static void main(String[] args) {
		new SendPortUdpServer(9999).receive();
	}
}
