package com.shuncom.hilink;

import java.io.File;
import java.util.Timer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.huawei.hilink.openapi.plugin.PluginListener;
import com.huawei.hilink.util.Logger;
import com.huawei.hilink.util.LoggerFactory;
import com.shuncom.tcp.server.gateway.DiscoverGwTasker;
import com.shuncom.tcp.server.gateway.SendPortUdpServer;
import com.shuncom.tcp.server.gateway.Server;

public class Activator implements BundleActivator, PluginListener {
	private final static Logger logger = LoggerFactory.getLogger(Activator.class);
	private static BundleContext bundleContext;
	private Server server;
	//private SearchServer searchServer;
	private  SendPortUdpServer udpServer;
	private Timer timer;
	public Activator() {
		//start gateway server
		server = new Server(6011);
		//searchServer = new SearchServer();
		udpServer = new SendPortUdpServer(9999);
		timer = new Timer();
	}
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.bundleContext = bundleContext;
		logger.info("Bundle start");
		logger.info("Ip {}", HiLinkUtil.inetAddress);
		//server.start();
		//udpServer.receive();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.start();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				searchServer.start();
			}
		}).start();*/
		new Thread(new Runnable() {
			@Override
			public void run() {
				udpServer.receive();
			}
		}).start();
		
		/*timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				new SearchClient(8888).sendPackage();
			}
		}, 0, 10 * 1000);*/
		try {
		timer.schedule(new DiscoverGwTasker(), 0, 60 * 1000);
		}catch (Exception e) {
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		logger.info("Bundle stop");
		//stop gateway server
		server.stop();
		/*searchServer.close();
		timer.cancel();*/
		udpServer.destroy();
		timer.cancel();
	}

	@Override
	public long getBundleId() {
		if (bundleContext == null || bundleContext.getBundle() == null) {
			return 0;
		}
		return bundleContext.getBundle().getBundleId();
	}

	@Override
	public void unInstall() {
		logger.info("Notify bundle uninstall");
		PluginMonitorProxy.pluginUninstallNotify(PluginMonitorProxy.pluginInfo);
	}
	
	public static File getRootFile() {
		return bundleContext == null ? null : bundleContext.getBundle().getDataFile(".");
	}

	@Override
	public String createUIURL(String url) {
		return null;
	}
	
	
}
