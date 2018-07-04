package com.shuncom.tcp.server.gateway;

import java.util.Arrays;

public class Packet {

	private short magic; //特殊标识，目前固定使用0xAA55
	private short payloadLen;  //payload长度
	private int version;  //版本
	private int enctype;  //数据包加密类型
	private int type;  //数据包类型
	private int reserved;  //保留字段
	private short crc;  //payload crc16校验值
	private byte[] payload;  //payload
	private Object convert;  //payload格式化处理后的内容
	/**
	 * @return the magic
	 */
	public short getMagic() {
		return magic;
	}
	/**
	 * @return the payloadLen
	 */
	public short getPayloadLen() {
		return payloadLen;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
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
	 * @return the reserved
	 */
	public int getReserved() {
		return reserved;
	}
	/**
	 * @return the crc
	 */
	public short getCrc() {
		return crc;
	}
	/**
	 * @return the payload
	 */
	public byte[] getPayload() {
		return payload.clone();
	}
	/**
	 * @param magic the magic to set
	 */
	public void setMagic(short magic) {
		this.magic = magic;
	}
	/**
	 * @param payloadLen the payloadLen to set
	 */
	public void setPayloadLen(short payloadLen) {
		this.payloadLen = payloadLen;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	/**
	 * @param enctype the enctype to set
	 */
	public void setEnctype(int enctype) {
		this.enctype = enctype;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @param reserved the reserved to set
	 */
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	/**
	 * @param crc the crc to set
	 */
	public void setCrc(short crc) {
		this.crc = crc;
	}
	/**
	 * @param payload the payload to set
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload.clone();
	}
	/**
	 * @return the convert
	 */
	public Object getConvert() {
		return convert;
	}
	/**
	 * @param convert the convert to set
	 */
	public void setConvert(Object convert) {
		this.convert = convert;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("(magic: ").append(magic)
			.append(", payloadLen: ").append(payloadLen)
			.append(", version: ").append(version)
			.append(", enctype:").append(enctype)
			.append(", type:").append(type)
			.append(", reserved:").append(reserved)
			.append(", crc:").append(crc)
			.append(", payload:").append(Arrays.toString(payload))
			.append(")").toString();
	}
}
