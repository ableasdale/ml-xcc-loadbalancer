package com.xmlmachines.providers;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.xmlmachines.exceptions.ConnectionFailedException;
import com.xmlmachines.exceptions.ContentSourceAlreadyEnlistedException;
import com.xmlmachines.util.Consts;

/**
 * The ContentSourceProvider is a Singleton Class - use getInstance() to access
 * 
 * @author ableasdale
 */
public class ContentSourceProvider {

	private volatile List<ContentSource> activeContentSourceList;
	private volatile AtomicInteger connectionCount;
	private final List<ContentSource> inactiveContentSourceList;
	private final Map<URI, Integer> connectionFailureMap;

	/*
	 * Default initial values
	 */
	private final int connectionsBeforeDelistCheck = 100000;

	private boolean ready = false;
	private static final Logger LOG = Logger
			.getLogger(ContentSourceProvider.class);

	/**
	 * Constructor method. Set to private as this is a Singleton class
	 */
	private ContentSourceProvider() {
		LOG.info(MessageFormat.format(
				"Constructor :: Creating ContentSourceProvider at {0}",
				new Date()));
		connectionCount = new AtomicInteger(0);

		/*
		 * Creates a synchronizedList from the contentSourceList for Thread safe
		 * access to the List when necessary/required.
		 */
		activeContentSourceList = Collections
				.synchronizedList(new ArrayList<ContentSource>());
		// Create the Objects for managing connection failures
		inactiveContentSourceList = Collections
				.synchronizedList(new ArrayList<ContentSource>());
		connectionFailureMap = new HashMap<URI, Integer>();
		LOG.debug("Populating ContentSourceProviders from Configuration.");

		/*
		 * TODO - add content source policy later?? or delete try { String
		 * configPolicy = checkAndExtractStringFromConfigFile(cfg,
		 * CONTENT_SOURCE_POLICY); if (configPolicy != null) { policy =
		 * configPolicy; } } catch (ConfigurationException ce) {
		 * _logger.warn(MessageFormat .format(
		 * "No Scheduling policy specified in config file.  Defaulting to {0}",
		 * policy)); }
		 */

		try {
			XMLConfiguration config = new XMLConfiguration("xcc.xml");
			init(Arrays.asList(config.getStringArray("uris.uri")));
			mapActiveContentSources();
			// TODO - can I get rid of this ready flag??
			ready = true;
		} catch (ConnectionFailedException e) {
			LOG.error(
					"Required Parameters are null when initializing, unable to continue",
					e);
		} catch (ConfigurationException e) {
			LOG.error(Consts.returnExceptionString(e));
		}
	}

	/**
	 * Nested Singleton private static class (Adopts the <strong>Initialization
	 * on Demand Holder Idiom</strong>)
	 * 
	 * See: http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
	 */
	private static class ContentSourceProviderWrapper {
		private static final ContentSourceProvider INSTANCE = new ContentSourceProvider();
	}

	/**
	 * Generates a Session Object from the ContentSource at the specified index
	 * position in the List<ContentSource>
	 * 
	 * @param idx
	 *            The index position in the List<ContentSource> which will be
	 *            ContentSource providing the Session.
	 * 
	 * @return A MarkLogic Session Object
	 * @throws ConnectionFailedException
	 */
	private Session getSessionAtIndex(int idx) throws ConnectionFailedException {

		if (activeContentSourceList != null
				&& activeContentSourceList.size() > 0) {
			Session s = activeContentSourceList.get(idx).newSession();
			LOG.debug(MessageFormat.format("Returning a connection from: {0}",
					s.getConnectionUri()));
			if (validateSession(s)) {
				return s;
			}
			// returns back to the calling method in the case of an invalid
			// Session.
			return openSession();
		} else {
			throw new ConnectionFailedException(
					"Unable to connect to MarkLogic server using any of the listed URIs. Please check configuration.");

		}

	}

	/**
	 * Initialises the MarkLogic ContentSource Objects and adds them to a List
	 * 
	 * @param server
	 *            The array of Server URIs
	 * @param userName
	 *            The userName for database authentication
	 * @param password
	 *            The password for database authentication
	 * @throws ConfigurationException
	 * @throws ConnectionFailedException
	 */
	private void init(List<String> uriList) throws ConfigurationException,
			ConnectionFailedException {

		LOG.info(MessageFormat.format(
				"Init: Creating ContentSources for {0} nodes.", uriList.size()));

		for (String s : uriList) {
			try {
				URI uri = new URI(s);
				ContentSource c = ContentSourceFactory.newContentSource(uri);
				addContentSourceToActiveList(activeContentSourceList, c);
				LOG.info(MessageFormat.format("Created XCC Connection: {0}",
						uri.toString()));
			} catch (URISyntaxException e) {
				LOG.error(Consts.returnExceptionString(e));
			} catch (XccConfigException e) {
				LOG.error(Consts.returnExceptionString(e));
			} catch (ContentSourceAlreadyEnlistedException e) {
				LOG.error(Consts.returnExceptionString(e));
			}
		}
		LOG.info(MessageFormat
				.format("ContentSourceProvider ready.  Delist check set to run every {0} connection requests.",
						connectionsBeforeDelistCheck));

		if (activeContentSourceList.size() == 0) {
			throw new ConnectionFailedException(
					"Unable to connect to MarkLogic server using any of the listed URIs. Please check configuration.");
		}
	}

