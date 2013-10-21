package com.xmlmachines;

import java.net.URI;
import java.text.MessageFormat;
import java.util.*;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XccLoadBalancerExample {

    private volatile List<ContentSource> activeContentSourceList;

    private static final Logger LOG = LoggerFactory
            .getLogger(XccLoadBalancerExample.class);

    /**
     * The candidate URIs for the load balancer to rotate through
     */
    private static List<String> uris = Arrays.asList(
            "xcc://admin:admin@localhost:8005",
            "xcc://admin:admin@localhost:8006",
            "xcc://admin:admin@localhost:8007");


    /**
     * Main method: get 20 connections and LOG the URI to show it working
     *
     */
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            Session s = XccLoadBalancerExample.getInstance().openSession();
            LOG.info("Index: " + i + " XCC URI: " + s.getConnectionUri().toString());
            s.close();
        }
    }

    /**
     * Constructor method. Set to private as this is a Singleton class
     */
    private XccLoadBalancerExample() {
        LOG.info(MessageFormat.format(
                "Constructor :: Creating XCC ContentSourceProvider at {0}",
                new Date()));


		/*
         * Creates a synchronizedList from the contentSourceList for Thread safe
		 * access to the List when necessary/required.
		 */
        activeContentSourceList = Collections
                .synchronizedList(new ArrayList<ContentSource>());

        try {
            init(uris);
            logActiveContentSources();
            LOG.info("** Configuration Complete : Ready **");
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Nested Singleton private static class (Adopts the <strong>Initialization
     * on Demand Holder Idiom</strong>)
     * <p/>
     * See: http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
     */
    private static class ContentSourceProviderWrapper {

        /**
         * The Constant INSTANCE.
         */
        private static final XccLoadBalancerExample INSTANCE = new XccLoadBalancerExample();
    }

    /**
     * Generates a Session Object from the ContentSource at the specified index
     * position in the List<ContentSource>.
     *
     * @param idx The index position in the List<ContentSource> which will be
     *            ContentSource providing the Session.
     * @return A MarkLogic Session Object
     * @throws Exception the connection failed exception
     */
    private Session getSessionAtIndex(int idx) throws Exception {

        if (activeContentSourceList != null
                && activeContentSourceList.size() > 0) {
            Session s = activeContentSourceList.get(idx).newSession();
            LOG.debug(MessageFormat.format("Returning a connection from: {0}",
                    s.getConnectionUri()));
            return s;
        } else {
            throw new Exception(
                    "Unable to connect to MarkLogic server using any of the listed URIs. Please check configuration.");

        }

    }

    /**
     * Initialises the MarkLogic ContentSource Objects and adds them to a List.
     *
     * @param uriList the uri list
     */
    private void init(List<String> uriList) throws Exception {

        LOG.info(MessageFormat.format(
                "Init: Creating ContentSources for {0} nodes.", uriList.size()));

        for (String s : uriList) {
            try {
                URI uri = new URI(s);
                ContentSource c = ContentSourceFactory.newContentSource(uri);
                addContentSourceToActiveList(activeContentSourceList, c);
                LOG.info(MessageFormat.format("Created XCC Connection: {0}",
                        uri.toString()));
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        if (activeContentSourceList.size() == 0) {
            throw new Exception(
                    "Unable to connect to MarkLogic server using any of the listed URIs. Please check configuration.");
        }
    }

    /**
     * Logs all Active ContentSources and adds them to a Map for Connection
     * validation management. This Map will be used to tally the number of times
     * issues are experienced with a Connection.
     * <p/>
     * In the event of a ConnectionSource producing 3 invalid connections, it
     * will be removed from the ContentSource list and re-added later (when
     * refreshActiveServerListFromConfiguration gets called).
     */
    private void logActiveContentSources() {
        LOG.debug("logActiveContentSources :: the load balancer will use the following ContentSource Objects for sessions");
        for (ContentSource c : activeContentSourceList) {
            LOG.debug(MessageFormat.format(
                    "ContentSource :: position: {0} | URI: {1}", activeContentSourceList.indexOf(c), c
                    .newSession().getConnectionUri()));
        }
    }

    /**
     * Takes a List<ContentSource> and an existing ContentSource, validates the
     * ContentSource and adds it to the serverList (usually the
     * activeServerList).
     *
     * @param cslist        A List for ContentSource Objects
     * @param contentSource The ContentSource to be added to the List
     */
    private void addContentSourceToActiveList(List<ContentSource> cslist,
                                              ContentSource contentSource)
            throws Exception {
        LOG.info(MessageFormat.format("Attempting to enlist ContentSource {0}",
                contentSource.newSession().getConnectionUri()));

        if (cslist.contains(contentSource)) {
            String message = MessageFormat
                    .format("{0} is already in the active contentSourceList, nothing to do",
                            contentSource.hashCode());
            LOG.info(message);
            throw new Exception(message);
        } else {
            LOG.info(MessageFormat.format(
                    "Adding a contentSource whose id is: {0}",
                    contentSource.hashCode()));
            cslist.add(contentSource);
        }
    }


    /**
     * Rotates the contentSourceList by 1 step and calls getSessionAtIndex(0) to
     * invoke a session from the ContentSource at the "top" of the List.
     * <p/>
     * Method is Synchronized so Accessor Threads can't get locked out.
     *
     * @return a new Session from the List
     */
    private synchronized Session rotateContentSourceListAndOpenSession() {
        Collections.rotate(activeContentSourceList, -1);
        try {
            return getSessionAtIndex(0);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    /**
     * Static getInstance method. Accessor is used to return the Singleton
     * Instance for this class.
     *
     * @return ContentSourceProvider Object
     */
    public static XccLoadBalancerExample getInstance() {
        return ContentSourceProviderWrapper.INSTANCE;
    }

    /**
     * Creates a MarkLogic XCC Session from a given List of ContentSource
     * Objects.
     *
     * @return a new MarkLogic XCC Session
     */
    public Session openSession() {
        return rotateContentSourceListAndOpenSession();
    }
}