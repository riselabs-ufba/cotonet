package br.com.riselabs.crawlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import br.com.riselabs.crawlers.util.IOHandler;

public class ReposCrawler {
	
	private static ReposCrawler instance;
	private String repositoryURL;
	private Repository repository;
	
	private ReposCrawler() {}
	
	public static ReposCrawler getInstance() {
		if (instance == null)
			instance = new ReposCrawler();
		return instance;
	}
	
	/**
	 * Clones the repository of a given URL. Returns an object that represents the repository.
	 * 
	 * @param repositoryURL
	 * @return
	 * @throws IOException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public void cloneRepository() throws IOException,
			InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		String targetSystemName = IOHandler.getRepositorySystemName(repositoryURL);
		File localPath = IOHandler.makeDirectory(targetSystemName);

		// then clone
		System.out.println("Cloning \"" + targetSystemName + "\" to " + localPath);
		try (Git result = Git.cloneRepository().setURI(repositoryURL + ".git")
				.setDirectory(localPath).call()) {
			// Note: the call() returns an opened repository already which
			// needs
			// to be closed to avoid file handle leaks!
			System.out.println("Writring repository.");
			repository =  result.getRepository();
		}
	}

	/**
	 * Returns the list of TAGs from the given repository.
	 * 
	 * @param repository
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public List<String> getTagsList(Repository repository) throws IOException,
			GitAPIException {
		List<String> tags = new ArrayList<String>();
		try (Git git = new Git(repository)) {
			List<Ref> call = git.tagList().call();
			for (Ref ref : call) {
				tags.add(ref.getName());
			}
		}
		return tags;
	}

	public void setRepositoryURL(String repoURL) {
		// workaround for
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
		if(repository!=null) repository.close();
		
		repositoryURL = repoURL;
	}

	public void writeTagsFile() {
		String targetSystem = IOHandler.getRepositorySystemName(repositoryURL);
		List<String> tags = new ArrayList<String>(repository.getTags().keySet()); 
		IOHandler.writeTagsFile(targetSystem, tags);
	}

	
}
