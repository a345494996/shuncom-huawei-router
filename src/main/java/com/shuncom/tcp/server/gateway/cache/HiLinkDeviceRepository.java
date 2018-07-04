package com.shuncom.tcp.server.gateway.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class HiLinkDeviceRepository {
    private static Map<String, DeviceConfig> deviceConfigs = new HashMap<>();
    private static Map<String, String> modelMappings = new HashMap<>();
    private static final String pkgName = "com.shuncom.huawei.router";
    private static final String hwv = "sz05l-pro-2";
    private static final String hiv = "2.1";
    private static final Map<Integer, String> ztypeToModelMappings = new HashMap<>();
    private static final Map<String, String> swidToModelMappings = new HashMap<>();
    private static final Map<String, String> modelToCategory = new HashMap<>();
    private static final Map<String, String> prodidToModel = new HashMap<>();
    
    public static String getModelToCategory(String localModel) {
    	
    	return modelToCategory.get(localModel);
    }
    
	public static DeviceConfig getDeviceConfig(String localModel) {
    	return deviceConfigs.get(localModel);
    }
    
    public static boolean containLocalModel(String localModel) {
    	
    	return deviceConfigs.containsKey(localModel);
    }
    
    public static boolean containDeviceConfig(DeviceConfig devConfig) {
    	
    	return deviceConfigs.containsValue(devConfig);
    }
    
    public static String getLocalModel(String model) {
    	return modelMappings.get(model);
    }
    
    public static DeviceConfig getDeviceConfigByModel(String model) {
    	return getDeviceConfig(getLocalModel(model));
    }
    
    public static DeviceConfig getDeviceConfigByProdid(String prodid) {
    	String localModel = getLocalModelByProdid(prodid);
    	if(localModel == null) {
    		return null;
    	}
    	return getDeviceConfig(localModel);
    }
    
    public static String getLocalModelByProdid(String prodid) {
    	if(!prodidToModel.containsKey(prodid)) {
    		return null;
    	}
    	return prodidToModel.get(prodid);
    }
    
    
    public static String getLocalModelByZtype(int ztype) {
    	return ztypeToModelMappings.get(ztype);
    }
    
    public static boolean containZtype(int ztype) {
    	return ztypeToModelMappings.containsKey(ztype);
    }
    
    public static String getLocalModelBySwid(String swid) {
    	return swidToModelMappings.get(swid);
    }
    public static boolean containSwid(String swid) {
    	return swidToModelMappings.containsKey(swid);
    }
    
    //Gateway
    static {
    	String localModel = "SHUNCOM-GATAWAY";
    	//网桥
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("020")
    	      .setModel("sz09-GW-02")
    	      .setProdId("1068")
    	      .setName("bridge")
    	      .setDescription("bridge")
    	      .setHwv(hwv)
    	      .setFwv("4531-cciot-v1.7-smarthome-67")
    	      .setSwv("4531-cciot-v1.7-smarthome-67")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("1068", localModel);
    	
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	//子设备发现添加
    	ServiceInfo discovery = new ServiceInfo("discovery", "discovery");
    	discovery.addProfileProp("enable", "REPORT/GET/PUT")//0关，1开
    	         .addProfileProp("times", "PUT")//0-255组网 0立刻停止加网
    	         .addProfileProp("productid", "PUT")//需要添加的设备id
    	         .addPropMapping("enable", "enable")
    	         .addPropMapping("times", "times")
    	         .addPropMapping("productid", "productid")
    	         .setUnmodifiable();
    	services.put("discovery", discovery);
    	
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services),new DefaultConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	
    	modelToCategory.put(localModel, "gateway");
    }
    
    //门磁感应设备shuncom contact sensor device 
    static {
    	String localModel = "HORN-MECI";
    	
    	//门磁感应设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("018")
    	      .setModel("sz09-(05)")
    	      .setProdId("1069")
    	      .setName("contact sensor")
    	      .setDescription("contact sensor")
    	      .setHwv(hwv)
    	      .setFwv("HORN-MECI-v3.70B")
    	      .setSwv("HORN-MECI-v3.70B")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("1069", localModel);
    	Convert convert = new DeviceStatusConvert();
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	//门磁（门窗）传感器
    	ServiceInfo doorSensor = new ServiceInfo("doorSensor", "doorSensor");
    	doorSensor.addProfileProp("state", "REPORT/GET")//0关闭，1打开 门窗开关状态
    	         .addProfileProp("event", "REPORT")//0关闭，1打开 门窗开关事件上报
    	         .addPropMapping("state", "state")
    	         .addPropMapping("event", "event")
    	         .setUnmodifiable();
    	services.put("doorSensor", doorSensor);
    	//电量
    	ServiceInfo battery = new ServiceInfo("battery", "battery");
    	battery.addProfileProp("lowBattery", "REPORT/GET")
    	         .addPropMapping("lowBattery", "lowBattery")//0无告警 ，1告警， 低电告警
    	         .setUnmodifiable();
    	services.put("battery", battery);
    	
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), convert));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	ztypeToModelMappings.put(21, localModel);
    	swidToModelMappings.put("HORN-MECI", localModel);
    	
    	modelToCategory.put(localModel, "meci");
    }
    
  //shuncom gas sensor device  燃气报警设备
    static {
    	String localModel = "HORN-GAS";
    	
    	//燃气报警设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("03A")
    	      .setModel("sz09-(02)")
    	      .setProdId("106A")
    	      .setName("gas sensor")
    	      .setDescription("gas sensor")
    	      .setHwv(hwv)
    	      .setFwv("HoenGSAL_BP_1.50")
    	      .setSwv("HoenGSAL_BP_1.50")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("106A", localModel);
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	//smoke
    	ServiceInfo smoke = new ServiceInfo("smoke", "smoke");
    	smoke.addProfileProp("alarm", "REPORT/GET/PUT")//0报警关闭 ，1报警开启 （开关控制）
    	         .addPropMapping("alarm", "zsta")
    	         .setUnmodifiable();
    	services.put("smoke", smoke);
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new GasSensorConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	ztypeToModelMappings.put(43, localModel);
    	swidToModelMappings.put("HORN-GAS", localModel);
    	modelToCategory.put(localModel, "gasSensor");
    }
    
  //shuncom PIR sensor device 人体红外感应设备
    static {
    	String localModel = "HORN-PIR";//SHUNCOM-R-001 ztype:13
    	
    	//人体红外感应设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("03E")
    	      .setModel("sz09-(04)")
    	      .setProdId("106B")
    	      .setName("PIR sensor")
    	      .setDescription("PIR sensor")
    	      .setHwv(hwv)
    	      .setFwv("HORN-PIR--v3.70B")
    	      .setSwv("HORN-PIR--v3.70B")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("106B", localModel);
    	Convert convert = new PirConvert();
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	//移动探测
    	ServiceInfo motionSensor = new ServiceInfo("motionSensor", "motionSensor");
    	motionSensor.addProfileProp("alarm", "REPORT")//0无人移动 ，1 有人移动
    	         .addPropMapping("alarm", "state")
    	         .setUnmodifiable();
    	services.put("montionSensor", motionSensor);
    	//电量
    	ServiceInfo battery = new ServiceInfo("battery", "battery");
    	battery.addProfileProp("lowBattery", "REPORT/GET")// 0无告警 ，1告警 （低电告警）
    	         .addPropMapping("lowBattery", "lowBattery")
    	         .setUnmodifiable();
    	services.put("battery", battery);
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), convert));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	ztypeToModelMappings.put(13, localModel);
    	swidToModelMappings.put("HORN-PIR", localModel);
    	
    	modelToCategory.put(localModel, "pir");
    }
    
  //shuncom smart warning siren device 声光报警设备
   /* static {
    	String localModel = "SHUNCOM-WARNING-SIREN";
    	
    	//声光报警设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("018")
    	      .setModel("sz09-(20)")
    	      .setProdId("106C")
    	      .setName("smart warning siren")
    	      .setDescription("smart warning siren")
    	      .setHwv(hwv)
    	      .setFwv("HoenGSAL_BP_1.50")
    	      .setSwv("HoenGSAL_BP_1.50")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	// 
    	ServiceInfo alertor = new ServiceInfo("alertor", "alertor");
    	alertor.addProfileProp("state", "REPORT/GET/PUT")//开关状态 0消除报警 1触发报警
		    	.addProfileProp("switch", "REPORT/GET/PUT")//报警灯控制器 0报警灯关闭 1报警灯开启
		    	.addProfileProp("time", "REPORT/GET/PUT")//报警持续时间 0 响一分钟 1一直响 （单位秒， 0-65535）
		    	.addProfileProp("pulse", "REPORT/GET/PUT")//闪光灯的亮度（占空比）（0-100 ,必须为10的倍数）
		    	.addProfileProp("frequency", "REPORT/GET/PUT")//闪光频率（0低，1中，2高，3非常高）
    	        .addPropMapping("state", "")
    	        .addPropMapping("switch", "")
    	        .addPropMapping("time", "")
    	        .addPropMapping("pulse", "")
    	        .addPropMapping("frequency", "")
    	        .setUnmodifiable();
    	services.put("alertor", alertor);
    	
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new DefaultConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    }*/
    
    
  //shuncom curtain motor device 窗帘电机设备
    static {
    	String localModel = "Shuncom-curtain-motor";
    	
    	//窗帘电机设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("01C")
    	      .setModel("sz09-(18)")
    	      .setProdId("106D")
    	      .setName("curtain motor device")
    	      .setDescription("curtain motor device")
    	      .setHwv(hwv)
    	      .setFwv("DuYaMoTo_BP_1.41")
    	      .setSwv("DuYaMoTo_BP_1.41")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("106D", localModel);
    	Convert convert = new CurtainConvert();
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	//窗帘电机
    	ServiceInfo motor = new ServiceInfo("motor", "motor");
    	motor.addProfileProp("mode", "PUT")//开关状态 0关/下 1开/上 2停止
		    	.addPropMapping("mode", "cts")//no need convert
    	        .setUnmodifiable();
    	services.put("motor", motor);
    	
    	//开合度
    	ServiceInfo openLevel = new ServiceInfo("openLevel","openLevel");
    	openLevel.addProfileProp("targetLevel", "PUT")//目标开合度（0-100）
    				.addProfileProp("currentLevel", "REPORT/GET")//当前开合度（0-100）
    				.addPropMapping("targetLevel", "pt")//pt打开百分比
    				.addPropMapping("currentLevel", "pt")
    				.setUnmodifiable();
    	services.put("openLevel", openLevel);
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), convert));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	swidToModelMappings.put("UiotMoto3_1.2.06", localModel);
    	
    	modelToCategory.put(localModel, "curtainMotor");
    }
    
    
  //shuncom smoke sensor device 烟雾传感器设备
    static {
    	String localModel = "HORN-SMOG";
    	
    	//烟雾报警设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("039")
    	      .setModel("sz09-(11)")
    	      .setProdId("106E")
    	      .setName("smoke sensor")
    	      .setDescription("smoke sensor")
    	      .setHwv(hwv)
    	      .setFwv("HORN-SMOG-v3.70B")
    	      .setSwv("HORN-SMOG-v3.70B")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("106E", localModel);
    	Map<String, ServiceInfo> services = new HashMap<>(1);
    	//报警器
    	ServiceInfo smoke = new ServiceInfo("smoke", "smoke");
    	smoke.addProfileProp("alarm", "REPORT/GET")//(0 无告警 ，1告警) 烟雾告警
		    	.addPropMapping("alarm", "state")
    	        .setUnmodifiable();
    	services.put("smoke", smoke);
    	//电量
    	ServiceInfo battery = new ServiceInfo("battery", "battery");
    	battery.addProfileProp("lowBattery", "REPORT/GET")//(0 无告警 ，1告警) 低电告警
		    	.addPropMapping("lowBattery", "lowBattery")
    	        .setUnmodifiable();
    	services.put("battery", battery);
    	
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new DeviceStatusConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	ztypeToModelMappings.put(40, localModel);
    	swidToModelMappings.put("HORN-SMOG", localModel);
    	
    	modelToCategory.put(localModel, "smokeSensor");
    }
    
    
  //shuncom smart door lock device 智能指纹锁 
    static {
    	String localModel = "320";//SHUNCOM-DOOR-LOCK
    	
    	//智能指纹锁设备
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
    	      .setDevType("04B")
    	      .setModel("sz09-(19)-320c")
    	      .setProdId("106F")
    	      .setName("smart door lock")
    	      .setDescription("smart door lock")
    	      .setHwv(hwv)
    	      .setFwv("BALLEGION_1.2.24")
    	      .setSwv("BALLEGION_1.2.24")
    	      .setHiv(hiv)
    	      .setPkgName(pkgName);
    	prodidToModel.put("106F", localModel);
    	Map<String, ServiceInfo> services = new HashMap<>();
    	//电量
    	ServiceInfo battery = new ServiceInfo("battery", "battery");
    	battery.addProfileProp("lowBattery", "REPORT/GET")//低电量告警 0 无告警，1告警
    			.addProfileProp("level", "REPORT/GET")//剩余电量百分比(0-100)
    			.addProfileProp("batteryThreshold", "REPORT/GET")//低电量告警门限值（0-100）
		    	.addPropMapping("lowBattery", "lowBattery")
		    	.addPropMapping("level", "level")
		    	.addPropMapping("batteryThreshold", "batteryThreshold")
    	        .setUnmodifiable();
    	services.put("battery", battery);
    	
    	//lockAlarm门锁告警
    	ServiceInfo lockAlarm = new ServiceInfo("lockAlarm", "lockAlarm");
    	lockAlarm.addProfileProp("alarm", "REPORT/GET")//(0-14) 15种情况
		    	.addPropMapping("alarm", "armCode")
    	        .setUnmodifiable();
    	services.put("lockAlarm", lockAlarm);
    	
    	//智能门锁
    	ServiceInfo smartLock = new ServiceInfo("smartLock", "smartLock");
    	smartLock.addProfileProp("state", "REPORT/GET")//(0-3)4种情况 门锁状态
    			.addProfileProp("id", "REPORT")//权限ID 由厂家提供 0未识别用户 （1-65535）正常用户编码
    			.addProfileProp("type", "REPORT")//开锁方式（1-4）
		    	.addPropMapping("state", "dsta")
		    	.addPropMapping("id", "uid")
		    	.addPropMapping("type", "type")
    	        .setUnmodifiable();
    	services.put("smartLock", smartLock);
    	
    	//账号管理
    	ServiceInfo account = new ServiceInfo("account", "account");
    	account.addProfileProp("action", "REPORT")//0增，1删，2改 增删改权限操作
    			.addProfileProp("type", "REPORT")//开户类型（1-4）
    			.addProfileProp("id", "REPORT")//权限ID，删除权限时需要 0未识别用户 （1-65535）正常用户编码
		    	.addPropMapping("action", "action")
		    	.addPropMapping("type", "proesc")
		    	.addPropMapping("id", "cid")
    	        .setUnmodifiable();
    	services.put("account", account);
    	
    	//开关门事件
    	ServiceInfo event = new ServiceInfo("event", "event");
    	event.addProfileProp("event", "REPORT")//（1-4）开关门事件
    			.addProfileProp("id", "REPORT")//权限ID 钥匙开锁时无需上报
    			.addPropMapping("event", "type")
    			.addPropMapping("id", "uid")
    			.setUnmodifiable();
    	services.put("event", event);
    	
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new DoorLockConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    	
    	modelToCategory.put(localModel, "doorLock");
    }
    
    //shuncom one key switch
    /*static {
    	String localModel = "SzDeLiXi Remote1";
    	//顺舟一键开关
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
	      .setDevType("")
	      .setModel("")
	      .setProdId("")
	      .setName("smart switch")
	      .setDescription("smart switch")
	      .setHwv(hwv)
	      .setFwv("")
	      .setSwv("")
	      .setHiv(hiv)
	      .setPkgName(pkgName);
    	prodidToModel.put("", localModel);
    	Map<String, ServiceInfo> services = new HashMap<>();
    	ServiceInfo swt = new ServiceInfo("switch", "binarySwitch"); 
    	swt.addProfileProp("on", "REPORT/GET/PUT")
				.addPropMapping("on", "on1")
				.setUnmodifiable();
    	services.put("switch", swt);
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new DoorLockConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    }
    
    //shuncom two key switch
    static {
    	String localModel = "SzDeLiXi Remote2";
    	//顺舟二键开关
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
	      .setDevType("")
	      .setModel("")
	      .setProdId("")
	      .setName("smart switch")
	      .setDescription("smart switch")
	      .setHwv(hwv)
	      .setFwv("")
	      .setSwv("")
	      .setHiv(hiv)
	      .setPkgName(pkgName);
    	prodidToModel.put("", localModel);
    	Map<String, ServiceInfo> services = new HashMap<>();
    	ServiceInfo swt = new ServiceInfo("switch01", "binarySwitch"); 
    	swt.addProfileProp("on", "REPORT/GET/PUT")
				.addPropMapping("on", "on1")
				.setUnmodifiable();
    	services.put("switch01", swt);
    	ServiceInfo swt2 = new ServiceInfo("switch02", "binarySwitch"); 
    	swt.addProfileProp("on", "REPORT/GET/PUT")
				.addPropMapping("on", "on2")
				.setUnmodifiable();
    	services.put("switch02", swt2);
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new DoorLockConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    }
    
  //shuncom three key switch
    static {
    	String localModel = "SzDeLiXi Remote3";
    	//顺舟三键开关
    	DeviceInfo device = new DeviceInfo();
    	device.setManu("064")
	      .setDevType("")
	      .setModel("")
	      .setProdId("")
	      .setName("smart switch")
	      .setDescription("smart switch")
	      .setHwv(hwv)
	      .setFwv("")
	      .setSwv("")
	      .setHiv(hiv)
	      .setPkgName(pkgName);
    	prodidToModel.put("", localModel);
    	Map<String, ServiceInfo> services = new HashMap<>();
    	ServiceInfo swt = new ServiceInfo("switch01", "binarySwitch"); 
    	swt.addProfileProp("on", "REPORT/GET/PUT")
				.addPropMapping("on", "on1")
				.setUnmodifiable();
    	services.put("switch01", swt);
    	ServiceInfo swt2 = new ServiceInfo("switch02", "binarySwitch"); 
    	swt.addProfileProp("on", "REPORT/GET/PUT")
				.addPropMapping("on", "on2")
				.setUnmodifiable();
    	services.put("switch02", swt2);
    	ServiceInfo swt3 = new ServiceInfo("switch03", "binarySwitch"); 
    	swt.addProfileProp("on", "REPORT/GET/PUT")
				.addPropMapping("on", "on2")
				.setUnmodifiable();
    	services.put("switch03", swt3);
    	deviceConfigs.put(localModel, new DeviceConfig(localModel, device, Collections.unmodifiableMap(services), new DoorLockConvert()));
    	modelMappings.put(device.getDevType() + ":" + device.getManu() + ":" + device.getModel(), localModel);
    }*/
     
    public static class DeviceConfig {
    	private final String model;
    	private final DeviceInfo deviceInfo;
    	private final Map<String, ServiceInfo> serviceInfos;
    	private final Convert convert;
    	public DeviceConfig(String model, DeviceInfo deviceInfo, Map<String, ServiceInfo> serviceInfos, Convert convert) {
    		this.model = model;
    		this.deviceInfo = deviceInfo;
    		this.serviceInfos = serviceInfos;
    		this.convert = convert;
    	}

		public String getModel() {
			return model;
		}
		

		public Convert getConvert() {
			return convert;
		}

		public DeviceInfo getDeviceInfo() {
			return deviceInfo;
		}

		public Map<String, ServiceInfo> getServiceInfos() {
			return serviceInfos;
		}
		
		public String getUIDType() {
			return deviceInfo.getDevType() + ":" + deviceInfo.getManu() + ":" +deviceInfo.getModel();
		}
    }
    
    public static class DeviceInfo {
    	private String name;  //设备名称
    	private String manu;  //设备厂商id
	    private String model;  //设备型号
	    private Integer protType;  //协议类型, 1 - WIFI, 2 - Z_WIFI, 3 - ZIGBEE
	    private String devType;  //设备类型id
	    private String description;  //设备简要描述
	    private String mac;  //设备mac地址
	    private String hwv;  //hardware version - 硬件版本
	    private String fwv;  //firmware version - 固件版本
	    private String hiv;  //HiLink协议版本
	    private String swv;  //software version - 软件版本
	    private String prodId;  //产品id
	    private String pkgName;  //需要安装组件的包名
	    
		public String getName() {
			return name;
		}
		
		public String getManu() {
			return manu;
		}
		
		public String getModel() {
			return model;
		}
		
		public Integer getProtType() {
			return protType;
		}
		
		public String getDevType() {
			return devType;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getMac() {
			return mac;
		}
		
		public String getHwv() {
			return hwv;
		}
		
		public String getFwv() {
			return fwv;
		}
		
		public String getHiv() {
			return hiv;
		}
		
		public String getSwv() {
			return swv;
		}
		
		public String getProdId() {
			return prodId;
		}
		
		public String getPkgName() {
			return pkgName;
		}

		public DeviceInfo setName(String name) {
			if (this.name == null) {
				this.name = name;
			}
			return this;
		}

		public DeviceInfo setManu(String manu) {
			if (this.manu == null) {
			    this.manu = manu;
			}
			return this;
		}

		public DeviceInfo setModel(String model) {
			if (this.model == null) {
				this.model = model;
			}
			return this;
		}

		public DeviceInfo setProtType(Integer protType) {
			if (this.protType == null) {
				this.protType = protType;
			}
			return this;
		}

		public DeviceInfo setDevType(String devType) {
			if (this.devType == null) {
				this.devType = devType;
			}
			return this;
		}

		public DeviceInfo setDescription(String description) {
			if (this.description == null) {
				this.description = description;
			} 
			return this;
			
		}

		public DeviceInfo setMac(String mac) {
			if (this.mac == null) {
				this.mac = mac;
			} 
			return this;
		}

		public DeviceInfo setHwv(String hwv) {
			if (this.hwv == null) {
				this.hwv = hwv;
			} 
			return this;
			
		}

		public DeviceInfo setFwv(String fwv) {
			if (this.fwv == null) {
				this.fwv = fwv;
			} 
			return this;
		}

		public DeviceInfo setHiv(String hiv) {
			if (this.hiv == null) {
				this.hiv = hiv;
			} 
			return this;
		}

		public DeviceInfo setSwv(String swv) {
			if (this.swv == null) {
				this.swv = swv;
			} 
			return this;
		}

		public DeviceInfo setProdId(String prodId) {
			if (this.prodId == null) {
				this.prodId = prodId;
			} 
			return this;
		}
		
		public DeviceInfo setPkgName(String pkgName) {
			if (this.pkgName == null) {
				this.pkgName = pkgName;
			} 
			return this;
		}
    } 
    
    public static class ServiceInfo {
    	private final String serviceId;
    	private final String serviceType;
    	private Map<String, Object> serviceProfile = new HashMap<>();
    	private Map<String, String> propsMapping = new HashMap<>();
    	public ServiceInfo(String serviceId, String serviceType) {
    		this.serviceId = serviceId;
    		this.serviceType = serviceType;
    	}

		public String getServiceId() {
			return serviceId;
		}

		public String getServiceType() {
			return serviceType;
		}
		
		public ServiceInfo addProfileProp(String key, Object value) {
			serviceProfile.put(key, value);
			return this;
		}
		
		public ServiceInfo addPropMapping(String profileProp, String localProp) {
			propsMapping.put(profileProp, localProp);
			return this;
		}
		
		public Map<String, Object> getServiceProfile() {
			return serviceProfile;
		}
		
		public Map<String, String> getPropsMapping() {
			return propsMapping;
		}
		
		
		public void setUnmodifiable() {
			serviceProfile = Collections.unmodifiableMap(serviceProfile);
			propsMapping = Collections.unmodifiableMap(propsMapping);
		}
    }
    
	public static interface Convert {
		//转换华为数据
		public abstract JSONObject convertHuaweiData(JSONObject param);
		//转换 shuncom数据
		public abstract JSONObject convertShuncomData(JSONObject param);
	}
	public static class DefaultConvert implements Convert {
		@Override
		public JSONObject convertHuaweiData(JSONObject param) {
			return param;
		}
		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			return param;
		}
	}
	public static class GasSensorConvert extends DefaultConvert {
		
		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			JSONObject result = new JSONObject();
			if(param.has("cluster") && param.has("commondid")) {
				return result;
			}
			
			return param;
		}
	}
	public static class DeviceStatusConvert extends DefaultConvert {
		
		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			
			JSONObject result = new JSONObject();
			if(param.has("cluster") && param.has("commondid")) {
				return result;
			}
			if(param.has("zsta")) {
				int zsta = param.getInt("zsta");
				if(zsta >= 12) {
					result.put("lowBattery", 1);
					result.put("event", 1);
					zsta = zsta - 12;
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				} else if(zsta >= 8) {
					result.put("lowBattery", 1);
					result.put("event", 0);
					zsta = zsta - 8;
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				} else if(zsta >= 4) {
					result.put("lowBattery", 0);
					result.put("event", 1);
					zsta = zsta - 4;
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				} else if(zsta >= 0 && zsta <= 1){
					result.put("lowBattery", 0);
					result.put("event", 0);
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				}
			}
			return result;
		}
	}
	
	
