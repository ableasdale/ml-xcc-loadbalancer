package com.xmlmachines;
import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: ableasdale Date: Sep 27, 2010 Time: 10:53:11
 * AM To change this template use File | Settings | File Templates.
 */
public class XccStreamedConnector2 {
	public static void main(String[] args) throws URISyntaxException,
            XccConfigException, RequestException {

        List<String> xmlStringList = new ArrayList<String>();

        URI uri = new URI("xcc://admin:admin@localhost:8003");
        ContentSource cs = ContentSourceFactory.newContentSource(uri);

        Session s = cs.newSession();

        RequestOptions ro = s.getDefaultRequestOptions();
        // set xcc/j to streaming mode
        ro.setCacheResult(false);
        s.setDefaultRequestOptions(ro);
        Request r = s.newAdhocQuery("doc()[1 to 50]");

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
//        System.out.println(xmlStringList.get(1));
//        System.out.println(xmlStringList.get((xmlStringList.size() - 1)));

    }
}

/**
 * A standalone example of how a large result set can be "streamed" to the
 * client
 * 
 * Used to demonstrate very fast transactions with MarkLogic - close to 300 docs
 * sorted into a List Object in less than half a second from beginning to end.
 * 
 * @author r327506
 * 
 */
