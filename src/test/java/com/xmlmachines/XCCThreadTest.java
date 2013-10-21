package com.xmlmachines;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import com.xmlmachines.beans.ThreadTimingBean;
import com.xmlmachines.util.StatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XCCThreadTest {

	/** The application start time. */
	private static long applicationStartTime;

	/** The application end time. */
	private static long applicationEndTime;

	/** The mgr. */
	private static StatisticsManager mgr;

	/** The timings. */
	private static List<ThreadTimingBean> timings;

	/** The counter. */
	private static AtomicInteger counter;

	/** The Constant THREADS. */
	private final static int THREADS = 1000;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(XCCThreadTest.class);

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {

		Logger LOG = LoggerFactory.getLogger(XCCThreadTest.class);

		applicationStartTime = System.currentTimeMillis();
		counter = new AtomicInteger(0);
		timings = new ArrayList<ThreadTimingBean>();
		mgr = new StatisticsManager("xccThreadTest");

		LOG.info(MessageFormat.format("App Started on {0}[{1}]", new Date(),
				System.currentTimeMillis()));

		XCCThreadTest app = new XCCThreadTest();

		for (int i = 0; i < THREADS; i++) {
			Process p = app.new Process();
			new Thread(p).start();
		}

	}

	/**
	 * The Class Process.
	 */
	public class Process implements Runnable {

		/**
		 * Save page.
		 */
		public void savePage() {
			/*
			 * try { Thread.sleep(10); } catch (InterruptedException e) {
			 * LOG.error(Consts.returnExceptionString(e)); }
			 */
			LOG.info("TODO - action here");
			// TODO - do something here like save a five NYT docs in a
			// collection?
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			ThreadTimingBean tt = new ThreadTimingBean();
			tt.setName(Thread.currentThread().getName());
			tt.setStart(System.currentTimeMillis());
			savePage();
			tt.setEnd(System.currentTimeMillis());
			putAndReport(tt);

			// System.out.println(Thread.currentThread().getName());
		}
	}

	/**
	 * Put and report.
	 * 
	 * @param t
	 *            the t
	 */
	private synchronized void putAndReport(ThreadTimingBean t) {
		t.setAtomicId(counter.incrementAndGet());
		timings.add(t);
		mgr.updateCsv(t);

		if (counter.get() == THREADS) {
			applicationEndTime = System.currentTimeMillis();
			LOG.info("All Threads accounted for. Generating test statistics...\n"
					+ mgr.generateStats(THREADS, applicationStartTime,
							applicationEndTime, timings));
		}

	}

}
