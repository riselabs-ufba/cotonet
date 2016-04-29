 /**
 * 
 */
package br.com.riselabs.cotonet.crawler.threads;

import java.io.File;

import br.com.riselabs.cotonet.crawler.ReposCrawler;
import br.com.riselabs.cotonet.util.Directories;
import br.com.riselabs.cotonet.util.Logger;

/**
 * This Runnable class executes three activities: </br>- it clones the
 * repository; </br>- it persists the tags mapping file; </br>- it creates the
 * <code>codeface</code> configuration file.
 * 
 * @author alcemirsantos
 *
 */
public class CrawlerThread implements Runnable {

	private File log;
	private Integer threadID;
	private String systemURL;
	
	private boolean skipCloning;

	public CrawlerThread(Integer threadID, String systemURL, boolean skip) {
		this.threadID = threadID;
		this.systemURL = systemURL;
		this.skipCloning = skip;
		this.log = new File(Directories.getLogDir(), "thread" +threadID + ".log");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String logLine = "ThreadID: " + threadID.toString();
		ReposCrawler crawler = null;
		try {
			Logger.log("[Cloning Start]" + logLine);
			Logger.log(log, "[Cloning Start]" + logLine);
			// create crawler
			crawler = new ReposCrawler(systemURL, skipCloning);
			crawler.run();
			Logger.log("[Cloning Finished]" + logLine);
			Logger.log(log, "[Cloning Finished]" + logLine);
		} catch (Exception  e) {
			Logger.log("[Cloning Failed]" + logLine);
			Logger.log(log, "[Cloning Failed]" + logLine);
			Logger.logStackTrace(log, e);
		}
		crawler = null;
		System.gc();
	}

}
