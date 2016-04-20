package br.com.riselabs.connet.filters;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

public class InBetweenRevFilter extends RevFilter {

	private AnyObjectId end;
	private AnyObjectId begin;

	public InBetweenRevFilter(AnyObjectId begin, AnyObjectId end) {
		this.begin = begin;
		this.end = end;
	}

	@Override
	public RevFilter clone() {
		return this;
	}

	@Override
	public boolean include(RevWalk walker, RevCommit c)
			throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		RevCommit from = walker.parseCommit(begin);
		RevCommit to = walker.parseCommit(end);
		return (walker.isMergedInto(from, c) && walker.isMergedInto(c, to));
	}
}