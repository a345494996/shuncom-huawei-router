package com.shuncom.util;

public class ValidationException extends NestedRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ValidationException(String msg) {
	    super(msg);
	}
	  
	public ValidationException(String msg, Throwable throwable) {
	    super(msg, throwable);
	}
}
