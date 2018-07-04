package com.shuncom.tcp.server;

public class ClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClientException(String msg) {
		super(msg);
	}
	
	public ClientException(String msg, Throwable throwable) {
		super(msg, throwable);
	}
}
