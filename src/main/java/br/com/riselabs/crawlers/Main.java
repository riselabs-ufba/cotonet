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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import br.com.riselabs.crawlers.db.DBManager;
import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.exceptions.InvalidNumberOfTagsException;

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
			RCProperties.setWorkingDir("/Downloads"); // it is running locally
			reposListFile = new File(RCProperties.getWorkingDir()
					+ "/ReposCrawler/target_systems_repo_urls.txt");
		}

		IOHandler io = new IOHandler();
		List<String> target_systems = new ArrayList<String>();

		Map<Integer, String> reposURLs = readRepositoryURLs("txt",
				reposListFile);

		// for each url in the list clones the repository
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			ReposCrawler crawler = new ReposCrawler();
			crawler.setRepositoryID(anEntry.getKey());
			crawler.setRepositoryURL(anEntry.getValue());
			String system = io.getRepositorySystemName(anEntry
					.getValue());
			target_systems.add(system);
			try {

				crawler.cloneRepository();
				crawler.createMergeBasedTags();

				log("Writing Tags Mapping File at: " + RCProperties.getReposDir()
						+ system + "_TAGsMapping.txt");
				crawler.persistTagsMapping();

				log("Writing codeface configuration file at: "
						+ RCProperties.getConfigDir() + system + ".conf");
				io.createCodefaceConfFiles(system, crawler.getTags()
						.size());

			} catch (InvalidRemoteException e) {
				e.printStackTrace();
			} catch (TransportException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		}

		log("Writing shell script to run the target systems.");
		io.createCodefaceRunScript(target_systems);

		log("_ Done. _");
	}

	private static void log(String message) {
		System.out.println(message);
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
		System.out.println("Reading URLs from: " + urlsFile.toString());

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
										+ new IOHandler().getRepositorySystemName(l
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

		System.out.println("Done.");
		return reposURLs;
	}
}
