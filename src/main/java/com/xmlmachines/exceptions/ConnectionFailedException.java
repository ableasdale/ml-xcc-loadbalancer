package com.xmlmachines.exceptions;

public class ConnectionFailedException extends Exception {

	private static final long serialVersionUID = 9026937340951798233L;

	public ConnectionFailedException(String msg) {
		super(msg);
	}

	public ConnectionFailedException(String msg, Throwable t) {
		super(msg, t);
	}
}
