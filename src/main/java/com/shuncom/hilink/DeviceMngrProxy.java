package com.shuncom.hilink;

import com.huawei.hilink.openapi.device.HiLinkDeviceMngr;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;

public class DeviceMngrProxy {
	private static final Logger logger = LoggerFactory.getLogger(DeviceMngrProxy.class);
	private static HiLinkDeviceMngr deviceMngr = null;
	
	public void activate() {
		logger.info("Device mngr proxy activate");
	}
	
	public void deactivate() {
		logger.info("Device mgnr proxy deactivate");
	}
	
	public static HiLinkDeviceMngr getDeviceMngr() {
	   	return deviceMngr;
	}
	
	public void setDeviceMngr(HiLinkDeviceMngr deviceMngr) {
		DeviceMngrProxy.deviceMngr = deviceMngr;
	}
	public void unsetDeviceMngr(HiLinkDeviceMngr deviceMngr) {
		DeviceMngrProxy.deviceMngr = null;
	}
}
