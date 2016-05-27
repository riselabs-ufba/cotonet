/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alcemir R. Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Alcemir R. Santos
 *
 */
public abstract class Logger {
	
	private static File log = new File(Directories.getLogDir(),
			"cotonet.log");;
	
	/**
	 * Logs a message in the system log.
	 * @param message
	 */
	public static synchronized void log(String message) {
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
			logFile = new File(logFile.getParentFile(),logFile.getName());
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
	public static synchronized void logStackTrace(File logFile, Exception anException){
		StringBuilder sb =  new StringBuilder();
		String timestamp =  new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		sb.append(">>>>> Exception Begin: ("+timestamp+")\n");
		sb.append(getStackTrace(anException));
		sb.append("<<<<< Exception End.");
		log(logFile, sb.toString());
	}

	/**
	 * Logs a given exception stack trace in the given file.
	 * 
	 * @param anException
	 */
	public static synchronized void logStackTrace(Exception anException) {
		logStackTrace(log, anException);
	}
	
	private static String getStackTrace(Exception anException) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		anException.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}
