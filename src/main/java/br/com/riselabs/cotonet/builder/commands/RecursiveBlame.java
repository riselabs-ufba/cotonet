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
package br.com.riselabs.cotonet.builder.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import br.com.riselabs.cotonet.model.beans.Blame;

/**
 * This class emulates a recursive git blame for a given time range.
 *
 * @author Alcemir R. Santos
 */
public class RecursiveBlame {

	private Repository repo;
	private RevCommit beginRevision;
	private RevCommit endRevision;
	private String path;

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
