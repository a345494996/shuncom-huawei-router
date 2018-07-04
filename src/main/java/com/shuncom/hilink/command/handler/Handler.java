package com.shuncom.hilink.command.handler;

import com.huawei.hilink.device.cmd.HiLinkDeviceCommand;
import com.huawei.hilink.device.uid.HiLinkDeviceUID;

public interface Handler {
	HiLinkDeviceCommand handle(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand);
	String handleInner(HiLinkDeviceUID deviceUID, HiLinkDeviceCommand deviceCommand);
}
