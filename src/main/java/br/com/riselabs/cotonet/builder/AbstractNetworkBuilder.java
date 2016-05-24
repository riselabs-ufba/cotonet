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

/**
 * @author Alcemir R. Santos
 *
 */
public abstract class AbstractNetworkBuilder {

	protected Project project;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void build() throws IOException, CheckoutConflictException, GitAPIException, InterruptedException {
		List<MergeScenario> conflictingScenarios = getMergeScenarios();
		for (MergeScenario scenario : conflictingScenarios) {
			List<File> conflictingFiles = getConflictingFiles(scenario);
			List<DeveloperNode> nodes = getDeveloperNodes(scenario, conflictingFiles);
			List<DeveloperEdge> edges = getDeveloperEdges(nodes);
			project.add(scenario, new ConflictBasedNetwork(nodes, edges));
		}
	}

	/**
	 * Returns the conflicting merge scenarios
	 * 
	 * @return
	 * 		- a list of merge scenarios. it may be empty in case of no conflict.
	 * @throws IOException 
	 */
	private List<MergeScenario> getMergeScenarios() throws IOException{
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
				}
			}
		} catch (GitAPIException e) {
			System.out.println(e.getMessage());
		}
		// selecting the conflicting ones
		for (RevCommit mergeCommit : merges) {
			// we know there is only to parents
			RevCommit leftHead = mergeCommit.getParent(0);
			RevCommit rightHead = mergeCommit.getParent(1);
			ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(
					getProject().getRepository(), true);
			if (merger.merge(leftHead, rightHead)) {
				continue;
			}
		
			RevWalk walk = new RevWalk(getProject().getRepository());
			RevCommit baseCommit = walk.parseCommit(merger.getBaseCommitId());
			walk.close();
		
			result.add(new MergeScenario(baseCommit, leftHead, rightHead));
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
	private  List<File> getConflictingFiles(MergeScenario scenario) throws CheckoutConflictException, GitAPIException{
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
		
		Map<String, int[][]> allConflicts = mResult.getConflicts();
		List<File> result = new ArrayList<File>();
		for (String path : allConflicts.keySet()) {
			result.add(new File(getProject().getRepository().getDirectory()
					.getParent(), path));
		}
		return result;
	}

	private List<DeveloperEdge> getDeveloperEdges(List<DeveloperNode> nodes){
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
	
	protected abstract List<DeveloperNode> getDeveloperNodes(
			MergeScenario scenario, List<File> conflictingFiles) throws IOException, GitAPIException, InterruptedException;
}
