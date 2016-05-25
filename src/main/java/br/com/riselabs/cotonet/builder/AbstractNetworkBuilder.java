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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.beans.DeveloperEdge;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.db.DBWritter;
import br.com.riselabs.cotonet.model.enums.NetworkType;

/**
 * @author Alcemir R. Santos
 *
 */
public abstract class AbstractNetworkBuilder {

	protected Project project;
	protected NetworkType type;

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

	public void build() throws IOException, CheckoutConflictException,
			GitAPIException, InterruptedException {
		List<MergeScenario> conflictingScenarios = getMergeScenarios();
		for (MergeScenario scenario : conflictingScenarios) {
			List<File> conflictingFiles = getConflictingFiles(scenario);
			getDeveloperNodes(scenario, conflictingFiles);
			List<DeveloperEdge> edges = getDeveloperEdges(project.getDevs());
			project.add(scenario, new ConflictBasedNetwork(
					new ArrayList<DeveloperNode>(project.getDevs().values()),
					edges, type));
		}
		DBWritter.INSTANCE.persist(project);
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
		// collecting merge commits
		List<RevCommit> merges = new LinkedList<>();
		Iterable<RevCommit> log;
		try {
			Git git = Git.wrap(getProject().getRepository());
			log = git.log().call();
			for (RevCommit commit : log) {
				if (commit.getParentCount() == 2) {
					merges.add(commit);
					// we know there is only to parents
					RevCommit leftParent = commit.getParent(0);
					RevCommit rightParent = commit.getParent(1);
					ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(
							getProject().getRepository(), true);
					// selecting the conflicting ones
					if (merger.merge(leftParent, rightParent)) {
						continue;
					}
					RevWalk walk = new RevWalk(getProject().getRepository());
					// for merges without a base commit
					if (merger.getBaseCommitId() == null)
						continue;
					RevCommit baseCommit = walk.lookupCommit(merger
							.getBaseCommitId());
					walk.close();

					result.add(new MergeScenario(baseCommit, leftParent,
							rightParent));
				}
			}
		} catch (GitAPIException e) {
			System.out.println(e.getMessage());
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

		Set<String> conflictingPaths = mResult.getConflicts().keySet();
		List<File> result = new ArrayList<File>();
		for (String path : conflictingPaths) {
			result.add(new File(getProject().getRepository().getDirectory()
					.getParent(), path));
		}
		return result;
	}

	private List<DeveloperEdge> getDeveloperEdges(
			Map<Integer, DeveloperNode> map) {
		List<DeveloperEdge> edges = new ArrayList<DeveloperEdge>();
		if (map.size() == 1) {
			edges.add(new DeveloperEdge(1, 1));
			return edges;
		}
		for (Entry<Integer, DeveloperNode> from : map.entrySet()) {
			for (Entry<Integer, DeveloperNode> to : map.entrySet()) {
				if (from.getValue().equals(to.getValue())) {
					continue;
				}
				DeveloperEdge edge = new DeveloperEdge(from.getKey(), to.getKey());
				if (!edges.contains(edge)) {
					edges.add(edge);
				}
			}
		}
		return edges;
	}

	protected abstract List<DeveloperNode> getDeveloperNodes(
			MergeScenario scenario, List<File> conflictingFiles)
			throws IOException, GitAPIException, InterruptedException;
}
