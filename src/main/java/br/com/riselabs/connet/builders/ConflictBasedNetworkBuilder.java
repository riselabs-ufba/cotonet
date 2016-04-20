/**
 * 
 */
package br.com.riselabs.connet.builders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import br.com.riselabs.connet.beans.Blame;
import br.com.riselabs.connet.beans.ConflictBasedNetwork;
import br.com.riselabs.connet.beans.ConflictBasedNetwork.NetworkType;
import br.com.riselabs.connet.beans.DeveloperEdge;
import br.com.riselabs.connet.beans.DeveloperNode;
import br.com.riselabs.connet.beans.JGitMergeScenario;
import br.com.riselabs.connet.beans.Project;
import br.com.riselabs.connet.commands.RecursiveBlame;
import br.com.riselabs.connet.filters.InBetweenRevFilter;

/**
 * @author alcemirsantos
 *
 */
public class ConflictBasedNetworkBuilder {

	private Project project;

	private NetworkType type;

	private Map<JGitMergeScenario, List<Blame>> scenarioBlamesMap;

	/**
	 * 
	 */
	public ConflictBasedNetworkBuilder() {
		this(null, NetworkType.CHUNK_BASED);
	}

	public ConflictBasedNetworkBuilder(Project aProject) {
		this(aProject, NetworkType.CHUNK_BASED);
	}

	public ConflictBasedNetworkBuilder(Project aProject, NetworkType aType) {
		setProject(aProject);
		setType(aType);
	}

