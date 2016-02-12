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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.crawlers.core.ReposCrawler;
import br.com.riselabs.crawlers.core.threads.CrawlerThread;
import br.com.riselabs.crawlers.core.threads.RCThreadPoolExecutor;
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

		if (args.length == 0) {// it is running locally
			RCProperties.setWorkingDir(File.separator + "Documents"
					+ File.separator + "Workspace");
			reposListFile = new File(RCProperties.getWorkingDir()
					+ File.separator + "repos_crawler" + File.separator + "src"
					+ File.separator + "test" + File.separator + "resources"
					+ File.separator + "target_systems_repo_urls.txt");

		} else if (args.length == 1) {// it is running in the VM
			reposListFile = new File(args[0]);
			if (!reposListFile.exists()) {
				System.out.println("ReposCrawler finished. File not found ("
						+ args[0] + ").");
				System.exit(0); // Ends execution if file not found.
			}

			RCProperties.setWorkingDir(""); // it is running in the VM
		} else if (args.length == 2) {// it may be running to rewrite
			if (args[0].equals("-rw")) {
				reposListFile = new File(args[1]);
			} else if (args[1].equals("-rw")) {
				reposListFile = new File(args[0]);
			} else {
				System.out
						.println("ReposCrawler finished. Illegal parameters. "
								+ "At least one the the two parameters must be \"-rw\".");
				System.exit(0);
			}
			
			if(System.getProperty("os.name").equals("Mac OS X")){
				RCProperties.setWorkingDir(File.separator + "Documents"
					+ File.separator + "Workspace");
				System.out.println(System.getProperty("os.name"));
			}else{ 
				RCProperties.setWorkingDir("");
			}
			File tmpLogFile = new File(RCProperties.getLogDir()
					+ File.separator + "repos_crawler.log");
			RCUtil.setLog(tmpLogFile);
			rewriteAuxFiles(reposListFile);
			System.out.println("ReposCrawler finished. Files rewritten.");
			System.exit(0);
		} else {
			System.out
					.println("ReposCrawler finished. Illegal amount of parameters.");
			System.exit(0);
		}

		File reposCrawlerLogFile = new File(RCProperties.getLogDir()
				+ File.separator + "repos_crawler.log");
		io.checkAndRemove(reposCrawlerLogFile);
		RCUtil.setLog(reposCrawlerLogFile);

		List<String> target_systems = new ArrayList<String>();

		Map<Integer, String> reposURLs = readRepositoryURLs("txt",
				reposListFile);

		int count = 1;
		io.makeDirectory(new File(RCProperties.getLogDir()));

		RCThreadPoolExecutor pool = new RCThreadPoolExecutor();

		// for each url in the list clones the repository
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			String system = RCUtil.getRepositorySystemName(anEntry.getValue());
			try {
				pool.runTask(new CrawlerThread(count, system, anEntry));
				target_systems.add(system);
			} catch (Exception e) {
				RCUtil.logStackTrace(e);
			}
			count++;
		}

		pool.shutDown();

		io.createCodefaceRunScript(target_systems);

		RCUtil.log("_ Done. _");
	}

	/**
	 * This method should be used to rewrite the auxiliary files (i.e. *.conf,
	 * *TAGsMapping.txt).
	 * 
	 * @param urlsFile
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 * @throws InvalidNumberOfTagsException
	 */
	public static void rewriteAuxFiles(File urlsFile) throws IOException,
			NullPointerException, EmptyContentException,
			InvalidNumberOfTagsException {
		IOHandler io = new IOHandler();
		List<String> target_systems = new ArrayList<String>();

		// read repos
		Map<Integer, String> reposURLs = readRepositoryURLs("txt", urlsFile);

		// for each repo write files
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {

			ReposCrawler crawler = new ReposCrawler();

			crawler.setRepositoryID(anEntry.getKey());
			crawler.setRepositoryURL(anEntry.getValue());

			String system = RCUtil.getRepositorySystemName(anEntry.getValue());

			target_systems.add(system);
			crawler.setWorkDir(io.getReposDirectory(system));
			Repository repository = crawler.getRepository();

			List<String> lines = new ArrayList<String>();

			try (Git git = new Git(repository)) {
				RevWalk revWalk = new RevWalk(repository);

				System.out.println("Writing Tags Mapping File for \"" + system
						+ "\".");
				for (Entry<String, Ref> e : repository.getTags().entrySet()) {
					RevCommit rc = revWalk.parseCommit(e.getValue()
							.getObjectId());
					lines.add(e.getKey() + ": " + rc.getName());
				}

				io.writeFile(new File(RCProperties.getReposDir()
						+ system + "_TAGsMapping.txt"), lines);

				System.out.println("Writing codeface configuration file for \""
						+ system + "\".");
				io.createCodefaceConfFiles(system, repository.getTags().size());
				revWalk.close();
			}
		}

		System.out.println("Writing shell script to run the target systems.");
		io.createCodefaceRunScript(target_systems);
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
						System.out.println("[Skipping "
								+ RCUtil.getRepositorySystemName(l.get(i - 1))
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
