package com.shuncom.tcp.server.gateway;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import com.shuncom.util.DigestUtil;
import com.shuncom.util.ValidationException;

import io.netty.buffer.ByteBuf;

public class CodecUtil {

	private CodecUtil() {
		
	}
	
	public static JSONObject toJSON(String message) {
		try {
			return new JSONObject(message);
		} 
		catch(Exception ex) {
			throw new ValidationException("Message can not convert to JSONObject", ex);
		}
	}
	
	public static void writeFixedField(ByteBuf out, int fieldLength, int value) {
	   switch(fieldLength) {
	    case 1:
            if (value >= 256) {
                throw new IllegalArgumentException(
                        "length does not fit into a byte: " + value);
            }
            out.writeByte((byte) value);
            break;
        case 2:
            if (value >= 65536) {
                throw new IllegalArgumentException(
                        "length does not fit into a short integer: " + value);
            }
            out.writeShort((short) value);
            break;
        case 3:
            if (value >= 16777216) {
                throw new IllegalArgumentException(
                        "length does not fit into a medium integer: " + value);
            }
            out.writeMedium(value);
            break;
        case 4:
            out.writeInt(value);
            break;
        case 8:
            out.writeLong(value);
            break;
        default:
            throw new Error("should not reach here");
		 }
	}
	
	public static short crc16(byte[] data) {
		int crc = 0xffff;
		for (int i = 0; i < data.length; i++) {
			crc = ((crc >> 8 ) & 0xff) | ((crc << 8) & 0xff00);
			crc ^= (data[i] & 0xff);
			crc ^= (((byte)crc & 0xff) >> 4);
			crc ^= ((crc << 12) & 0xffff);
			crc ^= (((crc & 0xff) << 5 ) & 0xffff);
		}
		return (short)crc;
	}
	
	public static byte[] encryptKey(String salt1, String salt2, long timestamp) {
		StringBuilder digest = new StringBuilder();
		digest.append(Long.toHexString(timestamp >> 5));
		long longSalt1 = Long.parseLong(salt1, 16);
		digest.append(Integer.toHexString((int)(longSalt1 >> 32))); //high 32 bits
		digest.append(Long.toHexString(timestamp >> 3));
		digest.append(Integer.toHexString((int)longSalt1)); //low 32 bits
		digest.append(Long.toHexString(timestamp >> 1));
		digest.append(salt2);
		return DigestUtil.getSha256Digest().digest(getBytes(digest.toString()));
	}
	
	public static byte[] getBytes(String value) {
		return value.getBytes(Charset.forName("UTF-8"));
	}
	
	private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
	
	public static byte[] encryptAES(byte[] key, byte[] iv, byte[] data) throws Exception {
		 SecretKey sk = new SecretKeySpec(key, "AES");
		 IvParameterSpec ips = new IvParameterSpec(iv);
	     Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
	     cipher.init(Cipher.ENCRYPT_MODE, sk, ips);
	     return cipher.doFinal(data);
	}
	
	public static byte[] decryptAES(byte[] key, byte[] iv, byte[] data) throws Exception {
		 SecretKey sk = new SecretKeySpec(key, "AES");
		 IvParameterSpec ips = new IvParameterSpec(iv);
	     Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
	     cipher.init(Cipher.DECRYPT_MODE, sk, ips);
	     return cipher.doFinal(data);
	}
}
