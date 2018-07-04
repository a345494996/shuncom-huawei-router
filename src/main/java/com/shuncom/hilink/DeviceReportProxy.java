package com.shuncom.hilink;

import java.util.List;

import com.huawei.hilink.device.data.HiLinkDeviceData;
import com.huawei.hilink.device.info.HiLinkDeviceInfo;
import com.huawei.hilink.device.info.HiLinkServiceInfo;
import com.huawei.hilink.device.status.HiLinkDeviceStatus;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;
import com.huawei.hilink.openapi.device.HiLinkDeviceReport;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;

public class DeviceReportProxy {

	private static final Logger logger = LoggerFactory.getLogger(DeviceReportProxy.class);
	
	private static HiLinkDeviceReport deviceReport = null;

	public void activate() {
		logger.info("Device report proxy activate");
	}
	
	public void deactivate() {
		logger.info("Device report proxy deactivate");
	}
	
	public void setHiLinkDeviceReport(HiLinkDeviceReport deviceReport) {
		DeviceReportProxy.deviceReport = deviceReport;
	}

	public void unsetHiLinkDeviceReport(HiLinkDeviceReport deviceReport) {
		DeviceReportProxy.deviceReport = null;
	}
	
	public static void reportDeviceInfo(HiLinkDeviceUID hiLinkDeviceUID, HiLinkDeviceInfo hiLinkDeviceInfo) {
		deviceReport.reportDeviceInfo(hiLinkDeviceUID, hiLinkDeviceInfo);
	}
	  
	public static void reportDeviceData(HiLinkDeviceData hiLinkDeviceData) {
		deviceReport.reportDeviceData(hiLinkDeviceData);
	}
	  
    public static void reportDeviceStatus(HiLinkDeviceUID hiLinkDeviceUID, HiLinkDeviceStatus hiLinkDeviceStatus) {
    	deviceReport.reportDeviceStatus(hiLinkDeviceUID, hiLinkDeviceStatus);
    }
	  
	public static void reportDeviceDiscovered(HiLinkDeviceUID hiLinkDeviceUID, HiLinkDeviceInfo hiLinkDeviceInfo, List<HiLinkServiceInfo> list) {
		deviceReport.reportDeviceDiscovered(hiLinkDeviceUID, hiLinkDeviceInfo, list);
	}
	  
	public static void reportDeviceRemoved(HiLinkDeviceUID hiLinkDeviceUID) {
		deviceReport.reportDeviceRemoved(hiLinkDeviceUID);
	}
	  
	  
	public static void reportNewDevice(String str, HiLinkDeviceInfo hiLinkDeviceInfo) {
		deviceReport.reportNewDevice(str, hiLinkDeviceInfo);
	}

	public static void reportDeviceAccess(String str, HiLinkDeviceInfo hiLinkDeviceInfo, List<HiLinkServiceInfo> serviceInfoList) {
		deviceReport.reportDeviceAccess(str, hiLinkDeviceInfo);
	}
}
