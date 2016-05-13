/**
 * 
 */
package br.com.riselabs.cotonet.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author alcemirsantos
 *
 */
public abstract class Logger {
	
	private static File log = new File(Directories.getLogDir(),
			"cotonet.log");;

	
	/**
	 * Logs a message in the system log.
	 * @param message
	 */
	public static void log(String message) {
		log(log, message);
	}
	
	public static File getLog() {
		return log;
	}

	/**
	 * Logs a message in a given file.
	 * @param logFile
	 * @param message
	 */
	public static void log(File logFile, String message) {
		IOHandler io = new IOHandler();
		try {
			logFile = io.createFile(logFile.getParentFile(),logFile.getName());
			io.appendLineToFile(logFile, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs a given exception stack trace in the given file.
	 * 
	 * @param logFile
	 * @param anException
	 */
	public static void logStackTrace(File logFile, Exception anException){
		log(logFile, getStackTrace(anException));
	}

	/**
	 * Logs a given exception stack trace in the given file.
	 * 
	 * @param anException
	 */
	public static void logStackTrace(Exception anException) {
		log(log, getStackTrace(anException));
	}
	
	private static String getStackTrace(Exception anException) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		anException.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}
