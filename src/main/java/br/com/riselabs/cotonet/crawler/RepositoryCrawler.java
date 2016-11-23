/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alcemir R. Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.crawler;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import br.com.riselabs.cotonet.builder.NetworkBuilder;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.util.CodefaceHelper;
import br.com.riselabs.cotonet.util.Directories;
import br.com.riselabs.cotonet.util.Logger;

/**
 * This Runnable class can execute four activities: </br>
 * - it clones the repository; </br>
 * - it persists the tags mapping file; </br>
 * - it creates the <code>codeface</code> configuration file; and </br>
 * - it triggers the conflict based network construction.
 * 
 * @author Alcemir R. Santos
 *
 */
public class RepositoryCrawler implements Runnable {

	private Project project;

	private File repositoryDir;
	private boolean skipNetworks;
	private File log;
	
	private NetworkType type;

	public RepositoryCrawler(String systemURL, boolean mustClone, NetworkType type)
			throws IOException {
		setProject(new Project(systemURL));
		setCloning(mustClone);
		setLogFile(new File(Directories.getLogDir(), "thread-" + getProject().getName() + ".log"));
		setRepositoryDir(new File(Directories.getReposDir(), project.getName()));
		setProgramType(type);
	}

	public NetworkType getProgramType() {
		return type;
	}

	public void setProgramType(NetworkType type) {
		this.type = type;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setCloning(boolean mustClone) {
		this.skipNetworks = mustClone;
	}

	public void setLogFile(File f) {
		this.log = f;
	}

	public void setRepositoryDir(File directory) {
		this.repositoryDir = directory;
	}

	@Override
	public void run() {
		try {
			// cloning or reading
			Repository repo;
			try (Repository aux = openRepository()) {
				repo = aux;

				// repository already exists, so we reset it to master to make sure it is not in a weird state
				Git git = Git.wrap(repo);
				git.reset().setRef("master").setMode(ResetCommand.ResetType.HARD).call();
			} catch (RepositoryNotFoundException e) {
				repo = cloneRepository();
			}
			project.setRepository(repo);
			if (!skipNetworks) {
				// building networks

				NetworkBuilder<Object> builder = new NetworkBuilder<Object>(getProject(), getProgramType());
				
				builder.setLogFile(log);
				builder.build();
				builder.persist();
			}
			// persisting aux files
			CodefaceHelper.createCodefaceConfFiles(project);
		} catch (NullPointerException | EmptyContentException | GitAPIException | InterruptedException
				| IOException e) {
			Logger.logStackTrace(log, e);
		}
		System.gc();
	}

	/**
	 * Clones the project's repository of this {@code RepositoryCrawler}
	 * instance. The method {@code #getRepository()} returns an object that
	 * represents the repository.
	 * 
	 * @param repositoryURL
	 * @return
	 * @throws IOException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public Repository cloneRepository() {
		Git result;
		try {
			Logger.log(log, "[" + getProject().getName() + "] Cloning Start.");
			result = Git.cloneRepository().setURI(project.getUrl() + ".git").setDirectory(repositoryDir).call();
			Logger.log(log, "[" + getProject().getName() + "] Cloning Finished.");
			return result.getRepository();
		} catch (GitAPIException e) {
			Logger.log(log, "[" + getProject().getName() + "] Cloning Failed.");
			Logger.logStackTrace(log, e);
		}
		System.gc();
		return null;
	}

	private Repository openRepository() throws IOException {
		// now open the resulting repository with a FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder.setWorkTree(repositoryDir);
		// builder.readEnvironment(); // scan environment GIT_* variables
		// builder.findGitDir(); // scan up the file system tree
		builder.setMustExist(true);
		Repository repository = builder.build();
		return repository;
	}
}
