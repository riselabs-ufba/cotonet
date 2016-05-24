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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import br.com.riselabs.cotonet.builder.AbstractNetworkBuilder;
import br.com.riselabs.cotonet.builder.ChunkBasedNetworkBuilder;
import br.com.riselabs.cotonet.builder.ConflictBasedNetworkBuilder;
import br.com.riselabs.cotonet.builder.FileBasedNetworkBuilder;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;
import br.com.riselabs.cotonet.util.Directories;
import br.com.riselabs.cotonet.util.IOHandler;
import br.com.riselabs.cotonet.util.Logger;

/**
 * This Runnable class can execute four activities: 
 * </br>- it clones the repository; 
 * </br>- it persists the tags mapping file; 
 * </br>- it creates the <code>codeface</code> configuration file; and 
 * </br>- it triggers the conflict based network construction.
 * 
 * @author Alcemir R. Santos
 *
 */
public class RepositoryCrawler implements Runnable {

	private Project project;
	// TODO put this variable `tagsMap' in other place
	private Map<String, String> tagsMap;

	private Integer repositoryID;
	private File repositoryDir;
	private boolean skipNetworks;
	private NetworkType type;

	private File log;

	public RepositoryCrawler(String systemURL, boolean mustClone, NetworkType type)
			throws IOException {
		setProject(new Project(systemURL));
		setCloning(mustClone);
		setLogFile(new File(Directories.getLogDir(), "thread-"
				+ getProject().getName() + ".log"));
		setRepositoryDir(new File(Directories.getReposDir(), project.getName()));
		setType(type);
		tagsMap = new HashMap<String, String>();
	}

