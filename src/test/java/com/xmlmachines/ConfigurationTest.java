package com.xmlmachines;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import org.junit.Test;

import com.xmlmachines.util.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertTrue;

public class ConfigurationTest {

	private static Logger LOG = LoggerFactory.getLogger(ConfigurationTest.class);

	@Test
	public void testConfig() {
		try {
			XMLConfiguration config = new XMLConfiguration(
					Consts.CONFIG_FILE_PATH);
			List<String> l = Arrays.asList(config.getStringArray("uris.uri"));
			LOG.info(MessageFormat.format("Number of xcc uris: {0}", l.size()));

			assertTrue("There should be at least one URI in the list",
                    l.size() > 1);
			assertTrue("An XCC URI should start with xcc://", l.get(0)
                    .startsWith("xcc://"));
			assertTrue("An XCC URI should contain an @ statement", l
                    .get(0).contains("@"));

		} catch (ConfigurationException e) {
			LOG.error(Consts.returnExceptionString(e));
		}
	}
}
