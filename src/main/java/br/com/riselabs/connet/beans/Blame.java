/**
 * 
 */
package br.com.riselabs.connet.beans;

import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author alcemirsantos
 *
 */
public class Blame {

	private RevCommit commit;
	private BlameResult result;

	public Blame() {
		this(null, null);
	}

	public Blame(RevCommit commit, BlameResult result) {
		super();
		this.commit = commit;
		this.result = result;
	}

	public RevCommit getCommit() {
		return commit;
	}

	public void setCommit(RevCommit commit) {
		this.commit = commit;
	}

	public BlameResult getResult() {
		return result;
	}

	public void setResult(BlameResult result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "['"+ this.result.getResultPath()+"' blame @ "
				+ "Commit ('"
				+ this.commit.getFullMessage() + "')]";
	}
}
