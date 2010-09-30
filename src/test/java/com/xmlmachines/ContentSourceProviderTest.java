package com.xmlmachines;

import org.junit.Assert;
import org.junit.Test;

import com.marklogic.xcc.Session;
import com.xmlmachines.providers.ContentSourceProvider;

public class ContentSourceProviderTest {

	@Test
	public void basicInitializeTest() {
		Session s = ContentSourceProvider.getInstance().openSession();
		Assert.assertTrue(s.getConnectionUri().toString().startsWith("xcc://"));
	}
}
