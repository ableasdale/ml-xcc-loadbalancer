package com.xmlmachines.util;
import java.text.MessageFormat;

public class Consts {

	/**
	 * TODO - should this go elsewhere?.
	 * 
	 * @param e
	 *            the e
	 * @return the string
	 * @return
	 */
	public static String returnExceptionString(Exception e) {
		return MessageFormat.format("{0} caught: {1}", e.getClass().getName(),
				e);
	}
}
