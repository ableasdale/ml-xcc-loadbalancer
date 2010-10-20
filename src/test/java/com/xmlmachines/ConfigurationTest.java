package com.xmlmachines;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

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
			XMLConfiguration config = new XMLConfiguration(
					Consts.CONFIG_FILE_PATH);
			List<String> l = Arrays.asList(config.getStringArray("uris.uri"));
			LOG.info(MessageFormat.format("Number of xcc uris: {0}", l.size()));

			Assert.assertTrue("There should be at least one URI in the list",
					l.size() > 1);
			Assert.assertTrue("An XCC URI should start with xcc://", l.get(0)
					.startsWith("xcc://"));
			Assert.assertTrue("An XCC URI should contain an @ statement", l
					.get(0).contains("@"));

		} catch (ConfigurationException e) {
			LOG.error(Consts.returnExceptionString(e));
		}
	}
}
