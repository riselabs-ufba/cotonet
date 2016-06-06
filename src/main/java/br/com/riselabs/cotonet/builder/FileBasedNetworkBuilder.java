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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import br.com.riselabs.cotonet.builder.commands.RecursiveBlame;
import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;
import br.com.riselabs.cotonet.model.beans.MergeScenario;
import br.com.riselabs.cotonet.model.beans.Project;
import br.com.riselabs.cotonet.model.enums.NetworkType;

/**
 * @author Alcemir R. Santos
 *
 */
public class FileBasedNetworkBuilder extends
		AbstractNetworkBuilder<BlameResult> {

	public FileBasedNetworkBuilder(Project project) {
		setProject(project);
		setType(NetworkType.FILE_BASED);
	}

	@Override
	protected Map<Blame<BlameResult>, List<DeveloperNode>> getDeveloperNodes(
			MergeScenario scenario, File file) throws IOException,
			InterruptedException, GitAPIException {
		Map<Blame<BlameResult>, List<DeveloperNode>> devs = new HashMap<Blame<BlameResult>, List<DeveloperNode>>();

		RecursiveBlame blamer = new RecursiveBlame(getProject().getRepository());
		List<Blame<BlameResult>> blames = null;
		blames = blamer.setRepository(getProject().getRepository())
				.setBeginRevision(scenario.getRight())
				.setEndRevision(scenario.getBase()).setFilePath(file.getName())
				.call();
		blames.addAll(blamer.setRepository(getProject().getRepository())
				.setBeginRevision(scenario.getLeft())
				.setEndRevision(scenario.getBase()).setFilePath(file.getName())
				.call());

		List<RevCommit> commits = getCommitsFrom(scenario);

		for (Blame<BlameResult> b : blames) {
			RevCommit key = b.getRevision();
			BlameResult value = b.getResult();

			// read the number of lines from the commit to not look at
			// changes in the working copy
			int lines = countFileLines(key.getId(), value.getResultPath());

			List<DeveloperNode> lDevs =  new ArrayList<DeveloperNode>();
			for (int i = 0; i < lines; i++) {
				if (commits.contains(value.getSourceCommit(i))) {
					PersonIdent person = value.getSourceAuthor(i);
					DeveloperNode dev = new DeveloperNode();
					dev.setName(person.getName());
					dev.setEmail(person.getEmailAddress());
					if (!getProject().getDevs().values().contains(dev)) {
						getProject().add(dev);
					} else {
						dev = getProject().getDevByMail(
								person.getEmailAddress());
					}
					if (!lDevs.contains(dev)) {
						lDevs.add(dev);
					}
				}
			}
			devs.put(b, lDevs);
		}
		return devs;
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
