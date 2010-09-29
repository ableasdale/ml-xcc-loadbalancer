package com.xmlmachines.example;

import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.xmlmachines.ContentSourceProvider;

/**
 * A standalone example of how a large result set can be "streamed" to the
 * client
 * 
 * @author ableasdale
 * 
 */
public class XccStreamedSession {

	public static void main(String[] args) throws URISyntaxException,
			XccConfigException, RequestException {

		Log LOG = LogFactory.getLog(XccStreamedSession.class);
		LOG.info(MessageFormat
				.format("Starting Application on {0}", new Date()));

		List<String> xmlStringList = new ArrayList<String>();
		Session s = ContentSourceProvider.getInstance().openSession();
		// set RequestOptions to put xcc/j into streaming mode
		RequestOptions ro = s.getDefaultRequestOptions();
		ro.setCacheResult(false);
		s.setDefaultRequestOptions(ro);
		Request r = s.newAdhocQuery("doc()[1 to 300]");

		long l1 = System.currentTimeMillis();
		ResultSequence rs = s.submitRequest(r);
		LOG.info("First benchmark: "
				+ (MessageFormat.format("{0}ms", System.currentTimeMillis()
						- l1)));

		long l2 = System.currentTimeMillis();
		while (rs.hasNext()) {
			xmlStringList.add(rs.next().asString());
		}
		LOG.info("Second benchmark: "
				+ (MessageFormat.format("{0}ms", System.currentTimeMillis()
						- l2)));

		s.close();
		LOG.info(MessageFormat.format("Received {0} records",
				xmlStringList.size()));
		// System.out.println(xmlStringList.get(1));
		// System.out.println(xmlStringList.get((xmlStringList.size() - 1)));

	}
}