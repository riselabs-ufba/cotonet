/**
 * 
 */
package br.com.riselabs.crawlers.test.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

/**
 * @author alcemir
 *
 */
public class GitHubDotComMock {

	private static GitHubDotComMock instance;
	private File localPath;
	private Repository repo;

	private GitHubDotComMock() {
		try {
			localPath = File.createTempFile("GitRepositoryMock", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static GitHubDotComMock getInstance() {
		if (instance == null)
			instance = new GitHubDotComMock();
		return instance;
	}

	public Repository getRepository() {
		return repo;
	}

	public void init() {
		try {
			createNewRepository();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}


	private void createNewRepository() throws IOException,
			IllegalStateException, GitAPIException {
		// prepare a new folder
		localPath.delete();

		// create the directory
		try (Git git = Git.init().setDirectory(localPath).call()) {
			repo =  git.getRepository();
		}
		
		FileUtils.deleteDirectory(localPath);
	}

	public void destroy() {
	}
}