	/**
	 * Logs all Active ContentSources and adds them to a Map for Connection
	 * validation management. This Map will be used to tally the number of times
	 * issues are experienced with a Connection.
	 * 
	 * In the event of a ConnectionSource producing 3 invalid connections, it
	 * will be removed from the ContentSource list and re-added later (when
	 * refreshActiveServerListFromConfiguration gets called).
	 */
	private void mapActiveContentSources() {
		for (ContentSource c : activeContentSourceList) {
			// initialize ContentSource 'strike' map
			connectionFailureMap.put(c.newSession().getConnectionUri(), 0);
			LOG.debug(MessageFormat.format(
					"id: {0} | position: {1} | URI: {2} | String: {3}", c
							.hashCode(), activeContentSourceList.indexOf(c), c
							.newSession().getConnectionUri(), c.toString()));
		}
	}

	/**
	 * Takes a List<ContentSource> and an existing ContentSource, validates the
	 * ContentSource and adds it to the serverList (usually the
	 * activeServerList).
	 * 
	 * @param cslist
	 *            A List for ContentSource Objects
	 * @param contentSource
	 *            The ContentSource to be added to the List
	 * @throws ContentSourceAlreadyEnlistedException
	 */
	private void addContentSourceToActiveList(List<ContentSource> cslist,
			ContentSource contentSource)
			throws ContentSourceAlreadyEnlistedException {
		LOG.info(MessageFormat.format("Attempting to enlist ContentSource {0}",
				contentSource.newSession().getConnectionUri()));

		if (validateConnection(contentSource)) {
			if (cslist.contains(contentSource)) {
				String message = MessageFormat
						.format("{0} is already in the active contentSourceList, nothing to do",
								contentSource.hashCode());
				LOG.info(message);
				throw new ContentSourceAlreadyEnlistedException(message);
			} else {
				LOG.info(MessageFormat.format(
						"Adding a contentSource whose id is: {0}",
						contentSource.hashCode()));
				cslist.add(contentSource);
			}
		} else {
			moveContentSourceToInactiveList(contentSource);
			// TODO handle the invalid here - exception maybe?
			LOG.error("Unable to enlist ContentSource: an error has occurred.  Please check your settings and configuration parameters.");
		}
	}

	/**
	 * Takes the suspicious ContentSource out of the List<ContentSource>
	 * contentSourceList and places it in the List<ContentSource>
	 * delistedContentSourceList
	 * 
	 * @param contentSource
	 *            The ContentSource Object for delisting.
	 */
	private void moveContentSourceToInactiveList(ContentSource contentSource) {
		URI u = contentSource.newSession().getConnectionUri();
		LOG.info(MessageFormat.format("Delisting ContentSouce: {0}",
				u.toString()));

		if (inactiveContentSourceList.contains(contentSource) == false) {
			inactiveContentSourceList.add(contentSource);
			LOG.info(MessageFormat.format(
					"Delisted ContentSource list now contains {0} item(s).",
					inactiveContentSourceList.size()));
		}

		activeContentSourceList.remove(contentSource);
		// reset the Map for that URI; when it is re-enlisted it will have 0
		// strikes against it
		connectionFailureMap.put(u, 0);
	}

	/**
	 * Periodically checks the delistedContentSourceList (every 100,000
	 * connection attempts by default). If any servers have been delisted in
	 * that time, it attempts to re-establish a connection with them. If it can
	 * connect successfully, it re-enlists the ContentSource to allow it to be
	 * used again by the API.
	 * 
	 */
	private void refreshActiveServerListFromConfiguration() {
		// reset connectionCounter
		connectionCount.set(0);

		// are any ContentSources delisted at this time? If so:
		if (inactiveContentSourceList.size() > 0) {
			LOG.info(MessageFormat
					.format("Rechecking and updating all currently delisted servers at {0}",
							new Date()));
			LOG.info(MessageFormat.format(
					"Currently there are {0} delisted ConnectionSources.",
					inactiveContentSourceList.size()));

			for (int i = 0; i < inactiveContentSourceList.size(); i++) {
				ContentSource c = inactiveContentSourceList.get(i);
				LOG.info(MessageFormat.format(
						"rechecking Server: {0} with properties: {1}",
						c.hashCode(), c.toString()));
				if (validateConnection(c)) {
					try {
						addContentSourceToActiveList(activeContentSourceList, c);
					} catch (ContentSourceAlreadyEnlistedException e) {
						LOG.info(MessageFormat
								.format("ContentSource is already in the list.  No further action required {0}",
										c.toString()));
					}

					if (activeContentSourceList.contains(c)) {
						inactiveContentSourceList.remove(c);
					}
				} else {
					LOG.warn("Still unable to enlist contentSource as no valid session could be created.  No further action can be taken right now.");
				}
			}
		} else {
			LOG.info("No ConnectionSources have been delisted.  No further action is required at this time.");
		}

	}

