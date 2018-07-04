package com.shuncom.util;

public abstract class NestedRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NestedRuntimeException(String msg) {
		super(msg);
	}

	public NestedRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	@Override
	public String getMessage() {
		return buildMessage(super.getMessage(), getCause());
	}

	public static String buildMessage(String message, Throwable cause) {
		if (cause != null) {
			StringBuilder sb = new StringBuilder();
			if (message != null) {
				sb.append(message).append("; ");
			}
			sb.append("nested exception is ").append(cause);
			return sb.toString();
		}
		else {
			return message;
		}
	}
}
