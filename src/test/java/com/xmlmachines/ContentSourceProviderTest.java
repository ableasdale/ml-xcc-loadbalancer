package com.xmlmachines;

import org.junit.Test;

import com.marklogic.xcc.Session;
import com.xmlmachines.providers.ContentSourceProvider;

import static junit.framework.Assert.assertTrue;

public class ContentSourceProviderTest {

	@Test
	public void basicInitializeTest() {
		Session s = ContentSourceProvider.getInstance().openSession();
		assertTrue(s.getConnectionUri().toString().startsWith("xcc://"));
	}
}
