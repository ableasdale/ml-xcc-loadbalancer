package com.xmlmachines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class XCCThreadTest {

	private static long applicationStartTime;
	private static long applicationEndTime;
	private static FileWriter fw;
	private static BufferedWriter buffer;
	private static List<ThreadTimingBean> timings;
	private static AtomicInteger counter;
	private final static int THREADS = 1000;

	// String path = System.getProperty(userHome);
	private final static String fileNamePrefix = System
			.getProperty("user.home") + "\\threadReport-";

	private static final Logger LOG = Logger.getLogger(XCCThreadTest.class);

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {

		Logger LOG = Logger.getLogger(XCCThreadTest.class);
		LOG.info("home dir: " + System.getProperty("user.home"));
		applicationStartTime = System.currentTimeMillis();
		counter = new AtomicInteger(0);
		timings = new ArrayList<ThreadTimingBean>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

		fw = new FileWriter(new File(fileNamePrefix
				+ formatter.format(Calendar.getInstance().getTime()) + ".csv"));
		buffer = new BufferedWriter(fw);

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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		StringBuilder sb = new StringBuilder();
		sb.append(t.getAtomicId()).append(",");
		sb.append(t.getName()).append(",");
		sb.append(t.getStart()).append(",");
		sb.append(t.getEnd()).append("\n");
		// sb.append((endTime - startTime) / 1000000L).append("\n");
		// sb.append(endTime - startTime).append("\n");
		try {
			buffer.write(sb.toString());
			buffer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (counter.get() == THREADS) {
			applicationEndTime = System.currentTimeMillis();
			LOG.info("All Threads accounted for. Generating test statistics...\n"
					+ stats());
		}

	}

	private String stats() {
		StringBuilder sb = new StringBuilder();
		appendStats(sb, "Report Generated", new Date().toString());
		appendStats(sb, "Number of Threads", THREADS);
		// TODO - sort this properly (num pages)
		appendStats(sb, "Number of MarkLogic Documents Created", (THREADS * 5));
		appendStats(sb, "Start Time", applicationStartTime);
		List<Long> timePerThread = new ArrayList<Long>();
		for (ThreadTimingBean tt : timings) {
			timePerThread.add(tt.getEnd() - tt.getStart());
		}
		appendStats(sb, "Fastest time for a Thread",
				Collections.min(timePerThread));
		appendStats(sb, "Longest time for a Thread",
				Collections.max(timePerThread));
		long sum = 0;
		for (Long l : timePerThread) {
			sum += l;
		}
		appendStats(sb, "Sum of all Thread timings", sum);
		appendStats(sb, "Average time for a Thread", (sum / THREADS));
		appendStats(sb, "End Time", applicationEndTime);
		// complete time for operation
		long totalApplicationTime = (applicationEndTime - applicationStartTime);
		appendStats(sb, "Total application running time", totalApplicationTime);
		appendStats(sb, "Application running time / No. Threads",
				(totalApplicationTime / THREADS));
		return sb.toString();
	}

	private void appendStats(StringBuilder sb, String statDescription,
			String stat) {
		sb.append(statDescription).append(": ").append(stat).append("\n");
	}

	private void appendStats(StringBuilder sb, String statDescription, long stat) {
		appendStats(sb, statDescription, String.valueOf(stat));
	}
}
