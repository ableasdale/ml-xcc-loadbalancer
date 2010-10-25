package com.xmlmachines.util;

import java.text.MessageFormat;

/**
 * The Class Consts.
 * 
 * @author ableasdale
 */
public class Consts {

	/** The Constant CONFIG_FILE_PATH. */
	public static final String CONFIG_FILE_PATH = "xcc.xml";

	/**
	 * TODO - should this go elsewhere?.
	 * 
	 * @param e
	 *            the e
	 * @return the string
	 */
	public static String returnExceptionString(Exception e) {
		return MessageFormat.format("{0} caught: {1}", e.getClass().getName(),
				e);
	}
}
