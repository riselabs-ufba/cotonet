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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.builder.commands.ExternalGitCommand;
import br.com.riselabs.cotonet.builder.commands.ExternalGitCommand.CommandType;
import br.com.riselabs.cotonet.model.beans.CommandLineBlameResult;
import br.com.riselabs.cotonet.model.beans.ConflictChunk;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.enums.NetworkType;
import br.com.riselabs.cotonet.model.exceptions.BlameException;

/**
 * @author Alcemir R. Santos
 *
 */
public class ChunkBasedNetworkBuilder extends
		AbstractNetworkBuilder<CommandLineBlameResult> {

	public ChunkBasedNetworkBuilder(Project project) {
		setProject(project);
		setType(NetworkType.CHUNK_BASED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.com.riselabs.cotonet.builder.AbstractNetworkBuilder#getConflictChunks
	 * (br.com.riselabs.cotonet.model.beans.MergeScenario, java.io.File)
	 */
	@Override
	protected List<ConflictChunk<CommandLineBlameResult>> getConflictChunks(
			MergeScenario scenario, File file) throws BlameException {
		ExternalGitCommand egit = new ExternalGitCommand();
		List<ConflictChunk<CommandLineBlameResult>> blames = null;
		blames = egit.setMergeScenario(scenario).setDirectory(file)
				.setType(CommandType.BLAME).call();
		return blames;
	}

	/*
	 * @see
	 * br.com.riselabs.cotonet.builder.AbstractNetworkBuilder#getDeveloperNodes
	 * (java.util.List)
	 */
	@Override
	protected Map<String, List<DeveloperNode>> getDeveloperNodes(
			MergeScenario scenario, ConflictChunk<CommandLineBlameResult> cChunk) {
		Map<String, List<DeveloperNode>> result = new HashMap<>();

		// getting nodes from the upper part of the conflict
		CommandLineBlameResult leftResult = cChunk.getLeft().getResult();
		result.put(
				scenario.getLeft().getName(),
				extractNodes(scenario.getBase(), scenario.getLeft(), leftResult));
		// getting nodes from the bottom part of the conflict
		CommandLineBlameResult rightResult = cChunk.getRight().getResult();
		result.put(
				scenario.getRight().getName(),
				extractNodes(scenario.getBase(), scenario.getRight(),
						rightResult));

		// for (Blame<CommandLineBlameResult> blame : cChunk.getBlames()) {
		// CommandLineBlameResult bResult = blame.getResult();
		//
		// for (DeveloperNode aDev : bResult.getAuthors()) {
		// if (!getProject().getDevs().values().contains(aDev)) {
		// // if there is no such dev in the project, then add it
		// getProject().add(aDev);
		// } else {
		// // else update the reference with the project one
		// aDev = getProject().getDevByMail(aDev.getEmail());
		// }
		//
		// if (!result.contains(aDev)) {
		// for (int line : bResult.getLineAuthorsMap().keySet()) {
		// String blameCommit = bResult.getLineCommitMap().get(line);
		//
		// if (line >= cChunk.getBeginLine()
		// && line <= cChunk.getEndLine()
		// && bResult.getLineAuthorsMap().get(line).equals(aDev)
		// && (inRange(blameCommit, scenario.getBase(), scenario.getLeft())
		// || inRange(blameCommit, scenario.getBase(), scenario.getRight()))
		// ) {
		// result.add(aDev);
		// break;
		// }
		// }
		//
		// }
		// }
		// }
		return result;
	}

	private List<DeveloperNode> extractNodes(RevCommit base, RevCommit side,
			CommandLineBlameResult aResult) {
		List<DeveloperNode> result = new ArrayList<>();
		for (DeveloperNode aDev : aResult.getAuthors()) {
			if (!getProject().getDevs().values().contains(aDev)) {
				// if there is no such dev in the project, then add it
				getProject().add(aDev);
			} else {
				// else update the reference with the project one
				aDev = getProject().getDevByMail(aDev.getEmail());
			}

			if (!result.contains(aDev)) {
				for (int line : aResult.getLineAuthorsMap().keySet()) {

					String lineCommit = aResult.getLineCommitMap().get(line);

					if (aResult.getLineAuthorsMap().get(line).equals(aDev)
							&& inRange(lineCommit, base, side)) {
						result.add(aDev);
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Determines whether a commit is in a specified range of commits.
	 *
	 * TODO: This should be done in some kind of GitHelper class or somewhere
	 * else.
	 */
	private boolean inRange(String commit, RevCommit begin, RevCommit end) {
		try (RevWalk rw = new RevWalk(getProject().getRepository())) {
			rw.markStart(rw.parseCommit(end));
			rw.markUninteresting(rw.parseCommit(begin));

			System.out.println("++");
			for (RevCommit cur; (cur = rw.next()) != null;) {
				if (!(cur.getName().equals(begin.getName()))
						&& cur.getName().equals(commit)) {
					return true;
				}
			}
		} catch (IOException e) {
		}

		return false;
	}

}