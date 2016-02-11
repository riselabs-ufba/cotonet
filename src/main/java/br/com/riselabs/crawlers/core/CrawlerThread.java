/**
 * 
 */
package br.com.riselabs.crawlers.core;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.eclipse.jgit.api.errors.GitAPIException;

import br.com.riselabs.crawlers.ReposCrawler;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.exceptions.InvalidNumberOfTagsException;
import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.util.RCUtil;

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
	private Entry<Integer, String> systemEntry;
	private String systemName;

	public CrawlerThread(Integer threadID, String systemName,
			Entry<Integer, String> systemEntry) {
		this.threadID = threadID;
		this.systemName = systemName;
		this.systemEntry = systemEntry;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		IOHandler io = new IOHandler();
		String logLine = " System: " + systemName + "; ThreadID: " + threadID;
		File systemsLogsDir;
		try {
			systemsLogsDir = io.getDirectory(new File(RCProperties.getLogDir()));
			this.log = new File(systemsLogsDir.getCanonicalPath()
					+ File.separator +threadID+ "-" +systemName + ".log");
		} catch (IOException e1) {
			RCUtil.log(e1.toString());
			RCUtil.log(e1.getMessage());
		}

		ReposCrawler crawler = new ReposCrawler();
		crawler.setRepositoryID(systemEntry.getKey());
		crawler.setRepositoryURL(systemEntry.getValue());

		try {
			RCUtil.log("[Cloning Start]" + logLine);
			RCUtil.log(log, "[Cloning Start]" + logLine);
			crawler.cloneRepository();
			crawler.createMergeBasedTags();

			RCUtil.log(
					log,
					"Writing Tags Mapping File at: "
							+ RCProperties.getReposDir() + systemName
							+ "_TAGsMapping.txt");
			crawler.persistTagsMapping();

			RCUtil.log(log, "Writing codeface configuration file at: "
					+ RCProperties.getConfigDir() + systemName + ".conf");
			io.createCodefaceConfFiles(systemName, crawler.getTags().size());

			RCUtil.log("[Cloning Successfully]" + logLine);
			RCUtil.log(log, "[Cloning Successfully]" + logLine);
		} catch (IOException | GitAPIException | NullPointerException
				| EmptyContentException | InvalidNumberOfTagsException e) {
			RCUtil.log("[Cloning Failed]" + logLine);
			RCUtil.log(log, "[Cloning Failed]" + logLine);
			RCUtil.logStackTrace(log, e);
			e.printStackTrace();
		}
		crawler = null;
		io = null;
		System.gc();

	}

	

}
