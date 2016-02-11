/**
 * 
 */
package br.com.riselabs.crawlers;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.com.riselabs.crawlers.core.CrawlerThread;
import br.com.riselabs.crawlers.core.RCThreadPoolExecutor;
import br.com.riselabs.crawlers.db.DBManager;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.exceptions.InvalidNumberOfTagsException;
import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.util.RCUtil;

/**
 * @author Alcemir R. Santos
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws EmptyContentException
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws InvalidNumberOfTagsException
	 */
	public static void main(String[] args) throws NullPointerException,
			IOException, EmptyContentException, InvalidNumberOfTagsException {
		IOHandler io = new IOHandler();
		File reposListFile = null;

		if (args.length == 1) {
			reposListFile = new File(args[0]);
			if (!reposListFile.exists()) {
				System.out.println("ReposCrawler finished. File not found ("
						+ args[0] + ").");
				System.exit(0); // Ends execution if file not found.
			}

			RCProperties.setWorkingDir(""); // it is running in the VM
		} else {
			RCProperties.setWorkingDir(File.separator+"Documents"+File.separator+"Workspace"); // it is running
																// locally
			reposListFile = new File(RCProperties.getWorkingDir()
					+ File.separator+"repos_crawler"
					+ File.separator+"src"
					+ File.separator+"test"
					+ File.separator+"resources"
					+ File.separator+"target_systems_repo_urls.txt");
		}
		File reposCrawlerLogFile = new File(RCProperties.getLogDir() + File.separator + "repos_crawler.log");
		io.checkAndRemove(reposCrawlerLogFile);
		RCUtil.setLog(reposCrawlerLogFile);

		List<String> target_systems = new ArrayList<String>();

		Map<Integer, String> reposURLs = readRepositoryURLs("txt",
				reposListFile);

		int count = 1;
		io.makeDirectory(new File( RCProperties.getLogDir()));
		
		RCThreadPoolExecutor pool = new RCThreadPoolExecutor();
		
		// for each url in the list clones the repository
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			String system = RCUtil.getRepositorySystemName(anEntry.getValue());
			try {
				pool.runTask(new CrawlerThread(count, system, anEntry));
			} catch (Exception e) {
				RCUtil.logStackTrace(e);
			}
			count++;
			target_systems.add(system);
		}
		
		pool.shutDown();
		
		io.createCodefaceRunScript(target_systems);

		RCUtil.log("_ Done. _");
	}


	/**
	 * use "db" to recover from the database and "txt" to recover from the file.
	 * <p>
	 * Ex: <code>readRepositoryURLs("txt", reposListFile);</code> or
	 * <code>readRepositoryURLs("db", null)</code>
	 * 
	 * @return - a map from the repository id in the ghanalysis db to the url.
	 */
	private static Map<Integer, String> readRepositoryURLs(String source,
			File urlsFile) {
		Map<Integer, String> reposURLs = null;
		System.out.print("Reading URLs from: " + urlsFile.toString());

		switch (source) {
		case "txt": // It is a test. It should read from te .txt file.
			reposURLs = new HashMap<Integer, String>();
			List<String> l = new IOHandler().readFile(urlsFile.toPath());

			for (int i = 1; i <= l.size(); i++) {
				try {
					ResultSet rs = DBManager
							.executeQuery("select r.id id from repository r where r.url=\'"
									+ l.get(i - 1).replace("https:", "http:")
									+ "\'");
					if (!rs.isBeforeFirst()) {
						System.out
								.println("[Skipping "
										+ RCUtil
												.getRepositorySystemName(l
														.get(i - 1))
										+ "]: DB entry not found.");
						continue; // to avoid nullpointer in case url does not
									// exists in the database.
					}
					rs.first();
					reposURLs.put(rs.getInt("id"), l.get(i - 1));
				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}

			}

			break;
		case "db":
		default:
			reposURLs = new IOHandler().readURLsFromDatabase();
			break;
		}

		System.out.println("..........[Done].");
		return reposURLs;
	}

	
}
