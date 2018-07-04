package com.shuncom.hilink;

import com.huawei.hilink.openapi.plugin.PluginInfo;
import com.huawei.hilink.openapi.plugin.PluginMonitor;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;

public class PluginMonitorProxy {
	public static final String pkgName = "com.shuncom.huawei.router";  //应用的包名com.shuncom.huawei.router
	private static final String pluginManu = "064";  //厂商id
	private static final String pluginType = PluginInfo.PLUGIN_TYPE_HILINKPROXY;  //应用类型
	 	
	public static final PluginInfo pluginInfo = new PluginInfo(pkgName, pluginManu, pluginType);
	private static final Logger logger = LoggerFactory.getLogger(PluginMonitorProxy.class);
	private static PluginMonitor pluginMonitor;
	
	public void activate() {
		logger.info("Plugin monitor proxy activate");
		pluginMonitor.pluginInstallNotify(pluginInfo);
	}
	
	public void deactivate() {
		
	}
	
	public void setPluginMonitor(PluginMonitor pluginMonitor) {
    	PluginMonitorProxy.pluginMonitor = pluginMonitor;
    }
    
	public void unsetPluginMonitor(PluginMonitor pluginMonitor) {
    	PluginMonitorProxy.pluginMonitor = null;
    }
    
    public static void pluginInstallNotify(PluginInfo pluginInfo) {
    	pluginMonitor.pluginInstallNotify(pluginInfo);
    }
    
    public static void pluginUninstallNotify(PluginInfo pluginInfo) {
    	pluginMonitor.pluginUninstallNotify(pluginInfo);
    }
    
}