	public void setType(NetworkType aType) {
		this.type = aType;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public NetworkType getType() {
		return type;
	}

	/**
	 * @return the scenarioBlamesMap
	 */
	public Map<JGitMergeScenario, List<Blame>> getScenarioBlamesMap() {
		return scenarioBlamesMap;
	}

	/**
	 * @param scenarioBlamesMap
	 *            the scenarioBlamesMap to set
	 */
	public void setScenarioBlamesMap(
			Map<JGitMergeScenario, List<Blame>> scenarioBlamesMap) {
		this.scenarioBlamesMap = scenarioBlamesMap;
	}

	public void add(JGitMergeScenario aScenario, List<Blame> blames) {
		if (this.scenarioBlamesMap == null) {
			this.scenarioBlamesMap = new HashMap<JGitMergeScenario, List<Blame>>();
		}
		this.scenarioBlamesMap.put(aScenario, blames);
	}

	/**
	 * Builds the conflict based network considering the previously network type
	 * set and the repository information provided. In case the the type was not
	 * set yet, this method used the <i>default</i> type (<i>i.e.,</i> the
	 * chunk-based {@code Network.CHUNK_BASED}).
	 * 
	 * OBS: You should set the repository first, otherwise this method will
	 * return <code>null</code>
	 * 
	 * @return {@code aNetwork}
	 * 
	 *         -<code>null</code> when the repository is not set.
	 * @throws Exception
	 */
	public List<ConflictBasedNetwork> execute() throws Exception {
		if (this.project.getRepository() == null)
			return null;
		return execute(this.type);
	}

	/**
	 * Builds the conflict based network considering the given network type.
	 * 
	 * OBS: You should set the repository first, otherwise this method will
	 * return <code>null</code>
	 * 
	 * @return {@code aNetwork}
	 * 
	 *         -<code>null</code> when the repository is not set.
	 * @throws Exception
	 */
	public List<ConflictBasedNetwork> execute(NetworkType aType)
			throws Exception {

		List<ConflictBasedNetwork> result = new ArrayList<ConflictBasedNetwork>();
		List<RevCommit> allMerges = getMergeCommits();

		for (RevCommit mergeCommit : allMerges) {
			RevCommit leftHead = mergeCommit.getParent(0);
			RevCommit rightHead = mergeCommit.getParent(1);

			ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(
					this.project.getRepository(), true);
			boolean canMerge = merger.merge(leftHead, rightHead);

			if (canMerge) {
				// only conflicting merges are needed
				continue;
			}

			RevWalk walk = new RevWalk(this.project.getRepository());
			RevCommit baseCommit = walk.parseCommit(merger.getBaseCommitId());

			JGitMergeScenario scenario = new JGitMergeScenario(baseCommit,
					leftHead, rightHead);

			ConflictBasedNetwork connet = buildConflictBasedNetwork(scenario);

			result.add(connet);
			walk.close();
		}
		return result;
	}

	public ConflictBasedNetwork buildConflictBasedNetwork(
			JGitMergeScenario aScenario) throws Exception {
		if (this.project == null) {
			throw new Exception(
					"You must set a project on which the network relates to.");
		}
		// TODO extract nodes and edges
		Git git = Git.wrap(this.project.getRepository());
		CheckoutCommand ckoutCmd = git.checkout();
		ckoutCmd.setName(aScenario.getLeft().getName());
		ckoutCmd.setStartPoint(aScenario.getLeft());
		ckoutCmd.call();

		MergeCommand mergeCmd = git.merge();
		mergeCmd.setCommit(false);
		mergeCmd.include(aScenario.getRight());
		MergeResult mResult = mergeCmd.call();

		if (this.type == NetworkType.FILE_BASED) {
			List<File> conflictingFiles = getFilesWithConflicts(mResult);
			return build(aScenario, conflictingFiles);
		} else {
			return build(aScenario);
		}
	}

	/**
	 * Builds a network considering the collaborations at files level.
	 * 
	 * @param aScenario
	 * @param files
	 * @return a {@code ConflictBasedNetwork} considering the
	 *         {@code NetworkType#CHUNK_BASED}.
	 * @throws IOException
	 * @throws GitAPIException
	 */
	private ConflictBasedNetwork build(JGitMergeScenario aScenario) {
		return null;
	}

	/**
	 * Builds a network considering the collaborations at files level.
	 * 
	 * @param aScenario
	 * @param files
	 * @return a {@code ConflictBasedNetwork} considering the
	 *         {@code NetworkType#FILE_BASED}.
	 * @throws IOException
	 * @throws GitAPIException
	 */
	private ConflictBasedNetwork build(JGitMergeScenario aScenario,
			List<File> files) throws IOException, GitAPIException {
		RecursiveBlame blamer = new RecursiveBlame(this.project.getRepository());
		ConflictBasedNetwork connet = new ConflictBasedNetwork(this.project,
				aScenario);
		List<DeveloperNode> nodes = new ArrayList<DeveloperNode>();
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();

		for (File f : files) {
			// adding left blames
			List<Blame> blames = blamer.setBeginRevision(aScenario.getLeft())
					.setEndRevision(aScenario.getBase())
					.setFilePath(f.getPath()).call();
			// adding right blames
			blames.addAll(blamer.setBeginRevision(aScenario.getRight())
					.setEndRevision(aScenario.getBase())
					.setFilePath(f.getPath()).call());

			add(aScenario, blames);

			List<RevCommit> commits = getCommitsFrom(aScenario);
			List<DeveloperNode> newNodes = getDeveloperNodes(blames, commits,
					false);
			for (DeveloperNode n : newNodes) {
				if (!nodes.contains(n)) {
					nodes.add(n);
				}
			}

			List<DeveloperEdge> newEdges = buildEdges(nodes);
			for (DeveloperEdge e : newEdges) {
				if (!edges.contains(e)) {
					edges.add(e);
				}
			}
		}
		connet.setNodes(nodes);
		connet.setEdges(edges);
		return connet;
	}

	private List<DeveloperEdge> buildEdges(List<DeveloperNode> nodes) {
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();
		for (DeveloperNode from : nodes) {
			for (DeveloperNode to : nodes) {
				if (from.equals(to)) {
					continue;
				}
				DeveloperEdge edge = new DeveloperEdge(from.getId(), to.getId());
				if (!edges.contains(edge)) {
					edges.add(edge);
				}
			}
		}
		return edges;
	}

	private List<File> getFilesWithConflicts(MergeResult mResult) {
		List<File> result = new ArrayList<File>();
		Map<String, int[][]> allConflicts = mResult.getConflicts();
		for (String path : allConflicts.keySet()) {
			result.add(new File(path));
		}
		return result;
	}

	/**
	 * Returns the {@code git merge-base}.
	 * 
	 * @param sha1A
	 * @param sha1B
	 * @return
	 * @throws RevisionSyntaxException
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws AmbiguousObjectException
	 * @throws IOException
	 */
	public RevCommit findMergeBase(String sha1A, String sha1B)
			throws RevisionSyntaxException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException, IOException {
		// solution from
		// http://stackoverflow.com/questions/26434452/how-is-a-merge-base-done-in-jgit?rq=1
		RevWalk walk = new RevWalk(this.project.getRepository());
		walk.setRevFilter(RevFilter.MERGE_BASE);
		walk.markStart(walk.lookupCommit(this.project.getRepository().resolve(
				sha1A)));
		walk.markStart(walk.lookupCommit(this.project.getRepository().resolve(
				sha1B)));
		RevCommit mergeBase = walk.next();
		walk.close();
		return mergeBase;
	}

	/**
	 * Returns the {@code git merge-base --fork-point}.
	 * 
	 * @param repository
	 * @param base
	 * @param tip
	 * @return
	 * @throws IOException
	 */
	public RevCommit findForkPoint(Repository repository, String base,
			String tip) throws IOException {
		// solution from https://gist.github.com/robinst/997da20d09a82d85a3e9
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit tipCommit = walk.lookupCommit(this.project
					.getRepository().resolve(tip));
			List<ReflogEntry> reflog = this.project.getRepository()
					.getReflogReader(base).getReverseEntries();
			if (reflog.isEmpty()) {
				return null;
			}
			// The `<=` is deliberate because we want to check both new and old
			// IDs for the oldest entry
			for (int i = 0; i <= reflog.size(); i++) {
				ObjectId id = i < reflog.size() ? reflog.get(i).getNewId()
						: reflog.get(i - 1).getOldId();
				RevCommit commit = walk.lookupCommit(id);
				if (walk.isMergedInto(commit, tipCommit)) {
					walk.parseBody(commit);
					return commit;
				}
			}
		}
		return null;
	}

