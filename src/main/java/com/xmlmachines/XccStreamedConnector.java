/**
 * A standalone example of how a large result set can be "streamed" to the
 * client
 * 
 * Used to demonstrate very fast transactions with MarkLogic - close to 300 docs
 * sorted into a List Object in less than half a second from beginning to end.
 * 
 * @author ableasdale
 * 
 */

package com.xmlmachines;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

/**
 * Created by IntelliJ IDEA. User: ableasdale Date: Sep 27, 2010 Time: 10:53:11
 * AM To change this template use File | Settings | File Templates.
 */
public class XccStreamedConnector {

	public static void main(String[] args) throws URISyntaxException,
			XccConfigException, RequestException {

		// Log LOG = LogFactory.getLog(getClass().getName());
		Log LOG = LogFactory.getLog(XccStreamedConnector.class);
		LOG.info(MessageFormat
				.format("Starting Application on {0}", new Date()));
		// LOG.info("starting");
		// LOG.("fine");
		List<String> xmlStringList = new ArrayList<String>();

		URI uri = new URI("xcc://admin:admin@localhost:8003");
		ContentSource cs = ContentSourceFactory.newContentSource(uri);

		Session s = cs.newSession();

		RequestOptions ro = s.getDefaultRequestOptions();
		// set xcc/j to streaming mode
		ro.setCacheResult(false);
		s.setDefaultRequestOptions(ro);
		Request r = s.newAdhocQuery("doc()[1 to 2]");

		long l1 = System.currentTimeMillis();
		ResultSequence rs = s.submitRequest(r);
		System.out.println("First benchmark: "
				+ (MessageFormat.format("{0}ms", System.currentTimeMillis()
						- l1)));

		long l2 = System.currentTimeMillis();
		while (rs.hasNext()) {
			xmlStringList.add(rs.next().asString());
		}
		System.out.println("Second benchmark: "
				+ (MessageFormat.format("{0}ms", System.currentTimeMillis()
						- l2)));

		s.close();
		System.out.println(MessageFormat.format("Received {0} records",
				xmlStringList.size()));
		// System.out.println(xmlStringList.get(1));
		// System.out.println(xmlStringList.get((xmlStringList.size() - 1)));

	}
}