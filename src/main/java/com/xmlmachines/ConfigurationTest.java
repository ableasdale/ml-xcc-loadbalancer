package com.xmlmachines;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder.ConfigurationProvider;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class ConfigurationTest {

	public static void main(String[] args) {

		Logger LOG = Logger.getLogger(ConfigurationProvider.class);

		try {
			XMLConfiguration config = new XMLConfiguration("xcc.xml");
			System.out.println(config.getString("test.uri"));
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("Loaded Config xml Document");

	}

}