public static class PirConvert extends DefaultConvert {
		
		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			JSONObject result = new JSONObject();
			
			if(param.has("zsta")) {
				int zsta = param.getInt("zsta");
				if(zsta >= 12) {
					result.put("lowBattery", 1);
					result.put("event", 1);
					zsta = zsta - 12;
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				} else if(zsta >= 8) {
					result.put("lowBattery", 1);
					result.put("event", 0);
					zsta = zsta - 8;
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				} else if(zsta >= 4) {
					result.put("lowBattery", 0);
					result.put("event", 1);
					zsta = zsta - 4;
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				} else if(zsta >= 0 && zsta <= 1){
					result.put("lowBattery", 0);
					result.put("event", 0);
					if(zsta == 0) {
						result.put("state", 0);
					} else if(zsta == 1) {
						result.put("state", 1);
					}
					return result;
				}
			}
			return result;
		}
	}
	
	public static class CurtainConvert extends DefaultConvert{
		@Override
		public JSONObject convertHuaweiData(JSONObject param) {
			JSONObject result = new JSONObject();
			if(param.has("pt")) {
				int pt = param.getInt("pt");
				if(pt >= 100) {
					result.put("cts", 1);
				}else if(pt <= 0) {
					result.put("cts", 0);
				}else {
					result.put("pt", pt);
				}
			}
			return result;
		}

		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			JSONObject result = new JSONObject();
			if(param.has("cts")) {
				int cts = param.getInt("cts");
				if(cts == 1) {
					result.put("pt", 100);
				}else if(cts == 0) {
					result.put("pt", 0);
				}
			}
			if(param.has("pt")) {			
				int pt = param.getInt("pt");
				if(pt > 100) {
					pt = (100 * pt) / 255;
				}
				if(pt < 0) {
					pt = 0;
				}
				result.put("pt", pt);
				
			}
			return result;
		}
		
	}
	
	public static class DoorLockConvert extends DefaultConvert{

		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			JSONObject result = new JSONObject();
			
			if(param.has("batpt")) {
				int bt = param.getInt("batpt") / 2;
				if(bt < 0) {
					bt = 0;
				}else if(bt > 100) {
					bt = 100;
				}else if(bt < 20) {
					result.put("lowBattery", 1);
				}else {
					result.put("lowBattery", 0);
				}
				result.put("batteryThreshold", 20);
				result.put("level", bt);
			}
			
			
			if(param.has("cluster") && param.has("commondid")) {
				int cluster = param.getInt("cluster");
				int commondid = param.getInt("commondid");
				
				if(cluster == 257) {
					//user operation
					if(commondid == 33) {
						if(param.has("proecd")) {
							int proecd = param.getInt("proecd");
							proecd = proecd - 2;
							result.put("action", proecd);
						}
						if(param.has("proesc")) {
							int proesc = param.getInt("proesc");
							if(proesc == 4) {
								result.put("proesc", 1);
							}else if(proesc == 0) {
								result.put("proesc", 2);
							}else if(proesc == 3) {
								result.put("proesc", 3);
							}else if(proesc == 2) {
								result.put("proesc", 4);
							}
							
						}
						
						if(param.has("uid")) {
							int uid = param.getInt("uid");
							result.put("cid", uid);
						}
					}else if(commondid == 32) {//operate door
						if(param.has("proecd")) {
							int proecd = param.getInt("proecd");
							if(proecd == 2) {
								result.put("dsta", 1);
							}else if(proecd == 1) {
								result.put("dsta", 0);
							}
						}
						if(param.has("proesc")) {
							int proesc = param.getInt("proesc");
							if(proesc == 4) {
								result.put("type", 1);
							}else if(proesc == 0) {
								result.put("type", 2);
							}else if(proesc == 3) {
								result.put("type", 3);
							}else if(proesc == 2) {
								result.put("type", 4);
							}
							
						}
						if(param.has("uid")) {
							int uid = param.getInt("uid");
							result.put("uid", uid);
						}
					}
					
				}else if(cluster == 9) {//lock alarm
					if(commondid == 0) {
						if(param.has("armCode")) {
							result.put("armCode", param.getInt("armCode") + 1);
						}
					}
				}
			}
			
			if(param.has("dsta")) {
				int dsta = param.getInt("dsta");
				if(dsta == 1 || dsta == 3) {
					result.put("dsta", 2);
				}else if(dsta == 0) {
					result.put("dsta", 0);
				}else if(dsta == 2) {
					result.put("dsta", 1);
				}
			}
			return result;
		}
	}
	
	/*public static class SwitchConvert extends DefaultConvert{
		String swt = "switch";

		@Override
		public JSONObject convertHuaweiData(JSONObject param) {
			JSONObject result = new JSONObject();
			//if()
			return super.convertHuaweiData(param);
		}

		@Override
		public JSONObject convertShuncomData(JSONObject param) {
			JSONObject result = new JSONObject();
			if(!param.has("ep")) {
				return result;
			}
			int ep = param.getInt("ep");
			if(param.has("on")) {
				if(param.getBoolean("on")) {
					result.put("on" + ep, 1);
				} else {
					result.put("on" + ep, 0);
				}
			}
			return result;
		}
	}*/
}
