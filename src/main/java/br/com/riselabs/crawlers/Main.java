/**
 * 
 */
package br.com.riselabs.crawlers;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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

/**
 * @author Alcemir R. Santos
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// read method documentation for more infos.
		Map<Integer, String> reposURLs = readRepositoryURLs("txt");

		// for each url in the list clones the repository
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			ReposCrawler crawler = ReposCrawler.getInstance();
			crawler.setRepositoryID(anEntry.getKey());
			crawler.setRepositoryURL(anEntry.getValue());
			try {

				crawler.cloneRepository();
				crawler.createMergeBasedTags();
				crawler.writeTagsFile();

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
		System.out.println("_ Done. _");
	}

	/**
	 * use "db" to recover from the database and "txt" to recover from the file.
	 * 
	 * @return
	 */
	private static Map<Integer, String> readRepositoryURLs(String source) {
		Map<Integer, String> reposURLs = null;
		System.out.print("Reading URLs...");

		switch (source) {
		case "txt": // It is a test. It should read from te .txt file.
			String filename = "target_systems_repo_urls.txt";
			File urlsFile = new File(RCProperties.REPOS_DIR + filename);	
			reposURLs = new HashMap<Integer, String>();
			List<String> l = IOHandler.readFile(urlsFile.toPath());
			
			for (int i=1; i<=l.size(); i++) {
			try {
				ResultSet rs = DBManager.executeQuery("select r.id id from repository r where r.url=\'"+l.get(i-1).replace("https", "http")+"\'");
				rs.first();
				reposURLs.put(rs.getInt("id"), l.get(i-1));
			} catch (ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
			}
			
			};
			
			break;
		case "db":
		default:
			reposURLs = IOHandler.readURLsFromDatabase();
			break;
		}
		
		System.out.println("Ready.");
		return reposURLs;
	}
}
