package com.xmlmachines.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.xmlmachines.beans.ThreadTimingBean;

public class StatisticsManager {
	private FileWriter fw;
	private final BufferedWriter buffer;
	private final Logger LOG;

	public StatisticsManager(String testName) {
		LOG = Logger.getLogger(getClass().getName());

		String fileNamePrefix = System.getProperty("user.home") + "\\"
				+ testName + "-";

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		try {
			fw = new FileWriter(new File(fileNamePrefix
					+ formatter.format(Calendar.getInstance().getTime())
					+ ".csv"));
		} catch (IOException e) {
			LOG.error(Consts.returnExceptionString(e));
		}
		buffer = new BufferedWriter(fw);

	}

	public synchronized void updateCsv(ThreadTimingBean t) {
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
			LOG.error(Consts.returnExceptionString(e));
		}
	}

	public String generateStats(int THREADS, long applicationStartTime,
			long applicationEndTime, List<ThreadTimingBean> timings) {
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
