package br.com.riselabs.cotonet.builder.commands;

/*
 Copyright 2013, 2014 Dominik Stadler

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import br.com.riselabs.cotonet.model.beans.Blame;
import br.com.riselabs.cotonet.model.beans.DeveloperNode;

/**
 * Simple snippet which shows how to get a diff showing who changed which line
 * in a file
 *
 * @author dominik.stadler at gmx.at
 */
public class RecursiveBlame {

	private Repository repo;
	private RevCommit beginRevision;
	private RevCommit endRevision;
	private String path;
	private Integer beginLine;
	private Integer endLine;

	public RecursiveBlame() {
		this(null);
	}

	public RecursiveBlame(Repository aRepository) {
		this.repo = aRepository;
	}

	public RecursiveBlame setRepository(Repository aRepository) {
		this.repo = aRepository;
		return this;
	}

	public RecursiveBlame setBeginRevision(RevCommit aCommit) {
		this.beginRevision = aCommit;
		return this;
	}

	public RecursiveBlame setEndRevision(RevCommit aCommit) {
		this.endRevision = aCommit;
		return this;
	}

	public RecursiveBlame setFilePath(String filepath) {
		this.path = filepath;
		return this;
	}

	public RecursiveBlame setLineRange(Integer beginLine, Integer endLine) {
		this.beginLine = beginLine - 1;
		this.endLine = endLine;
		return this;
	}

	public List<Blame> call() throws IOException, GitAPIException {
		if (this.path == null
				|| this.beginRevision == null
				|| this.endRevision == null) {
			return null;
		}
		
		List<Blame> result = new ArrayList<Blame>();
		try (RevWalk rw = new RevWalk(this.repo)) {
		    rw.markStart(rw.parseCommit(this.beginRevision));
		    rw.markUninteresting(rw.parseCommit(this.endRevision));
		    for (RevCommit curr; (curr = rw.next()) != null;){
		    	result.add(new Blame(curr, blameCommit(curr)));
		    }
		}
		
		return result;
	}

	private BlameResult blameCommit(RevCommit commitToBlame)
			throws GitAPIException {
		BlameCommand blamer = new BlameCommand(this.repo);
		ObjectId commitToBlameID = commitToBlame.getId();
		blamer.setStartCommit(commitToBlameID);
		blamer.setFilePath(this.path);
		return blamer.call();
	}

}