	private void setType(NetworkType type) {
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

	@Deprecated
	public void setRepositoryID(Integer key) {
		this.repositoryID = key;
	}

	@Deprecated
	public List<String> getTags() {
		return new ArrayList<String>(tagsMap.keySet());
	}

	/**
	 * Returns the list of TAGs from the given repository.
	 * 
	 * @param repository
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public List<String> getTags(Repository repository) throws IOException,
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
				Logger.log(log, "["+ getProject().getName()+ "] Cloning Start.");
				result = Git.cloneRepository()
						.setURI(project.getUrl() + ".git")
						.setDirectory(repositoryDir).call();
				Logger.log(log, "["+ getProject().getName()+ "] Cloning Finished.");
				return result.getRepository();
			} catch (GitAPIException e) {
				Logger.log(log, "["+ getProject().getName()+ "] Cloning Failed.");
				Logger.logStackTrace(log, e);
			}
			System.gc();
			return null;
		}

	@Override
	public void run() {
		try {
			// cloning or reading
			Repository repo;
			try(Repository aux = openRepository()){
				repo = aux;
			}catch(RepositoryNotFoundException e){
				repo = cloneRepository();
			}
			project.setRepository(repo);
			if (!skipNetworks) {
				// building networks
//				ConflictBasedNetworkBuilder builder = 
//						new ConflictBasedNetworkBuilder(project);
//				builder.execute();
				AbstractNetworkBuilder builderr = null;
 	 			switch (type) {
 	 			case FILE_BASED:
 	 				builderr =  new FileBasedNetworkBuilder(getProject());
 	 				break;
				case CHUNK_BASED:
				default:
					builderr =  new ChunkBasedNetworkBuilder( getProject());
					break;
				}
 	 			builderr.build();
			}
			// persisting aux files
// TODO			createTagToSHA1MappingFile();
// TODO			createCodefaceConfFile();

		} catch (Exception e) {
			Logger.logStackTrace(log, e);
		}
		System.gc();
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

	@Deprecated
	private void createMergeBasedTags() throws IOException {
		Repository repository = openRepository();
		List<MergeScenario> scenarios = IOHandler.getMergeScenarios(
				repositoryID, repository);
		try (Git git = new Git(repository)) {
			// remove existing tags
			for (String tag : repository.getTags().keySet()) {
				git.tagDelete().setTags(tag).call();
			}
		} catch (GitAPIException e) {
			Logger.logStackTrace(e);
			e.printStackTrace();
		}
		int count = 1;

		for (MergeScenario s : scenarios) {
			String tagB = project.getName() + "B" + count;
			String tagL = project.getName() + "L" + count;
			String tagR = project.getName() + "R" + count;

			tagsMap.put(tagB, s.getBase().getName());
			tagsMap.put(tagL, s.getLeft().getName());
			tagsMap.put(tagR, s.getRight().getName());

			// prepare test-repository
			try (Git git = new Git(repository)) {

				try (RevWalk walk = new RevWalk(repository)) {

					git.tag().setObjectId(s.getBase()).setName(tagB).call();
					git.tag().setObjectId(s.getLeft()).setName(tagL).call();
					git.tag().setObjectId(s.getRight()).setName(tagR).call();

					walk.dispose();
				} catch (ConcurrentRefUpdateException e) {
					e.printStackTrace();
				} catch (InvalidTagNameException e) {
					e.printStackTrace();
				} catch (NoHeadException e) {
					e.printStackTrace();
				} catch (GitAPIException e) {
					e.printStackTrace();
				}
			} catch (RevisionSyntaxException e) {
				e.printStackTrace();
			}
			count++;
		}

	}

	/**
	 * Writes a file with mapping from each tag created to its SHA1.
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws EmptyContentException
	 */
	public void createTagToSHA1MappingFile() throws NullPointerException,
			IOException, EmptyContentException {
		List<String> lines = new ArrayList<String>();
		RevWalk revWalk = new RevWalk(getProject().getRepository());

		for (Entry<String, Ref> e : getProject().getRepository().getTags()
				.entrySet()) {
			RevCommit rc = revWalk.parseCommit(e.getValue().getObjectId());
			lines.add(e.getKey() + ": " + rc.getName());
		}
		revWalk.close();
		IOHandler io = new IOHandler();
		File mappingFile = io.createFile(Directories.getMappingsDir(),
				getProject().getName()	+ "_TAGsMapping.txt");
		io.writeFile(mappingFile, lines);
		Logger.log("\'" + getProject().getName()
				+ "\' tags mapping file written.");

	}

	/**
	 * Writes the {@code .conf} file to be used with codeface.
	 * 
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws EmptyContentException
	 * @throws InvalidNumberOfTagsException
	 */
	public void createCodefaceConfFile() throws IOException,
			NullPointerException, EmptyContentException,
			InvalidNumberOfTagsException {
		String releases = getTupletsString(getProject().getMergeScenarios()
				.size());
		StringBuilder strBuilder = new StringBuilder();

		strBuilder.append("# Configuration file for the system "
				+ getProject().getName());
		strBuilder.append("");
		strBuilder.append("# Copyright UFBA/UniPassau - Alcemir Santos");
		strBuilder.append("#");
		strBuilder
				.append("# Copying and distribution of this file, with or without modification,");
		strBuilder
				.append("# are permitted in any medium without royalty provided the copyright");
		strBuilder
				.append("# notice and this notice are preserved.  This file is offered as-is,");
		strBuilder.append("# without any warranty.");
		strBuilder.append("");
		strBuilder.append("---");
		strBuilder.append("project: " + getProject().getName());
		strBuilder.append("repo: " + getProject().getName());
		strBuilder
				.append(" # Relative to git-dir as specified on the command line");
		strBuilder.append("description: " + getProject().getName());
		strBuilder.append("mailinglists: ");
		strBuilder.append("    -   name: ");
		strBuilder.append("        type: ");
		strBuilder.append("        source: ");
		strBuilder
				.append("#    - {name: gmane.comp.emulators.qemu.user, type: user, source: gmane}");

		strBuilder.append("revisions: [" + releases + "]");
		// + "rcs : ["+ releases +" ]\n"
		strBuilder
				.append("# tagging types: proximity, tag, file, feature, committer2author, feature_file");
		strBuilder.append("tagging: file\n");

		List<String> content = new ArrayList<String>();
		content.add(strBuilder.toString());

		File systemDir = new File(Directories.getConfigDir(), getProject()
				.getName() + ".conf");
		IOHandler io = new IOHandler();
		io.checkAndRemove(systemDir);
		systemDir = io.createFile(systemDir.getParentFile(), systemDir.getName());
		io.writeFile(systemDir, content);
	}

	public String getTupletsString(int numScenarios)
			throws InvalidNumberOfTagsException {
		// if (numScenarios % 3 != 0) {
		// throw new InvalidNumberOfTagsException(numScenarios);
		// }
		List<String> tuples = new ArrayList<String>();
		String systemName = getProject().getName();
		for (int i = 1; i <= numScenarios; i++) {
			tuples.add("\"" + systemName + "B" + i + "\", " + "\"" + systemName
					+ "L" + i + "\", " + "\"" + systemName + "R" + i + "\"");
		}
		return createReleasesString(tuples);
	}

	private static String createReleasesString(List<String> tagsList) {
		if (tagsList.size() == 1) {
			return tagsList.remove(0);
		} else {
			return tagsList.remove(0) + ", " + createReleasesString(tagsList);
		}
	}

}
