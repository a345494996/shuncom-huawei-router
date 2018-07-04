package com.shuncom.hilink;

public class ErrorConstants {

	private ErrorConstants () {}
	/** 成功 **/
	public static final int SUCCESS = 0;
	/** 设备已被删除 **/
	public static final int NO_DEVICE = 6;
	/** 设备请求没有响应 **/
	public static final int REQUEST_TIMEOUT = 10;
	/** 设备已离线 **/
	public static final int DEVICE_OFFLINE = 11;
	/** 参数有误 **/
	public static final int PARAMS_ERROR = 5100;
	/** 内部错误 **/
	public static final int INTERNAL_ERROR = 5102;
	
	public static String valueOfString(int code) {
		return String.valueOf(code);
	}
}
