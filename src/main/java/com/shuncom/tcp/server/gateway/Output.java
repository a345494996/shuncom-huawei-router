package com.shuncom.tcp.server.gateway;

public class Output {
	
	private final int enctype;
	private final int type;
	private final byte[] payload;
	
	public Output(int enctype, int type, byte[] payload) {
		this.enctype = enctype;
		this.type = type;
		this.payload = payload.clone();
	}
    public Output(int enctype, byte[] payload) {
		this(enctype, 1, payload);
	}
    public Output(byte[] payload) {
	    this(1, payload);
    }
	/**
	 * @return the enctype
	 */
	public int getEnctype() {
		return enctype;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @return the payload
	 */
	public byte[] getPayload() {
		return payload.clone();
	}
	
}
