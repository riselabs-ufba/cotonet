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
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import br.com.riselabs.cotonet.builder.ConflictBasedNetworkBuilder;
import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.DAOImpl;
import br.com.riselabs.cotonet.model.dao.validators.ProjectValidator;
import br.com.riselabs.cotonet.model.dao.validators.Validator;
import br.com.riselabs.cotonet.model.exceptions.EmptyContentException;
import br.com.riselabs.cotonet.model.exceptions.InvalidNumberOfTagsException;
import br.com.riselabs.cotonet.util.IOHandler;
import br.com.riselabs.cotonet.util.Directories;
import br.com.riselabs.cotonet.util.Logger;

public class ReposCrawler {

	private static Project project;
	private static File repositoryDir;
	private Integer repositoryID;
	private Map<String, String> tagsMap = new HashMap<String, String>();
	private boolean skipCloning = false;

	public ReposCrawler( String systemURL, boolean skipCloning) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		project =  new Project(systemURL, null);
		this.skipCloning  = skipCloning;
	}

	public ReposCrawler( String systemURL, 
			Repository repository) throws IOException {
		project = new Project(systemURL, repository);
		repositoryDir = new IOHandler().makeSystemDirectory(project.getName());
	}

	public List<String> getTags() {
		return new ArrayList<String>(tagsMap.keySet());
	}

	public void setRepositoryID(Integer key) {
		this.repositoryID = key;
	}

	/**
	 * Clones the repository of a given URL. Returns an object that represents
	 * the repository.
	 * 
	 * @param repositoryURL
	 * @return
	 * @throws IOException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	private static Repository cloneRepository() throws IOException, InvalidRemoteException,
			TransportException, GitAPIException {
		Logger.log("Starting the cloning of \"" + project.getName());

		Git result = Git.cloneRepository()
				.setURI(project.getUrl() + ".git")
				.setDirectory(repositoryDir).call();
		Logger.log("Clonning of \"" + project.getName() + "\": [_DONE_]");
		System.gc();
		return result.getRepository();
	}

	private static Repository buildRepository() throws IOException {
		// now open the resulting repository with a FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder.setWorkTree(repositoryDir);
		// builder.readEnvironment(); // scan environment GIT_* variables
		// builder.findGitDir(); // scan up the file system tree
		builder.setMustExist(true);
		Repository repository = builder.build();
		return repository;
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

	public void createMergeBasedTags() throws IOException {
		List<MergeScenario> scenarios = IOHandler
				.getMergeScenarios(repositoryID);
		Repository repository = buildRepository();
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

			tagsMap.put(tagB, s.getBase());
			tagsMap.put(tagL, s.getLeft());
			tagsMap.put(tagR, s.getRight());

			// prepare test-repository
			try (Git git = new Git(repository)) {

				// read some other commit and set the tag on it
				ObjectId shaBase = repository.resolve(s.getBase());
				ObjectId shaLeft = repository.resolve(s.getLeft());
				ObjectId shaRight = repository.resolve(s.getRight());

				try (RevWalk walk = new RevWalk(repository)) {
					RevCommit commitBase = walk.parseCommit(shaBase);
					RevCommit commitLeft = walk.parseCommit(shaLeft);
					RevCommit commitRight = walk.parseCommit(shaRight);

					git.tag().setObjectId(commitBase).setName(tagB).call();
					git.tag().setObjectId(commitLeft).setName(tagL).call();
					git.tag().setObjectId(commitRight).setName(tagR).call();

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
			} catch (AmbiguousObjectException e) {
				e.printStackTrace();
			} catch (IncorrectObjectTypeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			count++;
		}

	}

	public List<ConflictBasedNetwork> buildNetworks() throws Exception {
		ConflictBasedNetworkBuilder builder = new ConflictBasedNetworkBuilder(
				project);
		return builder.execute();
	}

	public Project getProject() {
		return project;
	}

	/**
	 * Writes a file with mapping from each tag created to its SHA1.
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws EmptyContentException
	 */
	public void writeTagToSHA1MappingFile() throws NullPointerException,
			IOException, EmptyContentException {
		List<String> lines = new ArrayList<String>();
		RevWalk revWalk = new RevWalk(getProject().getRepository());

		for (Entry<String, Ref> e : getProject().getRepository().getTags()
				.entrySet()) {
			RevCommit rc = revWalk.parseCommit(e.getValue().getObjectId());
			lines.add(e.getKey() + ": " + rc.getName());
		}
		revWalk.close();
		File f = new File(Directories.getMappingsDir(), getProject().getName()
				+ "_TAGsMapping.txt");
		if (f.exists())
			f.delete();
		IOHandler io = new IOHandler();
		io.writeFile(f, lines);
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
	public void writeCodefaceConfFile() throws IOException,
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
		// makeDirectory(systemDir);
		io.writeFile(systemDir, content);
	}

	public String getTupletsString(int numScenarios)
			throws InvalidNumberOfTagsException {
		if (numScenarios % 3 != 0) {
			throw new InvalidNumberOfTagsException(numScenarios);
		}
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

	public void run() throws Exception {
		// cloning or reading
		Repository repo;
		if (skipCloning) {
			repo = buildRepository();
		}else{
			repo = cloneRepository();
		}
		project.setRepository(repo);
		// building networks
		buildNetworks();
		// persisting
		writeTagToSHA1MappingFile();
		writeCodefaceConfFile();
		Validator<Project> validator = new ProjectValidator();
		DAOImpl<Project> dao = new DAOImpl<Project>(Project.class, validator);
		dao.save(getProject());
	}
}
