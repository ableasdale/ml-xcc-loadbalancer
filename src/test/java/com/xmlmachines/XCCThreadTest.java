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

public class XCCThreadTest {

	private static long applicationStartTime;
	private static long applicationEndTime;
	private static FileWriter fw;
	private static BufferedWriter buffer;
	private static List<ThreadTimingBean> timings;
	private static AtomicInteger counter;
	private final static int THREADS = 1000;

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {

		applicationStartTime = System.currentTimeMillis();
		counter = new AtomicInteger(0);
		timings = new ArrayList<ThreadTimingBean>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

		fw = new FileWriter(new File("C:\\Users\\ableasdale\\saveapage-"
				+ formatter.format(Calendar.getInstance().getTime()) + ".csv"));
		buffer = new BufferedWriter(fw);

		System.out.println(MessageFormat.format("App Started on {0}[{1}]",
				new Date(), System.currentTimeMillis()));

		XCCThreadTest app = new XCCThreadTest();

		// run multiple threads
		ArrayList<XCCThreadTest.Process> processes = new ArrayList<XCCThreadTest.Process>();
		for (int i = 0; i < THREADS; i++) {
			processes.add(app.new Process());
		}

		for (Process c : processes) {
			new Thread(c).start();
		}
	}

	public class Process implements Runnable {

		public void savePage() {
			System.out
					.println("TODO - do something here like save a five NYT docs in a collection?");
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

	private void putAndReport(ThreadTimingBean t) {
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
			System.out
					.println("All Threads accounted for. Generating test statistics at "
							+ new Date());
			applicationEndTime = System.currentTimeMillis();
			stats();
		}

	}

	private void stats() {

		System.out.println("Number of Threads: " + THREADS);
		System.out.println("Number of MarkLogic Documents Created: "
				+ (THREADS * 5)); // num pages
		System.out.println("Start Time: " + applicationStartTime);
		List<Long> timePerThread = new ArrayList<Long>();
		for (ThreadTimingBean tt : timings) {
			timePerThread.add(tt.getEnd() - tt.getStart());
		}
		long minTime = Collections.min(timePerThread);
		System.out.println("Fastest time for a Thread: " + minTime);
		long maxTime = Collections.max(timePerThread);
		System.out.println("Longest time for a Thread: " + maxTime);
		long sum = 0;
		for (Long l : timePerThread) {
			sum += l;
		}
		System.out.println("Sum of all Thread timings: " + sum);
		System.out.println("Average time for a Thread: " + (sum / THREADS));

		// min Thread time
		// max Thread time
		// avg Thread time
		// sum of all Threads

		System.out.println("End Time: " + applicationEndTime);
		// complete time for operation

		long totalApplicationTime = (applicationEndTime - applicationStartTime);
		long totalApplicationTimeDivNumThreads = (totalApplicationTime / THREADS);
		System.out.println("Total application running time: "
				+ totalApplicationTime);
		System.out.println("Application running time / No. Threads: "
				+ totalApplicationTimeDivNumThreads);
	}

}
