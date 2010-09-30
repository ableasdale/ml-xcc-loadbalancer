package com.xmlmachines;

import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.xmlmachines.util.Consts;

public class ConfigurationTest {

	private static Logger LOG = Logger.getLogger(ConfigurationTest.class);

	@Test
	public void testConfig() {
		try {
			XMLConfiguration config = new XMLConfiguration("xcc.xml");
			Object prop = config.getProperty("uris.uri");

			if (prop instanceof Collection) {
				LOG.info("Number of xcc uris: " + ((Collection) prop).size());
				Assert.assertTrue(((Collection) prop).size() > 1);
			}
		} catch (ConfigurationException e) {
			LOG.error(Consts.returnExceptionString(e));
		}
	}
}
