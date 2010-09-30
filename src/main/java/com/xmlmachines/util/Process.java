package com.xmlmachines.util;

import com.xmlmachines.beans.ThreadTimingBean;

public class Process implements Runnable {

	public Process() {
	}

	public void savePage() {
		System.out
				.println("TODO - any benefit in this being in a separate class?");
	}

	@Override
	public void run() {

		ThreadTimingBean tt = new ThreadTimingBean();
		tt.setName(Thread.currentThread().getName());
		tt.setStart(System.currentTimeMillis());
		savePage();
		tt.setEnd(System.currentTimeMillis());
		// sputAndReport(tt);

		// System.out.println(Thread.currentThread().getName());
	}
}
