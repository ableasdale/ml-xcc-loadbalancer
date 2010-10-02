package com.xmlmachines.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Splits a text file containing lots of XML Documents without any linebreaks.
 * Requires two folders in the User's home directory:
 * <strong>inputfolder</strong> and <strong>outputfolder</strong>
 * 
 * @author ableasdale
 */
public class XmlCutter {

	private static final String OUTPUT_FILE_NAME = "\\file";
	private static String USER_HOME = System.getProperty("user.home");
	private static final String INPUT_FOLDER = USER_HOME + "\\inputfolder";
	private static String OUTPUT_FOLDER = USER_HOME + "\\outputfolder";
	private static Logger LOG = Logger.getLogger("XmlCutter");
	private static long COUNTER = 0;

	public static void main(String[] args) {
		Handler h = new ConsoleHandler();
		h.setLevel(Level.FINE);
		LOG.addHandler(h);
		LOG.setLevel(Level.FINE);

		File inputDir = new File(INPUT_FOLDER);
		File[] filesInInputDir = inputDir.listFiles();
		for (File f : filesInInputDir) {
			if ((f.getName()).endsWith(".txt")) {
				LOG.fine((MessageFormat.format(
						"Found a text file {0}. Processing docs...",
						f.getName())));
				processFile(f);
			}
		}
	}

	private static void processFile(File f) {
		StringBuilder out = new StringBuilder();
		char prev = '#';
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "UTF8"));
			char[] buf = new char[1];
			while (br.read(buf) >= 0) {
				out.append(buf[0]);
				if (prev == '<' && buf[0] == '?') {
					LOG.finest((MessageFormat.format(
							"Start of XML PI Found: {0}{1}", prev, buf[0])));
					if (out.length() > 2) {
						flushToFile(out.substring(0, out.length() - 2));
					}
					out.setLength(2);
				}
				prev = buf[0];
			}
			LOG.finest("Writing final file");
			flushToFile(out.toString());
			br.close();
		} catch (IOException e) {
			LOG.fine(e.getMessage());
		}
		LOG.fine(MessageFormat.format("Generated {0} XML Documents", COUNTER));
	}

	private static void flushToFile(String s) {
		File f = new File(OUTPUT_FOLDER + OUTPUT_FILE_NAME + (++COUNTER)
				+ ".xml");
		LOG.finest(MessageFormat.format("Writing file: {0}", f.getName()));
		try {
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
			osw.write(s);
			osw.flush();
		} catch (IOException e) {
			LOG.fine(e.getMessage());
		}
	}
}