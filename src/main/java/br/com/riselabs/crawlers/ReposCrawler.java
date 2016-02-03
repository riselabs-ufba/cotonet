package br.com.riselabs.crawlers;

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

import br.com.riselabs.crawlers.beans.MergeScenario;
import br.com.riselabs.crawlers.util.IOHandler;
import br.com.riselabs.crawlers.util.RCProperties;
import br.com.riselabs.crawlers.exceptions.EmptyContentException;

public class ReposCrawler {

	private static ReposCrawler instance;
	private String repositoryURL;
	private File repositoryDir;
	private Integer repositoryID;
	private List<String> left = new ArrayList<String>();
	private List<String> right = new ArrayList<String>();
	private Map<String, String> tagsMap = new HashMap<String, String>();
	

	private ReposCrawler() {
	}

	public static ReposCrawler getInstance() {
		if (instance == null)
			instance = new ReposCrawler();
		return instance;
	}

	public List<String> getTags(){
		return new ArrayList<String>(tagsMap.keySet());
	}
	
	public List<String> getLeftTags() {
		return left;
	}

	public List<String> getRightTags() {
		return right;
	}

	public void setRepositoryID(Integer key) {
		this.repositoryID = key;
	}

	public void setRepositoryURL(String repoURL) {
		repositoryURL = repoURL;
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
	public void cloneRepository() throws IOException, InvalidRemoteException,
			TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		String targetSystemName = IOHandler
				.getRepositorySystemName(repositoryURL);
		setWorkDir(IOHandler.makeDirectory(targetSystemName));

		// then clone
		System.out.println("Cloning \"" + targetSystemName + "\" to "
				+ repositoryDir);
		try (Git result = Git.cloneRepository().setURI(repositoryURL + ".git")
				.setDirectory(repositoryDir).call()) {
			// Note: the call() returns an opened repository already which
			// needs to be closed to avoid file handle leaks!
			System.out.println("Writing repository.");
			// workaround for
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
			result.getRepository().close();
		}

	}

	public  Repository getRepository() throws IOException {
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

	public void persistTagsMapping() throws IOException, NullPointerException, EmptyContentException {
		String targetSystemName = IOHandler.getRepositorySystemName(repositoryURL);
		List<String> tags = new ArrayList<String>();
		
		for (Entry<String, String> e : tagsMap.entrySet()) {
			tags.add(e.getKey()+": "+e.getValue());
		}
		
		IOHandler.writeFile(new File(RCProperties.REPOS_DIR + targetSystemName
				+ "_TAGsMapping.txt"), tags);
	}


	public void createMergeBasedTags() throws IOException {
		List<MergeScenario> scenarios = IOHandler
				.getMergeScenarios(repositoryID);
		Repository repository = getRepository();
		try (Git git = new Git(repository)) {
			// remove existing tags
			for (String tag : repository.getTags().keySet()) {
				git.tagDelete().setTags(tag).call();
			}
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		int count = 1;
		for (MergeScenario s : scenarios) {
			String tagB = "B" + count;
			String tagL = "L" + count;
			String tagR = "R" + count;

			tagsMap.put(tagB,s.getBase());
			tagsMap.put(tagL, s.getLeft());
			tagsMap.put(tagR, s.getRight());
			
			left.add(tagB);
			left.add(tagL);
			right.add(tagB);
			right.add(tagR);

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

					Ref tagBase = git.tag().setObjectId(commitBase)
							.setName(tagB).call();
					Ref tagLeft = git.tag().setObjectId(commitLeft)
							.setName(tagL).call();
					Ref tagRight = git.tag().setObjectId(commitRight)
							.setName(tagR).call();

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

	public void setWorkDir(File gitRepositoryDirectory) {
		this.repositoryDir = gitRepositoryDirectory;
	}

}
