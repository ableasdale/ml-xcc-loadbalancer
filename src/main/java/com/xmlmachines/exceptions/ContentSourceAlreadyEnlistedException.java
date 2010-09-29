package com.xmlmachines.exceptions;

public class ContentSourceAlreadyEnlistedException extends Exception {

	private static final long serialVersionUID = -2946803673513824615L;

	public ContentSourceAlreadyEnlistedException(String msg) {
		super(msg);
	}

	public ContentSourceAlreadyEnlistedException(String msg, Throwable t) {
		super(msg, t);
	}
}
