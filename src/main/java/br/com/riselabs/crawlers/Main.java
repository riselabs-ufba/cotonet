/**
 * 
 */
package br.com.riselabs.crawlers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.util.IOHandler;;


/**
 * @author Alcemir R. Santos
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.print("Reading URLs...");
		List<String> reposURLs = readRepositoryURLs();
		System.out.println("Ready.");
		
		// for each url in the list clones the repository
		for (String repoURL : (List<String>) reposURLs) {
			ReposCrawler crawler = ReposCrawler.getInstance();
			crawler.setRepositoryURL(repoURL);
			try {

				crawler.cloneRepository();
				crawler.writeTagsFile();

			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 */
	private static List<String> readRepositoryURLs() {
		// read the target_systems_repo_urls 
		String filename = "target_systems_repo_urls.txt";
		File urlsFile = new File(RCProperties.REPOS_DIR + filename);		
		List<String> reposURLs = IOHandler.readFile(urlsFile.toPath());
		return reposURLs;
	}

}
