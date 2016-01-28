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

import javax.swing.text.html.HTML.Tag;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import br.com.riselabs.crawlers.db.DBManager;
import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.util.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.util.exceptions.InvalidNumberOfTagsException;

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
	public static void main(String[] args) throws NullPointerException, IOException, EmptyContentException, InvalidNumberOfTagsException {
		List<String> target_systems = new ArrayList<String>();

		// read method documentation for more infos.
		Map<Integer, String> reposURLs = readRepositoryURLs("txt");
		
		// for each url in the list clones the repository
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			ReposCrawler crawler = ReposCrawler.getInstance();
			crawler.setRepositoryID(anEntry.getKey());
			crawler.setRepositoryURL(anEntry.getValue());
			String system = IOHandler.getRepositorySystemName(anEntry
					.getValue());
			target_systems.add(system);
			try {

				crawler.cloneRepository();
				crawler.createMergeBasedTags();
				
				log("Writing Tags Mapping File.");
				crawler.persistTagsMapping();

				log("Writing codeface configuration file.");
				IOHandler.createCodefaceConfFiles(system, crawler.getTags().size());

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
		IOHandler.createCodefaceRunScript(target_systems);
		
		log("_ Done. _");
	}

	private static void log(String message){
		System.out.println(message);
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
			File urlsFile = new File(RCProperties.USER_HOME +"/Downloads/ReposCrawler/"+ filename);
			reposURLs = new HashMap<Integer, String>();
			List<String> l = IOHandler.readFile(urlsFile.toPath());

			for (int i = 1; i <= l.size(); i++) {
				try {
					ResultSet rs = DBManager
							.executeQuery("select r.id id from repository r where r.url=\'"
									+ l.get(i - 1).replace("https", "http")
									+ "\'");
					rs.first();
					reposURLs.put(rs.getInt("id"), l.get(i - 1));
				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}

			}

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