	/**
	 * Returns all commits from the {@code start} to {@code end} that are merge
	 * commits. We consider a commit as a merge commit if its number of parents
	 * is greater than 1.
	 *
	 * @param start
	 *            skip all commits before
	 * @param end
	 *            skip all commits after
	 * @return all commits within specified range that are merges
	 */
	public List<RevCommit> getMergeCommits(String start, String end) {
		List<RevCommit> merges = new LinkedList<>();
		Iterable<RevCommit> log;
		boolean addCommits = start == null;
		try {
			Git git = Git.wrap(this.project.getRepository());
			log = git.log().call();
			for (RevCommit commit : log) {
				if (!addCommits) {
					if (commit.getName().equals(start)) {
						addCommits = true;
					} else {
						continue;
					}
				}
				if (commit.getParentCount() > 1) {
					merges.add(commit);
				}
				if (end != null && commit.getName().equals(end)) {
					break;
				}
			}
		} catch (GitAPIException e) {
			System.out.println(e.getMessage());
		}

		return merges;
	}

	public List<RevCommit> getCommitsFrom(JGitMergeScenario aScenario)
			throws RevisionSyntaxException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException,
			IOException, GitAPIException {
		List<RevCommit> commits = between(aScenario.getBase().getName(),
				aScenario.getLeft().getName());
		commits.addAll(between(aScenario.getBase().getName(), aScenario
				.getRight().getName()));
		return commits;
	}

	/**
	 * Returns all commits from the {@code start} to {@code end}.
	 *
	 * @param start
	 *            - skip all commits before
	 * @param end
	 *            - skip all commits after
	 * @return all commits within the specified range
	 * @throws IOException
	 * @throws IncorrectObjectTypeException
	 * @throws MissingObjectException
	 */
	public List<RevCommit> between(String start, String end)
			throws RevisionSyntaxException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException,
			IOException, GitAPIException {
		RevWalk walk = new RevWalk(getProject().getRepository());
		RevCommit beginCommit = walk.parseCommit(getProject().getRepository()
				.resolve(start));
		RevCommit endCommit = walk.parseCommit(getProject().getRepository()
				.resolve(end));
		walk.close();
		return commitsBetween(beginCommit, endCommit);
	}

	private List<RevCommit> commitsBetween(RevCommit begin, RevCommit end)
			throws GitAPIException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		List<RevCommit> commits = new LinkedList<>();
		RevWalk walk = new RevWalk(getProject().getRepository());
		RevCommit parent = walk.parseCommit(end.getParent(0).getId());

