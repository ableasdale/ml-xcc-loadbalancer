package com.xmlmachines;

public class Process implements Runnable {

	public Process() {
	}

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
		// sputAndReport(tt);

		// System.out.println(Thread.currentThread().getName());
	}
}
