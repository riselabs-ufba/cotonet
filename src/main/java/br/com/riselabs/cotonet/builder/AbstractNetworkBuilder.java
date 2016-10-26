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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.builder.commands.ExternalGitCommand;
import br.com.riselabs.cotonet.builder.commands.ExternalGitCommand.CommandType;
import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.ConflictChunk;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.db.DBWritter;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.BlameException;
import br.com.riselabs.cotonet.util.Logger;

/**
 * @author Alcemir R. Santos
 * @param <T>
 * @param <T>
 *
 */
public abstract class AbstractNetworkBuilder<T> {

	protected Project project;
	protected NetworkType type;
	protected File log;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public NetworkType getType() {
		return type;
	}

	public void setType(NetworkType type) {
		this.type = type;
	}

	public void setLogFile(File log) {
		this.log = log;
	}

	/**
	 * Builds the conflict based network considering the previously network type
	 * set and the repository information provided. In case the the type was not
	 * set yet, this method used the <i>default</i> type (<i>i.e.,</i> the
	 * chunk-based {@code NetworkType.CHUNK_BASED}).
	 * 
	 * OBS: You should set the repository first, otherwise this method will
	 * return <code>null</code>
	 * 
	 * @return {@code aNetwork}
	 * 
	 *         -<code>null</code> when the repository is not set.
	 * @throws Exception
	 */
	public void build() throws IOException, CheckoutConflictException,
			GitAPIException, InterruptedException {
		Logger.log(log, "[" + project.getName() + "] Network building start.");
		List<MergeScenario> conflictingScenarios = getMergeScenarios();
		for (MergeScenario scenario : conflictingScenarios) {
			ConflictBasedNetwork connet = getConflictNetwork(scenario);
			if (connet!=null) {
				project.add(scenario, connet);
			}
		}
		Logger.log(log, "[" + project.getName()
				+ "] Network building finished.");
	}

	/**
	 * Triggers the persistence of the networks built for this project.
	 */
	public void persist() {
		Logger.log(log, "[" + project.getName()
				+ "] Project persistence start.");
		DBWritter.INSTANCE.setLogFile(log);
		DBWritter.INSTANCE.persist(project);
		Logger.log(log, "[" + project.getName()
				+ "] Project persistence finished.");
	}

	/**
	 * Returns the conflicting merge scenarios
	 * 
	 * @return - a list of merge scenarios. it may be empty in case of no
	 *         conflict.
	 * @throws IOException
	 */
	private List<MergeScenario> getMergeScenarios() throws IOException {
		List<MergeScenario> result = new ArrayList<MergeScenario>();
		List<RevCommit> mergeCommits = new ArrayList<RevCommit>();
		Iterable<RevCommit> gitlog;
		try {
			Git git = Git.wrap(getProject().getRepository());
			gitlog = git.log().call();
			for (RevCommit commit : gitlog) {
				if (commit.getParentCount() == 2) {
					mergeCommits.add(commit);
					// collecting merge commits
					// we know there is only to parents
					RevCommit leftParent = commit.getParent(0);
					RevCommit rightParent = commit.getParent(1);
					ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(
							getProject().getRepository(), true);
					// selecting the conflicting ones
					boolean noConflicts = false;
					try {
						noConflicts = merger.merge(leftParent, rightParent);
					} catch (NoMergeBaseException e) {
						StringBuilder sb = new StringBuilder();
						sb.append("[" + project.getName() + ":"
								+ project.getUrl() + "] "
								+ "Skipping merge scenario due to '"
								+ e.getMessage() + "'\n");
						sb.append("---> Skipped scenario:\n");
						sb.append("::Base (<several>): \n");
						sb.append("::Left ("
								+ leftParent.getAuthorIdent().getWhen()
										.toString() + "):"
								+ leftParent.getName() + "\n");
						sb.append("::Right ("
								+ rightParent.getAuthorIdent().getWhen()
										.toString() + "):"
								+ rightParent.getName() + "\n");
						Logger.log(log, sb.toString());
						Logger.logStackTrace(log, e);
						continue;
					}
					if (noConflicts) {
						continue;
					}
					RevWalk walk = new RevWalk(getProject().getRepository());
					// for merges without a base commit
					if (merger.getBaseCommitId() == null)
						continue;
					RevCommit baseCommit = walk.lookupCommit(merger
							.getBaseCommitId());
					walk.close();
					
					Timestamp mergeDate = new Timestamp(commit.getAuthorIdent().getWhen().getTime());
					result.add(new MergeScenario(baseCommit, leftParent,
							rightParent, commit, mergeDate));
				}
			}
		} catch (GitAPIException e) {
			Logger.logStackTrace(log, e);
		}
		return result;
	}

