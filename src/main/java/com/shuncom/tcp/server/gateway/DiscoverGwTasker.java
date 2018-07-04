package com.shuncom.tcp.server.gateway;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;

public class DiscoverGwTasker extends TimerTask {

	private final static Logger logger = LoggerFactory.getLogger(DiscoverGwTasker.class);
	@Override
	public void run() {
		try {
			
			JSONObject request = new JSONObject();
			request.put("code", 1);
			request.put("key", "shuncom_device");
			String sendStr = request.toString();
			DatagramSocket datagramSocket;
			datagramSocket = new DatagramSocket();
            byte[] buf = sendStr.getBytes("UTF-8");
            logger.debug("Send to {} ,{}", 8888, sendStr);
            InetAddress address = InetAddress.getByName("255.255.255.255");
			DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, 8888);
            datagramSocket.send(datagramPacket);
            logger.debug("Send successful");
			datagramSocket.close();
		} catch (Exception e) {
		}
	}
}