	/**
	 * Takes a Session Object and performs a PointInTime query using the session
	 * to confirm whether it is active (and ready for use). If the session is
	 * unable to provide a server point-in-time, a strike is recorded in the
	 * connectionFailureMap
	 * 
	 * @param session
	 *            The Session for validation
	 * 
	 * @return true or false
	 */
	private boolean validateSession(Session session) {
		try {
			session.getCurrentServerPointInTime();
			return true;
		} catch (RequestException e) {
			LOG.error(MessageFormat.format(
					"Error validating current session {0}", e));
			if (ready) {
				strike(session);
			}

		}
		return false;
	}

	/**
	 * Takes a ContentSource and ensures a valid Session can be created from it.
	 * Returns false if the Session doesn't enable the creation of a server
	 * timestamp
	 * 
	 * @param contentSource
	 *            The ContentSource for validation
	 * @return true or false
	 */
	private boolean validateConnection(ContentSource contentSource) {
		Session ses = contentSource.newSession();
		if (validateSession(ses)) {
			LOG.info(MessageFormat
					.format("Connection [{0}] has the URI of {1} and is valid - the Session hashCode is: {2}",
							contentSource.hashCode(), ses.getConnectionUri(),
							ses.hashCode()));
			return true;
		} else {
			LOG.warn("Unable to create an active Session from the ContentSource.");
		}
		ses.close();
		return false;
	}

	/**
	 * "Strikes" the ContentSource (for the errant Session) as having created 3
	 * unusable sessions. If the ContentSource is unable to create a usable
	 * Session 3 times in a row, the ContentSource will be delisted by the
	 * provider.
	 * 
	 * (According to the "3 strike rule").
	 * 
	 * @param s
	 *            The Session that caused connectivity issues.
	 */
	private void strike(Session s) {
		LOG.warn(MessageFormat.format(
				"Error connecting to {0} adding to strikelist",
				s.getConnectionUri()));

		int numStrikes = connectionFailureMap.get(s.getConnectionUri()) + 1;
		if (numStrikes >= 3) {
			LOG.info("Server has not responded for 3 attempts; delisting server");
			moveContentSourceToInactiveList(s.getContentSource());
		} else {
			connectionFailureMap.put(s.getConnectionUri(), numStrikes);
		}
	}

	/**
	 * Rotates the contentSourceList by 1 step and calls getSessionAtIndex(0) to
	 * invoke a session from the ContentSource at the "top" of the List.
	 * 
	 * Method is Synchronized so Accessor Threads can't get locked out.
	 * 
	 * @return a new Session from the List
	 */
	private synchronized Session rotateContentSourceListAndOpenSession() {
		Collections.rotate(activeContentSourceList, -1);
		try {
			return getSessionAtIndex(0);
		} catch (ConnectionFailedException e) {
			LOG.error(Consts.returnExceptionString(e));
		}
		return null;
	}

	/**
	 * Static getInstance method. Accessor is used to return the Singleton
	 * Instance for this class.
	 * 
	 * @return ContentSourceProvider Object
	 */
	public static ContentSourceProvider getInstance() {
		return ContentSourceProviderWrapper.INSTANCE;
	}

	/**
	 * Creates a MarkLogic XCC Session from a given List of ContentSource
	 * Objects
	 * 
	 * @return a new MarkLogic XCC Session
	 */
	public Session openSession() {

		if (connectionCount.incrementAndGet() >= connectionsBeforeDelistCheck) {
			LOG.info(MessageFormat
					.format("Reached {0} connections.  Running check against delisted content.",
							connectionsBeforeDelistCheck));
			/*
			 * This will be quick; it's looping through a small list (the
			 * delistedContentSourceList), it should add minimal overhead when
			 * it gets called (which is every time the connectionCount hits the
			 * specified CONNECTIONS_BEFORE_DELIST_CHECK). If the method gets
			 * more complicated in time, it may be worth creating a Thread to do
			 * this work.
			 */
			refreshActiveServerListFromConfiguration();
		}

		return rotateContentSourceListAndOpenSession();
	}
}