	/**
	 * Returns the conflicting files of the given scenario.
	 * 
	 * @param scenario
	 * @return
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 */
	private List<File> getConflictingFiles(MergeScenario scenario)
			throws CheckoutConflictException, GitAPIException {
		Git git = Git.wrap(getProject().getRepository());
		// this is for the cases of restarting after exception in a conflict
		// scenario analysis
		try {
			git.reset().setRef(scenario.getLeft().getName())
					.setMode(ResetType.HARD).call();
		} catch (JGitInternalException e) {
			Logger.log(log, "[" + project.getName()
					+ "] JGit Reset Command ended with exception."
					+ " Trying external reset command.");
			ExternalGitCommand egit = new ExternalGitCommand();
			try {
				egit.setType(CommandType.RESET)
						.setDirectory(
								project.getRepository().getDirectory()
										.getParentFile()).call();
			} catch (BlameException e1) {
				Logger.logStackTrace(log, e1);
				return null;
			}
		}

		CheckoutCommand ckoutCmd = git.checkout();
		ckoutCmd.setName(scenario.getLeft().getName());
		ckoutCmd.setStartPoint(scenario.getLeft());
		ckoutCmd.call();

		MergeCommand mergeCmd = git.merge();
		mergeCmd.setCommit(false);
		mergeCmd.setStrategy(MergeStrategy.RECURSIVE);
		mergeCmd.include(scenario.getRight());

		Set<String> conflictingPaths;
		try {
			// dealing with MissingObjectException
			MergeResult mResult = mergeCmd.call();
			// dealing with Ghosts conflicts
			conflictingPaths = mResult.getConflicts().keySet();
		} catch (NullPointerException | JGitInternalException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[" + project.getName() + ":" + project.getUrl() + "] "
					+ "Skipping merge scenario due to '" + e.getMessage()
					+ "'\n");
			sb.append("--> Exception: " + e.getClass());
			sb.append("--> Skipped scenario:\n");
			sb.append("::Base:" + scenario.getBase().getName() + "\n");
			sb.append("::Left:" + scenario.getLeft().getName() + "\n");
			sb.append("::Right:" + scenario.getRight().getName() + "\n");
			Logger.log(log, sb.toString());
			return null;
		}
		List<File> result = new ArrayList<File>();
		for (String path : conflictingPaths) {
			result.add(new File(getProject().getRepository().getDirectory()
					.getParent(), path));
		}
		return result;
	}

	private ConflictBasedNetwork getConflictNetwork(MergeScenario scenario)
			throws IOException, GitAPIException, InterruptedException {
		List<File> files = getConflictingFiles(scenario);
		if (files == null) {
			return null; // dealing with ghost scenarios or fail to hard reset.
		}
		List<DeveloperNode> nodes = new ArrayList<DeveloperNode>();
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();

		for (File file : files) {
			List<ConflictChunk<T>> cchunks;
			try {
				cchunks = getConflictChunks(scenario, file);
			} catch (BlameException  e) {
				Logger.log(log, "[" + project.getName() + "]" + e.getMessage());
				continue;
			}
			List<DeveloperNode> fNodes = null;
			List<DeveloperEdge> fEdges = null;
			
			/*
			 * iterates in each chunk of the file
			 */
			
			for (ConflictChunk<T> cChunk : cchunks) { 
				fNodes = getDeveloperNodes(cChunk);
				fEdges = getDeveloperEdges(fNodes, cChunk);
				
				//just moved this if inside for
				if(fNodes!=null && fEdges!=null){
					nodes.addAll(fNodes);
					edges.addAll(fEdges);
				}
			}
			
		}
		if(nodes.isEmpty()||edges.isEmpty()){
			return null;
		}
		return new ConflictBasedNetwork(project, scenario, nodes, edges, type);
	}

	private List<DeveloperEdge> getDeveloperEdges(List<DeveloperNode> nodes,
			ConflictChunk<T> cChunk) {
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();

		// if there is only one developer, create loop
		if (nodes.size() == 1) {
			DeveloperNode node = nodes.get(0);
			edges.add(new DeveloperEdge(node, node, cChunk.getChunkRange(),
					cChunk.getPath().toString()));
			return edges;
		}
		// create a fully connected graph
		for (DeveloperNode from : nodes) {
			for (DeveloperNode to : nodes) {
				
				/*
				 * the problem can be here
				 */
				if (from.equals(to)) { 
					continue;
				}
				DeveloperEdge newEdge;
				newEdge = new DeveloperEdge(from, to, cChunk.getChunkRange(),
						cChunk.getPath().toString());
				if (!edges.contains(newEdge)) {
					edges.add(newEdge);
				}
			}
		}

		return edges;
	}

	/**
	 * Returns the conflict chunks of a given file.
	 * 
	 * @param scenario
	 *            - the scenario that as merged
	 * @param file
	 *            - a file with conflicts
	 */
	protected abstract List<ConflictChunk<T>> getConflictChunks(
			MergeScenario aScenario, File file) throws BlameException;

	/**
	 * Creates the nodes for a given file.
	 * 
	 * @param cChunk
	 * @return
	 */
	protected abstract List<DeveloperNode> getDeveloperNodes(
			ConflictChunk<T> cChunk);

}
