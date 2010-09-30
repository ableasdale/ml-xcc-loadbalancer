package com.xmlmachines;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.xmlmachines.beans.ThreadTimingBean;
import com.xmlmachines.util.Consts;
import com.xmlmachines.util.StatisticsManager;

public class XCCThreadTest {

	private static long applicationStartTime;
	private static long applicationEndTime;
	private static StatisticsManager mgr;
	private static List<ThreadTimingBean> timings;
	private static AtomicInteger counter;
	private final static int THREADS = 1000;

	private static final Logger LOG = Logger.getLogger(XCCThreadTest.class);

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {

		Logger LOG = Logger.getLogger(XCCThreadTest.class);

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

	public class Process implements Runnable {

		public void savePage() {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LOG.error(Consts.returnExceptionString(e));
			}
			// TODO - do something here like save a five NYT docs in a
			// collection?
		}

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
