/**
 * 
 */
package br.com.riselabs.crawlers;

import java.io.File;
import java.io.IOException;
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

import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;
import br.com.riselabs.crawlers.exceptions.InvalidNumberOfTagsException;

/**
 * @author alcemir
 *
 */
public class RewriteFilesMain {

	public static void main(String[] args) throws IOException, NullPointerException, EmptyContentException, InvalidNumberOfTagsException{
		List<String> target_systems = new ArrayList<String>();

		// read repos
		Map<Integer, String> reposURLs = new HashMap<Integer, String>();
//		reposURLs.put(133 , "https://github.com/osmandapp/Osmand");
		reposURLs.put(601, "https://github.com/abarisain/dmix");
		reposURLs.put(151, "https://github.com/groovy/groovy-core");
		
		// for each repo write files
		for (Entry<Integer, String> anEntry : reposURLs.entrySet()) {
			
			ReposCrawler crawler = ReposCrawler.getInstance();
			
			crawler.setRepositoryID(anEntry.getKey());
			crawler.setRepositoryURL(anEntry.getValue());
			String system = IOHandler.getRepositorySystemName(anEntry
					.getValue());

			target_systems.add(system);
			crawler.setWorkDir(IOHandler.getDirectory(system));
			Repository repository = crawler.getRepository();
			
			List<String> lines = new ArrayList<String>();
			
			try (Git git = new Git(repository)) {
				RevWalk revWalk = new RevWalk(repository);

				System.out.println("Writing Tags Mapping File for \""+system+"\".");
				for (Entry<String, Ref>  e : repository.getTags().entrySet()) {
					RevCommit rc = revWalk.parseCommit(e.getValue().getObjectId());
					lines.add(e.getKey()+": "+rc.getName());
				}

				IOHandler.writeFile(new File(RCProperties.getReposDir() + system
						+ "_TAGsMapping.txt"), lines);

				System.out.println("Writing codeface configuration file for \""+system+"\".");
				IOHandler.createCodefaceConfFiles(system, repository.getTags().size() );
				revWalk.close();
			}
		}
		
		System.out.println("Writing shell script to run the target systems.");
		IOHandler.createCodefaceRunScript(target_systems);
		
		System.out.println("_ Done. _");
		
		}
}