		if (parent.getName().equals(begin.getName())) {
			commits.add(end);
			walk.close();
			return commits;
		} else {
			commits.add(end);
			commits.addAll(commitsBetween(begin, parent));
			walk.close();
			return commits;
		}
	}

	/**
	 * Returns all commits of the project which are merges. </p> We consider a
	 * commit as a merge commit if its number of parents is greater than 1. </p>
	 * Note after the updating the JGit, should be used the native method in
	 * {@code LogCommand}.
	 * 
	 * @return all commits which are merges
	 */
	@Deprecated
	public List<RevCommit> getMergeCommits() {
		return getMergeCommits(null, null);
	}

	/**
	 * Returns a list of the developers that collaborated in a given file.
	 * 
	 * @param fileBlames
	 *            - the blames related to file
	 * @param beginLine
	 *            - used in case of filter a specific line range
	 * @param endLine
	 *            - used in case of filter a specific line range
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public List<DeveloperNode> getDeveloperNodes(List<Blame> fileBlames,
			List<RevCommit> commits, Boolean flag)
			throws IOException, GitAPIException {

		List<DeveloperNode> devs = new ArrayList<DeveloperNode>();

		for (Blame b : fileBlames) {
			RevCommit key = b.getCommit();
			BlameResult value = b.getResult();

			// read the number of lines from the commit to not look at
			// changes in the working copy
			int lines = countFileLines(key.getId(), value.getResultPath());

			// running FILE_BASED network
			if (!flag) {
				for (int i = 0; i < lines; i++) {
					if (commits.contains(value.getSourceCommit(i))) {
						PersonIdent person = value.getSourceAuthor(i);
						DeveloperNode dev = new DeveloperNode();
						dev.setName(person.getName());
						dev.setEmail(person.getEmailAddress());
						if (!this.project.getDevs().contains(dev)) {
							dev.setId(this.project.getNextID());
							this.project.add(dev);
							if (!devs.contains(dev)) {
								devs.add(dev);
							}
						}
					}
				}
				// running CHUNK_BASED network
			} else {

				for (int i = 0; i < lines; i++) {
					PersonIdent person = value.getSourceAuthor(i);
					DeveloperNode dev = new DeveloperNode();
					dev.setName(person.getName());
					dev.setEmail(person.getEmailAddress());
					if (!this.project.getDevs().contains(dev)) {
						dev.setId(this.project.getNextID());
						this.project.add(dev);
						if (!devs.contains(dev)) {
							devs.add(dev);
						}
					}
				}
			}
		}
		return devs;
	}

	private int countFileLines(ObjectId commitID, String name)
			throws IOException {
		try (RevWalk revWalk = new RevWalk(this.project.getRepository())) {
			RevCommit commit = revWalk.parseCommit(commitID);
			RevTree tree = commit.getTree();

			// now try to find a specific file
			try (TreeWalk treeWalk = new TreeWalk(this.project.getRepository())) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create(name));
				if (!treeWalk.next()) {
					throw new IllegalStateException(
							"Did not find expected file '" + name + "'");
				}

				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = this.project.getRepository().open(
						objectId);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// and then one can the loader to read the file
				loader.copyTo(stream);

				revWalk.dispose();

				return IOUtils.readLines(
						new ByteArrayInputStream(stream.toByteArray())).size();
			}
		}
	}

	private List<DiffEntry> call(RevCommit olderCommit, RevCommit newerCommit,
			String filepath) throws IOException, GitAPIException {
		// the diff works on TreeIterators, we prepare two for the two branches
		AbstractTreeIterator oldTreeParser = prepareTreeParser(olderCommit
				.getName());
		AbstractTreeIterator newTreeParser = prepareTreeParser(newerCommit
				.getName());

		// then the procelain diff-command returns a list of diff entries
		Git git = Git.wrap(this.project.getRepository());
		List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser)
				.setNewTree(newTreeParser)
				.setPathFilter(PathFilter.create(filepath)).call();
		return diff;
	}

	/**
	 * Prints only changed lines
	 * 
	 * @param diff
	 */
	@Deprecated
	public void printDiff(List<DiffEntry> diff) {
		for (DiffEntry entry : diff) {
			System.out.println("Entry: " + entry + ", from: "
					+ entry.getOldId() + ", to: " + entry.getNewId());

			try (DiffFormatter formatter = new DiffFormatter(System.out)) {
				formatter.setRepository(this.project.getRepository());
				formatter.setContext(0);
				formatter.format(entry);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private AbstractTreeIterator prepareTreeParser(String objectId)
			throws IOException, MissingObjectException,
			IncorrectObjectTypeException {
		// from the commit we can build the tree which allows us to construct
		// the TreeParser
		try (RevWalk walk = new RevWalk(this.project.getRepository())) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			try (ObjectReader oldReader = this.project.getRepository()
					.newObjectReader()) {
				oldTreeParser.reset(oldReader, tree.getId());
			}

			walk.dispose();

			return oldTreeParser;
		}
	}
}
