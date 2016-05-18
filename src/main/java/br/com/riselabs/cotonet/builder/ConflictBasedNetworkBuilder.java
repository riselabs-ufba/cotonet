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
package br.com.riselabs.cotonet.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import br.com.riselabs.cotonet.builder.commands.GitConflictBlame;
import br.com.riselabs.cotonet.builder.commands.RecursiveBlame;
import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.ChunkBlame;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;
import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.dao.ConflictBasedNetworkDAO;
import br.com.riselabs.cotonet.model.dao.DAOFactory;
import br.com.riselabs.cotonet.model.dao.DAOFactory.CotonetBean;
import br.com.riselabs.cotonet.model.dao.DeveloperEdgeDAO;
import br.com.riselabs.cotonet.model.dao.DeveloperNodeDAO;
import br.com.riselabs.cotonet.model.dao.MergeScenarioDAO;
import br.com.riselabs.cotonet.model.dao.ProjectDAO;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.InvalidCotonetBeanException;

/**
 * @author Alcemir R. Santos
 *
 */
public class ConflictBasedNetworkBuilder {

	private Project project;
	private Map<MergeScenario, List<Blame>> scenarioBlamesMap;

	public ConflictBasedNetworkBuilder() {
		this(null);
	}

	public ConflictBasedNetworkBuilder(Project aProject) {
		setProject(aProject);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * @return the scenarioBlamesMap
	 */
	public Map<MergeScenario, List<Blame>> getScenarioBlamesMap() {
		return scenarioBlamesMap;
	}

	/**
	 * @param scenarioBlamesMap
	 *            the scenarioBlamesMap to set
	 */
	public void setScenarioBlamesMap(
			Map<MergeScenario, List<Blame>> scenarioBlamesMap) {
		this.scenarioBlamesMap = scenarioBlamesMap;
	}

	public void add(MergeScenario aScenario, List<Blame> blames) {
		if (this.scenarioBlamesMap == null) {
			this.scenarioBlamesMap = new HashMap<MergeScenario, List<Blame>>();
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
	public Project execute() throws Exception {
		if (getProject().getRepository() == null)
			return null;
		return execute(NetworkType.CHUNK_BASED);
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
	public Project execute(NetworkType aType) throws Exception {
		ProjectDAO pdao = (ProjectDAO) DAOFactory.getDAO(CotonetBean.PROJECT);
		pdao.save(getProject());
		getProject().setID(pdao.get(getProject()).getID());

		List<RevCommit> allMerges = getMergeCommits();

		for (RevCommit mergeCommit : allMerges) {
			RevCommit leftHead = mergeCommit.getParent(0);
			RevCommit rightHead = mergeCommit.getParent(1);

			ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(
					getProject().getRepository(), true);
			boolean canMerge = merger.merge(leftHead, rightHead);

			if (canMerge) {
				// only conflicting merges are needed
				continue;
			}

			RevWalk walk = new RevWalk(getProject().getRepository());
			RevCommit baseCommit = walk.parseCommit(merger.getBaseCommitId());
			walk.close();

			MergeScenario scenario = new MergeScenario(getProject().getID(),
					baseCommit, leftHead, rightHead);

			Git git = Git.wrap(getProject().getRepository());
			// this is for the cases of restarting after exception in a conflict
			// scenario analysis
			git.reset().setRef(scenario.getLeft().getName())
					.setMode(ResetType.HARD).call();

			CheckoutCommand ckoutCmd = git.checkout();
			ckoutCmd.setName(scenario.getLeft().getName());
			ckoutCmd.setStartPoint(scenario.getLeft());
			ckoutCmd.call();

			MergeCommand mergeCmd = git.merge();
			mergeCmd.setCommit(false);
			mergeCmd.setStrategy(MergeStrategy.RECURSIVE);
			mergeCmd.include(scenario.getRight());
			MergeResult mResult = mergeCmd.call();

			ConflictBasedNetwork connet = null;
			List<File> conflictingFiles = getFilesWithConflicts(mResult);
			connet = build(scenario, conflictingFiles, aType);

			if (!connet.getEdges().isEmpty()) {
				getProject().add(scenario, connet);
				persistEntry(scenario, connet);
			}

			// workaround to "run merge --abort"
			git.reset().setRef(scenario.getLeft().getName())
					.setMode(ResetType.HARD).call();
		}
		return getProject();
	}

	private void persistEntry(MergeScenario scenario,
			ConflictBasedNetwork connet) throws InvalidCotonetBeanException {
		// save merge scenario
		MergeScenarioDAO msdao = (MergeScenarioDAO) DAOFactory
				.getDAO(CotonetBean.MERGE_SCENARIO);
		msdao.save(scenario);
		scenario.setID(msdao.get(scenario).getID());

		// save networks
		ConflictBasedNetworkDAO cndao = (ConflictBasedNetworkDAO) DAOFactory
				.getDAO(CotonetBean.CONFLICT_NETWORK);
		connet.setMergeScenarioID(scenario.getID());
		cndao.save(connet);
		connet.setID(cndao.get(connet).getID());

		// save edges
		DeveloperEdgeDAO edao = (DeveloperEdgeDAO) DAOFactory
				.getDAO(CotonetBean.EDGE);
		for (DeveloperEdge edge : connet.getEdges()) {
			boolean leftupdated = false, rightupdated = false;
			for (DeveloperNode node : getProject().getDevs()) {
				if (leftupdated && rightupdated) {
					break;
				}
				if (node.getID() == edge.getLeft() && !leftupdated) {
					edge.setLeft(getNodeIDFromDB(node));
					leftupdated = true;
				} else if (node.getID() == edge.getRight() && !rightupdated) {
					edge.setRight(getNodeIDFromDB(node));
					rightupdated = true;
				}
			}
			edge.setNetworkID(connet.getID());
			edao.save(edge);
		}

	}

	private Integer getNodeIDFromDB(DeveloperNode node) throws InvalidCotonetBeanException {
		// save developers
		DeveloperNodeDAO ddao = (DeveloperNodeDAO) DAOFactory
				.getDAO(CotonetBean.NODE);
		DeveloperNode aux;
		node.setID(null);
		if ((aux = ddao.get(node) )== null) {
			node.setSystemID(getProject().getID());
			ddao.save(node);
			aux = node;
		}else{
			aux = node;
		}
		return ddao.get(aux).getID();
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
	 * @throws InterruptedException 
	 */
	public ConflictBasedNetwork build(MergeScenario aScenario,
			List<File> files, NetworkType type) throws IOException,
			GitAPIException, InterruptedException {
		ConflictBasedNetwork connet = new ConflictBasedNetwork(getProject(),
				aScenario);
		List<DeveloperNode> nodes = new ArrayList<DeveloperNode>();
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();

		for (File f : files) {
			List<DeveloperNode> newNodes = null;
			List<DeveloperEdge> newEdges = null;

			if (type == NetworkType.CHUNK_BASED) {
				List<ChunkBlame> blames = GitConflictBlame
						.getConflictingLinesBlames(f);
				// add(aScenario, blames);
				newNodes = getDeveloperNodes(blames);
				newEdges = buildEdges(newNodes);

			} else if (type == NetworkType.FILE_BASED) {
				RecursiveBlame blamer = new RecursiveBlame(getProject()
						.getRepository());
				List<Blame> blames = blamer
						.setRepository(getProject().getRepository())
						.setBeginRevision(aScenario.getRight())
						.setEndRevision(aScenario.getBase())
						.setFilePath(f.getName()).call();
				blames.addAll(blamer
						.setRepository(getProject().getRepository())
						.setBeginRevision(aScenario.getLeft())
						.setEndRevision(aScenario.getBase())
						.setFilePath(f.getName()).call());

				add(aScenario, blames);

				List<RevCommit> commits = getCommitsFrom(aScenario);
				newNodes = getDeveloperNodes(blames, commits);
				newEdges = buildEdges(newNodes);
			}

			for (DeveloperNode n : newNodes) {
				if (!nodes.contains(n)) {
					nodes.add(n);
				}
			}

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

	/**
	 * Returns developers nodes that contributed in the conflicting lines.
	 * 
	 * @param blames
	 * @return
	 */
	public List<DeveloperNode> getDeveloperNodes(List<ChunkBlame> blames) {
		List<DeveloperNode> result = new ArrayList<DeveloperNode>();
		for (ChunkBlame blame : blames) {
			CommandLineBlameResult bResult = blame.getResult();
			for (String anEmail : bResult.getAuthors()) {
				DeveloperNode newNode = new DeveloperNode(anEmail);
				if (!getProject().getDevs().contains(newNode)) {
					newNode.setID(getProject().getNextID());
					getProject().add(newNode);
				} else {
					newNode = getProject().getDevByMail(anEmail);
				}
				if (!result.contains(newNode)) {
					result.add(newNode);
				}
			}
		}
		return result;
	}

	/**
	 * Builds the edges among the developers.
	 * 
	 * @param nodes
	 * @return
	 */
	private List<DeveloperEdge> buildEdges(List<DeveloperNode> nodes) {
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();
		if(nodes.size()==1){
			edges.add(new DeveloperEdge(nodes.get(0).getID(), nodes.get(0).getID()));
			return edges;
		}
		for (DeveloperNode from : nodes) {
			for (DeveloperNode to : nodes) {
				if (from.equals(to)) {
					continue;
				}
				DeveloperEdge edge = new DeveloperEdge(from.getID(), to.getID());
				if (!edges.contains(edge)) {
					edges.add(edge);
				}
			}
		}
		return edges;
	}

	/**
	 * Given a {@code MergeResult}, this method returns files that ended up in
	 * conflicts.
	 * 
	 * @param mResult
	 * @return
	 */
	public List<File> getFilesWithConflicts(MergeResult mResult) {
		List<File> result = new ArrayList<File>();
		Map<String, int[][]> allConflicts = mResult.getConflicts();
		for (String path : allConflicts.keySet()) {
			result.add(new File(getProject().getRepository().getDirectory()
					.getParent(), path));
		}
		return result;
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
		try {
			Git git = Git.wrap(getProject().getRepository());
			log = git.log().call();
			for (RevCommit commit : log) {
				if (commit.getParentCount() == 2) {
					merges.add(commit);
				}
			}
		} catch (GitAPIException e) {
			System.out.println(e.getMessage());
		}

		return merges;
	}

	public List<RevCommit> getCommitsFrom(MergeScenario aScenario)
			throws RevisionSyntaxException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException,
			IOException, GitAPIException {
		List<RevCommit> commits = between(aScenario.getLeft(),
				aScenario.getBase());
		commits.addAll(between(aScenario.getRight(), aScenario.getBase()));
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
		return between(beginCommit, endCommit);
	}

	private List<RevCommit> between(RevCommit begin, RevCommit end)
			throws GitAPIException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		List<RevCommit> result = new ArrayList<RevCommit>();
		try (RevWalk rw = new RevWalk(getProject().getRepository())) {
			rw.markStart(rw.parseCommit(begin));
			rw.markUninteresting(rw.parseCommit(end));
			for (RevCommit curr; (curr = rw.next()) != null;) {
				result.add(curr);
			}
		}
		return result;
	}

	/**
	 * Returns all commits of the project which are merges. </p> We consider a
	 * commit as a merge commit if its number of parents is greater than 1. </p>
	 * Note after the updating the JGit, should be used the native method in
	 * {@code LogCommand}.
	 * 
	 * @return all commits which are merges
	 */
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
			List<RevCommit> commits) throws IOException, GitAPIException {

		List<DeveloperNode> devs = new ArrayList<DeveloperNode>();

		for (Blame b : fileBlames) {
			RevCommit key = b.getCommit();
			BlameResult value = b.getResult();

			// read the number of lines from the commit to not look at
			// changes in the working copy
			int lines = countFileLines(key.getId(), value.getResultPath());

			for (int i = 0; i < lines; i++) {
				if (commits.contains(value.getSourceCommit(i))) {
					PersonIdent person = value.getSourceAuthor(i);
					DeveloperNode dev = new DeveloperNode();
					dev.setName(person.getName());
					dev.setEmail(person.getEmailAddress());
					if (!getProject().getDevs().contains(dev)) {
						dev.setID(getProject().getNextID());
						getProject().add(dev);
					} else {
						dev = getProject().getDevByMail(
								person.getEmailAddress());
					}
					if (!devs.contains(dev)) {
						devs.add(dev);
					}
				}
			}

		}
		return devs;
	}

	private int countFileLines(ObjectId commitID, String name)
			throws IOException {
		try (RevWalk revWalk = new RevWalk(getProject().getRepository())) {
			RevCommit commit = revWalk.parseCommit(commitID);
			RevTree tree = commit.getTree();

			// now try to find a specific file
			try (TreeWalk treeWalk = new TreeWalk(getProject().getRepository())) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create(name));
				if (!treeWalk.next()) {
					throw new IllegalStateException(
							"Did not find expected file '" + name + "'");
				}

				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = getProject().getRepository().open(
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

}
